package me.blast.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.slf4j.LoggerFactory

object Cordex {
  const val VERSION = "0.1"
  val logger = LoggerFactory.getLogger(Cordex::class.java)
  val scope = CoroutineScope(Dispatchers.Default)
}

class CordexBuilder(token: String) {
  var prefix: (Long) -> String = { "!" }
  val handler = CordexCommands()
  val api: DiscordApiBuilder = DiscordApiBuilder().setToken(token)
  
  /**
   * Set a function for determining the command prefix.
   *
   * @param lazy The lazy evaluation function that returns the command prefix.
   */
  fun prefix(lazy: (Long) -> String) {
    prefix = lazy
  }
  
  /**
   * Configure the [DiscordApiBuilder].
   *
   * @param block The configuration block for [DiscordApiBuilder].
   */
  fun config(block: DiscordApiBuilder.() -> Unit) = api.apply(block)
  
  /**
   * Add or remove bot commands.
   *
   * @param block The configuration block for managing bot commands.
   */
  fun commands(block: CordexCommands.() -> Unit) = handler.apply(block)
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
    api.loginAllShards().map {
      emit(it.join().apply {
        addListener(CordexListener(this@run))
      })
    }
  }
}