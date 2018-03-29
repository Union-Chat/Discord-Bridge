package blog.pprogs.unionDiscordBridge

import blog.pprogs.ktunion.UnionClient
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import java.io.File

fun main(args: Array<String>) {
    val creds = File("creds.txt").readLines()
    val client = UnionClient(username = creds[0], password = creds[1], selfbot = true, silent = true)
    val jda = JDABuilder(AccountType.BOT)
            .setToken(creds[2])
            .buildAsync()

    client.onTextMessage = { who, content, _ ->
        jda.getTextChannelById(creds[3]).sendMessage("```yaml\n${who.replace("```", "`\u200B`\u200B`\u200B")}: ${content.replace("```", "`\u200B`\u200B`\u200B")}```").queue()
    }
    client.start()
}

