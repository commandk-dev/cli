package dev.commandk.cli.common

object CommonEnvironmentVars {
    const val CompletionScript = "CMDK_COMPLETION_SCRIPT"
    const val DefaultEnvironment = "CMDK_DEFAULT_ENVIRONMENT"
    const val ApiAccessToken = "CMDK_API_ACCESS_TOKEN"
    const val ApiEndpoint = "CMDK_API_ENDPOINT"

    object Internals {
        const val EnableHttpLogging = "CMDK_INTERNAL_ENABLE_HTTP_LOGGING"
    }
}
