package guild.tracker.client

import MythicPLusRatingProfile
import guild.tracker.model.CharacterProfileResponse
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import java.net.URL

class DiscordClient(
    @Property(name = "discord.webhook.url") private val webhookUrl: String
) {
    private val httpClient: HttpClient = HttpClient.create(URL("https://discord.com"))

    private val classColors = mapOf(
        "Death Knight" to "\u001B[0;31m", // Red
        "Demon Hunter" to "\u001B[0;35m", // Magenta
        "Druid" to "\u001B[0;33m",        // Yellow (Orange)
        "Hunter" to "\u001B[0;32m",       // Green
        "Mage" to "\u001B[0;34m",         // Cyan (Light Blue)
        "Monk" to "\u001B[0;92m",         // Bright Green
        "Paladin" to "\u001B[0;35m",      // Bright Magenta (Pink)
        "Priest" to "\u001B[0;97m",       // Bright White
        "Rogue" to "\u001B[0;33m",        // Yellow
        "Shaman" to "\u001B[0;34m",       // Blue
        "Warlock" to "\u001B[0;35m",      // Pink / Purp (Purple)
        "Warrior" to "\u001B[0;33m",       // Gold (Brown not available)
        "Evoker" to "\u001B[0;36m"        // Greenish color
    )

    fun postItemLevelMessage(profiles: List<CharacterProfileResponse>) {
        val header = "### Gear Leaderboard \n```ansi"

        // Sort profiles by item level descending to rank them
        val content = profiles.withIndex().joinToString("\n") { (index, profile) ->
            val colorCode = classColors[profile.characterClass.name] ?: "\u001B[0m"
            val resetCode = "\u001B[0m"
            val boldCode = "\u001B[1m" // Bold

            "${index + 1}. $colorCode${profile.name}$resetCode: item level $boldCode${profile.averageItemLevel}$resetCode"
        }
        val body = mapOf("content" to "$header\n$content\n```")

        val webhookUri = webhookUrl.substringAfter("https://discord.com")

        val request = HttpRequest.POST(webhookUri, body)
            .contentType(MediaType.APPLICATION_JSON_TYPE)

        httpClient.toBlocking().exchange(request, String::class.java)
    }

    fun postMythicRatingsMessage(ratingProfiles: List<MythicPLusRatingProfile>) {
        val header = "### Mythic+ Leaderboard \n```ansi"
        val content = ratingProfiles.withIndex().joinToString("\n") { (index, ratingProfile) ->
            val colorCode = classColors[ratingProfile.characterClass.name] ?: "\u001B[0m"
            val resetCode = "\u001B[0m"
            val boldCode = "\u001B[1m" // Bold

            "${index + 1}. $colorCode${ratingProfile.characterName}$resetCode: rating $boldCode${ratingProfile.rating}$resetCode"
        }
        val body = mapOf("content" to "$header\n$content\n```")

        val webhookUri = webhookUrl.substringAfter("https://discord.com")

        val request = HttpRequest.POST(webhookUri, body)
            .contentType(MediaType.APPLICATION_JSON_TYPE)

        httpClient.toBlocking().exchange(request, String::class.java)
    }
}
