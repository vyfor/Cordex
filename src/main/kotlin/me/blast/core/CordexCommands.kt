package me.blast.core

import me.blast.command.Command
import me.blast.parser.exceptions.ArgumentException
import me.blast.utils.Utils

class CordexCommands {
  private val commands = mutableMapOf<String, Command>()
  var errorHandler: ((ArgumentException, Command) -> Unit)? = null
  
  /**
   * Get a map containing the registered commands.
   *
   * @return A map of command names to their corresponding instances.
   */
  fun getCommands() = commands.toMap()
  
  /**
   * Register a command.
   *
   * @param command The command to register.
   */
  fun register(command: Command) {
    commands[command.name] = command
    command.aliases?.forEach { commands[it] = command }
  }
  
  /**
   * Unregister a command.
   *
   * @param command The command to unregister.
   */
  fun unregister(command: Command) {
    commands.remove(command.name)
    command.aliases?.forEach { commands.remove(it) }
  }
  
  /**
   * Load commands from the specified package.
   *
   * @param packageName The package to search for command classes.
   * *If not provided, the function will scan all source
   * code files for applicable classes extending [Command] class.*
   */
  fun load(packageName: String = "") {
    Utils.loadClasses(packageName).filter {
      it.superclass == Command::class.java
    }.forEach { command ->
      try {
        val constructor = command.getDeclaredConstructor()
        constructor.isAccessible = true
        register(constructor.newInstance() as Command)
      } catch (e: Exception) {
        Cordex.logger.error("Could not load class ${command.name}!", e)
      }
    }
  }
  
  /**
   * Set an error handler for handling exceptions thrown during parsing of arguments.
   *
   * *If not specified, will default to sending the help embed of the command.*
   * @param block The block of code to execute when an argument exception occurs.
   *
   * @see [ArgumentException]
   */
  fun onError(block: (ArgumentException, Command) -> Unit) {
    errorHandler = block
  }
  
  /**
   * Register a command.
   *
   * @receiver The command to register.
   */
  operator fun Command.unaryPlus() = register(this)
  
  /**
   * Unregister a command.
   *
   * @receiver The command to unregister.
   */
  operator fun Command.unaryMinus() = unregister(this)
}