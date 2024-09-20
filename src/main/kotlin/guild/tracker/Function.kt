package guild.tracker

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import guild.tracker.service.DiscordApi
import guild.tracker.service.SheetApi
import com.fasterxml.jackson.databind.ObjectMapper
import guild.tracker.service.BlizzardApi
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.PropertySource
import io.micronaut.http.client.HttpClient

class Function : RequestHandler<Map<String, Any>, String> {

    override fun handleRequest(input: Map<String, Any>, context: Context): String {
//        val environment = Environment.ENVIRONMENT_LAMBDA
        val applicationContext = ApplicationContext.builder()
            .propertySources(
                PropertySource.of(
                    "lambda",
                    mapOf(
                        "blizzard.client-id" to System.getenv("BLIZZARD_CLIENT_ID"),
                        "blizzard.client-secret" to System.getenv("BLIZZARD_CLIENT_SECRET"),
                        "google.service-account.key" to System.getenv("GOOGLE_SERVICE_ACCOUNT_KEY"),
                        "google.spreadsheet.id" to System.getenv("GOOGLE_SPREADSHEET_ID"),
                        "discord.webhook.url" to System.getenv("DISCORD_WEBHOOK_URL")
                    )
                )
            )
            .build()
        applicationContext.start()

        val objectMapper = applicationContext.getBean(ObjectMapper::class.java)

        val blizzardApiService = BlizzardApi(
            applicationContext.getRequiredProperty("blizzard.client-id", String::class.java),
            applicationContext.getRequiredProperty("blizzard.client-secret", String::class.java),
            applicationContext.createBean(HttpClient::class.java, "https://us.battle.net"),
            applicationContext.createBean(HttpClient::class.java, "https://us.api.blizzard.com"),
            objectMapper
        )

        val googleSheetsService = SheetApi(
            applicationContext.getRequiredProperty("google.service-account.key", String::class.java),
            applicationContext.getRequiredProperty("google.spreadsheet.id", String::class.java),
            objectMapper
        )

        val discordService = DiscordApi(
            applicationContext.getRequiredProperty("discord.webhook.url", String::class.java)
        )

        try {
            val realmSlug = "barthilas"
            val guildSlug = "sm%C3%B6rg%C3%A5shorde" // URL-encoded slug

            val members = blizzardApiService.getGuildRoster(realmSlug, guildSlug)

            val characterItemLevels = mutableListOf<Pair<String, Int>>()
            val characterMythicPlusRatings = mutableListOf<Pair<String, Double>>()

            members.forEach { member ->
                val itemLevel = blizzardApiService.getCharacterItemLevel(member.second, member.first)
                var mythicPlusRating = blizzardApiService.getCharacterMythicPlusRatings(member.second, member.first)
                characterItemLevels.add(Pair(member.first, itemLevel))
                characterMythicPlusRatings.add(Pair(member.first, mythicPlusRating))
            }

//          sort
            characterItemLevels.sortByDescending { it.second }
            characterMythicPlusRatings.sortByDescending { it.second }

            // Update Google Sheets
            googleSheetsService.updateSheet(characterItemLevels)

            discordService.postItemLevelMessage(characterItemLevels)
            discordService.postMythicRatingsMessage(characterMythicPlusRatings)
            //todo pvp rating
            //todo mythic plus rating
            //todo rating against other guilds

            return "Success"
        } finally {
            applicationContext.close()
        }
    }
}
