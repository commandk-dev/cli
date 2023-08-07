package dev.commandk.cli.models

abstract class CliError : RuntimeException()

sealed class CentralDataError : CliError() {
    data class ProviderNotFound(
        val providerName: String,
        override val message: String = "No provider with name $providerName was found",
        override val cause: Exception? = null,
    ) : CentralDataError()

    data class EnvironmentNotFound(
        val environmentIdentifier: String,
        override val message: String = "No environment with identifier $environmentIdentifier was found",
        override val cause: Exception? = null,
    ) : CentralDataError()

    data class ApplicationNameNotUnique(
        override val message: String = "The provided name for the application was not found to be unique, please specify a catalogAppSubType to narrow it down",
        override val cause: Exception? = null
    ) : CentralDataError()
}

sealed class NetworkError : CliError() {
    data class GenericNetworkError(
        override val message: String = "Encountered a network error when making an API call",
        override val cause: Exception? = null,
    ) : NetworkError()
}

data class OperationNotImplemented(
    val operationName: String,
    override val message: String = "The operation $operationName is not implemented",
    override val cause: Exception?,
) : CliError()
