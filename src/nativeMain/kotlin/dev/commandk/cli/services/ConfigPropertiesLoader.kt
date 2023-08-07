package dev.commandk.cli.services

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

class ConfigPropertiesLoader {
    fun loadProperties(file: String): Map<String, String> {
        if (!FileSystem.SYSTEM.exists(file.toPath())) {
            return emptyMap()
        }

        val contents = FileSystem.SYSTEM.source(file.toPath()).use { fileSource ->
            fileSource.buffer().use { bufferedFileSource ->
                bufferedFileSource.readUtf8()
            }
        }

        val properties = Json.parseToJsonElement(contents)

        return (properties as JsonObject).toMap()
            .mapValues { (it.value as JsonPrimitive).content }
    }
}
