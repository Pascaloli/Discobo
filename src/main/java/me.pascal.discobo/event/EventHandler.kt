package me.pascal.discobo.event

import me.pascal.discobo.Bot
import me.pascal.discobo.utils.ConfigHandler
import me.pascal.discobo.utils.DatabaseHandler
import me.pascal.discobo.utils.JDAUtils
import net.dv8tion.jda.core.events.message.MessageDeleteEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.MessageUpdateEvent
import net.dv8tion.jda.core.events.user.update.UserUpdateAvatarEvent
import net.dv8tion.jda.core.events.user.update.UserUpdateDiscriminatorEvent
import net.dv8tion.jda.core.events.user.update.UserUpdateNameEvent
import net.dv8tion.jda.core.events.user.update.UserUpdateOnlineStatusEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter

class EventHandler(val config: ConfigHandler, val database: DatabaseHandler) : ListenerAdapter() {

    //Calls when message has been received
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val message = event.message
        val author = event.author

        if (!Bot.debug)
            database.logMessage(message)

        //Check if author has admin permissions
        val admin = Bot.config.mode == 1 || JDAUtils.isAdmin(author.id)

        //check message for filters if author isnt admin
        if (Bot.config.mode == 2 && !admin) {
            Bot.filter.filterList.forEach {
                val regex = it.toRegex()
                if (regex.containsMatchIn(message.contentRaw) || message.contentRaw.contains(it)) {
                    //filter found
                    message.delete().queue()
                    return
                }
            }
        }

        //handle command
        Bot.command.handleInput(message, admin)

        super.onMessageReceived(event)
    }

    //Calls when message has been edited
    override fun onMessageUpdate(event: MessageUpdateEvent) {
        if (!Bot.debug)
            database.logEdit(event.message)
        super.onMessageUpdate(event)
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        if (!Bot.debug)
            database.logDelete(event)
        super.onMessageDelete(event)
    }

    override fun onUserUpdateOnlineStatus(event: UserUpdateOnlineStatusEvent) {
        if (!Bot.debug)
            database.logStatus(event)
        super.onUserUpdateOnlineStatus(event)
    }

    override fun onUserUpdateName(event: UserUpdateNameEvent) {
        if (!Bot.debug)
            database.logName(event.user.id, event.newName, event.user.discriminator)
        super.onUserUpdateName(event)
    }

    override fun onUserUpdateDiscriminator(event: UserUpdateDiscriminatorEvent) {
        if (!Bot.debug)
            database.logName(event.user.id, event.user.name, event.newDiscriminator)
        super.onUserUpdateDiscriminator(event)
    }

    override fun onUserUpdateAvatar(event: UserUpdateAvatarEvent) {
        if (!Bot.debug)
            database.logProfileimage(event.user.id, event.newAvatarId, event.newAvatarUrl)
        super.onUserUpdateAvatar(event)
    }

}