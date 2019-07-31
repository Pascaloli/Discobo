package me.pascal.discobo.command

import net.dv8tion.jda.core.entities.Message

open class Command(open var name: String, open var admin: Boolean = false) {


    open fun run(arguments: Array<String>, message: Message, admin: Boolean, origin: Long, channel: Long) {

    }

}