package blog.pprogs.unionDiscordBridge

import blog.pprogs.ktunion.UnionClient
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.dv8tion.jda.webhook.WebhookClientBuilder
import net.dv8tion.jda.webhook.WebhookMessageBuilder
import java.io.File

fun String.clean(jda: JDA): String {
    var content = this

    content = Regex("<@!?(\\d{17,20})>").replace(content) {
        val user = jda.getUserById(it.groups[1]!!.value)
        if (user != null) "@${user.name}" else "@mention"
    }

    return content
            .replace("@everyone", "@\u200beveryone")
            .replace("@here", "@\u200bhere")
}

fun Message.displayForUnion(): String {
    var contentRaw = contentRaw
    contentRaw = Regex("<@!?(\\d{17,20})>").replace(contentRaw) {
        val foundUser = jda.getUserById(it.groups[1]!!.value)
        if (foundUser != null) "{${foundUser.name}}" else "{mention}"
    }
    emotes.forEach { emote ->
        contentRaw = contentRaw.replace(emote.asMention, ":" + emote.name + ":")
    }
    mentionedChannels.forEach { mentionedChannel ->
        contentRaw = contentRaw.replace(mentionedChannel.asMention, '#' + mentionedChannel.name)
    }
    mentionedRoles.forEach { mentionedRole ->
        contentRaw = contentRaw.replace(mentionedRole.asMention, '@' + mentionedRole.name)
    }

    return contentRaw + attachments.filter { it.isImage }.joinToString(" ", " ") { it.url }
}

fun main(args: Array<String>) {
    val creds = File("creds.txt").readLines()
    val client = UnionClient(username = creds[0], password = creds[1], selfbot = true, bot = false)

    client.start()

    val jda = JDABuilder(AccountType.BOT)
            .setToken(creds[2])
            .addEventListener(Listener(client))
            .buildAsync()

    val whClient = WebhookClientBuilder(creds[3]).build()

    client.onTextMessage = { who, content, _ ->
        if (who.id != creds[0]) {
            whClient.send(WebhookMessageBuilder()
                    .setAvatarUrl(who.avatarUrl)
                    .setContent(content.clean(jda).take(1500))
                    .setUsername(who.id)
                    .build())
        }
    }

    client.onStartClosed = { _, _ ->
        Thread.sleep(1000 * 10)
        System.exit(0)
    }
}

class Listener(private val client: UnionClient) : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent?) {
        if (event == null) return

        if (event.author.isBot) return

        if (event.author.idLong == 429038614320513024) return

        if (event.channel.idLong != 429044006392037376) return

        client.sendMessage("<${event.author.name}> ${event.message.displayForUnion().take(900)}")
    }
}
