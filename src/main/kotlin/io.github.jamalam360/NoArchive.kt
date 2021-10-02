package io.github.jamalam360

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.Snowflake
import io.github.jamalam360.extensions.NoArchiveExtension

val TEST_SERVER_ID = Snowflake(
    env("TEST_SERVER")?.toLong()  // Get the test server ID from the env vars or a .env file
        ?: error("Env var TEST_SERVER not provided")
)

private val TOKEN = env("TOKEN")   // Get the bot' token from the env vars or a .env file
    ?: error("Env var TOKEN not provided")

suspend fun main() {
    val bot = ExtensibleBot(TOKEN) {
        slashCommands {
            enabled = true
        }

        extensions {
            add(::NoArchiveExtension)
        }
    }

    bot.start()
}
