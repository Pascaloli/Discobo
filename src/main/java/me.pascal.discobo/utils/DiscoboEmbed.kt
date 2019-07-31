package me.pascal.discobo.utils

import me.pascal.discobo.Bot
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed

class DiscoboEmbed() {

    private var title = ""
    private var description = ""
    private var footer = ""
    private var fields = arrayListOf<MessageEmbed.Field>()

    fun setTitle(title: String) {
        this.title = title
    }

    fun setDescription(description: String) {
        this.description = description
    }

    fun setFooter(footer: String) {
        this.footer = footer
    }

    fun addField(name: String, value: String, inline: Boolean) {
        this.fields.add(MessageEmbed.Field(name, value, inline))
    }

    fun build(): Message {
        if (Bot.config.mode == 1) {
            //Client
            val message = StringBuilder()
            if (title.isNotEmpty()) message.append("**$title**\n")
            if (description.isNotEmpty()) message.append("$description\n")
            if (fields.isNotEmpty()) {
                message.append("\n")
                fields.forEach {
                    message.append("**${it.name}**\n")
                    message.append("${it.value}\n\n")
                }
            }
            if (footer.isNotEmpty()) message.append("*$footer*")

            return MessageBuilder(message.toString()).build()
        } else {
            //Bot
            val embed = EmbedBuilder()
            if (this.title.isNotEmpty()) embed.setTitle(this.title)
            if (this.description.isNotEmpty()) embed.setDescription(this.description)
            if (this.footer.isNotEmpty()) embed.setFooter(this.footer, null)
            if (fields.isNotEmpty()) fields.forEach { embed.addField(it) }

            return MessageBuilder(embed.build()).build()
        }
    }


}