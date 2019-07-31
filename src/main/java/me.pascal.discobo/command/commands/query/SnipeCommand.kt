package me.pascal.discobo.command.commands.query

import me.pascal.discobo.Bot
import me.pascal.discobo.command.Command
import me.pascal.discobo.utils.DiscoboEmbed
import me.pascal.discobo.utils.JDAUtils
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Message
import java.text.DateFormat
import java.util.*


class SnipeCommand : Command("snipe") {

    override fun run(arguments: Array<String>, message: Message, admin: Boolean, origin: Long, channel: Long) {
        message.delete().queue()
        val author = message.author
        val amount: Int
        val mentioned: Member?

        val query = if (message.mentionedUsers.size > 0) {
            mentioned = message.mentionedMembers[0]
            amount = if (arguments.size > 1) 1 else arguments[1].toInt()
            if (amount > 5 || amount < 1) {
                JDAUtils.sendMessage(message.channel.sendMessage("Invalid argument `$amount`, expected a number between 1 and 5"), true)
                return
            }
            "SELECT content, authorid, messageid, timestamp, attachments, originid FROM messages WHERE content NOT LIKE '+snipe%' AND deleted = true AND originid = '$origin' AND channelid = '$channel' AND authorid = '${mentioned.user.id}' ORDER BY deletestamp DESC LIMIT $amount"
        } else {
            mentioned = null
            amount = if (arguments.isEmpty()) 1 else arguments[0].toInt()
            if (amount > 5 || amount < 1) {
                JDAUtils.sendMessage(message.channel.sendMessage("Invalid argument `$amount`, expected a number between 1 and 5"), true)
                return
            }
            "SELECT content, authorid, messageid, timestamp, attachments, originid FROM messages WHERE content NOT LIKE '+snipe%' AND deleted = true AND originid = '$origin' AND channelid = '$channel' ORDER BY deletestamp DESC LIMIT $amount"
        }


        val set = Bot.database.connection.createStatement().executeQuery(query)

        val messagesToSend = arrayListOf<String>()
        var temp = ""
        while (set.next()) {
            val content =
                    if (set.getString(1).length > 1500) "${set.getString(1).substring(0, 1500)}..." else set.getString(1)
            val author = message.jda.getUserById(set.getString(2))
            val messageId = set.getString(3)
            val timestamp = set.getLong(4)
            val attachments = set.getString(5)
            val origin = set.getString(6)
            val text =
                    (if (mentioned == null) "Author: ${if (author == null) set.getString(2) else author.asMention}\n" else "") +
                            "Timestamp: ${DateFormat.getDateTimeInstance().format(Date(timestamp))}\n" +
                            (if (attachments.isNullOrEmpty()) "" else "Attachments: ${JDAUtils.copyAttachments(messageId, attachments, origin)} \n") +
                            "MessageId: $messageId\n" +
                            (if (content.isNotEmpty()) "```${content.replace("`", "")}```\n\n" else "\n\n")
            if (temp.length + text.length > 2000) {
                messagesToSend.add(temp)
                temp = text
            } else {
                temp += text
            }
        }
        messagesToSend.add(temp)
        for (s in messagesToSend) {
            var text = s
            val eb = DiscoboEmbed()
            eb.setTitle("Recent ($amount) deleted ${if (amount > 1) "messages" else "message"} ${if (mentioned == null) "" else "of ${mentioned.effectiveName}"}")
            if (text.endsWith("\n\n"))
                text = text.removeSuffix("\n\n")
            eb.setDescription(text)
            eb.setFooter("Requested by ${author.name}")
            JDAUtils.sendMessage(message.channel.sendMessage(eb.build()))
        }
        super.run(arguments, message, admin, origin, channel)
    }

}