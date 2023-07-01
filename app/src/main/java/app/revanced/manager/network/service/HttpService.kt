package app.revanced.manager.network.service

import android.util.Log
import app.revanced.manager.network.utils.APIError
import app.revanced.manager.network.utils.APIFailure
import app.revanced.manager.network.utils.APIResponse
import app.revanced.manager.util.tag
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

/**
 * @author Aliucord Authors, DiamondMiner88
 */
class HttpService(
    val json: Json,
    val http: HttpClient,
) {
    suspend inline fun <reified T> request(builder: HttpRequestBuilder.() -> Unit = {}): APIResponse<T> {
        var body: String? = null

        val response = try {
            val response = http.request(builder)

            if (response.status.isSuccess()) {
                body = response.bodyAsText()

                if (T::class == String::class) {
                    return APIResponse.Success(body as T)
                }

                APIResponse.Success(json.decodeFromString<T>(body))
            } else {
                body = try {
                    response.bodyAsText()
                } catch (t: Throwable) {
                    null
                }

                Log.e(tag, "Failed to fetch: API error, http status: ${response.status}, body: $body")
                APIResponse.Error(APIError(response.status, body))
            }
        } catch (t: Throwable) {
            Log.e(tag, "Failed to fetch: error: $t, body: $body")
            APIResponse.Failure(APIFailure(t, body))
        }
        return response
    }
}