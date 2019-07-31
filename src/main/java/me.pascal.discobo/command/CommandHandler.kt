package me.pascal.discobo.command

import me.pascal.discobo.Bot
import me.pascal.discobo.command.commands.admin.AdminCommand
import me.pascal.discobo.command.commands.admin.BlacklistCommand
import me.pascal.discobo.command.commands.query.*
import me.pascal.discobo.command.commands.selfbot.EmotestealCommand
import me.pascal.discobo.command.commands.selfbot.SimulateCommand
import me.pascal.discobo.utils.JDAUtils
import net.dv8tion.jda.core.entities.Message

class CommandHandler {

    val commands = arrayListOf<Command>()

    init {
        //general commands
        commands.addAll(arrayListOf(
                DeleteCommand(),
                SnipeCommand(),
                HistoryCommand(),
                AdminCommand(),
                BlacklistCommand(),
                OnlineHistoryCommand(),
                InfoCommand()
        ))

        //Mode specific commands
        when (Bot.config.mode) {
            //Client only commands
            1 -> commands.addAll(arrayListOf(
                    SimulateCommand(),
                    EmotestealCommand()
            ))
            //Bot only commands
            2 -> commands.addAll(arrayListOf(
                    //gibts noch nix lolo
            ))
        }
    }

    private fun getCommandByName(name: String): Command? {
        return commands.find { it.name == name }
    }

    // @return If the command was executed successfully
    fun handleInput(message: Message, admin: Boolean): Boolean {

        //Check if message starts with the prefix and if the author is valid
        if (!message.contentRaw.startsWith(Bot.config.prefix)
                || !((Bot.config.mode == 1 && message.author == Bot.jda.selfUser)
                        || Bot.config.mode == 2)) {
            return false
        }

        val input = message.contentRaw
        val commandName = input.split(" ")[0].substring(1)

        //Check if the author is blacklisted
        if (Bot.config.mode == 2 && JDAUtils.isBlacklisted(message.author.id))
            return false

        //Get command
        val command = getCommandByName(commandName)
        if (command != null) {
            //Check if the author can use the command
            if (Bot.config.mode == 2 && admin < command.admin) {
                message.delete().queue()
                JDAUtils.sendMessage(message.channel.sendMessage("You can't use this command."), true)
                return true
            }

            //Parse the message into Arguments
            val arguments = if (input.length < commandName.length + 2) "" else input.substring(commandName.length + 2)
            val argumentsArr = if (arguments.isEmpty()) arrayOf() else arguments.split(" ").toTypedArray()
            //Run the command with given arguments
            val origin = JDAUtils.getOriginId(message)
            val channel = JDAUtils.getChannelId(message)
            command.run(argumentsArr, message, admin, origin, channel)

            if (command !is SimulateCommand && JDAUtils.isSimulated())
                JDAUtils.resetSimulation()
            return true
        }
        return false
    }
}