package dev.commandk.cli.models

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class CommandKProvider(
    val id: String,
    val name: String,
    val slug: String
)

@Serializable
data class CommandKProviders(
    val providers: List<CommandKProvider>
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CommandKEnvironment(
    val id: String,
    @EncodeDefault val name: String = "",
    @EncodeDefault val slug: String = "",
    @EncodeDefault val label: String = ""
)

@Serializable
data class CommandKApp(
    val id: String,
    val name: String,
    val slug: String
)

@Serializable
data class CommandKEnvironments(
    val environments: List<CommandKEnvironment>
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CommandKCreateAppSecretRequest(
    val name: String,
    val providerId: String,
    val environmentScopedValues: List<CommandKEnvironmentScopedValue>,
    @EncodeDefault val purposeType: String = "Other",
)

@Serializable
data class CommandKSetAppSecretValuesRequest(
    val environmentScopedValues: List<CommandKEnvironmentScopedValue>
)

@Serializable
data class CommandKEnvironmentScopedValue(
    val environment: CommandKEnvironment,
    val value: CommandKAppSecretValue?,
)

@Serializable
data class CommandKAppSecret(
    val id: String,
    val name: String,
    val slug: String,
    val environmentScopedValues: List<CommandKEnvironmentScopedValue>,
    @EncodeDefault val purposeType: String = "Other"
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CommandKAppSecretValue(
    val kvAppSecretValue: CommandKKeyValueAppSecretValue,
    val activity: String?,
    @EncodeDefault val hidden: Boolean = false,
    @EncodeDefault val valueTypeUrn: String = "urn:app-secret:app-secret-type:keyValueAppSecret",
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CommandKKeyValueAppSecretValue(
    val text: String,
    @EncodeDefault val typeHint: String = "urn:app-secret:kv-secret-value:textType",
)

@Serializable
data class CommandKAppRenderedSecrets(
    val secrets: List<CommandKRenderedAppSecret>
)

@Serializable
data class CommandKRenderedAppSecret(
    val key: String,
    val valueType: String,
    val secretId: String,
    val serializedValue: String,
)
