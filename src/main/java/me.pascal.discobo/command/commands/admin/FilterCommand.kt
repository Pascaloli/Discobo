package me.pascal.discobo.command.commands.admin

import me.pascal.discobo.Bot
import me.pascal.discobo.command.Command
import me.pascal.discobo.utils.JDAUtils
import net.dv8tion.jda.core.entities.Message

class FilterCommand : Command("filter", true) {

    override fun run(arguments: Array<String>, message: Message, admin: Boolean, origin: Long, channel: Long) {
        message.delete().queue()
        if (arguments.isEmpty()) {
            var output = "Current Filters:\n"

            val query = "SELECT * FROM filters"
            val set = Bot.database.connection.createStatement().executeQuery(query)
            while (set.next()) {
                output += "- `${set.getString(1)}`\n"
            }
            JDAUtils.sendMessage(message.channel.sendMessage(output))
        } else {
            val filter = message.contentRaw.substring(8)
            val output = if (Bot.filter.handleFilter(filter)) "Sucessfully added the filter `$filter`" else "Sucessfully removed the filter `$filter`    "
            JDAUtils.sendMessage(message.channel.sendMessage(output))
        }

        super.run(arguments, message, admin, origin, channel)
    }

}