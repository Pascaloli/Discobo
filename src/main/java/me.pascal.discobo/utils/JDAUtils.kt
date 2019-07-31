package me.pascal.discobo.utils

import me.pascal.discobo.Bot
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.requests.restaction.MessageAction
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.concurrent.TimeUnit

object JDAUtils {

    val adminCache = hashMapOf<String, Boolean>()
    val blacklistCache = hashMapOf<String, Boolean>()

    var simulatedChannel: Long = 0
    var simulatedOrigin: Long = 0

    fun sendMessage(action: MessageAction, delete: Boolean = false, delay: Long = 10, unit: TimeUnit = TimeUnit.SECONDS) {
        action.queue {
            if (delete)
                it.delete().queueAfter(delay, unit)
            else if (Bot.config.mode == 1)
                it.delete().queueAfter(5, TimeUnit.MINUTES)
        }
    }

    fun isAdmin(userId: String): Boolean {
        return if (adminCache.containsKey(userId)) adminCache.get(userId)!! else {
            val query = "SELECT admin FROM admins WHERE userid = $userId"
            val set = Bot.database.connection.createStatement().executeQuery(query)
            if (set.next() && set.getBoolean(1)) {
                adminCache[userId] = true
                true
            } else {
                adminCache[userId] = false
                false
            }
        }
    }

    fun isBlacklisted(userId: String): Boolean {
        return if (blacklistCache.containsKey(userId)) blacklistCache.get(userId)!! else {
            val query = "SELECT blacklisted FROM blacklist WHERE userid = $userId"
            val set = Bot.database.connection.createStatement().executeQuery(query)
            if (set.next() && set.getBoolean(1)) {
                blacklistCache[userId] = true
                true
            } else {
                blacklistCache[userId] = false
                false
            }
        }
    }

    fun copyAttachments(messageId: String, attachmentJson: String, origin: String): String {
        val tokener = JSONTokener(attachmentJson)
        println(attachmentJson)
        val jsonObj = JSONObject(tokener)
        val path = File("${Bot.config.attachmentsPath}$messageId")
        path.mkdirs()
        println(path)
        for (s in jsonObj.keys()) {
            val file = File("${Bot.config.sessionPath}/attachments/$origin/$s")
            file.copyTo(File("$path/$s"))
        }
        return "${Bot.config.attachmentsDomain}$messageId"
    }

    fun isSimulated(): Boolean {
        return simulatedChannel != 0L || simulatedOrigin != 0L
    }

    fun simulate(origin: Long, channel: Long) {
        simulatedOrigin = origin
        simulatedChannel = channel
    }

    fun resetSimulation() {
        simulatedOrigin = 0L
        simulatedChannel = 0L
    }

    fun getChannelId(message: Message): Long {
        return if (simulatedChannel != 0L)
            simulatedChannel
        else
            message.channel.idLong
    }

    fun getOriginId(message: Message): Long {
        return if (simulatedOrigin != 0L)
            simulatedOrigin
        else
            when (message.channelType) {
                ChannelType.TEXT -> message.guild.idLong
                ChannelType.GROUP -> message.group.idLong
                ChannelType.PRIVATE -> message.privateChannel.idLong
                else -> 0
            }
    }

    fun getOriginIdOriginal(message: Message): String {
        return when (message.channelType) {
            ChannelType.TEXT -> message.guild.id
            ChannelType.GROUP -> message.group.id
            ChannelType.PRIVATE -> message.privateChannel.id
            else -> "invalid"
        }
    }

    fun saveProfileImage(userId: String, avatarId: String, avatarUrl: String) {
        val file = File("${Bot.config.sessionPath}/profileimages/${userId}/${avatarId}")
        file.parentFile.mkdirs()
        val urlConnection = URL(avatarUrl).openConnection()
        urlConnection.addRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0"
        )
        Files.copy(urlConnection.getInputStream(), Paths.get(file.path), StandardCopyOption.REPLACE_EXISTING)
    }

    fun saveAttachments(message: Message): Array<String> {
        val list = ArrayList<String>()
        message.attachments.forEach {
            val file = getAttachmentFile(message, it)
            it.download(file)
            list.add(file.name)
        }
        return list.toTypedArray()
    }

    private fun getAttachmentFile(message: Message, attachment: Message.Attachment): File {
        val baseFile = File("${Bot.config.sessionPath}/attachments/${getOriginId(message)}/${attachment.fileName}")
        baseFile.parentFile.mkdirs()
        return { number: Int -> File(baseFile.parent, baseFile.nameWithoutExtension + number + "." + baseFile.extension) }
                .let { f -> f(generateSequence(0) { it + 1 }.dropWhile { f(it).exists() }.first()) }
    }

    fun unEscapeString(s: String): String {
        val sb = StringBuilder()
        for (i in 0 until s.length)
            when (s[i]) {
                '\n' -> sb.append("\\n")
                '\t' -> sb.append("\\t")
                // ... rest of escape characters
                else -> sb.append(s[i])
            }
        return sb.toString()
    }

}