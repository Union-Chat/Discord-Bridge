package blog.pprogs.unionDiscordBridge

import blog.pprogs.ktunion.UnionClient
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.io.File

fun main(args: Array<String>) {
    val creds = File("creds.txt").readLines()
    val client = UnionClient(username = creds[0], password = creds[1], selfbot = true, bot = false)
    val jda = JDABuilder(AccountType.BOT)
            .setToken(creds[2])
            .addEventListener(Listener(client))
            .buildAsync()

    client.onTextMessage = { who, content, _ ->
        var message = content
        if (message.length > 1500) {
            message = message.substring(0, 1500)
        }

        jda.getTextChannelById(creds[3]).sendMessage("```yaml\n${who.replace("`", "`\u200B")}: ${message.replace("`", "`\u200B")}```").queue()
    }

    client.onStartClosed = { _, _ ->
        Thread.sleep(1000 * 10)
        System.exit(0)
    }

    client.start()
}

class Listener(private val client: UnionClient) : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent?) {
        if (event == null) return

        if (event.author.isBot) return

        if (event.author.idLong == 429038614320513024) return

        if (event.channel.idLong != 429044006392037376) return

        var message = event.message.contentRaw

        if (message.length > 801) {
            message = message.substring(0, 801)
        }

        client.sendMessage("<${event.author.name}> $message")
        event.message.delete().queue()
    }
}
