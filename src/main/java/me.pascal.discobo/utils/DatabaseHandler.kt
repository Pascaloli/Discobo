package me.pascal.discobo.utils

import me.pascal.discobo.Bot
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.MessageDeleteEvent
import net.dv8tion.jda.core.events.user.update.UserUpdateOnlineStatusEvent
import org.json.JSONObject
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet


class DatabaseHandler(config: ConfigHandler) {

    val connection: Connection
    val ignoreDelete: ArrayList<String>

    init {
        Class.forName("com.mysql.cj.jdbc.Driver")
        connection =
                DriverManager.getConnection("${config.jdbcUrl}&autoReconnect=true&allowMultiQueries=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC")
        ignoreDelete = ArrayList<String>()
        init()
    }

    private fun init() {
        val set = connection.createStatement().executeQuery("SHOW TABLES")
        val tables = set.use { generateSequence { it.takeIf(ResultSet::next)?.getString(1) }.toList() }

        var statement = ""
        var initNames = false
        var initProfileimages = false

        if (!tables.contains("messages")) {
            statement += "create table messages\n" +
                    "(\n" +
                    "    messageid   varchar(18)    not null\n" +
                    "        primary key,\n" +
                    "    originid    varchar(18)    not null,\n" +
                    "    channelid   varchar(18)    not null,\n" +
                    "    authorid    varchar(18)    not null,\n" +
                    "    content     varchar(3000)  not null,\n" +
                    "    attachments varchar(10000) not null,\n" +
                    "    deleted     tinyint(1)     not null,\n" +
                    "    deletestamp varchar(18)    not null,\n" +
                    "    timestamp   varchar(18)    not null\n" +
                    ");\n" +
                    "create index messages_idx_delete_origin_channe_author_conten\n" +
                    "    on messages (deleted, originid, channelid, authorid, content);\n" +
                    "create index messages_idx_deletestamp\n" +
                    "    on messages (deletestamp);\n"
        }
        if (!tables.contains("edits")) {
            statement += "create table edits\n" +
                    "(\n" +
                    "    messageid varchar(18)   not null,\n" +
                    "    content   varchar(3000) not null,\n" +
                    "    timestamp varchar(18)   not null\n" +
                    ");\n" +
                    "create index edits_idx_messageid\n" +
                    "    on edits (messageid);\n"
        }
        if (!tables.contains("names")) {
            statement += "create table names\n" +
                    "(\n" +
                    "    userid        varchar(18) null,\n" +
                    "    name          varchar(32) null,\n" +
                    "    discriminator varchar(4)  null,\n" +
                    "    timestamp     varchar(18) null\n" +
                    ");\n"
            initNames = true
        }
        if (!tables.contains("statuses")) {
            statement += "create table statuses\n" +
                    "(\n" +
                    "    userid    varchar(18) null,\n" +
                    "    status    varchar(18) null,\n" +
                    "    type      varchar(18) null,\n" +
                    "    timestamp varchar(18) null\n" +
                    ");\n" +
                    "create index statuses_idx_timestamp\n" +
                    "    on statuses (timestamp);\n" +
                    "create index statuses_idx_userid_timestamp\n" +
                    "    on statuses (userid, timestamp);\n"
        }
        if (!tables.contains("admins")) {
            statement += "create table admins\n" +
                    "(\n" +
                    "    userid varchar(18) null,\n" +
                    "    admin  tinyint(1)  null\n" +
                    ");\n"
        }
        if (!tables.contains("blacklist")) {
            statement += "create table blacklist\n" +
                    "(\n" +
                    "    userid      varchar(18) null,\n" +
                    "    blacklisted tinyint(1)  null\n" +
                    ");\n"
        }
        if (!tables.contains("filters")) {
            statement += "create table filters\n" +
                    "(\n" +
                    "    censor varchar(2000) null\n" +
                    ");\n"
        }
        if (!tables.contains("profileimages")) {
            statement += "create table profileimages\n" +
                    "(\n" +
                    "    userid    varchar(18) null,\n" +
                    "    avatarid  varchar(18) null,\n" +
                    "    timestamp varchar(18) null\n" +
                    ");\n"
            initProfileimages = true
        }
        if (statement.isNotEmpty())
            connection.createStatement().executeUpdate(statement)

        if (initNames || initProfileimages) {
            val done = arrayListOf<String>()
            for (g in Bot.jda.guilds) {
                for (m in g.members) {
                    if (!done.contains(m.user.id)) {
                        if (initNames) {
                            val query = "INSERT INTO names (userid, name, discriminator, timestamp) VALUES(?, ?, ?, ?);"
                            Bot.database.connection.prepareStatement(query).use {
                                it.setString(1, m.user.id)
                                it.setString(2, m.user.name)
                                it.setString(3, m.user.discriminator)
                                it.setLong(4, System.currentTimeMillis())
                                it.executeUpdate()
                            }
                            println("init name ${m.user.id}")
                        }
                        if (initProfileimages) {
                            val avatarId = if (m.user.avatarId == null) "null" else m.user.avatarId
                            JDAUtils.saveProfileImage(m.user.id, avatarId, m.user.effectiveAvatarUrl)
                            val query = "INSERT INTO profileimages (userid, avatarid, timestamp) VALUES(?, ?, ?);"
                            Bot.database.connection.prepareStatement(query).use {
                                it.setString(1, m.user.id)
                                it.setString(2, avatarId)
                                it.setLong(3, System.currentTimeMillis())
                                it.executeUpdate()
                            }
                            println("init pfp ${m.user.id}")
                        }
                        done.add(m.user.id)
                    }
                }
            }
        }
    }

