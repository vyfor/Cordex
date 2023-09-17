@file:Suppress("unused", "NAME_SHADOWING")

package me.blast.utils.pagination

import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.MessageBuilder
import org.javacord.api.entity.message.MessageUpdater
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.message.MessageCreateEvent
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

fun <T> defaultOnStart(title: String, messageEvent: MessageCreateEvent, paginator: Paginator<T>, currentItems: List<T>): MessageBuilder {
  return MessageBuilder().setEmbed(
    EmbedBuilder()
      .setTitle(title)
      .setDescription(currentItems.joinToString("\n"))
      .setFooter("Page ${paginator.currentPageIndex + 1}/${paginator.totalPages}")
  ).replyTo(messageEvent.messageId)
}

fun <T> defaultOnPagination(title: String, message: Message, paginator: Paginator<T>, currentItems: List<T>): MessageUpdater {
  return MessageUpdater(message).setEmbed(
    EmbedBuilder()
      .setTitle(title)
      .setDescription(currentItems.joinToString("\n"))
      .setFooter("Page ${paginator.currentPageIndex + 1}/${paginator.totalPages}")
  )
}

fun defaultOnEmpty(messageEvent: MessageCreateEvent): MessageBuilder {
  return MessageBuilder().setEmbed(
    EmbedBuilder()
      .setTitle("No items found")
  ).apply {
    replyTo(messageEvent.messageId)
  }
}

fun <T> List<T>.paginate(
  channel: TextChannel,
  messageEvent: MessageCreateEvent,
  itemsPerPage: Int,
  onStart: (messageEvent: MessageCreateEvent, paginator: Paginator<T>, currentItems: List<T>) -> MessageBuilder,
  onPagination: (message: Message, paginator: Paginator<T>, currentItems: List<T>) -> MessageUpdater,
  onEmpty: (messageEvent: MessageCreateEvent) -> MessageBuilder = { defaultOnEmpty(messageEvent) },
  removeAfter: Duration = 5.minutes,
  canClose: Boolean = false
) {
  val paginator = Paginator(this, itemsPerPage, canClose)
  paginator.onStart = onStart
  paginator.onPagination = onPagination
  paginator.onEmpty = onEmpty
  paginator.startPagination(UUID.randomUUID().toString(), messageEvent, channel, removeAfter)
}

fun <T> List<T>.paginateDefault(
  title: String,
  channel: TextChannel,
  messageEvent: MessageCreateEvent,
  itemsPerPage: Int,
  removeAfter: Duration = 5.minutes,
  canClose: Boolean = false
) {
  val paginator = Paginator(this, itemsPerPage, canClose)
  paginator.onStart = { messageEvent, paginator, currentItems -> defaultOnStart(title, messageEvent, paginator, currentItems) }
  paginator.onPagination = { message, paginator, currentItems -> defaultOnPagination(title, message, paginator, currentItems) }
  paginator.onEmpty = { defaultOnEmpty(messageEvent) }
  paginator.startPagination(UUID.randomUUID().toString(), messageEvent, channel, removeAfter)
}