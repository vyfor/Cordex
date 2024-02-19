@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package me.blast.core

import me.blast.command.BaseCommand
import me.blast.command.text.TextCommand
import me.blast.command.slash.SlashCommand
import me.blast.utils.Utils
import kotlin.system.measureTimeMillis

class CordexCommands {
  private val textCommands = mutableMapOf<String, TextCommand>()
  private val slashCommands = mutableMapOf<String, SlashCommand>()
  
  /**
   * Get a map containing the registered text commands.
   *
   * @return A map of command names to their corresponding instances.
   */
  fun getTextCommands() = textCommands.toMap()
  
  /**
   * Get a map containing the registered slash commands.
   *
   * @return A map of command names to their corresponding instances.
   */
  fun getSlashCommands() = slashCommands.toMap()
  
  /**
   * Register a text command.
   *
   * @param command The command to register.
   */
  fun register(command: TextCommand) {
    if (textCommands.containsKey(command.name)) throw RuntimeException("Text command with name '${command.name}' already exists!")
    textCommands[command.name] = command
    command.aliases?.forEach {
      if (textCommands.containsKey(it)) throw RuntimeException("Text command alias with name '${it}' already exists!")
      textCommands[it] = command
    }
  }
  
  /**
   * Unregister a text command.
   *
   * @param command The command to unregister.
   */
  fun unregister(command: TextCommand) {
    textCommands.remove(command.name)
    command.aliases?.forEach { textCommands.remove(it) }
  }
  
  /**
   * Register a slash command.
   *
   * @param command The command to register.
   */
  fun register(command: SlashCommand) {
    if (slashCommands.containsKey(command.name)) throw RuntimeException("Slash command with name '${command.name}' already exists!")
    slashCommands[command.name] = command
  }
  
  /**
   * Unregister a slash command.
   *
   * @param command The command to unregister.
   */
  fun unregister(command: SlashCommand) {
    slashCommands.remove(command.name)
  }
  
  /**
   * Load commands from the specified package.
   *
   * @param packageName The package to search for command classes. (e.g. me.blast)
   *
   * **If not provided, the function will scan all source
   * code files for applicable classes implementing the [BaseCommand] interface.**
   */
  fun load(packageName: String = "") {
    val classes: List<Class<*>>
    val millis= measureTimeMillis {
      classes = Utils.loadClasses(packageName).filter {
        BaseCommand::class.java.isAssignableFrom(it)
      }
      classes.forEach { command ->
        try {
          val constructor = command.getDeclaredConstructor()
          constructor.isAccessible = true
          when (val instance = constructor.newInstance()) {
            is SlashCommand -> {
              register(instance)
            }
            
            is TextCommand -> {
              register(instance)
            }
            
            else -> {
              Cordex.logger.error("Class ${instance.javaClass.name} does not extend the right class.")
            }
          }
        } catch (e: Exception) {
          Cordex.logger.error("Could not load class ${command.name}!", e.cause)
        }
      }
    }
    Cordex.logger.info("Took ${millis}ms to load ${classes.size} commands from $packageName")
  }
  
  /**
   * Register a text command.
   *
   * @receiver The command to register.
   */
  operator fun TextCommand.unaryPlus() = register(this)
  
  /**
   * Unregister a text command.
   *
   * @receiver The command to unregister.
   */
  operator fun TextCommand.unaryMinus() = unregister(this)
  
  /**
   * Register a slash command.
   *
   * @receiver The command to register.
   */
  operator fun SlashCommand.unaryPlus() = register(this)
  
  /**
   * Unregister a slash command.
   *
   * @receiver The command to unregister.
   */
  operator fun SlashCommand.unaryMinus() = unregister(this)
}