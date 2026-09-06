package net.incongru.brewt

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class RecordingLogger : Logger {
    val lines = mutableListOf<String>()
    override fun invoke(level: LogLevel, message: String) {
        lines += "$level $message"
    }
}

class MutableClock(var current: Instant = Instant.fromEpochMilliseconds(1_000_000_000_000)) : Clock {
    override fun now(): Instant = current
    operator fun plusAssign(d: Duration) { current += d }
}

class FakeShell(private val result: ShellResult) : Shell {
    val commands = mutableListOf<String>()
    override fun invoke(command: String): ShellResult {
        commands += command
        return result.orThrow()
    }
    override fun withoutThrowing(command: String): ShellResult {
        commands += command
        return result
    }
}
