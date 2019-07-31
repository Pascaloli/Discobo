package me.pascal.discobo.command.commands.admin

import me.pascal.discobo.Bot
import me.pascal.discobo.command.Command
import me.pascal.discobo.utils.JDAUtils
import net.dv8tion.jda.core.entities.Message

class AdminCommand : Command("admin", true) {

    override fun run(arguments: Array<String>, message: Message, admin: Boolean, origin: Long, channel: Long) {
        message.delete().queue()
        if (arguments.isEmpty()) {
            JDAUtils.sendMessage(message.channel.sendMessage("Not enough arguments"), true)
            return
        }
        val id = arguments[0].toLongOrNull()
        if ((id == null || id !in 10000000000000000..999999999999999999) && message.mentionedUsers.size == 0) {
            JDAUtils.sendMessage(message.channel.sendMessage("Invalid argument `$id`, expected a user id (17-18 numbers)"), true)
            return
        }

        val user = Bot.jda.getUserById(if (id == null) message.mentionedUsers[0].id else id.toString())!!
        val wasAdmin = JDAUtils.isAdmin(user.id)

        val query = if (wasAdmin) "DELETE FROM admins WHERE userid=${user.id}" else "INSERT INTO admins(userid, admin) VALUES(${user.id},1)"
        Bot.database.connection.createStatement().executeUpdate(query)

        JDAUtils.sendMessage(message.channel.sendMessage("${user.asMention} (${user.id})" +
                " is ${if (wasAdmin) "no longer" else "now"} admin."))

        JDAUtils.adminCache.put(user.id, !wasAdmin)
        super.run(arguments, message, admin, origin, channel)
    }

}