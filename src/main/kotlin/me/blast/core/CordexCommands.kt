package me.blast.core

import me.blast.command.Command
import me.blast.utils.Utils

class CordexCommands {
    private val commands = mutableMapOf<String, Command>()
    
    fun getCommands() = commands.toMap()

    fun register(command: Command) {
        commands[command.name] = command
        command.aliases?.forEach { commands[it] = command }
    }
    
    fun unregister(command: Command) {
        commands.remove(command.name)
        command.aliases?.forEach { commands.remove(it) }
    }
    
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
    
    operator fun Command.unaryPlus() = register(this)
    
    operator fun Command.unaryMinus() = unregister(this)
}