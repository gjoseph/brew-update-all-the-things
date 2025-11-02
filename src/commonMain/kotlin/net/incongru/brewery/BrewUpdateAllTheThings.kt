package net.incongru.brewery

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
private fun log(msg: String) {
    println("${Clock.System.now()} $msg")
}

private fun notif(title: String, message: String) {
    "echo 'display notification \"$message\" with title \"$title\" sound name \"Frog\"' | osascript"
        .runCommand()
}

private fun confirm(title: String, message: String): Boolean {
    return "echo 'display alert \"$title\" message \"$message\" buttons {\"No\", \"🥜 Yes\"} default button 2 cancel button 1' | osascript"
        .runCommand(false) == 0
}

fun runUpgrade(dryRun: Boolean) {
    val suffix = if (dryRun) "--dry-run" else ""
    val logSuffix = if (dryRun) " (dry-run):" else ":"
    log("Brew upgrade$logSuffix")
    "brew upgrade --greedy $suffix".runCommand()
    log("Brew autoremove$logSuffix")
    "brew autoremove $suffix".runCommand()
    log("Brew cleanup$logSuffix")
    "brew cleanup $suffix".runCommand()
    log("Brew doctor$logSuffix")
    if ("brew doctor".runCommand(!dryRun) != 0) {
        log("... the doc wasn't happy 🧑")
    }
}


fun main() {
    log("Updating Homebrew:")
    // brew update echoes "Updated N taps" to stderr, which causes cronic to sends me an unnecessary email, but other stderr output is useful
    // well we don't care here, do we
    // "(brew update 2> >(grep -vE \"^Updated \\d taps\" >&2))".runCommand()
    "brew update".runCommand()

    log("Brew pre-fetch outdated formulaes:")
    "brew outdated -q --formula | xargs brew fetch --formula --deps".runCommand()
    log("Brew pre-fetch outdated casks:")
    "brew outdated -q --cask --greedy | xargs brew fetch --cask".runCommand()

    runUpgrade(true)
    val outdated = "brew outdated --greedy".runCommandAndCaptureOutput()

    log("Update and download done. List of outdated formulaes and casks: $outdated")

    if (confirm(
            "🥜 Brew upgrade ready", "Outdated packages: ${outdated}\nDo you want to install them now?"
        )
    ) {
        runUpgrade(false)
        notif(
            "🥜Brew upgrade completed",
            "Congrats, Homebrew upgrade is finally completed"
        )
    } else {
        log("kthxbye")
    }
}

