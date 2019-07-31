package me.pascal.discobo.command.commands.query

import me.pascal.discobo.Bot
import me.pascal.discobo.command.Command
import me.pascal.discobo.utils.JDAUtils
import net.dv8tion.jda.core.entities.Message
import java.util.concurrent.TimeUnit

class DeleteCommand : Command("delete", true) {

    override fun run(arguments: Array<String>, message: Message, admin: Boolean, origin: Long, channel: Long) {
        message.delete().queue()
        if (arguments.isEmpty()) {
            JDAUtils.sendMessage(message.channel.sendMessage("Not enough arguments"), true)
            return
        }
        val author = message.author.id

        val amount = arguments[0].toIntOrNull()
        if (amount == null || amount >= 100 || amount < 0) {
            message.channel.sendMessage("Invalid argument `$amount`, expected a number between 0 and 100").submit().get().delete().submitAfter(10, TimeUnit.SECONDS)
            return
        }
        val query = "SELECT messageid FROM messages WHERE deleted = false AND authorid = '$author' AND originid = '$origin' AND channelid = '$channel' AND messageid != '${message.id}' ORDER BY timestamp DESC LIMIT $amount"
        val set = Bot.database.connection.createStatement().executeQuery(query)

        while (set.next()) {
            val messageId = set.getString(1)
            message.channel.deleteMessageById(messageId).queue()
        }

        super.run(arguments, message, admin, origin, channel)
    }
}