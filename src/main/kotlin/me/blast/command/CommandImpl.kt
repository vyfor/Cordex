package me.blast.command

import me.blast.utils.Utils
import org.javacord.api.entity.channel.*
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.user.User
import org.javacord.api.event.message.MessageCreateEvent
import java.net.URL
import java.time.Duration
import java.util.*
import kotlin.reflect.KProperty

abstract class CommandImpl {
  @PublishedApi
  internal var event: MessageCreateEvent? = null
  val options = LinkedList<Delegate<*>>()
  val args = LinkedList<String>()
  
  abstract inner class Delegate<T> {
    lateinit var name: String
    lateinit var short: String
    var optionDescription: String = "No description provided."
    var value: Any? = null
    var validator: (String.() -> T)? = null
    var listValidator: (List<String>.() -> Any?)? = null
    var isOptional: Boolean = false
    var defaultValue: Any? = null
    var multipleValues: Int = 1
    
    fun validate(input: String) {
      value = validator?.invoke(input) ?: input
    }
    
    fun validate(input: List<String>) {
      value = listValidator?.invoke(input) ?: input
    }
    
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
      return if (this is FlagDelegate) {
        if (value == null) false as T
        else value as T
      } else (value as? T) ?: defaultValue as T
    }
    
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): Delegate<T> {
      name = Utils.convertCamelToKebab(property.name)
      short = name.substring(0, 1)
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
      
      fun <R : Any> multiple(count: Int = 0, validator: List<String>.() -> R): OptionalOptionDelegate<List<R>, R> {
        return OptionalOptionDelegate<List<R>, R>().apply {
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
    
    fun <R : Any> multiple(count: Int = 0, validator: List<String>.() -> R): MultipleOptionDelegate<List<R>, R> {
      return MultipleOptionDelegate<List<R>, R>().apply {
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
    inner class OptionalPositionalDelegate<T: Any?, C> : Delegate<T>() {
      fun multiple(count: Int = 0): OptionalPositionalDelegate<List<C>, C> {
        return OptionalPositionalDelegate<List<C>, C>().apply {
          isOptional = this@PositionalDelegate.isOptional
          defaultValue = this@PositionalDelegate.defaultValue
          multipleValues = count
          optionDescription = this@PositionalDelegate.optionDescription
        }
      }
      
      fun <R: Any> multiple(count: Int = 0, validator: List<String>.() -> R): OptionalPositionalDelegate<List<R>, R> {
        return OptionalPositionalDelegate<List<R>, R>().apply {
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
    
    inner class MultiplePositionalDelegate<T: Any?, C> : Delegate<T>() {
      fun optional(): MultiplePositionalDelegate<T, T> {
        return MultiplePositionalDelegate<T, T>().apply {
          isOptional = true
          multipleValues = this@PositionalDelegate.multipleValues
          optionDescription = this@PositionalDelegate.optionDescription
        }
      }
      
      fun <R: Any> optional(default: R): MultiplePositionalDelegate<R, R> {
        return MultiplePositionalDelegate<R, R>().apply {
          isOptional = true
          defaultValue = default
          multipleValues = this@PositionalDelegate.multipleValues
          optionDescription = this@PositionalDelegate.optionDescription
        }
      }
      
      fun <R: Any> optional(validator: String.() -> R): MultiplePositionalDelegate<R?, R> {
        return MultiplePositionalDelegate<R?, R>().apply {
          isOptional = true
          multipleValues = this@PositionalDelegate.multipleValues
          optionDescription = this@PositionalDelegate.optionDescription
          addValidator(validator)
        }
      }
      
      fun <R: Any> optional(default: R, validator: String.() -> R): MultiplePositionalDelegate<R, R> {
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
    
    fun <R: Any> optional(default: R): OptionalPositionalDelegate<R, R> {
      return OptionalPositionalDelegate<R, R>().apply {
        isOptional = true
        defaultValue = default
        multipleValues = this@PositionalDelegate.multipleValues
        optionDescription = this@PositionalDelegate.optionDescription
      }
    }
    
    fun <R: Any> optional(validator: String.() -> R): OptionalPositionalDelegate<R?, R> {
      return OptionalPositionalDelegate<R?, R>().apply {
        isOptional = true
        multipleValues = this@PositionalDelegate.multipleValues
        optionDescription = this@PositionalDelegate.optionDescription
        addValidator(validator)
      }
    }
    
    fun <R: Any> optional(default: R, validator: String.() -> R): OptionalPositionalDelegate<R, R> {
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
    
    fun <R: Any> multiple(count: Int = 0, validator: List<String>.() -> R): MultiplePositionalDelegate<List<R>, R> {
      return MultiplePositionalDelegate<List<R>, R>().apply {
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
      if(description != null) optionDescription = description
      if(fullName != null) name = fullName
      if(shortName != null) short = shortName
    }
  }
  
  inline fun <reified T> option(description: String? = null, fullName: String? = null, shortName: String? = null, noinline validator: String.() -> T): OptionDelegate<T> {
    return OptionDelegate<T>().addValidator(validator).apply {
      if(description != null) optionDescription = description
      if(fullName != null) name = fullName
      if(shortName != null) short = shortName
    }
  }
  
  fun flag(description: String? = null, fullName: String? = null, shortName: String? = null): Delegate<Boolean> {
    return FlagDelegate().apply {
      if(description != null) optionDescription = description
      if(fullName != null) name = fullName
      if(shortName != null) short = shortName
    }
  }
  
  fun positional(description: String? = null, fullName: String? = null, shortName: String? = null): PositionalDelegate<String> {
    return PositionalDelegate<String>().apply {
      if(description != null) optionDescription = description
      if(fullName != null) name = fullName
      if(shortName != null) short = shortName
    }
  }
  
  inline fun <reified T> positional(description: String? = null, fullName: String? = null, shortName: String? = null, noinline validator: String.() -> T): PositionalDelegate<T> {
    return PositionalDelegate<T>().addValidator(validator).apply {
      if(description != null) optionDescription = description
      if(fullName != null) name = fullName
      if(shortName != null) short = shortName
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
    return OptionDelegate<User>().apply {
      require(event!!.server.isPresent)
      optionDescription = this@user.optionDescription
      isOptional = this@user.isOptional
      defaultValue = this@user.defaultValue
      multipleValues = this@user.multipleValues
      addValidator {
        event!!.server.get().let { server ->
          server.getMembersByDisplayNameIgnoreCase(this).firstOrNull() ?:
          server.getMemberByDiscriminatedName(this).orElse(
            server.getMemberById(Utils.extractDigits(this)).orElse(
              server.getMembersByNameIgnoreCase(this).firstOrNull())) ?: throw IllegalArgumentException()
        }
      }
    }
  }
  
  fun OptionDelegate<*>.channel(): OptionDelegate<ServerChannel> {
    return OptionDelegate<ServerChannel>().apply {
      require(event!!.server.isPresent)
      optionDescription = this@channel.optionDescription
      isOptional = this@channel.isOptional
      defaultValue = this@channel.defaultValue
      multipleValues = this@channel.multipleValues
      addValidator {
        event!!.server.get().let { server ->
          server.getChannelsByNameIgnoreCase(this).firstOrNull() ?:
          server.getChannelById(Utils.extractDigits(this)).orElse(null) ?: throw IllegalArgumentException()
        }
      }
    }
  }
  
  inline fun <reified R: ServerChannel> OptionDelegate<*>.channelType(): OptionDelegate<R> {
    return OptionDelegate<R>().apply {
      optionDescription = this@channelType.optionDescription
      isOptional = this@channelType.isOptional
      defaultValue = this@channelType.defaultValue
      multipleValues = this@channelType.multipleValues
      addValidator {
        val channel = event!!.server.get().let { server ->
          server.getChannelsByNameIgnoreCase(this).firstOrNull() ?:
          server.getChannelById(Utils.extractDigits(this)).orElse(null) ?: throw IllegalArgumentException()
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
    return OptionDelegate<Role>().apply {
      require(event!!.server.isPresent)
      optionDescription = this@role.optionDescription
      isOptional = this@role.isOptional
      defaultValue = this@role.defaultValue
      multipleValues = this@role.multipleValues
      addValidator {
        event!!.server.get().let { server ->
          server.getRolesByNameIgnoreCase(this).firstOrNull() ?:
          server.getRoleById(Utils.extractDigits(this)).orElse(null) ?: throw IllegalArgumentException()
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