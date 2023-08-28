package me.blast.utils

import me.blast.command.Command
import me.blast.command.CommandImpl
import me.blast.command.argument.ArgumentType
import me.blast.parser.exception.ArgumentException
import org.javacord.api.entity.message.MessageAuthor
import org.javacord.api.entity.message.embed.EmbedBuilder
import java.awt.Color
import java.io.File
import java.util.*

object Utils {
  val DURATION_REGEX = Regex("^(\\d+(\\.\\d+)?)\\s*(\\w+)$")
  
  fun loadClasses(packageName: String): List<Class<*>> {
    fun findClasses(dir: File, packageName: String): List<Class<*>> {
      val classes = mutableListOf<Class<*>>()
      if (!dir.exists()) {
        return classes
      }
      val files = dir.listFiles()
      for (file in files!!) {
        if (file.isDirectory) {
          assert(!file.name.contains("."))
          classes.addAll(findClasses(file, "$packageName.${file.name}"))
        } else if (file.name.endsWith(".class")) {
          classes.add(Class.forName("$packageName.${file.name.substring(0, file.name.length - 6)}"))
        }
      }
      return classes
    }
    
    val classLoader = Thread.currentThread().contextClassLoader
    val path = packageName.replace('.', '/')
    val resources = classLoader.getResources(path)
    val dirs = mutableListOf<File>()
    
    while (resources.hasMoreElements()) {
      val resource = resources.nextElement()
      dirs.add(File(resource.file))
    }
    val classes = mutableListOf<Class<*>>()
    for (dir in dirs) {
      classes.addAll(findClasses(dir, packageName))
    }
    
    return classes
  }
  
  fun extractDigits(s: String): String {
    return s.replace("[^0-9]".toRegex(), "")
  }
  
  fun convertCamelToKebab(input: String): String {
    val builder = StringBuilder()
    
    for (char in input) {
      if (char.isUpperCase()) {
        builder.append('-')
        builder.append(char.lowercaseChar())
      } else {
        builder.append(char)
      }
    }
    
    return builder.toString()
  }
  
  fun <T> ListIterator<T>.takeWhileWithIndex(predicate: (index: Int, T) -> Boolean): List<T> {
    val resultList = mutableListOf<T>()
    var currentIndex = 0
    
    while (hasNext()) {
      val item = next()
      if (predicate(currentIndex, item)) {
        resultList.add(item)
        currentIndex++
      } else {
        previous()
        break
      }
    }
    
    return resultList
  }
  
  fun <T> Optional<T>.toNullable(): T? {
    return orElse(null)
  }
  
  fun Command.generateHelpMessage(exception: ArgumentException, user: MessageAuthor) = EmbedBuilder().apply {
    setTitle("Help for ${name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} command")
    if (aliases?.isNotEmpty() == true) addField("Aliases", aliases.joinToString(prefix = "`", separator = "`, `", postfix = "`"))
    addField("Description", description)
    setFooter(user.name, user.avatar)
    setTimestampToNow()
    setColor(Color.RED)
    options.takeIf { it.isNotEmpty() }?.let { addField("Arguments", generateArgumentUsage(it)) }
  }
  
  fun generateArgumentUsage(options: List<CommandImpl.Delegate<*>>): String? {
    return options.takeIf { it.isNotEmpty() }?.run {
      val formattedArgs: String
      val formattedOptions: String
      partition { it is CommandImpl.PositionalDelegate || it is CommandImpl.PositionalDelegate<*>.OptionalPositionalDelegate<*, *> || it is CommandImpl.PositionalDelegate<*>.MultiplePositionalDelegate<*, *> }.apply {
        formattedArgs = first.joinToString("\n") { option ->
          "\u001B[0;30m[\u001B[0;31m${if (option.argumentIsOptional || option is CommandImpl.FlagDelegate) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${option.argumentName}\u001B[0;30m>:\n     \u001B[0;33m${option.argumentDescription}"
        }
        formattedOptions = second.joinToString("\n") { option ->
          "\u001B[0;30m[\u001B[0;31m${if (option.argumentIsOptional || option is CommandImpl.FlagDelegate) "?" else "*"}\u001B[0;30m]  \u001B[0;30m--\u001B[1;31m${option.argumentName}${if (option.argumentShortName != null) "\u001B[0;30m, -\u001B[1;31m${option.argumentShortName}" else ""}:\n     \u001B[0;33m${option.argumentDescription}"
        }
      }
      "```ansi\n${if (formattedArgs.isEmpty()) {
        formattedOptions
      } else if (formattedOptions.isEmpty()) {
        formattedArgs
      } else {
        "\u001B[1;37mPositional Arguments\n$formattedArgs\n\n\u001B[1;37mOptions\n$formattedOptions"
      }}```"
    }
  }
  
  fun generateArgumentError(exception: ArgumentException): String {
    return when (exception) {
      is ArgumentException.Empty -> "${if(exception.argument.argumentType == ArgumentType.POSITIONAL) "Positional Arguments" else "Options"}:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${exception.argument.argumentName}\u001B[0;30m>:\n     \u001B[0;33m${exception.argument.argumentDescription}"
      is ArgumentException.Insufficient -> "${if(exception.argument.argumentType == ArgumentType.POSITIONAL) "Positional Arguments" else "Options"}:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${exception.argument.argumentName}\u001B[0;30m>:\n     \u001B[0;33m${exception.argument.argumentDescription}"
      is ArgumentException.Invalid -> "${if(exception.argument.argumentType == ArgumentType.POSITIONAL) "Positional Arguments" else "Options"}:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${exception.argument.argumentName}\u001B[0;30m>:\n     \u001B[0;33m${exception.argument.argumentDescription}"
      is ArgumentException.Missing -> {
        exception.arguments.partition { it.argumentType == ArgumentType.POSITIONAL }.run {
          first.joinToString("\n") { option ->
            "\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (option.argumentIsOptional || option is CommandImpl.FlagDelegate) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${option.argumentName}\u001B[0;30m>:\n     \u001B[0;33m${option.argumentDescription}"
          } + second.joinToString("\n") { option ->
            "\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (option.argumentIsOptional || option is CommandImpl.FlagDelegate) "?" else "*"}\u001B[0;30m]  \u001B[0;30m--\u001B[1;31m${option.argumentName}${if (option.argumentShortName != null) "\u001B[0;30m, -\u001B[1;31m${option.argumentShortName}" else ""}:\n     \u001B[0;33m${option.argumentDescription}"
          }
        }
      }
    }
  }
  
  fun <T> Optional<T>.hasValue() = orElse(null) != null
}