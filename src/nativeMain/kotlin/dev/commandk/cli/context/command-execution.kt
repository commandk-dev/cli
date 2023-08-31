package dev.commandk.cli.context

import arrow.core.Either
import dev.commandk.cli.common.CommonEnvironmentVars
import dev.commandk.cli.models.CliError
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.coroutines.runBlocking
import platform.posix.exit
import platform.posix.getenv
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class CliExecutionContext(
    private val commonContext: CommonContext
) : CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<CliExecutionContext>
    override val key: CoroutineContext.Key<*> get() = Key
    fun writeLine(line: String) {
        commonContext.terminal.println(line)
    }
    fun writeError(line: String) {
        commonContext.terminal.println(line, stderr = true)
    }
    fun writeLog(line: String) {}
    fun writeObject(output: CliCommandOutput) {}
}

abstract class CliCommandOutput

class StringCliCommandOutput : CliCommandOutput()

fun <T> CommonContext.executeCliCommand(block: suspend () -> Either<CliError, T>) {
    runBlocking(CliExecutionContext(this)) {
        try {
            block()
                .fold(ifLeft = {
                    it
                }, ifRight = { null })
        } catch (e: Exception) {
            e
        }
    }?.let { handleException(it) }
}

@OptIn(ExperimentalForeignApi::class)
private fun CommonContext.handleException(exception: Exception) {
    terminal.println("Received an error, exiting", stderr = true)
    terminal.println("  Message - ${exception.message ?: "Unknown error occurred, run with CMDK_DEBUG_MODE=true to print stacktrace"}")

    if (getenv(CommonEnvironmentVars.Internals.EnableDebugMode)?.toKString() == "true") {
        exception.printStackTrace()
    }
    exit(1)
}

suspend fun cc() = coroutineContext[CliExecutionContext.Key]
    ?: throw IllegalArgumentException("This function must be called from within a running CLI context")
