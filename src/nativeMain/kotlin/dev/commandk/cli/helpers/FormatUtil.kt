package dev.commandk.cli.helpers

import dev.commandk.cli.models.CommandKRenderedAppSecret
import io.ktor.util.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class FormatUtil {
    fun formatSecrets(data: List<CommandKRenderedAppSecret>, format: String): String {
        return when (format) {
            "json" -> formatAsJson(data)
            "env" -> formatAsEnv(data)
            "yaml" -> formatAsYaml(data)
            else -> throw IllegalArgumentException("Unknown format $format")
        }
    }

    private fun formatAsJson(renderedSecrets: List<CommandKRenderedAppSecret>): String {
        val secrets = renderedSecrets.associate { it.key to JsonPrimitive(it.serializedValue) }
        return JsonObject(secrets).toString()
    }

    private fun formatAsEnv(renderedSecrets: List<CommandKRenderedAppSecret>): String {
        val keyValuePairs =
            renderedSecrets.map { it.key to it.serializedValue }

        val builder = StringBuilder()
        for ((key, value) in keyValuePairs) {
            builder.append("${key.toUpperCasePreservingASCIIRules()}=$value\n")
        }
        return builder.toString().trim()
    }

    private fun formatAsYaml(renderedSecrets: List<CommandKRenderedAppSecret>): String {
        val keyValuePairs =
            renderedSecrets.map { it.key to it.serializedValue }
        val yamlBuilder = StringBuilder()
        keyValuePairs.forEach { (key, value) ->
            yamlBuilder.append("$key: $value\n")
        }
        return yamlBuilder.toString().trim()
    }
}