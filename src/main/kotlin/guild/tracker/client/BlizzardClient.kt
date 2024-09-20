package guild.tracker.client

import com.fasterxml.jackson.databind.ObjectMapper
import guild.tracker.model.CharacterProfileResponse
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.uri.UriBuilder

class BlizzardClient(
    @Property(name = "blizzard.client-id") private val clientId: String,
    @Property(name = "blizzard.client-secret") private val clientSecret: String,
    @Client("https://us.battle.net") private val oauthClient: HttpClient,
    @Client("https://us.api.blizzard.com") private val apiClient: HttpClient,
    private val objectMapper: ObjectMapper
) {
    private var accessToken: String? = null
    private var tokenExpiration: Long = 0

    fun getAccessToken(): String {
        val currentTime = System.currentTimeMillis()
        if (accessToken != null && currentTime < tokenExpiration) {
            return accessToken!!
        }

        val request = HttpRequest.POST("/oauth/token", "grant_type=client_credentials")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .basicAuth(clientId, clientSecret)

        val response = oauthClient.toBlocking().retrieve(request, String::class.java)
        val jsonNode = objectMapper.readTree(response)
        accessToken = jsonNode["access_token"].asText()
        val expiresIn = jsonNode["expires_in"].asLong()
        tokenExpiration = currentTime + (expiresIn * 1000) - 60000 // Subtract 1 minute for safety

        return accessToken!!
    }

    fun getGuildRoster(realmSlug: String, guildSlug: String): List<Pair<String, String>> {
        val token = getAccessToken()
        val uri = UriBuilder.of("/data/wow/guild/$realmSlug/$guildSlug/roster")
            .queryParam("namespace", "profile-us")
            .queryParam("locale", "en_US")
            .queryParam("access_token", token)
            .build()

        val request = HttpRequest.GET<String>(uri)
        val response = apiClient.toBlocking().retrieve(request, String::class.java)
        val jsonNode = objectMapper.readTree(response)

        val members = mutableListOf<Pair<String, String>>()
        val memberNodes = jsonNode["members"]
        if (memberNodes != null && memberNodes.isArray) {
            for (memberNode in memberNodes) {
                val characterNode = memberNode["character"]
                val name = characterNode["name"].asText()
                val realm = characterNode["realm"]["slug"].asText()
                members.add(Pair(name, realm))
            }
        }
        return members
    }

    fun getCharacter(realmSlug: String, characterName: String): CharacterProfileResponse? {
        val token = getAccessToken()
        val uri = UriBuilder.of("/profile/wow/character/$realmSlug/${characterName.toLowerCase()}")
            .queryParam("namespace", "profile-us")
            .queryParam("locale", "en_US")
            .queryParam("access_token", token)
            .build()


        val request = HttpRequest.GET<String>(uri)
        return try {
            apiClient.toBlocking().retrieve(request, CharacterProfileResponse::class.java)
        } catch (e: Exception) {
            println("failed to retrieve character profile $characterName, exception $e")
            null
        }
    }

    fun getCharacterMythicPlusRatings(realmSlug: String, characterName: String): Int? {
        try {
            val token = getAccessToken()
            val uri =
                UriBuilder.of("/profile/wow/character/$realmSlug/${characterName.toLowerCase()}/mythic-keystone-profile")
                    .queryParam("namespace", "profile-us")
                    .queryParam("locale", "en_US")
                    .queryParam("access_token", token)
                    .build()

            val request = HttpRequest.GET<String>(uri)
            val response = apiClient.toBlocking().retrieve(request, String::class.java)
            val jsonNode = objectMapper.readTree(response)

            val currentMythicRatingNode = jsonNode["current_mythic_rating"]

            return currentMythicRatingNode["rating"].asDouble().toInt()
        } catch (e: NullPointerException) {
            println("failed to retrieve mythic rating for ${characterName}")
            return null
        }
    }

    //todo get weekly best run <- available in API
}
