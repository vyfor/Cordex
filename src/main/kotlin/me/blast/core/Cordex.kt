@file:Suppress("unused")

package me.blast.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.future.await
import me.blast.parser.exception.ArgumentException
import me.blast.utils.command.CommandUtils
import me.blast.utils.command.suggestions.DistanceAccuracy
import me.blast.utils.cooldown.Cooldown
import me.blast.utils.cooldown.CooldownManager
import me.blast.utils.cooldown.CooldownType
import me.blast.utils.event.CommandEvent
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.slf4j.LoggerFactory

object Cordex {
  const val VERSION = "0.3.2"
  val logger = LoggerFactory.getLogger(Cordex::class.java)!!
  val scope = CoroutineScope(Dispatchers.Default)
}

class CordexBuilder(token: String) {
  internal val config = CordexConfiguration()
  internal val cooldownManager = CooldownManager()
  val api = DiscordApiBuilder().setToken(token)!!
  val handler = CordexCommands()
  
  /**
   * Set a function for determining the command prefix.
   *
   * @param lazy The lazy evaluation function that returns the command prefix.
   */
  fun prefix(lazy: (Long) -> String) {
    config.prefix = lazy
  }
  
  /**
   * Enable sending command suggestions when a command the user provided is not found.
   *
   * @property accuracy Level of strictness for matching user input to commands.
   */
  fun enableCommandSuggestion(accuracy: DistanceAccuracy = DistanceAccuracy.STRICT) {
    config.enableCommandSuggestions = true
    config.commandSuggestionAccuracy = accuracy
  }
  
  /**
   * Disable command suggestions.
   */
  fun disableCommandSuggestion() {
    config.enableCommandSuggestions = false
  }
  
  /**
   * Configure the [DiscordApiBuilder].
   *
   * @param block The configuration block for [DiscordApiBuilder].
   */
  fun api(block: DiscordApiBuilder.() -> Unit) = block(api)
  
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
  fun onError(block: (CommandEvent, Exception) -> Unit) {
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
  fun onParseError(block: (CommandEvent, ArgumentException) -> Unit) {
    config.parsingErrorHandler = block
  }
  
  /**
   * Set a handler for when a user hits the cooldown.
   *
   * @param block The block of code to execute when a user hits the cooldown.
   */
  fun onCooldown(block: (CommandEvent, Cooldown, CooldownType) -> Unit) {
    config.cooldownHandler = block
  }
  
  /**
   * Adds an interceptor that runs before given command's execution.
   *
   * @param command Command to intercept.
   * @param block The block of code to run.
   * Return true to continue the execution of the command, false otherwise.
   */
  fun intercept(command: String, block: (CommandEvent) -> Boolean) {
    config.interceptors[command.lowercase()] = block
  }
  
  /**
   * Removes the interceptor associated with the given command.
   *
   * @param command Command to remove from interceptors.
   */
  fun removeInterceptor(command: String) {
    config.interceptors.remove(command.lowercase())
  }
}

/**
 * Initialize and configure Cordex.
 *
 * @param token The Discord bot token.
 * @param block The configuration block for [CordexBuilder].
 * @return A [Flow] emitting [DiscordApi] instances for each shard.
 */
inline fun cordex(
  token: String,
  crossinline block: (CordexBuilder.() -> Unit),
): Flow<DiscordApi> = flow {
  CordexBuilder(token).run {
    block()
    api.loginAllShards().map {
      emit(it.await().apply {
        addListener(CordexListener(this@run))
        CommandUtils.generateSlashCommands(handler.getSlashCommands().values).let { (globalCommands, serverCommands) ->
          bulkOverwriteGlobalApplicationCommands(globalCommands)
          serverCommands.forEach { (guildId, slashCommands) ->
            bulkOverwriteServerApplicationCommands(guildId, slashCommands)
          }
        }
      })
    }
  }
}