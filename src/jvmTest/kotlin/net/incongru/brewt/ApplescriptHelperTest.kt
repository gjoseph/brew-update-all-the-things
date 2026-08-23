package net.incongru.brewt

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain

class ApplescriptHelperTest : FunSpec({

    test("apostrophes in messages are retained") {
        val sh = FakeShell(ShellResult(0, ""))
        ApplescriptHelper(sh).notif("Download failed on Cask 'docker-desktop'; retrying when it's back")
        // sh -c sees: echo '... Cask '\''docker-desktop'\''; ... it'\''s back ...' | osascript
        sh.commands.single() shouldContain """Cask '\''docker-desktop'\''; retrying when it'\''s back"""
    }
})
