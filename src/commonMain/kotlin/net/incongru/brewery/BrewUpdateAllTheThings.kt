package net.incongru.brewery

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
private fun log(msg: String) {
    println("${Clock.System.now()} $msg")
}

private fun notif(message: String) {
    "echo 'display notification \"$message\" with title \"🥜Brew upgrade\" sound name \"Frog\"' | osascript"
        .runCommand()
}

private fun confirm(title: String, message: String): Boolean {
    return "echo 'display alert \"$title\" message \"$message\" buttons {\"No\", \"🥜 Yes\"} default button 2 cancel button 1' | osascript"
        .runCommand(false) == 0
}

private fun runUpgrade(dryRun: Boolean, formulae: List<BrewFormula>, casks: List<BrewFormula>) {
    val cmdSuffix = if (dryRun) "--dry-run" else ""
    val logSuffix = if (dryRun) " (dry-run):" else ":"
    log("Brew upgrade$logSuffix")
    "brew upgrade --greedy $cmdSuffix ${formulae.asCliArgs()} ${casks.asCliArgs()}".runCommand()

    log("Brew autoremove$logSuffix")
    "brew autoremove $cmdSuffix".runCommand()

    log("Brew cleanup$logSuffix")
    "brew cleanup $cmdSuffix".runCommand()

    log("Brew doctor$logSuffix")
    if ("brew doctor".runCommand(!dryRun) != 0) {
        log("... the doc wasn't happy 🧑")
    }
}

@Serializable
private data class BrewFormula(
    val name: String,
    @SerialName("installed_versions")
    val installedVersions: List<String>,
    @SerialName("current_version")
    val currentVersion: String
) {
    override fun toString(): String {
        return "$name (${installedVersions.joinToString()} → $currentVersion)"
    }
}

@Serializable
private data class BrewOutdatedOutput(
    val formulae: List<BrewFormula>,
    val casks: List<BrewFormula>
)

private fun List<BrewFormula>.asCliArgs(): String {
    return this.joinToString(" ") { it.name }
}

fun main() {
    notif("Brew upgrade starting")
    log("Updating Homebrew:")
    "brew update".runCommand()

    val outdated = "brew outdated --greedy --json".runCommandAndCaptureOutputAs<BrewOutdatedOutput>()
    log("List of outdated formulae: ${outdated.formulae}")
    log("List of outdated casks: ${outdated.casks}")
    val formulaeToUpdate = outdated.formulae
    val casksToUpdate = outdated.casks.filterNot { it.name == "microsoft-excel" }

    if (formulaeToUpdate.isNotEmpty()) {
        log("Brew pre-fetch outdated formulae:")
        "brew fetch --formula --deps ${formulaeToUpdate.asCliArgs()}".runCommand()
    }

    if (casksToUpdate.isNotEmpty()) {
        log("Brew pre-fetch outdated casks:")
        "brew fetch --cask ${casksToUpdate.asCliArgs()}".runCommand()
    }

    if (formulaeToUpdate.isNotEmpty() || casksToUpdate.isNotEmpty()) {
        runUpgrade(true, formulaeToUpdate, casksToUpdate)

        log("Update and download done.")

        if (confirm(
                "🥜 Brew upgrade ready",
                "Outdated formulae: ${outdated.formulae}\n" +
                        "Outdated casks: ${outdated.casks}\n\n" +
                        "Do you want to install them now?"
            )
        ) {
            runUpgrade(false, formulaeToUpdate, casksToUpdate)
            notif("All your Homebrew packages are up-to-date")
        } else {
            log("Kthxbye")
        }
    } else {
        log("Nothing to upgrade")
        notif(".... there was nothing to upgrade")
    }
}

