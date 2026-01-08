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
    "display notification \"$message\" with title \"🥜Brew upgrade\" sound name \"Frog\"".runAsAppleScript()
}

private fun confirm(message: String): Boolean {
    // if we have a cancel button, we can use exit code of the process since it yields an error
    return "display alert \"🥜 Brew upgrade\" message \"$message\" buttons {\"No\", \"🥜 Yes\"} default button 2 cancel button 1"
        .runAsAppleScript(exitOnFail = false, noOutput = true) == 0
}

private fun String.runAsAppleScript(exitOnFail: Boolean = true, noOutput: Boolean = false): Int {
    // -s e : print errors on stderr (default)
    // -s o : print errors on stdout
    // -s h : print result in human-readable form (default).
    // -s s : print result in parseable form
    // If using "noOutput", we'll use -sh to avoid `""` in the logs; otherwise we'll use -ss which is vaguely more parseable.
    // `tell result to return` would mean "return nothing"; with -ss that would still print a `""`.
    // `tell result to return button returned` or `tell result to return someVar` -- this explicitly makes the script return nothing
    val script = this // if (noOutput) "$this\ntell result to return" else this
    val flags = if (noOutput) "-s h > /dev/null" else "-s s"
    return "echo '$script' | osascript -s o ${flags}".runCommand(exitOnFail)
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
    notif("Brew upgrade starting 😊")
    log("Updating Homebrew:")
    "brew update".runCommand()

    val outdated = "brew outdated --greedy --json".runCommandAndCaptureOutputAs<BrewOutdatedOutput>()
    log("Outdated formulae: ${outdated.formulae}")
    log("Outdated casks: ${outdated.casks}")
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
                "Outdated formulae: ${formulaeToUpdate}\n" +
                        "Outdated casks: ${casksToUpdate}\n\n" +
                        "Do you want to install them now?"
            )
        ) {
            runUpgrade(false, formulaeToUpdate, casksToUpdate)
            notif("All your Homebrew packages are up-to-date 😀")
        } else {
            log("Kthxbye")
        }
    } else {
        log("Nothing to upgrade")
        notif(".... there was nothing to upgrade 😏")
    }
}

