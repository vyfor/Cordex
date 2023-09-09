@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package me.blast.utils

import me.blast.command.Command
import me.blast.command.argument.Argument
import me.blast.command.argument.builder.ArgumentType
import me.blast.core.Cordex
import me.blast.parser.exception.ArgumentException
import org.javacord.api.entity.message.MessageAuthor
import org.javacord.api.entity.message.embed.EmbedBuilder
import java.awt.Color
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.*

object Utils {
  val DURATION_REGEX = Regex("^(\\d+(\\.\\d+)?)\\s*(\\w+)$")
  val localDatePatterns = listOf(
    "dd.MM[.yyyy][ HH:mm:ss]",
    "dd-MM[-yyyy][ HH:mm:ss]",
    "dd/MM[/yyyy][ HH:mm:ss]",
    "[yyyy.]MM.dd[ HH:mm:ss]",
    "[yyyy-]MM-dd[ HH:mm:ss]",
    "[yyyy/]MM/dd[ HH:mm:ss]",
    "dd MM[ yyyy][ HH:mm:ss]",
    "dd MMM[ yyyy][ HH:mm:ss]",
    "MMM dd[ yyyy][ HH:mm:ss]",
  )
  val lazyEmptyList = emptyList<Any>()
  
  fun loadClasses(packageName: String): List<Class<*>> {
    val classLoader = Thread.currentThread().contextClassLoader
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
          val clazz = try {
            Class.forName("$packageName.${file.name.substring(0, file.name.length - 6)}", true, classLoader)
          } catch (e: ExceptionInInitializerError) {
            Cordex.logger.trace("$packageName.${file.name}", e)
            continue
          }
          classes.add(clazz)
        }
      }
      return classes
    }
    
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
  
  fun Command.generateHelpMessage(user: MessageAuthor) = EmbedBuilder().apply {
    setTitle("Help for ${name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} command")
    if (aliases?.isNotEmpty() == true) addField("Aliases", aliases.joinToString(prefix = "`", separator = "`, `", postfix = "`"))
    addField("Description", description)
    setFooter(user.name, user.avatar)
    setTimestampToNow()
    setColor(Color.RED)
    options.takeIf { it.isNotEmpty() }?.let { addField("Arguments", generateArgumentUsage(it)) }
  }
  
  fun generateArgumentUsage(options: List<Argument<*>>, errorMessage: String? = null): String? {
    return options.takeIf { it.isNotEmpty() }?.run {
      val formattedArgs: String
      val formattedOptions: String
      partition { it.argumentType == ArgumentType.POSITIONAL }.apply {
        formattedArgs = first.joinToString("\n") { option ->
          "\u001B[0;30m[\u001B[0;31m${if (option.argumentIsOptional || option.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${option.argumentName}\u001B[0;30m>:\n     \u001B[0;33m${option.argumentDescription}"
        }
        formattedOptions = second.joinToString("\n") { option ->
          "\u001B[0;30m[\u001B[0;31m${if (option.argumentIsOptional || option.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m--\u001B[1;31m${option.argumentName}${if (option.argumentShortName != null) "\u001B[0;30m, -\u001B[1;31m${option.argumentShortName}" else ""}:\n     \u001B[0;33m${option.argumentDescription}"
        }
      }
      "${if (errorMessage != null) "> ${errorMessage}\n" else ""}```ansi\n${
        if (formattedArgs.isEmpty()) {
          formattedOptions
        } else if (formattedOptions.isEmpty()) {
          formattedArgs
        } else {
          "\u001B[1;37mPositional Arguments\n$formattedArgs\n\n\u001B[1;37mOptions\n$formattedOptions"
        }
      }```"
    }
  }
  
  fun generateArgumentError(exception: ArgumentException): String {
    return when (exception) {
      is ArgumentException.Empty -> {
        if (exception.argument.argumentType == ArgumentType.POSITIONAL) "Positional Arguments:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${exception.argument.argumentName}\u001B[0;30m>:\n     \u001B[0;33m${exception.argument.argumentDescription}"
        else "Options:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m--\u001B[1;31m${exception.argument.argumentName}${if (exception.argument.argumentShortName != null) "\u001B[0;30m, -\u001B[1;31m${exception.argument.argumentShortName}" else ""}:\n     \u001B[0;33m${exception.argument.argumentDescription}"
      }
      
      is ArgumentException.Insufficient -> {
        if (exception.argument.argumentType == ArgumentType.POSITIONAL) "Positional Arguments:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${exception.argument.argumentName}\u001B[0;30m>:\n     \u001B[0;33m${exception.argument.argumentDescription}"
        else "Options:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m--\u001B[1;31m${exception.argument.argumentName}${if (exception.argument.argumentShortName != null) "\u001B[0;30m, -\u001B[1;31m${exception.argument.argumentShortName}" else ""}:\n     \u001B[0;33m${exception.argument.argumentDescription}"
      }
      
      is ArgumentException.Invalid -> {
        if (exception.argument.argumentType == ArgumentType.POSITIONAL) "Positional Arguments:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${exception.argument.argumentName}\u001B[0;30m>:\n     \u001B[0;33m${exception.argument.argumentDescription}"
        else "Options:\n\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (exception.argument.argumentIsOptional || exception.argument.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m--\u001B[1;31m${exception.argument.argumentName}${if (exception.argument.argumentShortName != null) "\u001B[0;30m, -\u001B[1;31m${exception.argument.argumentShortName}" else ""}:\n     \u001B[0;33m${exception.argument.argumentDescription}"
      }
      
      is ArgumentException.Missing -> {
        exception.arguments.partition { it.argumentType == ArgumentType.POSITIONAL }.run {
          first.joinToString("\n") { option ->
            "\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (option.argumentIsOptional || option.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m<\u001B[1;31m${option.argumentName}\u001B[0;30m>:\n     \u001B[0;33m${option.argumentDescription}"
          } + second.joinToString("\n") { option ->
            "\u001B[4;31m>>>\u001B[0m  \u001B[0;30m[\u001B[0;31m${if (option.argumentIsOptional || option.argumentType == ArgumentType.FLAG) "?" else "*"}\u001B[0;30m]  \u001B[0;30m--\u001B[1;31m${option.argumentName}${if (option.argumentShortName != null) "\u001B[0;30m, -\u001B[1;31m${option.argumentShortName}" else ""}:\n     \u001B[0;33m${option.argumentDescription}"
          }
        }
      }
    }
  }
  
  fun <T> Optional<T>.hasValue() = orElse(null) != null
  
  fun parseDuration(input: String): Duration? {
    val matchResult = DURATION_REGEX.matchEntire(input) ?: return null
    
    val (floatValueStr, _, timeUnit) = matchResult.destructured
    val floatValue = floatValueStr.toDouble()
    
    return Duration.ofSeconds(
      when (timeUnit.lowercase()) {
        "mo", "month", "months" -> (floatValue * 30.4375 * 24 * 60 * 60).toLong()
        "w", "week", "weeks" -> (floatValue * 7 * 24 * 60 * 60).toLong()
        "d", "day", "days" -> (floatValue * 24 * 60 * 60).toLong()
        "h", "hour", "hours" -> (floatValue * 60 * 60).toLong()
        "m", "min", "mins", "minute", "minutes" -> (floatValue * 60).toLong()
        "s", "sec", "secs", "second", "seconds" -> floatValue.toLong()
        else -> throw IllegalArgumentException()
      }
    )
  }
  
  fun parseDate(input: String, locale: Locale): LocalDateTime? {
    for (pattern in localDatePatterns) {
      try {
        val formatter = DateTimeFormatter.ofPattern(pattern, locale).withResolverStyle(ResolverStyle.STRICT)
        return LocalDateTime.parse(input, formatter)
      } catch (_: Exception) {
      }
    }
    return null
  }
}

inline fun <T> throwIf(condition: Boolean, block: () -> T): T {
  if (!condition) {
    return block()
  } else throw IllegalArgumentException()
}

inline fun <T> throwUnless(condition: Boolean, block: () -> T): T {
  if (condition) {
    return block()
  } else throw IllegalArgumentException()
}