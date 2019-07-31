package me.pascal.discobo.command.commands.query

import me.pascal.discobo.Bot
import me.pascal.discobo.command.Command
import me.pascal.discobo.utils.JDAUtils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import java.text.DateFormat

class OnlineHistoryCommand : Command("onlinehistory") {

    override fun run(arguments: Array<String>, message: Message, admin: Boolean, origin: Long, channel: Long) {
        message.delete().queue()
        val user = if (message.mentionedUsers.size > 0) message.mentionedUsers[0] else message.jda.getUserById(arguments[0])
                ?: message.author
        var amount = arguments.last().toIntOrNull() ?: 1

        if (!admin && amount > 10) {
            amount = 10
        }

        val query = "SELECT * FROM (SELECT * FROM statuses WHERE userid='${user.id}' ORDER BY timestamp DESC LIMIT $amount) as reverse ORDER BY timestamp ASC"
        val set = Bot.database.connection.createStatement().executeQuery(query)

        val embed = EmbedBuilder()
        var content = "${user.name}s Online History: \n"
        embed.setTitle("Online Status History")
        var i = 0
        while (set.next()) {

            val status = set.getString(2)
            //val device = set.getString(3)
            val timestamp = DateFormat.getDateTimeInstance().format(set.getLong(4))

            content += "`$status` ${if (++i == amount) "since" else "from"} $timestamp to\n"
        }
        content = content.substring(0, content.length - 3)
        embed.setDescription(content)

        embed.setFooter("Requested by ${message.author.name}", null)
        JDAUtils.sendMessage(message.channel.sendMessage(embed.build()))

        super.run(arguments, message, admin, origin, channel)
    }

}