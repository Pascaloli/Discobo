package me.pascal.discobo

import me.pascal.discobo.command.CommandHandler
import me.pascal.discobo.event.EventHandler
import me.pascal.discobo.utils.ConfigHandler
import me.pascal.discobo.utils.DatabaseHandler
import me.pascal.discobo.utils.FilterHandler
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import javax.security.auth.login.LoginException


object Bot {

    lateinit var jda: JDA
    lateinit var session: String
    lateinit var config: ConfigHandler
    lateinit var database: DatabaseHandler
    lateinit var command: CommandHandler
    lateinit var filter: FilterHandler
    var debug = true
    var startTime: Long = 0

    fun start(session: String) {
        this.startTime = System.currentTimeMillis()
        this.session = session
        this.config = ConfigHandler(session)
        this.database = DatabaseHandler(config)
        this.command = CommandHandler()
        if (config.mode == 2)
            this.filter = FilterHandler(database)
        //Loop until jda is initialised
        while (true) {
            try {
                val accountType = if (config.mode == 1) AccountType.CLIENT else AccountType.BOT
                this.jda = JDABuilder(accountType)
                        .setToken(config.token)
                        .setAutoReconnect(true)
                        .addEventListener(EventHandler(this.config, this.database)).build().awaitReady()

                break
            } catch (e: LoginException) {
                println("Login failed, please re-enter your token")
                this.config.token = ""
                this.config.updateConfig()
            } catch (e: Exception) {
                e.printStackTrace()
                println("retrying")
            }
        }
    }
}
