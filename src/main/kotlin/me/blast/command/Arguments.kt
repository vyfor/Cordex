package me.blast.command

class Arguments(private val args: Map<CommandImpl.Delegate<*>, Any>) {
  operator fun <T> get(key: CommandImpl.Delegate<T>): T = args[key] as T
}