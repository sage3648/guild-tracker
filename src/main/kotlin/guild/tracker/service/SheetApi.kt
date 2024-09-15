package guild.tracker.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.uri.UriBuilder
import java.net.URL
import java.time.Instant
import java.util.*

class SheetApi(
    @Property(name = "google.service-account.key") private val serviceAccountKey: String,
    @Property(name = "google.spreadsheet.id") private val spreadsheetId: String,
    private val objectMapper: ObjectMapper
) {
    private val oauthClient: HttpClient = HttpClient.create(URL("https://oauth2.googleapis.com"))
    private val sheetsApiClient: HttpClient = HttpClient.create(URL("https://sheets.googleapis.com"))
    private var accessToken: String? = null
    private var tokenExpiration: Instant = Instant.EPOCH

    fun getAccessToken(): String {
        val currentTime = Instant.now()
        if (accessToken != null && currentTime.isBefore(tokenExpiration)) {
            return accessToken!!
        }

        val jwt = generateJwtToken()
        val requestBody = mapOf(
            "grant_type" to "urn:ietf:params:oauth:grant-type:jwt-bearer",
            "assertion" to jwt
        )

        val request = HttpRequest.POST("/token", requestBody)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_TYPE)

        val response = oauthClient.toBlocking().retrieve(request, String::class.java)
        val jsonNode = objectMapper.readTree(response)
        accessToken = jsonNode["access_token"].asText()
        val expiresIn = jsonNode["expires_in"].asLong()
        tokenExpiration = currentTime.plusSeconds(expiresIn - 60) // Subtract 60 seconds for safety

        println("got access sheets access token!")
        return accessToken!!
    }

    fun updateSheet(data: List<Pair<String, Int>>) {
        println("attempting to update google sheet")

        try {
            val token = getAccessToken()
            val uri = UriBuilder.of("/v4/spreadsheets/$spreadsheetId/values/Sheet1!A2:append")
                .queryParam("valueInputOption", "RAW")
                .build()

            val values = data.map { listOf(it.first, it.second) }
            val body = mapOf("values" to values)

            val request = HttpRequest.POST(uri, body)
                .contentType(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")

            sheetsApiClient.toBlocking().exchange(request, String::class.java)
        } catch (e: Exception){
            println("failed in updateSheet function with exception $e")
        }
        println("updated sheet")
    }

    private fun generateJwtToken(): String {
        // Parse the service account key JSON
        val serviceAccount = objectMapper.readTree(serviceAccountKey)
        val clientEmail = serviceAccount["client_email"].asText()
        val privateKeyPem = serviceAccount["private_key"].asText()

        // Create the JWT header
        val header = mapOf(
            "alg" to "RS256",
            "typ" to "JWT"
        )

        // Create the JWT claim set
        val now = Instant.now().epochSecond
        val claimSet = mapOf(
            "iss" to clientEmail,
            "scope" to "https://www.googleapis.com/auth/spreadsheets",
            "aud" to "https://oauth2.googleapis.com/token",
            "exp" to now + 3600,
            "iat" to now
        )

        // Encode the header and claim set
        val headerJson = objectMapper.writeValueAsString(header)
        val claimSetJson = objectMapper.writeValueAsString(claimSet)

        val headerBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.toByteArray())
        val claimSetBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(claimSetJson.toByteArray())

        val unsignedToken = "$headerBase64.$claimSetBase64"

        // Sign the JWT using the private key
        val signature = signData(unsignedToken.toByteArray(), privateKeyPem)
        val signatureBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(signature)

        // Construct the final JWT
        return "$unsignedToken.$signatureBase64"
    }

    private fun signData(data: ByteArray, privateKeyPem: String): ByteArray {
        val privateKey = getPrivateKeyFromPem(privateKeyPem)
        val signatureInstance = java.security.Signature.getInstance("SHA256withRSA")
        signatureInstance.initSign(privateKey)
        signatureInstance.update(data)
        return signatureInstance.sign()
    }

    private fun getPrivateKeyFromPem(pem: String): java.security.PrivateKey {
        val cleanedPem = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")
        val decoded = Base64.getDecoder().decode(cleanedPem)
        val keySpec = java.security.spec.PKCS8EncodedKeySpec(decoded)
        val keyFactory = java.security.KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec)
    }
}
