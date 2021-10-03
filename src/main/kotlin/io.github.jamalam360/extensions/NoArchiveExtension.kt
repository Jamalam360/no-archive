package io.github.jamalam360.extensions

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.AutoAckType
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.channel.thread.ThreadChannelCreateEvent
import dev.kord.core.event.channel.thread.ThreadChannelDeleteEvent
import dev.kord.core.event.channel.thread.ThreadUpdateEvent
import io.github.jamalam360.Database

@OptIn(KordPreview::class)
class NoArchiveExtension : Extension() {
    private val db = Database()
    override val name = "noarchive"
    val error = "An unexpected error occurred"

    override suspend fun setup() {
        slashCommand(::Arguments) {
            name = "autonoarchive"
            description = "Set threads in this server to automatically be added to the NoArchive database"
            autoAck = AutoAckType.EPHEMERAL
            // guild(TEST_SERVER_ID)

            check {
                hasPermission(Permission.Administrator)
            }

            action {
                val guildId: Snowflake = this.guild!!.id

                if (!db.hasServerConfig(guildId)) {
                    db.createServerConfig(guildId)
                }

                val response: String = if (db.getServerConfig(guildId)!!.autoPrevent) {
                    db.updateServerConfig(guildId, false)
                    "Successfully turned off auto-no-archiving for this server"
                } else if (!db.getServerConfig(guildId)!!.autoPrevent) {
                    db.updateServerConfig(guildId, true)
                    "Successfully turned on auto-no-archiving for this server"
                } else {
                    error
                }

                ephemeralFollowUp {
                    content = response
                }
            }
        }

        slashCommand(::Arguments) {
            name = "preventarchive"
            description = "Prevent this thread from archiving"
            autoAck = AutoAckType.EPHEMERAL
            // guild(TEST_SERVER_ID)

            action {
                val response: String = if (!db.hasThread(this.channel.id) && isThread(this.channel)) {
                    db.addThread(this.channel.id)
                    "Successfully added thread to the NoArchive database"
                } else if (db.hasThread(this.channel.id)) {
                    "This thread is already on the NoArchive database"
                } else if (!isThread(this.channel)) {
                    "This command is only usable in a thread"
                } else {
                    error
                }

                ephemeralFollowUp {
                    content = response
                }
            }
        }

        slashCommand(::Arguments) {
            name = "allowarchive"
            description = "Allow this thread to archive again"
            autoAck = AutoAckType.EPHEMERAL
            // guild(TEST_SERVER_ID)

            action {
                val response: String = if (db.hasThread(this.channel.id) && isThread(this.channel)) {
                    db.removeThread(this.channel.id)
                    "Successfully removed thread from the NoArchive database"
                } else if (db.hasThread(this.channel.id)) {
                    "This thread is not on the NoArchive database"
                } else if (!isThread(this.channel)) {
                    "This command is only usable in a thread"
                } else {
                    error
                }

                ephemeralFollowUp {
                    content = response
                }
            }
        }

        event<ThreadUpdateEvent> {
            action {
                if (db.hasThread(event.channel.id) && event.channel.isArchived) {
                    event.channel.createMessage("Prevent Archive").delete()
                }
            }
        }

        event<ThreadChannelCreateEvent> {
            action {
                val guildId: Snowflake = this.event.channel.guildId

                if (!db.hasServerConfig(guildId)) {
                    db.createServerConfig(guildId)
                }

                if (db.getServerConfig(guildId)!!.autoPrevent) {
                    db.addThread(this.event.channel.id)
                }
            }
        }

        event<ThreadChannelDeleteEvent> {
            action {
                if (db.hasThread(this.event.old!!.id)) {
                    db.removeThread(this.event.old!!.id)
                }
            }
        }
    }

    private fun isThread(channel: MessageChannel): Boolean = channel.type == ChannelType.PrivateThread || channel.type == ChannelType.PublicGuildThread || channel.type == ChannelType.PublicNewsThread
}
