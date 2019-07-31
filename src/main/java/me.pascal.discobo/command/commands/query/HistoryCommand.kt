package me.pascal.discobo.command.commands.query

import me.pascal.discobo.Bot
import me.pascal.discobo.command.Command
import me.pascal.discobo.utils.DiscoboEmbed
import me.pascal.discobo.utils.JDAUtils
import net.dv8tion.jda.core.entities.Message
import java.text.DateFormat
import java.util.*

class HistoryCommand : Command("history") {

    override fun run(arguments: Array<String>, message: Message, admin: Boolean, origin: Long, channel: Long) {
        message.delete().queue()
        if (arguments.isEmpty()) {
            JDAUtils.sendMessage(message.channel.sendMessage("Not enough arguments"), true)
            return
        }
        val id = arguments[0].toLongOrNull()
        if (id == null || id !in 10000000000000000..999999999999999999) {
            JDAUtils.sendMessage(message.channel.sendMessage("Invalid argument `$id`, expected a message id (17-18 numbers)"), true)
            return
        }

        val query = "SELECT messages.messageid, authorid, messages.content, messages.timestamp, deleted, deletestamp, edits.content, edits.timestamp, attachments, originid FROM messages LEFT JOIN edits ON messages.messageid=edits.messageid WHERE messages.messageid = $id"
        val set = Bot.database.connection.createStatement().executeQuery(query)

        val embed = DiscoboEmbed()
        embed.setTitle("Message history")
        var state = 0
        while (set.next()) {
            if (state++ == 0) {
                val originalContent = set.getString(3)
                val author = message.jda.getUserById(set.getString(2))
                val messageId = set.getString(1)
                val timestamp = set.getLong(4)
                val attachments = set.getString(9)
                val text = "Author: ${author?.asMention}\n" +
                        "Timestamp: ${DateFormat.getDateTimeInstance().format(Date(timestamp))}\n" +
                        (if (attachments.isNullOrEmpty()) "" else "Attachments: ${JDAUtils.copyAttachments(messageId, attachments, set.getString(10))} \n") +
                        "MessageId: $messageId\n"
                embed.setDescription(text)

                val title = "Original State"
                val content = "Timestamp: ${DateFormat.getDateTimeInstance().format(Date(timestamp))}\n" +
                        "Content: ${if (originalContent.isEmpty()) "<no content>" else "```${JDAUtils.unEscapeString(originalContent)}```"}"
                embed.addField(title, content, false)
            }

            val editTimestamp = set.getLong(8)
            if (editTimestamp > 0) {
                val editContent =
                        if (set.getString(7).length > 1500) "${set.getString(7).substring(0, 1500)}..." else set.getString(7)
                val title = "Edit state ${state}"
                val content = "Timestamp: ${DateFormat.getDateTimeInstance().format(Date(editTimestamp))}\n" +
                        "Content: ${if (editContent.isEmpty()) "<no content>" else "```${JDAUtils.unEscapeString(editContent)}```"}"
                embed.addField(title, content, true)
            }

            if (set.isLast && set.getBoolean(5)) {
                val deleteTimestamp = set.getLong(6)
                val title = "Edit state $state"
                val content = "Timestamp: ${DateFormat.getDateTimeInstance().format(Date(deleteTimestamp))}\n" +
                        "--deleted--"
                embed.addField(title, content, false)
            }
        }

        if (state == 0) {
            JDAUtils.sendMessage(message.channel.sendMessage("Message `$id` not found."), true)
            return
        }

        embed.setFooter("Requested by ${message.author.name}")
        JDAUtils.sendMessage(message.channel.sendMessage(embed.build()))

        super.run(arguments, message, admin, origin, channel)
    }

}