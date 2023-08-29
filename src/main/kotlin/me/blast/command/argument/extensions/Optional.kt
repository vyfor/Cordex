@file:Suppress("UNCHECKED_CAST", "unused")

package me.blast.command.argument.extensions

import me.blast.command.argument.OptionalArgument
import me.blast.command.argument.Argument
import me.blast.utils.Utils
import me.blast.utils.Utils.hasValue
import me.blast.utils.throwUnless
import net.fellbaum.jemoji.Emoji
import net.fellbaum.jemoji.EmojiManager
import org.javacord.api.entity.channel.ChannelCategory
import org.javacord.api.entity.channel.ServerChannel
import org.javacord.api.entity.emoji.CustomEmoji
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.user.User
import org.javacord.api.util.DiscordRegexPattern
import java.awt.Color
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.util.*
import kotlin.reflect.KClass

/**
 * Converts the argument value(s) to an integer.
 *
 * @return An Argument containing the retrieved nullable [Int] value.
 */
fun OptionalArgument<*>.int(): Argument<Int?> {
  return (this as Argument<Int?>).apply {
    argumentValidator = {
      toInt()
    }
  }
}

/**
 * Converts the argument value(s) to a long.
 *
 * @return An Argument containing the retrieved nullable [Long] value.
 */
fun OptionalArgument<*>.long(): Argument<Long?> {
  return (this as Argument<Long?>).apply {
    argumentValidator = { toLong() }
  }
}

/**
 * Converts the argument value(s) to a float.
 *
 * @return An Argument containing the retrieved nullable [Float] value.
 */
fun OptionalArgument<*>.float(): Argument<Float?> {
  return (this as Argument<Float?>).apply {
    argumentValidator = { toFloat() }
  }
}

/**
 * Converts the argument value(s) to a double.
 *
 * @return An Argument containing the retrieved nullable [Double] value.
 */
fun OptionalArgument<*>.double(): Argument<Double?> {
  return (this as Argument<Double?>).apply {
    argumentValidator = { toDouble() }
  }
}

/**
 * Converts the argument value(s) to a [URL].
 *
 * @return An Argument containing the retrieved nullable [URL] value.
 */
fun OptionalArgument<*>.url(): Argument<URL?> {
  return (this as Argument<URL?>).apply {
    argumentValidator = { URL(this) }
  }
}

/**
 * Retrieves a [User] based on the argument value(s).
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing the retrieved nullable [User] value.
 */
fun OptionalArgument<*>.user(searchMutualGuilds: Boolean = false): Argument<User?> {
  return (this as Argument<User?>).apply {
    argumentValidator = {
      if (guildOnly) {
        argumentEvent.server.get().let { server ->
          server.getMembersByDisplayNameIgnoreCase(this).firstOrNull() ?: server.getMemberByDiscriminatedName(this).orElse(
            server.getMemberById(Utils.extractDigits(this)).orElse(
              server.getMembersByNameIgnoreCase(this).first()
            )
          )
        }
      } else {
        throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.getMembersByDisplayNameIgnoreCase(this).firstOrNull() ?: server.getMemberByDiscriminatedName(this).orElse(
              server.getMemberById(Utils.extractDigits(this)).orElse(
                server.getMembersByNameIgnoreCase(this).firstOrNull()
              )
            )
          }
        }
      }
    }
  }
}

/**
 * Retrieves a [ServerChannel] based on the argument value(s).
 *
 * Use [channels] to convert each value separately.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing the retrieved nullable [ServerChannel] value.
 */
fun OptionalArgument<*>.channel(searchMutualGuilds: Boolean = false): Argument<ServerChannel?> {
  return (this as Argument<ServerChannel?>).apply {
    argumentValidator = {
      if (guildOnly) {
        argumentEvent.server.get().let { server ->
          server.getChannelsByNameIgnoreCase(this).firstOrNull() ?: server.getChannelById(Utils.extractDigits(this)).get()
        }
      } else {
        throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.getChannelsByNameIgnoreCase(this).firstOrNull() ?: server.getChannelById(Utils.extractDigits(this)).orElse(null)
          }
        }
      }
    }
  }
}

/**
 * Retrieves a [ServerChannel] of one of the specified `types` based on the argument value(s).
 *
 * Use [channels] to convert each value separately.
 *
 * @param types types An array of classes extending [ServerChannel] representing the supported channel types.
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing the retrieved nullable [ServerChannel] value.
 */
inline fun <reified R : ServerChannel> OptionalArgument<*>.channel(vararg types: KClass<out R>, searchMutualGuilds: Boolean = false): Argument<R?> {
  return (this as Argument<R?>).apply {
    argumentValidator = {
      val channel = if (guildOnly) {
        argumentEvent.server.get().let { server ->
          server.getChannelsByNameIgnoreCase(this).firstOrNull() ?: server.getChannelById(Utils.extractDigits(this)).get()
        }
      } else {
        throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.getChannelsByNameIgnoreCase(this).firstOrNull() ?: server.getChannelById(Utils.extractDigits(this)).orElse(null)
          }
        }
      }
      if (types.any { it.isInstance(channel) }) {
        channel as R
      } else if (R::class.isInstance(channel)) {
        channel as R
      } else {
        throw IllegalArgumentException()
      }
    }
  }
}

/**
 * Retrieves a [ChannelCategory] based on the argument value(s).
 *
 * Use [categories] to convert each value separately.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing the retrieved nullable [ChannelCategory] value.
 */
