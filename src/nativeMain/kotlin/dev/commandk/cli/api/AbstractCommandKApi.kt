package dev.commandk.cli.api

import dev.commandk.cli.CommandK
import dev.commandk.cli.common.CommonEnvironmentVars
import dev.commandk.cli.context.AccessAuthorizationParameters
import dev.commandk.cli.context.CommonContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.curl.Curl
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Url
import io.ktor.http.append
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.use
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import platform.posix.getenv
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform.osFamily

abstract class AbstractCommandKApi(
    private val commonContext: CommonContext,
) : CommandKApi {
    @OptIn(ExperimentalForeignApi::class)
    protected suspend fun <T> apiCall(block: suspend HttpClient.() -> T): T {
        return HttpClient(Curl) {
            expectSuccess = false
            install(AccessTokenHeaderPlugin)

            if (getenv(CommonEnvironmentVars.Internals.EnableHttpLogging)?.toKString().toBoolean()) {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.BODY
                }
            }

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }.use { client -> client.block() }
    }

    @OptIn(ExperimentalNativeApi::class)
    private val CliUserAgentPlugin = createClientPlugin("CliUserAgentPlugin") {
        onRequest { request, _ ->
            request.headers.append("User-Agent", "CommandK CLI/${CommandK.Version} os: ${osFamily.name}, arch: ${Platform.cpuArchitecture.name}")
        }
    }

    private val AccessTokenHeaderPlugin = createClientPlugin("AccessTokenHeaderPlugin") {
        onRequest { request, _ ->

            //If there is no scheme present, resolve to a default scheme
            val resolvedApiEndpoint = if(!commonContext.apiEndpoint.contains("://")) {
               "https://${commonContext.apiEndpoint}"
            } else {
                commonContext.apiEndpoint
            }

            val endpoint = Url(resolvedApiEndpoint)
            request.url.host = endpoint.host
            request.url.port = endpoint.port
            request.url.protocol = endpoint.protocol

            (commonContext.accessAuthorizationParameters as? AccessAuthorizationParameters.ApiAccessToken)?.let { apiAccessToken ->
                request.headers.append(
                    "Authorization",
                    "Bearer ${apiAccessToken.accessToken}",
                )

                request.headers.append(
                    "x-commandk-cli-access-params",
                    "api-access-token",
                )
            }

            (commonContext.accessAuthorizationParameters as? AccessAuthorizationParameters.UserSessionToken)?.let { (sessionToken) ->
                request.headers.append(
                    "x-commandk-user-session-token",
                    sessionToken
                )

                request.headers.append(
                    "x-commandk-cli-access-params",
                    "user-session-token",
                )
            }
        }
    }

    suspend fun deserializeResponseError(response: HttpResponse): JsonObject {
        return Json.decodeFromString<JsonObject>(response.body())
    }

    suspend fun decodeResponseError(response: HttpResponse) : String {
        val responseString =  response.body<String>()
        return try {
            val responseStructured = Json.decodeFromString<JsonObject>(responseString)
            return "RequestId=${responseStructured["requestId"]}," +
                " ErrorCodes=(${response.status.value}, ${responseStructured["error"]})," +
                " Message=${responseStructured["message"]}"
        } catch (e: Exception) {
            e.printStackTrace()
            responseString
        }
    }
}
