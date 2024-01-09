@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package me.blast.utils.pagination

import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.MessageBuilder
import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.entity.message.MessageUpdater
import org.javacord.api.entity.message.component.ActionRow
import org.javacord.api.entity.message.component.Button
import org.javacord.api.entity.message.component.ButtonBuilder
import org.javacord.api.entity.message.component.SelectMenuBuilder
import org.javacord.api.event.interaction.ButtonClickEvent
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.interaction.ButtonClickListener
import org.javacord.api.util.event.ListenerManager
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.time.Duration

class Paginator<T>(
  val items: List<T>,
  private val itemsPerPage: Int,
  private val canClose: Boolean = false
) {
  var currentPageIndex = 0
    private set
  val totalPages = (items.size + itemsPerPage - 1) / itemsPerPage
  internal lateinit var onStart: (messageEvent: MessageCreateEvent, paginator: Paginator<T>, currentItems: List<T>) -> MessageBuilder
  internal lateinit var onPagination: (message: Message, paginator: Paginator<T>, currentItems: List<T>) -> MessageUpdater
  internal lateinit var onEmpty: (messageEvent: MessageCreateEvent) -> MessageBuilder
  
  fun hasPage(index: Int) = index in 1..totalPages
  fun hasNextPage() = currentPageIndex < totalPages - 1
  fun hasPreviousPage() = currentPageIndex > 0
  
  fun itemsOnCurrentPage(): List<T> {
    val startIndex = currentPageIndex * itemsPerPage
    val endIndex = min(startIndex + itemsPerPage, items.size)
    return items.subList(startIndex, endIndex)
  }
  
  fun moveToPage(page: Int) {
    if(hasPage(page)) currentPageIndex = page - 1
  }
  
  fun nextPage() {
    if (hasNextPage()) currentPageIndex++
  }
  
  fun previousPage() {
    if (hasPreviousPage()) currentPageIndex--
  }
  
  fun startPagination(
    customId: String,
    messageEvent: MessageCreateEvent,
    channel: TextChannel,
    removeAfter: Duration
  ) {
    if (items.isEmpty()) {
      onEmpty(messageEvent).send(channel)
      return
    }
    if (removeAfter.isNegative()) throw IllegalArgumentException("Duration cannot be negative.")
    addButtons(customId, onStart(messageEvent, this, itemsOnCurrentPage())).send(channel).thenAcceptAsync { message ->
      var listener: ListenerManager<ButtonClickListener>? = null
      listener = message.addButtonClickListener { buttonEvent ->
        handle(
          customId,
          messageEvent,
          buttonEvent,
          message,
          listener
        )
      }
      listener.addRemoveHandler { handleRemove(message) }
      if (removeAfter.isFinite()) listener.removeAfter(removeAfter.inWholeMilliseconds, TimeUnit.MILLISECONDS)
    }
  }
  
  private fun handle(customId: String, messageEvent: MessageCreateEvent, buttonEvent: ButtonClickEvent, message: Message, listener: ListenerManager<ButtonClickListener>?) {
    if (buttonEvent.buttonInteraction.user.id != messageEvent.messageAuthor.id) {
      buttonEvent.buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent("This is not your interaction!").respond()
      return
    }
    when (buttonEvent.buttonInteraction.customId) {
      customId + "FIRST" -> {
        currentPageIndex = 0
        handlePagination(customId, onPagination(message, this, itemsOnCurrentPage()), buttonEvent)
      }
      customId + "PREV" -> {
        currentPageIndex--
        handlePagination(customId, onPagination(message, this, itemsOnCurrentPage()), buttonEvent)
      }
      customId + "NEXT" -> {
        currentPageIndex++
        handlePagination(customId, onPagination(message, this, itemsOnCurrentPage()), buttonEvent)
      }
      customId + "LAST" -> {
        currentPageIndex = totalPages - 1
        handlePagination(customId, onPagination(message, this, itemsOnCurrentPage()), buttonEvent)
      }
      customId + "CLOSE" -> {
        listener?.remove()
        handleRemove(message)
        buttonEvent.buttonInteraction.acknowledge()
      }
    }
  }
  
  private fun handlePagination(
    customId: String,
    updater: MessageUpdater,
    buttonEvent: ButtonClickEvent
  ) {
    addButtons(customId, updater.removeAllComponents())
    updater.applyChanges().thenRunAsync { buttonEvent.buttonInteraction.acknowledge() }
  }
  
  private fun handleRemove(message: Message) {
    val components = message.components
    val updater = message.createUpdater().removeAllComponents()
    components.forEach {
      it.asActionRow().ifPresent { actionRow ->
        updater.addComponents(
          ActionRow.of(
            actionRow.components.map { component ->
              if(component.isButton) ButtonBuilder().copy(component.asButton().get()).setDisabled(true).build()
              else component.asSelectMenu().get().apply { SelectMenuBuilder(type, customId).copy(this).setDisabled(true).build() }
            }
          )
        )
      }
    }
    updater.applyChanges()
  }
  
  private fun addButtons(customId: String, messageBuilder: MessageBuilder): MessageBuilder {
    return messageBuilder.addComponents(ActionRow.of(getButtons(customId)))
  }
  
  private fun addButtons(customId: String, messageUpdater: MessageUpdater): MessageUpdater {
    return messageUpdater.addComponents(ActionRow.of(getButtons(customId)))
  }
  
  private fun getButtons(customId: String): List<Button> = buildList {
    if (totalPages > 3) add(Button.primary(customId + "FIRST", "«", currentPageIndex == 0))
    add(Button.primary(customId + "PREV", "←", currentPageIndex == 0))
    if (canClose) add(Button.danger(customId + "CLOSE", "\uD83D\uDDD1"))
    add(Button.primary(customId + "NEXT", "→", currentPageIndex == totalPages - 1))
    if (totalPages > 3) add(Button.primary(customId + "LAST", "»", currentPageIndex == totalPages - 1))
  }
}