fun OptionalArgument<*>.category(searchMutualGuilds: Boolean = false): Argument<ChannelCategory?> {
  return (this as Argument<ChannelCategory?>).apply {
    argumentValidator = {
      if (guildOnly) {
        argumentEvent.server.get().let { server ->
          server.getChannelCategoriesByNameIgnoreCase(this).firstOrNull() ?: server.getChannelCategoryById(Utils.extractDigits(this)).get()
        }
      } else {
        throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.getChannelCategoriesByNameIgnoreCase(this).firstOrNull() ?: server.getChannelCategoryById(Utils.extractDigits(this)).orElse(null)
          }
        }
      }
    }
  }
}

/**
 * Retrieves a [Role] based on the argument value(s).
 *
 * Use [roles] to convert each value separately.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing the retrieved nullable [Role] value.
 */
fun OptionalArgument<*>.role(searchMutualGuilds: Boolean = false): Argument<Role?> {
  return (this as Argument<Role?>).apply {
    argumentValidator = {
      if (guildOnly) {
        argumentEvent.server.get().let { server ->
          server.getRolesByNameIgnoreCase(this).firstOrNull() ?: server.getRoleById(Utils.extractDigits(this)).get()
        }
      } else {
        throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.getRolesByNameIgnoreCase(this).firstOrNull() ?: server.getRoleById(Utils.extractDigits(this)).orElse(null)
          }
        }
      }
    }
  }
}

/**
 * Retrieves a [Message] based on the argument value(s).
 *
 * Use [messages] to convert each value separately.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @param includePrivateChannels Whether to include messages in the private channel between the user and the bot in search. Defaults to false.
 * @return An Argument containing the retrieved nullable [Message] value.
 */
fun OptionalArgument<*>.message(searchMutualGuilds: Boolean = false, includePrivateChannels: Boolean = false): Argument<Message?> {
  return (this as Argument<Message?>).apply {
    argumentValidator = {
      val matchResult = DiscordRegexPattern.MESSAGE_LINK.toRegex().matchEntire(this) ?: throw IllegalArgumentException()
      if (matchResult.groups["server"] == null) {
        require(includePrivateChannels)
        argumentEvent.messageAuthor.asUser().get().openPrivateChannel().get()
          .getMessageById(matchResult.groups["message"]!!.value).get()
      } else {
        try {
          argumentEvent.server.get().getTextChannelById(matchResult.groups["channel"]!!.value).get().takeIf { it.canSee(argumentEvent.messageAuthor.asUser().get()) }!!
            .getMessageById(matchResult.groups["message"]!!.value).get()
        } catch (_: NullPointerException) {
          throw IllegalAccessException()
        } catch (_: Exception) {
          throwUnless(searchMutualGuilds) {
            argumentEvent.messageAuthor.asUser().get().mutualServers.find { it.idAsString == matchResult.groups["server"]!!.value }!!
              .getTextChannelById(matchResult.groups["channel"]!!.value).get().takeIf { it.canSee(argumentEvent.messageAuthor.asUser().get()) }!!
              .getMessageById(matchResult.groups["message"]!!.value).get()
          }
        }
      }
    }
  }
}

/**
 * Retrieves a [CustomEmoji] based on the argument value(s).
 *
 * Use [customEmojis] to convert each value separately.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing the retrieved nullable [CustomEmoji] value.
 */
fun OptionalArgument<*>.customEmoji(searchMutualGuilds: Boolean = false): Argument<CustomEmoji?> {
  return (this as Argument<CustomEmoji?>).apply {
    argumentValidator = {
      val matchResult = DiscordRegexPattern.CUSTOM_EMOJI.toRegex().matchEntire(this) ?: throw IllegalArgumentException()
      if (guildOnly) {
        argumentEvent.server.get().getCustomEmojiById(matchResult.groups["id"]!!.value).get()
      } else {
        throwUnless(searchMutualGuilds) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf {
            it.getCustomEmojiById(matchResult.groups["id"]!!.value).get()
          }
        }
      }
    }
  }
}

/**
 * Converts the argument value(s) to a [Duration].
 *
 * Use [durations] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable [Duration] value.
 */
fun OptionalArgument<*>.duration(): Argument<Duration?> {
  return (this as Argument<Duration?>).apply {
    argumentValidator = {
      Utils.parseDuration(this) ?: throw IllegalArgumentException()
    }
  }
}

/**
 * Converts the argument value(s) to a [LocalDate].
 *
 * Use [dates] to convert each value separately.
 *
 * @param locale The locale used for date parsing. Defaults to [Locale.ENGLISH].
 * @return An Argument containing the retrieved nullable [LocalDate] value.
 */
fun OptionalArgument<*>.date(locale: Locale = Locale.ENGLISH): Argument<LocalDate?> {
  return (this as Argument<LocalDate?>).apply {
    argumentValidator = {
      Utils.parseDate(this, locale) ?: throw IllegalArgumentException()
    }
  }
}

/**
 * Converts the argument value(s) to a [Color].
 *
 * Use [colors] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable [Color] value.
 */
fun OptionalArgument<*>.color(): Argument<Color?> {
  return (this as Argument<Color?>).apply {
    argumentValidator = {
      Color::class.java.getField(this)[null] as? Color ?: Color.decode(this)
    }
  }
}

/**
 * Converts the argument value(s) to an [Emoji].
 *
 * Use [unicodeEmojis] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable [Emoji] value.
 */
fun OptionalArgument<*>.unicodeEmoji(): Argument<Emoji?> {
  return (this as Argument<Emoji?>).apply {
    argumentValidator = {
      EmojiManager.getEmoji(this).get()
    }
  }
}

/**
 * Converts the argument value(s) to the value of the given enum class.
 *
 * Use [enums] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable enum value.
 */
inline fun <reified T : Enum<T>> OptionalArgument<*>.enum(): Argument<T?> {
  return (this as Argument<T?>).apply {
    argumentValidator = {
      try {
        enumValueOf<T>(uppercase().replace(" ", "_"))
      } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException()
      }
    }
  }
}