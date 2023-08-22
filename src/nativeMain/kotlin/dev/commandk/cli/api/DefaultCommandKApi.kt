package dev.commandk.cli.api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.github.ajalt.mordant.rendering.TextColors
import dev.commandk.cli.context.CommonContext
import dev.commandk.cli.context.cc
import dev.commandk.cli.models.CentralDataError
import dev.commandk.cli.models.CliError
import dev.commandk.cli.models.CommandKApp
import dev.commandk.cli.models.CommandKAppRenderedSecrets
import dev.commandk.cli.models.CommandKAppSecret
import dev.commandk.cli.models.CommandKAppSecretValue
import dev.commandk.cli.models.CommandKCreateAppSecretRequest
import dev.commandk.cli.models.CommandKEnvironment
import dev.commandk.cli.models.CommandKEnvironmentScopedValue
import dev.commandk.cli.models.CommandKEnvironments
import dev.commandk.cli.models.CommandKKeyValueAppSecretValue
import dev.commandk.cli.models.CommandKProviders
import dev.commandk.cli.models.CommandKSetAppSecretValuesRequest
import dev.commandk.cli.models.NetworkError
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.util.Identity.decode
import kotlinx.serialization.json.Json
import platform.posix.err

class DefaultCommandKApi(
    private val commonContext: CommonContext,
) : AbstractCommandKApi(commonContext) {
    override suspend fun getCatalogApp(appName: String, catalogAppSubtype: String?): Either<CliError, CommandKApp> {
        return apiCall {
            val subTypeParam = catalogAppSubtype?.let { subType -> "&catalogAppSubType=$subType" } ?: ""
            val response = get("/apps/${appName}?identifierType=Name$subTypeParam")

            if (response.status.value in 200 .. 299) {
                response.body<CommandKApp>().right()
            } else {
                if (response.status.value == 400) {
                    try {
                        val error = deserializeResponseError(response)["errorType"]?.toString()
                        if (error == "DataEntityNonSingular") {
                            CentralDataError.ApplicationNameNotUnique().left()
                        } else {
                            NetworkError.GenericNetworkError("Could not decode response from the network")
                                .left()
                        }
                    } catch (e: Exception) {
                        NetworkError.GenericNetworkError("Could not decode response from the network")
                            .left()
                    }
                } else {
                    NetworkError.GenericNetworkError("An error occured trying to fetch the application." +
                        " Server response - ${decodeResponseError(response)}").left()
                }
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
                environmentScopedValues = listOf(
                    CommandKEnvironmentScopedValue(
                        environment = commandKEnvironment,
                        value = CommandKAppSecretValue(
                            activity = "Create",
                            kvAppSecretValue = CommandKKeyValueAppSecretValue(
                                text = secretValue
                            )
                        )
                    )
                )
            )

            val response = post("/apps/${applicationId}/secrets") {
                setBody(createAppSecretRequest)
                headers.append("Content-Type", "application/json")
            }

            if (response.status.value in 200..299) {
                cc().writeLine("Wrote secret ${TextColors.green("[$secretName]")}")
                Unit.right()
            } else {
                if (response.status.value == 409) {
                    cc().writeLine("Secret ${TextColors.green("[$secretName]")} already exists, attempting to update values only")
                    val entityIdToRetry = Json.decodeFromString<Map<String, String>>(response.body<String>())["entityId"]!!
                    updateSecretValues(
                        applicationId,
                        entityIdToRetry,
                        CommandKSetAppSecretValuesRequest(createAppSecretRequest.environmentScopedValues)
                    )
                } else {
                    cc().writeError("Failed to write secret ${TextColors.green("[$secretName]")}")
                    NetworkError.GenericNetworkError("Received an error from the server, status=${response.status.value}")
                        .left()
                }
            }
        }
    }

    private suspend fun updateSecretValues(
        applicationId: String,
        secretId: String,
        setAppSecretValuesRequest: CommandKSetAppSecretValuesRequest
    ) : Either<NetworkError, Unit> {
        val existingSecretRequest = apiCall {
            val response = get("/apps/${applicationId}/secrets/$secretId")
            response.body<CommandKAppSecret>()
        }

        return apiCall {
            val response = put("/apps/${applicationId}/secrets/$secretId") {
                setBody(
                    existingSecretRequest.copy(
                        environmentScopedValues = setAppSecretValuesRequest.environmentScopedValues.map { envScopedValue ->
                            envScopedValue.copy(value = envScopedValue.value?.copy(
                                activity = if (existingSecretRequest.environmentScopedValues.any {
                                    it.environment.id == envScopedValue.environment.id && it.value != null
                                }) {
                                    "Update"
                                } else {
                                    "Create"
                                }
                            ))
                        }
                    )
                )
                headers.append("Content-Type", "application/json")
            }

            if (response.status.value in 200 .. 209) {
                cc().writeLine("  Update/Set secret value for [${TextColors.green(existingSecretRequest.name)}]")
                Unit.right()
            } else {
                cc().writeError("Failed to write secret ${TextColors.green("[${existingSecretRequest.name}]")}")
                NetworkError.GenericNetworkError("Received an error from the server, status=${response.status.value}")
                    .left()
            }
        }
    }
}
