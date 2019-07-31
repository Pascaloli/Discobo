package me.pascal.discobo.utils

import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File

class ConfigHandler(session: String) {

    val homePath = System.getProperty("user.home")
    val rootPath = "$homePath/discobo"
    val sessionPath = "$rootPath/configs/$session"
    var mode = 0 //1 = Client, 2 = Bot
    var token = ""
    var prefix = ""
    var jdbcUrl = ""
    var logAttachments = ""
    var attachmentsPath = ""
    var attachmentsDomain = ""

    init {
        println(sessionPath)
        readConfig()
    }

    private fun readConfig() {

        val file = File("$sessionPath/config.json")
        if (!file.exists()) {
            file.getParentFile().mkdirs()
            file.createNewFile()
            updateConfig()
        }



        while (true) {
            try {
                val inputStream = File("$sessionPath/config.json").inputStream()
                val tokener = JSONTokener(inputStream)
                val jsonobj = JSONObject(tokener)

                mode = jsonobj.getInt("mode")
                token = jsonobj.getString("token")
                prefix = jsonobj.getString("prefix")
                jdbcUrl = jsonobj.getString("jdbcUrl")
                logAttachments = jsonobj.getString("logAttachments")
                attachmentsPath = jsonobj.getString("attachmentsPath")
                attachmentsDomain = jsonobj.getString("attachmentsDomain")
                if (!updateConfig())
                    break
            } catch (e: JSONException) {
                e.printStackTrace()
                println("Error occured while reading the config")
                updateConfig()
            }
        }


    }

    //Called when config file didnt exist or an invalid setting was found
    fun updateConfig(): Boolean {
        val file = File("$sessionPath/config.json")
        var changed = false
        val jsonobj = JSONObject()

        while (mode !in 1..2) {
            changed = true
            println("Modus: (1) Client, (2) Bot")
            val input = readLine()
            if (input.equals("1") || input.equals("2")) {
                mode = input!!.toInt()
                break
            }
            println("Invalid input, please try again")
        }
        jsonobj.put("mode", mode)

        while (token.isEmpty()) {
            changed = true
            println("Please enter your token")
            val input = readLine()
            if (!input.isNullOrBlank()) {
                token = input
                break
            }
            println("Invalid input, please try again")
        }
        jsonobj.put("token", token)

        while (jdbcUrl.isEmpty()) {
            changed = true
            println("Please enter your mysql jdbc url (ex: jdbc:mysql://1.2.3.4:3306/testDb?user=foo&password=bar)")
            val input = readLine()
            if (!input.isNullOrBlank()) {
                jdbcUrl = input
                break
            }
            println("Invalid input, please try again")
        }
        jsonobj.put("jdbcUrl", jdbcUrl)


        while (prefix.isEmpty()) {
            changed = true
            println("Please enter the command prefix you wish to use")
            val input = readLine()
            if (!input.isNullOrBlank() && input.length == 1) {
                prefix = input
                break
            }
            println("Invalid input, please try again")
        }
        jsonobj.put("prefix", prefix)

        while (logAttachments.isEmpty()) {
            changed = true
            println("Do you wish to use Attachment logging y/n (Requires proper nginx setup) ")
            val input = readLine()
            if (!input.isNullOrBlank() && (input.equals("y") || input.equals("n"))) {
                logAttachments = input
                break
            }
            println("Invalid input, please try again")
        }
        jsonobj.put("logAttachments", logAttachments)

        while (logAttachments == "y" && attachmentsPath.isEmpty()) {
            changed = true
            println("Please specify your previously setup nginx path (ex: /var/www/discobo/)")
            val input = readLine()
            if (!input.isNullOrBlank()) {
                attachmentsPath = if (input.endsWith("/")) input else "$input/"
                break
            }
            println("Invalid input, please try again")
        }
        jsonobj.put("attachmentsPath", attachmentsPath)

        while (logAttachments == "y" && attachmentsDomain.isEmpty()) {
            changed = true
            println("Please specify your domain pointing to the nginx path (ex: https://foo.bar/)")
            val input = readLine()
            if (!input.isNullOrBlank()) {
                attachmentsDomain = if (input.endsWith("/")) input else "$input/"
                break
            }
            println("Invalid input, please try again")
        }
        jsonobj.put("attachmentsDomain", attachmentsDomain)

        val jsonString = jsonobj.toString(2)
        if (changed) {
            file.writeText(jsonString)
        }
        return changed
    }
}

