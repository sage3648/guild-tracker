package guild.tracker.service

import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import java.net.URL

class DiscordApi(
    @Property(name = "discord.webhook.url") private val webhookUrl: String
) {
    private val httpClient: HttpClient = HttpClient.create(URL("https://discord.com"))

    fun postItemLevelMessage(data: List<Pair<String, Int>>) {
        val content = data.joinToString("\n") { "${it.first}: Item Level ${it.second}" }
        val body = mapOf("content" to content)

        // Extract the path from the webhook URL
        val webhookUri = webhookUrl.substringAfter("https://discord.com")

        val request = HttpRequest.POST(webhookUri, body)
            .contentType(MediaType.APPLICATION_JSON_TYPE)

        httpClient.toBlocking().exchange(request, String::class.java)
    }

    fun postMythicRatingsMessage(data: MutableList<Pair<String, Int>>) {
        val header = "Mythic+ Leaderboard \n"
        val content = data.withIndex()
            .joinToString("\n") { (index, rating) -> "${index + 1}. ${rating.first}: Mythic Rating ${rating.second}" }
        val body = mapOf("content" to header + content)

        // Extract the path from the webhook URL
        val webhookUri = webhookUrl.substringAfter("https://discord.com")

        val request = HttpRequest.POST(webhookUri, body)
            .contentType(MediaType.APPLICATION_JSON_TYPE)

        httpClient.toBlocking().exchange(request, String::class.java)
    }
}
