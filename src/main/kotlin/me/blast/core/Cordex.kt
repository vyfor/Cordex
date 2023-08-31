@file:Suppress("unused")

package me.blast.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.future.await
import me.blast.command.Command
import me.blast.parser.exception.ArgumentException
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.event.message.MessageCreateEvent
import org.slf4j.LoggerFactory

object Cordex {
  const val VERSION = "0.2.3"
  val logger = LoggerFactory.getLogger(Cordex::class.java)
  val scope = CoroutineScope(Dispatchers.Default)
}

class CordexBuilder(token: String) {
  val config = CordexConfiguration()
  val handler = CordexCommands()
  val api = DiscordApiBuilder().setToken(token)
  
  /**
   * Set a function for determining the command prefix.
   *
   * @param lazy The lazy evaluation function that returns the command prefix.
   */
  fun prefix(lazy: (Long) -> String) {
    config.prefix = lazy
  }
  
  /**
   * Configure the [DiscordApiBuilder].
   *
   * @param block The configuration block for [DiscordApiBuilder].
   */
  fun config(block: DiscordApiBuilder.() -> Unit) = block(api)
  
  /**
   * Add or remove bot commands.
   *
   * @param block The configuration block for managing bot commands.
   */
  fun commands(block: CordexCommands.() -> Unit) = block(handler)
  
  /**
   * Set an error handler for handling exceptions thrown during execution of commands.
   *
   * @param block The block of code to execute when an exception occurs.
   */
  fun onError(block: (MessageCreateEvent, Command, Exception) -> Unit) {
    config.errorHandler = block
  }
  
  /**
   * Set an error handler for handling exceptions thrown during parsing of arguments.
   *
   * *If not specified, will default to sending the help embed of the command.*
   * @param block The block of code to execute when an argument exception occurs.
   *
   * @see [ArgumentException]
   */
  fun onParseError(block: (MessageCreateEvent, Command, ArgumentException) -> Unit) {
    config.parsingErrorHandler = block
  }
}

/**
 * Initialize and configure Cordex.
 *
 * @param token The Discord bot token.
 * @param block The configuration block for [CordexBuilder].
 * @return A [Flow] emitting [DiscordApi] instances for each shard.
 */
suspend inline fun cordex(
  token: String,
  crossinline block: (CordexBuilder.() -> Unit),
): Flow<DiscordApi> = flow {
  CordexBuilder(token).run {
    block()
    api.setAllIntents().setWaitForUsersOnStartup(true).loginAllShards().map {
      emit(it.await().apply {
        addListener(CordexListener(this@run))
      })
    }
  }
}