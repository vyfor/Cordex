package me.blast.command

import me.blast.utils.Utils
import org.javacord.api.entity.channel.*
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.user.User
import org.javacord.api.event.message.MessageCreateEvent
import java.net.URL
import java.time.Duration
import kotlin.reflect.KProperty

abstract class CommandImpl(open val guildOnly: Boolean) {
  val options = ArrayList<Delegate<*>>()
  
  abstract inner class Delegate<T> {
    lateinit var event: MessageCreateEvent
    lateinit var name: String
    var short: String? = null
    var optionDescription = "No description provided."
    var validator: (String.() -> T)? = null
    var listValidator: (List<String>.() -> Any?)? = null
    var isOptional: Boolean = false
    var defaultValue: Any? = null
    var multipleValues: Int = 1
    
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = this
    
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): Delegate<T> {
      name = Utils.convertCamelToKebab(property.name)
      if (this is CommandImpl.PositionalDelegate || this is CommandImpl.PositionalDelegate<*>.OptionalPositionalDelegate<*, *> || this is CommandImpl.PositionalDelegate<*>.MultiplePositionalDelegate<*, *>) short = name.substring(0, 1)
      options.add(this)
      return this
    }
  }
  
  inner class FlagDelegate : Delegate<Boolean>()
  
  inner class OptionDelegate<T> : Delegate<T>() {
    inner class OptionalOptionDelegate<T : Any?, S> : Delegate<T>() {
      fun multiple(count: Int = 0): OptionalOptionDelegate<List<S>, S> {
        return OptionalOptionDelegate<List<S>, S>().apply {
          isOptional = this@OptionDelegate.isOptional
          defaultValue = this@OptionDelegate.defaultValue
          multipleValues = count
          optionDescription = this@OptionDelegate.optionDescription
        }
      }
      
      fun <R : Any> multiple(count: Int = 0, validator: List<String>.() -> R): OptionalOptionDelegate<R, R> {
        return OptionalOptionDelegate<R, R>().apply {
          isOptional = this@OptionDelegate.isOptional
          defaultValue = this@OptionDelegate.defaultValue
          multipleValues = count
          optionDescription = this@OptionDelegate.optionDescription
          listValidator = validator
        }
      }
      
      fun addValidator(newValidator: String.() -> T): OptionalOptionDelegate<T, S> {
        validator = newValidator
        return this
      }
    }
    
    inner class MultipleOptionDelegate<T : Any?, S> : Delegate<T>() {
      fun optional(): MultipleOptionDelegate<T, T> {
        return MultipleOptionDelegate<T, T>().apply {
          isOptional = true
          multipleValues = this@OptionDelegate.multipleValues
          optionDescription = this@OptionDelegate.optionDescription
        }
      }
      
      fun <R : Any> optional(default: R): MultipleOptionDelegate<R, R> {
        return MultipleOptionDelegate<R, R>().apply {
          isOptional = true
          defaultValue = default
          multipleValues = this@OptionDelegate.multipleValues
          optionDescription = this@OptionDelegate.optionDescription
        }
      }
      
      fun <R : Any> optional(validator: String.() -> R): MultipleOptionDelegate<R?, R> {
        return MultipleOptionDelegate<R?, R>().apply {
          isOptional = true
          multipleValues = this@OptionDelegate.multipleValues
          optionDescription = this@OptionDelegate.optionDescription
          addValidator(validator)
        }
      }
      
      fun <R : Any> optional(default: R, validator: String.() -> R): MultipleOptionDelegate<R, R> {
        return MultipleOptionDelegate<R, R>().apply {
          isOptional = true
          defaultValue = default
          multipleValues = this@OptionDelegate.multipleValues
          optionDescription = this@OptionDelegate.optionDescription
          addValidator(validator)
        }
      }
      
      fun addValidator(newValidator: String.() -> T): MultipleOptionDelegate<T, S> {
        validator = newValidator
        return this
      }
    }
    
    fun optional(): OptionalOptionDelegate<T?, T> {
      return OptionalOptionDelegate<T?, T>().apply {
        isOptional = true
        multipleValues = this@OptionDelegate.multipleValues
        optionDescription = this@OptionDelegate.optionDescription
      }
    }
    
    fun <R : Any> optional(default: R): OptionalOptionDelegate<R, R> {
      return OptionalOptionDelegate<R, R>().apply {
        isOptional = true
        defaultValue = default
        multipleValues = this@OptionDelegate.multipleValues
        optionDescription = this@OptionDelegate.optionDescription
      }
    }
    
    fun <R : Any> optional(validator: String.() -> R): OptionalOptionDelegate<R?, R> {
      return OptionalOptionDelegate<R?, R>().apply {
        isOptional = true
        multipleValues = this@OptionDelegate.multipleValues
        optionDescription = this@OptionDelegate.optionDescription
        addValidator(validator)
      }
    }
    
    fun <R : Any> optional(default: R, validator: String.() -> R): OptionalOptionDelegate<R, R> {
      return OptionalOptionDelegate<R, R>().apply {
        isOptional = true
        defaultValue = default
        multipleValues = this@OptionDelegate.multipleValues
        optionDescription = this@OptionDelegate.optionDescription
        addValidator(validator)
      }
    }
    
    fun multiple(count: Int = 0): MultipleOptionDelegate<List<T>, T> {
      return MultipleOptionDelegate<List<T>, T>().apply {
        isOptional = this@OptionDelegate.isOptional
        defaultValue = this@OptionDelegate.defaultValue
        multipleValues = count
        optionDescription = this@OptionDelegate.optionDescription
      }
    }
    
    fun <R : Any> multiple(count: Int = 0, validator: List<String>.() -> R): MultipleOptionDelegate<T, R> {
      return MultipleOptionDelegate<T, R>().apply {
        isOptional = this@OptionDelegate.isOptional
        defaultValue = this@OptionDelegate.defaultValue
        multipleValues = count
        optionDescription = this@OptionDelegate.optionDescription
        listValidator = validator
      }
    }
    
    fun addValidator(newValidator: String.() -> T): OptionDelegate<T> {
      validator = newValidator
      return this
    }
  }
  
  inner class PositionalDelegate<T> : Delegate<T>() {
    inner class OptionalPositionalDelegate<T : Any?, C> : Delegate<T>() {
      fun multiple(count: Int = 0): OptionalPositionalDelegate<List<C>, C> {
        return OptionalPositionalDelegate<List<C>, C>().apply {
          isOptional = this@PositionalDelegate.isOptional
          defaultValue = this@PositionalDelegate.defaultValue
          multipleValues = count
          optionDescription = this@PositionalDelegate.optionDescription
        }
      }
      
      fun <R : Any> multiple(count: Int = 0, validator: List<String>.() -> R): OptionalPositionalDelegate<R, R> {
        return OptionalPositionalDelegate<R, R>().apply {
          isOptional = this@PositionalDelegate.isOptional
          defaultValue = this@PositionalDelegate.defaultValue
          multipleValues = count
          optionDescription = this@PositionalDelegate.optionDescription
          listValidator = validator
        }
      }
      
      fun addValidator(newValidator: String.() -> T): OptionalPositionalDelegate<T, C> {
        validator = newValidator
        return this
      }
    }
    
    inner class MultiplePositionalDelegate<T : Any?, C> : Delegate<T>() {
      fun optional(): MultiplePositionalDelegate<T, T> {
        return MultiplePositionalDelegate<T, T>().apply {
          isOptional = true
          multipleValues = this@PositionalDelegate.multipleValues
          optionDescription = this@PositionalDelegate.optionDescription
        }
      }
      
      fun <R : Any> optional(default: R): MultiplePositionalDelegate<R, R> {
        return MultiplePositionalDelegate<R, R>().apply {
          isOptional = true
          defaultValue = default
          multipleValues = this@PositionalDelegate.multipleValues
          optionDescription = this@PositionalDelegate.optionDescription
        }
      }
      
      fun <R : Any> optional(validator: String.() -> R): MultiplePositionalDelegate<R?, R> {
        return MultiplePositionalDelegate<R?, R>().apply {
          isOptional = true
          multipleValues = this@PositionalDelegate.multipleValues
          optionDescription = this@PositionalDelegate.optionDescription
          addValidator(validator)
        }
      }
      
      fun <R : Any> optional(default: R, validator: String.() -> R): MultiplePositionalDelegate<R, R> {
        return MultiplePositionalDelegate<R, R>().apply {
          isOptional = true
          defaultValue = default
          multipleValues = this@PositionalDelegate.multipleValues
          optionDescription = this@PositionalDelegate.optionDescription
          addValidator(validator)
        }
      }
      
      fun addValidator(newValidator: String.() -> T): MultiplePositionalDelegate<T, C> {
        validator = newValidator
        return this
      }
    }
    
    fun optional(): OptionalPositionalDelegate<T?, T> {
      return OptionalPositionalDelegate<T?, T>().apply {
        isOptional = true
        multipleValues = this@PositionalDelegate.multipleValues
        optionDescription = this@PositionalDelegate.optionDescription
      }
    }
    
    fun <R : Any> optional(default: R): OptionalPositionalDelegate<R, R> {
      return OptionalPositionalDelegate<R, R>().apply {
        isOptional = true
        defaultValue = default
        multipleValues = this@PositionalDelegate.multipleValues
        optionDescription = this@PositionalDelegate.optionDescription
      }
    }
    
    fun <R : Any> optional(validator: String.() -> R): OptionalPositionalDelegate<R?, R> {
      return OptionalPositionalDelegate<R?, R>().apply {
        isOptional = true
        multipleValues = this@PositionalDelegate.multipleValues
        optionDescription = this@PositionalDelegate.optionDescription
        addValidator(validator)
      }
    }
    
    fun <R : Any> optional(default: R, validator: String.() -> R): OptionalPositionalDelegate<R, R> {
      return OptionalPositionalDelegate<R, R>().apply {
        isOptional = true
        defaultValue = default
        multipleValues = this@PositionalDelegate.multipleValues
        optionDescription = this@PositionalDelegate.optionDescription
        addValidator(validator)
      }
    }
    
    fun multiple(count: Int = 0): MultiplePositionalDelegate<List<T>, T> {
      return MultiplePositionalDelegate<List<T>, T>().apply {
        isOptional = this@PositionalDelegate.isOptional
        defaultValue = this@PositionalDelegate.defaultValue
        multipleValues = count
        optionDescription = this@PositionalDelegate.optionDescription
      }
    }
    
    fun <R : Any> multiple(count: Int = 0, validator: List<String>.() -> R): MultiplePositionalDelegate<R, R> {
      return MultiplePositionalDelegate<R, R>().apply {
        isOptional = this@PositionalDelegate.isOptional
        defaultValue = this@PositionalDelegate.defaultValue
        multipleValues = count
        optionDescription = this@PositionalDelegate.optionDescription
        listValidator = validator
      }
    }
    
    fun addValidator(newValidator: String.() -> T): PositionalDelegate<T> {
      validator = newValidator
      return this
    }
  }
  
  fun option(description: String? = null, fullName: String? = null, shortName: String? = null): OptionDelegate<String> {
    return OptionDelegate<String>().apply {
      if (description != null) optionDescription = description
      if (fullName != null) name = fullName
      short = shortName
    }
  }
  
  inline fun <reified T> option(description: String? = null, fullName: String? = null, shortName: String? = null, noinline validator: String.() -> T): OptionDelegate<T> {
    return OptionDelegate<T>().addValidator(validator).apply {
      if (description != null) optionDescription = description
      if (fullName != null) name = fullName
      short = shortName
    }
  }
  
  fun flag(description: String? = null, fullName: String? = null, shortName: String? = null): Delegate<Boolean> {
    return FlagDelegate().apply {
      if (description != null) optionDescription = description
      if (fullName != null) name = fullName
      short = shortName
    }
  }
  
  fun positional(description: String? = null, fullName: String? = null, shortName: String? = null): PositionalDelegate<String> {
    return PositionalDelegate<String>().apply {
      if (description != null) optionDescription = description
      if (fullName != null) name = fullName
      short = shortName
    }
  }
  
  inline fun <reified T> positional(description: String? = null, fullName: String? = null, shortName: String? = null, noinline validator: String.() -> T): PositionalDelegate<T> {
    return PositionalDelegate<T>().addValidator(validator).apply {
      if (description != null) optionDescription = description
      if (fullName != null) name = fullName
      short = shortName
    }
  }
  
  fun OptionDelegate<*>.int(): OptionDelegate<Int> {
    return OptionDelegate<Int>().apply {
      optionDescription = this@int.optionDescription
      isOptional = this@int.isOptional
      defaultValue = this@int.defaultValue
      multipleValues = this@int.multipleValues
      addValidator {
        toInt()
      }
    }
  }
  
  fun OptionDelegate<*>.long(): OptionDelegate<Long> {
    return OptionDelegate<Long>().apply {
      optionDescription = this@long.optionDescription
      isOptional = this@long.isOptional
      defaultValue = this@long.defaultValue
      multipleValues = this@long.multipleValues
      addValidator {
        toLong()
      }
    }
  }
  
  fun OptionDelegate<*>.float(): OptionDelegate<Float> {
    return OptionDelegate<Float>().apply {
      optionDescription = this@float.optionDescription
      isOptional = this@float.isOptional
      defaultValue = this@float.defaultValue
      multipleValues = this@float.multipleValues
      addValidator {
        toFloat()
      }
    }
  }
  
  fun OptionDelegate<*>.double(): OptionDelegate<Double> {
    return OptionDelegate<Double>().apply {
      optionDescription = this@double.optionDescription
      isOptional = this@double.isOptional
      defaultValue = this@double.defaultValue
      multipleValues = this@double.multipleValues
      addValidator {
        toDouble()
      }
    }
  }
  
  fun OptionDelegate<*>.url(): OptionDelegate<URL> {
    return OptionDelegate<URL>().apply {
      optionDescription = this@url.optionDescription
      isOptional = this@url.isOptional
      defaultValue = this@url.defaultValue
      multipleValues = this@url.multipleValues
      addValidator {
        URL(this)
      }
    }
  }
  
  fun OptionDelegate<*>.user(): OptionDelegate<User> {
    require(guildOnly) { "This option can only be used in guilds!" }
    return OptionDelegate<User>().apply {
      optionDescription = this@user.optionDescription
      isOptional = this@user.isOptional
      defaultValue = this@user.defaultValue
      multipleValues = this@user.multipleValues
      addValidator {
        event.server.get().let { server ->
          server.getMembersByDisplayNameIgnoreCase(this).firstOrNull() ?: server.getMemberByDiscriminatedName(this).orElse(
            server.getMemberById(Utils.extractDigits(this)).orElse(
              server.getMembersByNameIgnoreCase(this).firstOrNull()
            )
          ) ?: throw IllegalArgumentException()
        }
      }
    }
  }
  
  fun OptionDelegate<*>.channel(): OptionDelegate<ServerChannel> {
    require(guildOnly) { "This option can only be used in guilds!" }
    return OptionDelegate<ServerChannel>().apply {
      optionDescription = this@channel.optionDescription
      isOptional = this@channel.isOptional
      defaultValue = this@channel.defaultValue
      multipleValues = this@channel.multipleValues
      addValidator {
        event.server.get().let { server ->
          server.getChannelsByNameIgnoreCase(this).firstOrNull() ?: server.getChannelById(Utils.extractDigits(this)).orElse(null) ?: throw IllegalArgumentException()
        }
      }
    }
  }
  
  inline fun <reified R : ServerChannel> OptionDelegate<*>.channelType(): OptionDelegate<R> {
    require(guildOnly) { "This option can only be used in guilds!" }
    return OptionDelegate<R>().apply {
      optionDescription = this@channelType.optionDescription
      isOptional = this@channelType.isOptional
      defaultValue = this@channelType.defaultValue
      multipleValues = this@channelType.multipleValues
      addValidator {
        val channel = event.server.get().let { server ->
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
  
  fun OptionDelegate<*>.role(): OptionDelegate<Role> {
    require(guildOnly) { "This option can only be used in guilds!" }
    return OptionDelegate<Role>().apply {
      optionDescription = this@role.optionDescription
      isOptional = this@role.isOptional
      defaultValue = this@role.defaultValue
      multipleValues = this@role.multipleValues
      addValidator {
        event.server.get().let { server ->
          server.getRolesByNameIgnoreCase(this).firstOrNull() ?: server.getRoleById(Utils.extractDigits(this)).orElse(null) ?: throw IllegalArgumentException()
        }
      }
    }
  }
  
  fun OptionDelegate<*>.duration(): OptionDelegate<Duration> {
    return OptionDelegate<Duration>().apply {
      optionDescription = this@duration.optionDescription
      isOptional = this@duration.isOptional
      defaultValue = this@duration.defaultValue
      multipleValues = this@duration.multipleValues
      addValidator {
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
}