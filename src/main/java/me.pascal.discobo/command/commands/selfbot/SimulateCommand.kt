package me.pascal.discobo.command.commands.selfbot

import me.pascal.discobo.command.Command
import me.pascal.discobo.utils.JDAUtils
import net.dv8tion.jda.core.entities.Message

class SimulateCommand : Command("simulate", true) {

    override fun run(arguments: Array<String>, message: Message, admin: Boolean, origin: Long, channel: Long) {
        message.delete().queue()
        JDAUtils.simulate(arguments[0].toLong(), arguments[1].toLong())
    }
}