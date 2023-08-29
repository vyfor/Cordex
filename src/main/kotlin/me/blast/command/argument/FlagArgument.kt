package me.blast.command.argument

import me.blast.command.argument.builder.ArgumentType

class FlagArgument(options: ArrayList<Argument<*>>) : Argument<Boolean>(options = options) {
  init {
    argumentType = ArgumentType.FLAG
  }
}