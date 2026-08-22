package net.incongru.brewt

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.findOrSetObject
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import net.incongru.brewt.cli.ScheduleCmd

fun startCLI(brewt: Brewt, args: Array<String>) {
    BrewtCLI(brewt).subcommands(
        UpdateAllCmd(),
        ScheduleCmd(brewt)
    ).main(args)
}

private class BrewtCLI(val brewt: Brewt) : CliktCommand() {
    override val invokeWithoutSubcommand = true

    val cfg by findOrSetObject { brewt.readConfig() }

    override fun run() {
        brewt.log.debug("Env: ${brewt.env}")
        brewt.log.debug("Configuration: $cfg")

        // No subcommand (or UpdateAllCmd) specified, run update:
        if (currentContext.invokedSubcommand == null || currentContext.invokedSubcommand is UpdateAllCmd) {
            updateAll(BrewWrapper(brewt), ApplescriptHelper(brewt.sh), brewt.log, cfg)
        }
    }
}

// Just a placeholder which we might as well remove, since we let the root Brewt cmd do the job
class UpdateAllCmd : NoOpCliktCommand("update-all") {}

