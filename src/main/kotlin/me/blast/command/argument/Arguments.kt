package me.blast.command.argument

import me.blast.command.CommandImpl

class Arguments(private val args: Map<String, Any>) {
  operator fun <T> get(key: CommandImpl.Delegate<T>): T = args[key.argumentName] as T
}