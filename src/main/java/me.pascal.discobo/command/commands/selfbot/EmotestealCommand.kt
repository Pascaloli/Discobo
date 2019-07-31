package me.pascal.discobo.command.commands.selfbot

import me.pascal.discobo.Bot
import me.pascal.discobo.command.Command
import net.dv8tion.jda.core.entities.Icon
import net.dv8tion.jda.core.entities.Message
import java.net.URL


class EmotestealCommand : Command("steal", true) {

    override fun run(arguments: Array<String>, message: Message, admin: Boolean, origin: Long, channel: Long) {
        message.delete().queue()

        val guild = Bot.jda.getGuildById("549888103012368385")
        var emoteid = arguments[0]
        val name = arguments[1]

        val animated = emoteid.endsWith("a")
        if (animated) emoteid = emoteid.removeSuffix("a")
        val filetype = if (animated) "gif" else "png"

        val url = "https://cdn.discordapp.com/emojis/$emoteid.$filetype?v=1"
        val con = URL(url).openConnection()
        con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0")
        val icon = Icon.from(con.getInputStream())
        guild.getController().createEmote(name, icon).complete()
    }

}