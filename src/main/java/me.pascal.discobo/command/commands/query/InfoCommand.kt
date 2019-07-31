package me.pascal.discobo.command.commands.query

import me.pascal.discobo.Bot
import me.pascal.discobo.command.Command
import me.pascal.discobo.utils.DiscoboEmbed
import me.pascal.discobo.utils.JDAUtils
import net.dv8tion.jda.core.JDAInfo
import net.dv8tion.jda.core.entities.Message
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit


class InfoCommand : Command("info") {

    override fun run(arguments: Array<String>, message: Message, admin: Boolean, origin: Long, channel: Long) {

        val hours = String.format("%02d Hours",
                TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - Bot.startTime))
        val minutes = String.format("%02d Minutes",
                TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - Bot.startTime) % TimeUnit.HOURS.toMinutes(1))

        val runtime = "$hours $minutes"
        val dbVersion = Bot.database.connection.metaData.databaseProductVersion
        val statement = Bot.database.connection.createStatement()
        val set = statement.executeQuery(
                "SELECT " +
                        "(SELECT COUNT(*) FROM messages), " +
                        "(SELECT COUNT(*) FROM edits), " +
                        "(SELECT COUNT(*) FROM names), " +
                        "(SELECT COUNT(*) FROM statuses)")
        set.next()
        val messageCount = set.getInt(1)
        val editCount = set.getInt(2)
        val namechangeCount = set.getInt(3)
        val onlinestatusCount = set.getInt(4)
        set.close()
        statement.close()

        val embed = DiscoboEmbed()
        embed.setTitle("Statistics")
        embed.setDescription("API version: " + JDAInfo.VERSION)
        embed.addField(":clock2: Bot Runtime", runtime, true)
        embed.addField(":computer: Database Version", dbVersion, true)
        embed.addField(":love_letter: Messages",
                DecimalFormat("#,###").format(messageCount), true)
        embed.addField(":pencil2: Message Edits",
                DecimalFormat("#,###").format(editCount), true)
        embed.addField(":bust_in_silhouette: Name Changes",
                DecimalFormat("#,###").format(namechangeCount), true)
        embed.addField(":red_circle: Online status changes",
                DecimalFormat("#,###").format(onlinestatusCount), false)
        embed.setFooter("Requested by ${message.author.name}")
        JDAUtils.sendMessage(message.channel.sendMessage(embed.build()))

        super.run(arguments, message, admin, origin, channel)
    }
}