import me.pascal.discobo.Bot

fun main(args: Array<String>) {
    val session = if (args.isNotEmpty() && args[0].startsWith("session=")) {
        args[0].substring(8)
    } else {
        println("Invalid launch arguments, please specify which config to use by adding \"session=foobar\" to your launch arguments")
        return
    }
    Bot.start(session)
}
