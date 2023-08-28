@file:Suppress("unused")

package me.blast.command

import me.blast.command.argument.ArgumentType
import me.blast.utils.Utils
import net.fellbaum.jemoji.Emoji
import net.fellbaum.jemoji.EmojiManager
import org.javacord.api.entity.channel.*
import org.javacord.api.entity.emoji.CustomEmoji
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.user.User
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.util.DiscordRegexPattern
import java.awt.Color
import java.net.URL
import java.time.Duration
import kotlin.collections.ArrayList
import kotlin.jvm.optionals.getOrElse
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate")
abstract class CommandImpl(open val guildOnly: Boolean) {
  val options = ArrayList<Delegate<*>>()
  
  open inner class Delegate<T>(copyFrom: Delegate<*>? = null) {
    lateinit var argumentEvent: MessageCreateEvent
    lateinit var argumentName: String
    lateinit var argumentType: ArgumentType
    var argumentShortName: String? = null
    var argumentDescription = "No description provided."
    var argumentValidator: (String.() -> Any?)? = null
      set(value) {
        if(argumentListValidator != null) argumentListValidator = null
        field = value
      }
    var argumentListValidator: (List<String>.() -> Any)? = null
      set(value) {
        if(argumentValidator != null) argumentValidator = null
        field = value
      }
    var argumentIsOptional: Boolean = false
    var argumentDefaultValue: Any? = null
    // Setting first range value to 0 will make the argument optional
    // Setting last range value to 0 will make the argument take infinite amount of values
    var argumentRange: IntRange = 1..1
    
    init {
      if(copyFrom != null) {
        this.argumentType = copyFrom.argumentType
        this.argumentDescription = copyFrom.argumentDescription
        this.argumentValidator = copyFrom.argumentValidator
        this.argumentListValidator = copyFrom.argumentListValidator
        this.argumentIsOptional = copyFrom.argumentIsOptional
        this.argumentDefaultValue = copyFrom.argumentDefaultValue
        this.argumentRange = copyFrom.argumentRange
      }
    }
    
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = this
    
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): Delegate<T> {
      require(argumentRange.first >= 0 || argumentRange.last >= 0) { "Argument's range must be positive!" }
      if(argumentRange.first == 0) argumentIsOptional = true
      argumentName = Utils.convertCamelToKebab(property.name)
      if (argumentType != ArgumentType.POSITIONAL) argumentShortName = argumentName.substring(0, 1)
      require(options.none { it.argumentName == argumentName || it.argumentShortName == argumentShortName }) { "Argument with name --${argumentName} or -${argumentShortName} already exists!" }
      options.add(this)
      return this
    }
  }
  
  open inner class MultipleValueDelegate<T>(copyFrom: Delegate<*>? = null) : Delegate<T>(copyFrom)
  
  inner class FlagDelegate : Delegate<Boolean>() {
    init {
      argumentType = ArgumentType.FLAG
    }
  }
  
  inner class OptionDelegate<T> : Delegate<T>() {
    init {
      argumentType = ArgumentType.OPTION
    }
    
    inner class OptionalOptionDelegate<T : Any?, S> : Delegate<T>(this) {
      fun multiple(range: IntRange = 0..0): MultipleValueDelegate<List<S>> {
        return MultipleOptionDelegate<List<S>, S>().apply {
          argumentRange = range
        }
      }
      
      fun <R : Any> multiple(range: IntRange = 0..0, validator: List<String>.() -> R): MultipleValueDelegate<R> {
        return MultipleOptionDelegate<R, R>().apply {
          argumentRange = range
          argumentListValidator = validator
        }
      }
      
      fun addValidator(newValidator: String.() -> T): OptionalOptionDelegate<T, S> {
        argumentValidator = newValidator
        return this
      }
    }
    
    inner class MultipleOptionDelegate<T : Any?, S> : MultipleValueDelegate<T>(this) {
      fun optional(): Delegate<T> {
        return MultipleOptionDelegate<T, T>().apply {
          argumentIsOptional = true
        }
      }
      
      fun <R : Any> optional(default: R): Delegate<R> {
        return MultipleOptionDelegate<R, R>().apply {
          argumentIsOptional = true
          argumentDefaultValue = default
        }
      }
      
      fun <R : Any> optional(validator: String.() -> R): Delegate<R?> {
        return MultipleOptionDelegate<R?, R>().apply {
          argumentIsOptional = true
          addValidator(validator)
        }
      }
      
      fun <R : Any> optional(default: R, validator: String.() -> R): Delegate<R> {
        return MultipleOptionDelegate<R, R>().apply {
          argumentIsOptional = true
          argumentDefaultValue = default
          addValidator(validator)
        }
      }
      
      private fun addValidator(newValidator: String.() -> T): MultipleOptionDelegate<T, S> {
        argumentValidator = newValidator
        return this
      }
    }
    
    fun optional(): OptionalOptionDelegate<T?, T> {
      return OptionalOptionDelegate<T?, T>().apply {
        argumentIsOptional = true
      }
    }
    
    fun <R : Any> optional(default: R): OptionalOptionDelegate<R, R> {
      return OptionalOptionDelegate<R, R>().apply {
        argumentIsOptional = true
        argumentDefaultValue = default
      }
    }
    
    fun <R : Any> optional(validator: String.() -> R): OptionalOptionDelegate<R?, R> {
      return OptionalOptionDelegate<R?, R>().apply {
        argumentIsOptional = true
        addValidator(validator)
      }
    }
    
    fun <R : Any> optional(default: R, validator: String.() -> R): OptionalOptionDelegate<R, R> {
      return OptionalOptionDelegate<R, R>().apply {
        argumentIsOptional = true
        argumentDefaultValue = default
        addValidator(validator)
      }
    }
    
    fun multiple(range: IntRange = 0..0): MultipleOptionDelegate<List<T>, T> {
      return MultipleOptionDelegate<List<T>, T>().apply {
        argumentRange = range
      }
    }
    
    fun <R : Any> multiple(range: IntRange = 0..0, validator: List<String>.() -> R): MultipleOptionDelegate<T, R> {
      return MultipleOptionDelegate<T, R>().apply {
        argumentRange = range
        argumentListValidator = validator
      }
    }
    
    fun addValidator(newValidator: String.() -> T): OptionDelegate<T> {
      argumentValidator = newValidator
      return this
    }
  }
  
  inner class PositionalDelegate<T> : Delegate<T>() {
    init {
      argumentType = ArgumentType.POSITIONAL
    }
    
    inner class OptionalPositionalDelegate<T : Any?, C> : Delegate<T>(this) {
      fun multiple(range: IntRange = 0..0): MultipleValueDelegate<List<C>> {
        return MultiplePositionalDelegate<List<C>, C>().apply {
          argumentRange = range
        }
      }
      
      fun <R : Any> multiple(range: IntRange = 0..0, validator: List<String>.() -> R): MultipleValueDelegate<R> {
        return MultiplePositionalDelegate<R, R>().apply {
          argumentRange = range
          argumentListValidator = validator
        }
      }
      
      fun addValidator(newValidator: String.() -> T): OptionalPositionalDelegate<T, C> {
        argumentValidator = newValidator
        return this
      }
    }
    
    inner class MultiplePositionalDelegate<T : Any?, C> : MultipleValueDelegate<T>(this) {
      fun optional(): Delegate<T> {
        return MultiplePositionalDelegate<T, T>().apply {
          argumentIsOptional = true
        }
      }
      
      fun <R : Any> optional(default: R): Delegate<R> {
        return MultiplePositionalDelegate<R, R>().apply {
          argumentIsOptional = true
          argumentDefaultValue = default
        }
      }
      
      fun <R : Any> optional(validator: String.() -> R): Delegate<R?> {
        return MultiplePositionalDelegate<R?, R>().apply {
          argumentIsOptional = true
          addValidator(validator)
        }
      }
      
      fun <R : Any> optional(default: R, validator: String.() -> R): Delegate<R> {
        return MultiplePositionalDelegate<R, R>().apply {
          argumentIsOptional = true
          argumentDefaultValue = default
          addValidator(validator)
        }
      }
      
      private fun addValidator(newValidator: String.() -> T): MultiplePositionalDelegate<T, C> {
        argumentValidator = newValidator
        return this
      }
    }
    
    fun optional(): OptionalPositionalDelegate<T?, T> {
      return OptionalPositionalDelegate<T?, T>().apply {
        argumentIsOptional = true
      }
    }
    
    fun <R : Any> optional(default: R): OptionalPositionalDelegate<R, R> {
      return OptionalPositionalDelegate<R, R>().apply {
        argumentIsOptional = true
        argumentDefaultValue = default
      }
    }
    
    fun <R : Any> optional(validator: String.() -> R): OptionalPositionalDelegate<R?, R> {
      return OptionalPositionalDelegate<R?, R>().apply {
        argumentIsOptional = true
        addValidator(validator)
      }
    }
    
    fun <R : Any> optional(default: R, validator: String.() -> R): OptionalPositionalDelegate<R, R> {
      return OptionalPositionalDelegate<R, R>().apply {
        argumentIsOptional = true
        argumentDefaultValue = default
        addValidator(validator)
      }
    }
    
    fun multiple(range: IntRange = 0..0): MultiplePositionalDelegate<List<T>, T> {
      return MultiplePositionalDelegate<List<T>, T>().apply {
        argumentRange = range
      }
    }
    
    fun <R : Any> multiple(range: IntRange = 0..0, validator: List<String>.() -> R): MultiplePositionalDelegate<R, R> {
      return MultiplePositionalDelegate<R, R>().apply {
        argumentRange = range
        argumentListValidator = validator
      }
    }
    
    fun addValidator(newValidator: String.() -> T): PositionalDelegate<T> {
      argumentValidator = newValidator
      return this
    }
  }
  
  fun option(description: String? = null, fullName: String? = null, shortName: String? = null): OptionDelegate<String> {
    return OptionDelegate<String>().apply {
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
      argumentShortName = shortName
    }
  }
  
  inline fun <reified T> option(description: String? = null, fullName: String? = null, shortName: String? = null, noinline validator: String.() -> T): OptionDelegate<T> {
    return OptionDelegate<T>().addValidator(validator).apply {
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
      argumentShortName = shortName
    }
  }
  
  fun flag(description: String? = null, fullName: String? = null, shortName: String? = null): Delegate<Boolean> {
    return FlagDelegate().apply {
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
      argumentShortName = shortName
    }
  }
  
  fun positional(description: String? = null, fullName: String? = null, shortName: String? = null): PositionalDelegate<String> {
    return PositionalDelegate<String>().apply {
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
      argumentShortName = shortName
    }
  }
  
  inline fun <reified T> positional(description: String? = null, fullName: String? = null, shortName: String? = null, noinline validator: String.() -> T): PositionalDelegate<T> {
    return PositionalDelegate<T>().addValidator(validator).apply {
      if (description != null) argumentDescription = description
      if (fullName != null) argumentName = fullName
      argumentShortName = shortName
    }
  }
  
  fun Delegate<*>.int(): Delegate<Int> {
    return (this as Delegate<Int>).apply {
      argumentValidator = {
        toInt()
      }
    }
  }
  
  fun Delegate<*>.long(): Delegate<Long> {
    return (this as Delegate<Long>).apply {
      argumentValidator = { toLong() }
    }
  }
  
  fun Delegate<*>.float(): Delegate<Float> {
    return (this as Delegate<Float>).apply {
      argumentValidator = { toFloat() }
    }
  }
  
  fun Delegate<*>.double(): Delegate<Double> {
    return (this as Delegate<Double>).apply {
      argumentValidator = { toDouble() }
    }
  }
  
  fun Delegate<*>.url(): Delegate<URL> {
    return (this as Delegate<URL>).apply {
      argumentValidator = { URL(this) }
    }
  }
  
  fun Delegate<*>.user(): Delegate<User> {
    require(guildOnly) { "This option can only be used in guilds!" }
    return (this as Delegate<User>).apply {
      argumentValidator = {
        argumentEvent.server.get().let { server ->
          server.getMembersByDisplayNameIgnoreCase(this).firstOrNull() ?: server.getMemberByDiscriminatedName(this).orElse(
            server.getMemberById(Utils.extractDigits(this)).orElse(
              server.getMembersByNameIgnoreCase(this).firstOrNull()
            )
          ) ?: throw IllegalArgumentException()
        }
      }
    }
  }
  
  fun Delegate<*>.channel(): Delegate<ServerChannel> {
    require(guildOnly) { "This option can only be used in guilds!" }
    return (this as Delegate<ServerChannel>).apply {
      argumentValidator = {
        argumentEvent.server.get().let { server ->
          server.getChannelsByNameIgnoreCase(this).firstOrNull() ?: server.getChannelById(Utils.extractDigits(this)).orElse(null) ?: throw IllegalArgumentException()
        }
      }
    }
  }
  
  inline fun <reified R : ServerChannel> Delegate<*>.channelType(): Delegate<R> {
    require(guildOnly) { "This option can only be used in guilds!" }
    return (this as Delegate<R>).apply {
      argumentValidator = {
        val channel = argumentEvent.server.get().let { server ->
          server.getChannelsByNameIgnoreCase(this).firstOrNull() ?: server.getChannelById(Utils.extractDigits(this)).orElse(null) ?: throw IllegalArgumentException()
        }
        when (R::class) {
          ServerChannel::class -> channel as R
          ServerTextChannel::class -> channel.asServerTextChannel().get() as R
          ServerVoiceChannel::class -> channel.asServerVoiceChannel().get() as R
          ServerThreadChannel::class -> channel.asServerThreadChannel().get() as R
          ServerStageVoiceChannel::class -> channel.asServerStageVoiceChannel().get() as R
          ServerForumChannel::class -> channel.asServerForumChannel().get() as R
          else -> throw IllegalArgumentException("Unsupported channel type.")
        }
      }
    }
  }
  
  fun Delegate<*>.category(): Delegate<ChannelCategory> {
    require(guildOnly) { "This option can only be used in guilds!" }
    return (this as Delegate<ChannelCategory>).apply {
      argumentValidator = {
        argumentEvent.server.get().let { server ->
          server.getChannelCategoriesByNameIgnoreCase(this).firstOrNull() ?: server.getChannelCategoryById(Utils.extractDigits(this)).orElse(null) ?: throw IllegalArgumentException()
        }
      }
    }
  }
  
  fun Delegate<*>.role(): Delegate<Role> {
    require(guildOnly) { "This option can only be used in guilds!" }
    return (this as Delegate<Role>).apply {
      argumentValidator = {
        argumentEvent.server.get().let { server ->
          server.getRolesByNameIgnoreCase(this).firstOrNull() ?: server.getRoleById(Utils.extractDigits(this)).orElse(null) ?: throw IllegalArgumentException()
        }
      }
    }
  }
  
  fun Delegate<*>.duration(): Delegate<Duration> {
    return (this as Delegate<Duration>).apply {
      argumentValidator = {
        val matchResult = Utils.DURATION_REGEX.matchEntire(this) ?: throw IllegalArgumentException()
        
        val (floatValueStr, _, timeUnit) = matchResult.destructured
        val floatValue = floatValueStr.toDouble()
        
        when (timeUnit.lowercase()) {
          "mo", "month", "months" -> Duration.ofDays((floatValue * 30.4375).toLong())
          "w", "week", "weeks" -> Duration.ofDays((floatValue * 7).toLong())
          "d", "day", "days" -> Duration.ofDays(floatValue.toLong())
          "m", "min", "mins", "minute", "minutes" -> Duration.ofMinutes(floatValue.toLong())
          "s", "sec", "secs", "second", "seconds" -> Duration.ofSeconds(floatValue.toLong())
          else -> throw IllegalArgumentException()
        }
      }
    }
  }
  
  fun Delegate<*>.color(): Delegate<Color> {
    return (this as Delegate<Color>).apply {
      argumentValidator = {
        Color::class.java.getField(this)[null] as? Color ?: Color.decode(this)
      }
    }
  }
  
  fun Delegate<*>.unicodeEmoji(): Delegate<Emoji> {
    return (this as Delegate<Emoji>).apply {
      argumentValidator = {
        EmojiManager.getEmoji(this).get()
      }
    }
  }
  
  fun Delegate<*>.customEmoji(): Delegate<CustomEmoji> {
    return (this as Delegate<CustomEmoji>).apply {
      argumentValidator = {
        val matchResult = DiscordRegexPattern.CUSTOM_EMOJI.toRegex().matchEntire(this) ?: throw IllegalArgumentException()
        argumentEvent.server.get().getCustomEmojiById(matchResult.groups["id"]!!.value).get()
      }
    }
  }
  
  fun MultipleValueDelegate<*>.int(): Delegate<Int> {
    return (this as Delegate<Int>).apply {
      argumentValidator = {
        this.toInt()
      }
    }
  }
  
  fun MultipleValueDelegate<*>.long(): Delegate<Long> {
    return (this as Delegate<Long>).apply {
      argumentValidator = {
        this.toLong()
      }
    }
  }
  
  fun MultipleValueDelegate<*>.float(): Delegate<Float> {
    return (this as Delegate<Float>).apply {
      argumentValidator = {
        this.toFloat()
      }
    }
  }
  
  fun MultipleValueDelegate<*>.double(): Delegate<Double> {
    return (this as Delegate<Double>).apply {
      argumentValidator = {
        this.toDouble()
      }
    }
  }
  
  fun MultipleValueDelegate<*>.url(): Delegate<URL> {
    return (this as Delegate<URL>).apply {
      argumentValidator = {
        URL(this)
      }
    }
  }
  
  fun MultipleValueDelegate<*>.user(): Delegate<User> {
    require(guildOnly) { "This option can only be used in guilds!" }
    return (this as Delegate<User>).apply {
      argumentValidator = {
        map {
          argumentEvent.server.get().let { server ->
            server.getMembersByDisplayNameIgnoreCase(this).firstOrNull() ?: server.getMemberByDiscriminatedName(this).orElse(
              server.getMemberById(Utils.extractDigits(this)).orElse(
                server.getMembersByNameIgnoreCase(this).firstOrNull()
              )
            ) ?: throw IllegalArgumentException()
          }
        }
      }
    }
  }
  
  fun MultipleValueDelegate<*>.channel(): Delegate<ServerChannel> {
    require(guildOnly) { "This option can only be used in guilds!" }
    return (this as Delegate<ServerChannel>).apply {
      argumentValidator = {
        argumentEvent.server.get().let { server ->
          server.getChannelsByNameIgnoreCase(this).firstOrNull() ?: server.getChannelById(Utils.extractDigits(this)).orElse(null) ?: throw IllegalArgumentException()
        }
      }
    }
  }
  
  inline fun <reified R : ServerChannel> MultipleValueDelegate<*>.channelType(): Delegate<R> {
    require(guildOnly) { "This option can only be used in guilds!" }
    return (this as Delegate<R>).apply {
      argumentValidator = {
        val channel = argumentEvent.server.get().let { server ->
          server.getChannelsByNameIgnoreCase(this).firstOrNull() ?: server.getChannelById(Utils.extractDigits(this)).orElse(null) ?: throw IllegalArgumentException()
        }
        when (R::class) {
          ServerChannel::class -> channel as R
          ServerTextChannel::class -> channel.asServerTextChannel().get() as R
          ServerVoiceChannel::class -> channel.asServerVoiceChannel().get() as R
          ServerThreadChannel::class -> channel.asServerThreadChannel().get() as R
          ServerStageVoiceChannel::class -> channel.asServerStageVoiceChannel().get() as R
          ServerForumChannel::class -> channel.asServerForumChannel().get() as R
          else -> throw IllegalArgumentException("Unsupported channel type.")
        }
      }
    }
  }
  
  fun MultipleValueDelegate<*>.category(): Delegate<ChannelCategory> {
    require(guildOnly) { "This option can only be used in guilds!" }
    return (this as Delegate<ChannelCategory>).apply {
      argumentValidator = {
        argumentEvent.server.get().let { server ->
          server.getChannelCategoriesByNameIgnoreCase(this).firstOrNull() ?: server.getChannelCategoryById(Utils.extractDigits(this)).orElse(null) ?: throw IllegalArgumentException()
        }
      }
    }
  }
  
  fun MultipleValueDelegate<*>.role(): Delegate<Role> {
    require(guildOnly) { "This option can only be used in guilds!" }
    return (this as Delegate<Role>).apply {
      argumentValidator = {
        argumentEvent.server.get().let { server ->
          server.getRolesByNameIgnoreCase(this).firstOrNull() ?: server.getRoleById(Utils.extractDigits(this)).orElse(null) ?: throw IllegalArgumentException()
        }
      }
    }
  }
  
  fun MultipleValueDelegate<*>.duration(): Delegate<Duration> {
    return (this as Delegate<Duration>).apply {
      argumentValidator = {
        val matchResult = Utils.DURATION_REGEX.matchEntire(this) ?: throw IllegalArgumentException()
        
        val (floatValueStr, _, timeUntil) = matchResult.destructured
        val floatValue = floatValueStr.toDouble()
        
        when (timeUntil.lowercase()) {
          "mo", "month", "months" -> Duration.ofDays((floatValue * 30.4375).toLong())
          "w", "week", "weeks" -> Duration.ofDays((floatValue * 7).toLong())
          "d", "day", "days" -> Duration.ofDays(floatValue.toLong())
          "m", "min", "mins", "minute", "minutes" -> Duration.ofMinutes(floatValue.toLong())
          "s", "sec", "secs", "second", "seconds" -> Duration.ofSeconds(floatValue.toLong())
          else -> throw IllegalArgumentException()
        }
      }
    }
  }
  
  fun MultipleValueDelegate<*>.color(): Delegate<Color> {
    return (this as Delegate<Color>).apply {
      argumentValidator = {
        Color::class.java.getField(this)[null] as? Color ?: Color.decode(this)
      }
    }
  }
  
  fun MultipleValueDelegate<*>.unicodeEmoji(): Delegate<Emoji> {
    return (this as Delegate<Emoji>).apply {
      argumentValidator = {
        EmojiManager.getEmoji(this).get()
      }
    }
  }
  
  fun MultipleValueDelegate<*>.customEmoji(): Delegate<CustomEmoji> {
    return (this as Delegate<CustomEmoji>).apply {
      argumentValidator = {
        val matchResult = DiscordRegexPattern.CUSTOM_EMOJI.toRegex().matchEntire(this) ?: throw IllegalArgumentException()
        argumentEvent.server.get().getCustomEmojiById(matchResult.groups["id"]!!.value).get()
      }
    }
  }
  
  fun MultipleValueDelegate<*>.ints(): Delegate<List<Int>> {
    return (this as Delegate<List<Int>>).apply {
      argumentListValidator = {
        map {
          it.toInt()
        }
      }
    }
  }
  
  fun MultipleValueDelegate<*>.longs(): Delegate<List<Long>> {
    return (this as Delegate<List<Long>>).apply {
      argumentListValidator = {
        map {
          it.toLong()
        }
      }
    }
  }
  
  fun MultipleValueDelegate<*>.floats(): Delegate<List<Float>> {
    return (this as Delegate<List<Float>>).apply {
      argumentListValidator = {
        map {
          it.toFloat()
        }
      }
    }
  }
  
  fun MultipleValueDelegate<*>.doubles(): Delegate<List<Double>> {
    return (this as Delegate<List<Double>>).apply {
      argumentListValidator = {
        map {
          it.toDouble()
        }
      }
    }
  }
  
  fun MultipleValueDelegate<*>.urls(): Delegate<List<URL>> {
    return (this as Delegate<List<URL>>).apply {
      argumentListValidator = {
        map {
          URL(it)
        }
      }
    }
  }
  
  fun MultipleValueDelegate<*>.users(): Delegate<List<User>> {
    require(guildOnly) { "This option can only be used in guilds!" }
    return (this as Delegate<List<User>>).apply {
      argumentListValidator = {
        map {
          argumentEvent.server.get().let { server ->
            server.getMembersByDisplayNameIgnoreCase(it).firstOrNull() ?: server.getMemberByDiscriminatedName(it).orElse(
              server.getMemberById(Utils.extractDigits(it)).orElse(
                server.getMembersByNameIgnoreCase(it).firstOrNull()
              )
            ) ?: throw IllegalArgumentException()
          }
        }
      }
    }
  }
  
  fun MultipleValueDelegate<*>.channels(): Delegate<List<ServerChannel>> {
    require(guildOnly) { "This option can only be used in guilds!" }
    return (this as Delegate<List<ServerChannel>>).apply {
      argumentListValidator = {
        map {
          argumentEvent.server.get().let { server ->
            server.getChannelsByNameIgnoreCase(it).firstOrNull() ?: server.getChannelById(Utils.extractDigits(it)).orElse(null) ?: throw IllegalArgumentException()
          }
        }
      }
    }
  }
  
  inline fun <reified R : ServerChannel> MultipleValueDelegate<*>.channelTypes(): Delegate<List<R>> {
    require(guildOnly) { "This option can only be used in guilds!" }
    return (this as Delegate<List<R>>).apply {
      argumentListValidator = {
        map {
          val channel = argumentEvent.server.get().let { server ->
            server.getChannelsByNameIgnoreCase(it).firstOrNull() ?: server.getChannelById(Utils.extractDigits(it)).orElse(null) ?: throw IllegalArgumentException()
          }
          when (R::class) {
            ServerChannel::class -> channel as R
            ServerTextChannel::class -> channel.asServerTextChannel().get() as R
            ServerVoiceChannel::class -> channel.asServerVoiceChannel().get() as R
            ServerThreadChannel::class -> channel.asServerThreadChannel().get() as R
            ServerStageVoiceChannel::class -> channel.asServerStageVoiceChannel().get() as R
            ServerForumChannel::class -> channel.asServerForumChannel().get() as R
            else -> throw IllegalArgumentException("Unsupported channel type.")
          }
        }
      }
    }
  }
  
  fun MultipleValueDelegate<*>.categories(): Delegate<List<ChannelCategory>> {
    require(guildOnly) { "This option can only be used in guilds!" }
    return (this as Delegate<List<ChannelCategory>>).apply {
      argumentListValidator = {
        map {
          argumentEvent.server.get().let { server ->
            server.getChannelCategoriesByNameIgnoreCase(it).firstOrNull() ?: server.getChannelCategoryById(Utils.extractDigits(it)).orElse(null) ?: throw IllegalArgumentException()
          }
        }
      }
    }
  }
  
  fun MultipleValueDelegate<*>.roles(): Delegate<List<Role>> {
    require(guildOnly) { "This option can only be used in guilds!" }
    return (this as Delegate<List<Role>>).apply {
      argumentListValidator = {
        map {
          argumentEvent.server.get().let { server ->
            server.getRolesByNameIgnoreCase(it).firstOrNull() ?: server.getRoleById(Utils.extractDigits(it)).orElse(null) ?: throw IllegalArgumentException()
          }
        }
      }
    }
  }
  
  fun MultipleValueDelegate<*>.durations(): Delegate<List<Duration>> {
    return (this as Delegate<List<Duration>>).apply {
      argumentListValidator = {
        map {
          val matchResult = Utils.DURATION_REGEX.matchEntire(it) ?: throw IllegalArgumentException()
          
          val (floatValueStr, _, timeUnit) = matchResult.destructured
          val floatValue = floatValueStr.toDouble()
          
          when (timeUnit.lowercase()) {
            "mo", "month", "months" -> Duration.ofDays((floatValue * 30.4375).toLong())
            "w", "week", "weeks" -> Duration.ofDays((floatValue * 7).toLong())
            "d", "day", "days" -> Duration.ofDays(floatValue.toLong())
            "m", "min", "mins", "minute", "minutes" -> Duration.ofMinutes(floatValue.toLong())
            "s", "sec", "secs", "second", "seconds" -> Duration.ofSeconds(floatValue.toLong())
            else -> throw IllegalArgumentException()
          }
        }
      }
    }
  }
  
  fun MultipleValueDelegate<*>.colors(): Delegate<List<Color>> {
    return (this as Delegate<List<Color>>).apply {
      argumentListValidator = {
        map {
          Color::class.java.getField(it)[null] as? Color ?: Color.decode(it)
        }
      }
    }
  }
  
  fun MultipleValueDelegate<*>.unicodeEmojis(): Delegate<List<Emoji>> {
    return (this as Delegate<List<Emoji>>).apply {
      argumentListValidator = {
        map {
          EmojiManager.getEmoji(it).get()
        }
      }
    }
  }
  
  fun MultipleValueDelegate<*>.customEmojis(): Delegate<List<CustomEmoji>> {
    return (this as Delegate<List<CustomEmoji>>).apply {
      argumentListValidator = {
        map {
          val matchResult = DiscordRegexPattern.CUSTOM_EMOJI.toRegex().matchEntire(it) ?: throw IllegalArgumentException()
          argumentEvent.server.get().getCustomEmojiById(matchResult.groups["id"]!!.value).get()
        }
      }
    }
  }
}