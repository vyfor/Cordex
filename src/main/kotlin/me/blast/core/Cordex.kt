package me.blast.core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.blast.command.Context
import me.blast.parser.ArgumentsParser
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.event.message.MessageCreateEvent
import org.slf4j.LoggerFactory

class Cordex(val prefix: (Long) -> String, private val handler: CordexCommands) {
  fun executeCommand(event: MessageCreateEvent, prefix: String) {
    val args = event.messageContent.substring(prefix.length).split(Regex("\\s+"))

    handler.getCommands()[args[0].lowercase()]?.apply {
      this.event = event
      ArgumentsParser.parse(args.drop(1), options)
      
      scope.launch {
        execute(
          Context(
            event,
            event.server.get(),
            event.channel,
            event.messageAuthor.asUser().get(),
            event.message,
            prefix,
            this@apply.args.toTypedArray()
          )
        )
      }
    }
  }
  
  companion object {
    const val VERSION = "0.1"
    
    val logger = LoggerFactory.getLogger(Cordex::class.java)
    val scope = CoroutineScope(Dispatchers.Default)
  }
}

class CordexBuilder(token: String) {
  var prefix: (Long) -> String = { "!" }
  val handler = CordexCommands()
  val api: DiscordApiBuilder = DiscordApiBuilder().setToken(token)
  
  fun prefix(lazy: (Long) -> String) { prefix = lazy }
  
  fun config(block: DiscordApiBuilder.() -> Unit): DiscordApiBuilder = api.apply(block)
  
  fun commands(block: CordexCommands.() -> Unit) = handler.apply(block)
}

suspend inline fun cordex(
  token: String,
  crossinline block: (CordexBuilder.() -> Unit)
): Flow<DiscordApi> = flow {
  CordexBuilder(token).run {
    block()
    api.loginAllShards().map {
      emit(it.join().apply {
        addListener(CordexListener(Cordex(prefix, handler)))
      })
    }
  }
}