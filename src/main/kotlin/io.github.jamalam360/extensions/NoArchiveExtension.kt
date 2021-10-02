package io.github.jamalam360.extensions

import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.AutoAckType
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ChannelType
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.channel.thread.ThreadUpdateEvent
import io.github.jamalam360.Database
import io.github.jamalam360.TEST_SERVER_ID

@OptIn(KordPreview::class)
class NoArchiveExtension : Extension() {
    private val db = Database()
    override val name = "noarchive"

    override suspend fun setup() {
        slashCommand(::Arguments) {
            name = "preventarchive"
            description = "Prevent this thread from archiving"
            autoAck = AutoAckType.EPHEMERAL
            guild(TEST_SERVER_ID)

            action {
                val response: String = if (!db.hasThread(this.channel.id) && isThread(this.channel)) {
                    db.addThread(this.channel.id)
                    "Successfully added thread to the NoArchive database"
                } else if (db.hasThread(this.channel.id)) {
                    "This thread is already on the NoArchive database"
                } else if (!isThread(this.channel)) {
                    "This command is only usable in a thread"
                } else {
                    "An unexpected error occurred"
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
            guild(TEST_SERVER_ID)

            action {
                val response: String = if (db.hasThread(this.channel.id) && isThread(this.channel)) {
                    db.removeThread(this.channel.id)
                    "Successfully removed thread from the NoArchive database"
                } else if (db.hasThread(this.channel.id)) {
                    "This thread is not on the NoArchive database"
                } else if (!isThread(this.channel)) {
                    "This command is only usable in a thread"
                } else {
                    "An unexpected error occurred"
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
    }

    private fun isThread(channel: MessageChannel): Boolean {
        return channel.type == ChannelType.PrivateThread
                || channel.type == ChannelType.PublicGuildThread
                || channel.type == ChannelType.PublicNewsThread
    }
}