    //Log message into table `messages`
    fun logMessage(message: Message) {
        val messageId = message.id
        val originId = JDAUtils.getOriginIdOriginal(message)
        val channelId = message.channel.id
        val authorId = message.author.id
        val content = message.contentRaw
        val timestamp = message.creationTime.toInstant().toEpochMilli()

        val jsonObj = JSONObject()
        if (Bot.config.logAttachments == "y")
            JDAUtils.saveAttachments(message).forEach { jsonObj.put(it, "") }

        val attachments = if (jsonObj.isEmpty) "" else jsonObj.toString(2)

        val query =
                "INSERT INTO messages (messageid, originid, channelid , authorid, content, attachments, deleted, deletestamp, timestamp) VALUES(?,?,?,?,?,?,?,?,?);"
        connection.prepareStatement(query).use {
            it.setString(1, messageId)
            it.setString(2, originId)
            it.setString(3, channelId)
            it.setString(4, authorId)
            it.setString(5, content)
            it.setString(6, attachments)
            it.setBoolean(7, false)
            it.setLong(8, 0)
            it.setLong(9, timestamp)
            it.executeUpdate()
        }
    }

    fun logEdit(message: Message) {
        val messageId = message.id
        val content = message.contentRaw
        val timestamp = message.editedTime!!.toInstant().toEpochMilli()

        val query =
                "INSERT into edits(messageid, content, timestamp) VALUES (?,?,?)"
        connection.prepareStatement(query).use {
            it.setString(1, messageId)
            it.setString(2, content)
            it.setLong(3, timestamp)
            it.executeUpdate()
        }
    }

    fun logDelete(event: MessageDeleteEvent) {
        val messageId = event.messageId
        val timestamp = System.currentTimeMillis()

        val query = "UPDATE messages SET deleted = true, deletestamp = ? WHERE messageid = ?;"
        connection.prepareStatement(query).use {
            it.setLong(1, timestamp)
            it.setString(2, messageId)
            it.executeUpdate()
        }
    }

    val buffer: HashMap<Long, String> = HashMap()
    fun logStatus(event: UserUpdateOnlineStatusEvent) {
        val userId = event.user.idLong
        val status = event.newOnlineStatus.name
        //val device = status.
        if (buffer.containsKey(userId) && buffer.get(userId).equals(status)) return else {
            buffer.put(userId, status)
        }

        val query = "INSERT INTO statuses (userid, status, timestamp) VALUES(?, ?, ?);"
        Bot.database.connection.prepareStatement(query).use {
            it.setString(1, userId.toString())
            it.setString(2, status)
            it.setLong(3, System.currentTimeMillis())
            it.executeUpdate()
        }
    }

    fun logName(userId: String, name: String, discriminator: String) {
        val query = "INSERT INTO names (userid, name, discriminator, timestamp) VALUES(?, ?, ?, ?);"
        Bot.database.connection.prepareStatement(query).use {
            it.setString(1, userId)
            it.setString(2, name)
            it.setString(3, discriminator)
            it.setLong(4, System.currentTimeMillis())
            it.executeUpdate()
        }
    }

    fun logProfileimage(userId: String, avatarId: String, avatarUrl: String) {
        JDAUtils.saveProfileImage(userId, avatarId, avatarUrl)
        val query = "INSERT INTO profileimages (userid, avatarid, timestamp) VALUES(?, ?, ?);"
        Bot.database.connection.prepareStatement(query).use {
            it.setString(1, userId)
            it.setString(2, avatarId)
            it.setLong(3, System.currentTimeMillis())
            it.executeUpdate()
        }
    }
}