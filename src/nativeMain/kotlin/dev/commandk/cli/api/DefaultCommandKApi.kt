package dev.commandk.cli.api

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.github.ajalt.mordant.rendering.TextColors
import dev.commandk.cli.context.CommonContext
import dev.commandk.cli.context.cc
import dev.commandk.cli.models.CliError
import dev.commandk.cli.models.CommandKApp
import dev.commandk.cli.models.CommandKAppRenderedSecrets
import dev.commandk.cli.models.CommandKAppSecretDescriptor
import dev.commandk.cli.models.CommandKAppSecretValue
import dev.commandk.cli.models.CommandKCreateAppSecretRequest
import dev.commandk.cli.models.CommandKEnvironment
import dev.commandk.cli.models.CommandKEnvironments
import dev.commandk.cli.models.CommandKKeyValueAppSecretValue
import dev.commandk.cli.models.CommandKProviders
import dev.commandk.cli.models.NetworkError
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.serialization.json.Json

class DefaultCommandKApi(
    private val commonContext: CommonContext,
) : AbstractCommandKApi(commonContext) {
    override suspend fun getCatalogApp(appName: String): Either<CliError, CommandKApp> {
        return apiCall {
            val response = get("/apps/${appName}?identifierType=Name")

            if (response.status.value in 200 .. 299) {
                response.body<CommandKApp>().right()
            } else {
                NetworkError.GenericNetworkError("An error occured trying to fetch the application." +
                    " Server response - ${decodeResponseError(response)}").left()
            }
        }
    }

    override suspend fun getProviders(): Either<NetworkError, CommandKProviders> {
        return apiCall {
            val response = get("/providers")
            if (response.status.value in 200 .. 299) {
                response.body<CommandKProviders>()
                    .right()
            } else {
                NetworkError.GenericNetworkError("Received an error from the server, could not fetch " +
                    "providers status=${response.status.value}. Server response - ${decodeResponseError(response)}")
                    .left()
            }
        }
    }

    override suspend fun getEnvironments(): Either<NetworkError, CommandKEnvironments> {
        return apiCall {
            val response = get("/environments")
            if (response.status.value in 200 .. 299) {
                response
                    .body<CommandKEnvironments>()
                    .right()
            } else {
                NetworkError.GenericNetworkError("Received an error from the server, could not fetch" +
                    " environments status=${response.status.value}. Server response - ${decodeResponseError(response)}"
                )
                    .left()
            }
        }
    }

    override suspend fun getRenderedSecrets(applicationName: String, commandKEnvironment: CommandKEnvironment): Either<NetworkError, CommandKAppRenderedSecrets> {
        return apiCall {
            val response = get("/apps/${applicationName}/secrets/rendered"){
                parameter("environment", commandKEnvironment.id)
                parameter("mode", "Full")
            }

            if (response.status.value in 200 .. 299) {
                response.body<CommandKAppRenderedSecrets>()
                    .right()
            } else {
                NetworkError.GenericNetworkError("Received an error from the server, could not fetch" +
                    " environments status=${response.status.value}. Server response - ${decodeResponseError(response)}"
                )
                    .left()
            }

        }
    }

    override suspend fun writeSecretValue(
        applicationId: String,
        commandKEnvironment: CommandKEnvironment,
        providerId: String,
        secretName: String,
        secretValue: String
    ): Either<NetworkError, Unit> {
        return apiCall {
            val createAppSecretRequest = CommandKCreateAppSecretRequest(
                name = secretName,
                providerId = providerId,
                environmentScopedValues = listOf()
            )

            val response = post("/apps/${applicationId}/secrets") {
                setBody(createAppSecretRequest)
                headers.append("Content-Type", "application/json")
            }

            val secretId = if (response.status.value in 200..299) {
                val createdAppSecretResponse = response.body<CommandKAppSecretDescriptor>()
                cc().writeLine("Created secret ${TextColors.green("[$secretName] (id=${createdAppSecretResponse.id})")}, writing values")
                createdAppSecretResponse.id.right()
            } else {
                if (response.status.value == 409) {
                    val existingSecretId = Json.decodeFromString<Map<String, String>>(response.body<String>())["entityId"]!!
                    cc().writeLine("Secret ${TextColors.green("[$secretName] (id=${existingSecretId})")} already exists, updating values")
                    existingSecretId.right()
                } else {
                    cc().writeError("Failed to write secret ${TextColors.green("[$secretName]")}")
                    NetworkError.GenericNetworkError("Received an error from the server, status=${response.status.value}")
                        .left()
                }
            }

            secretId.flatMap { operativeSecretId ->
                writeSecretValues(
                    applicationId,
                    secretName,
                    operativeSecretId,
                    commandKEnvironment.id,
                    CommandKAppSecretValue(
                        kvAppSecretValue = CommandKKeyValueAppSecretValue(
                            text = secretValue
                        ),
                        activity = null
                    )
                )
            }
        }
    }

    private suspend fun writeSecretValues(
        applicationId: String,
        secretName: String,
        secretId: String,
        environmentId: String,
        appSecretValue: CommandKAppSecretValue
    ) : Either<NetworkError, Unit> {
        return apiCall {
            val response = put("/apps/${applicationId}/secrets/$secretId/environments/$environmentId/values") {
                setBody(appSecretValue)
                headers.append("Content-Type", "application/json")
            }

            if (response.status.value in 200 .. 209) {
                cc().writeLine("  Update/Set secret value for [${TextColors.green(secretName)}]")
                Unit.right()
            } else {
                cc().writeError("Failed to write secret ${TextColors.green("[${secretName}]")}")
                NetworkError.GenericNetworkError("Received an error from the server, status=${response.status.value}")
                    .left()
            }
        }
    }
}
