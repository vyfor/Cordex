@file:Suppress("UNCHECKED_CAST", "unused")

package me.blast.command.argument.extensions

import me.blast.command.argument.Argument
import me.blast.command.argument.OptionalMultiValueArgument
import me.blast.utils.Snowflake
import me.blast.utils.Utils
import me.blast.utils.Utils.hasValue
import me.blast.utils.throwUnless
import net.fellbaum.jemoji.Emoji
import net.fellbaum.jemoji.EmojiManager
import org.javacord.api.entity.channel.*
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
 * Use [ints] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable [Int] value.
 */
fun OptionalMultiValueArgument<*>.int(): Argument<Int?> {
  return (this as Argument<Int?>).apply {
    argumentValidator = { toInt() }
  }
}

/**
 * Converts the argument value(s) to an unsigned integer.
 *
 * Use [uInts] to convert each value separately.
 * @return An Argument containing the retrieved nullable [UInt] value.
 */
fun OptionalMultiValueArgument<*>.uInt(): Argument<UInt?> {
  return (this as Argument<UInt?>).apply {
    argumentValidator = { toUInt() }
  }
}

/**
 * Converts the argument value(s) to a long.
 *
 * Use [longs] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable [Long] value.
 */
fun OptionalMultiValueArgument<*>.long(): Argument<Long?> {
  return (this as Argument<Long?>).apply {
    argumentValidator = { toLong() }
  }
}

/**
 * Converts the argument value(s) to an unsigned long.
 *
 * Use [uLongs] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable [ULong] value.
 */
fun OptionalMultiValueArgument<*>.uLong(): Argument<ULong?> {
  return (this as Argument<ULong?>).apply {
    argumentValidator = { toULong() }
  }
}

/**
 * Converts the argument value(s) to a float.
 *
 * Use [floats] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable [Float] value.
 */
fun OptionalMultiValueArgument<*>.float(): Argument<Float?> {
  return (this as Argument<Float?>).apply {
    argumentValidator = { toFloat() }
  }
}

/**
 * Converts the argument value(s) to a double.
 *
 * Use [doubles] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable [Double] value.
 */
fun OptionalMultiValueArgument<*>.double(): Argument<Double?> {
  return (this as Argument<Double?>).apply {
    argumentValidator = { toDouble() }
  }
}

/**
 * Retrieves a [User] based on the argument value(s).
 *
 * Use [users] to convert each value separately.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing the retrieved nullable [User] value.
 */
fun OptionalMultiValueArgument<*>.user(searchMutualGuilds: Boolean = false): Argument<User?> {
  return (this as Argument<User?>).apply {
    argumentValidator = {
      if (guildOnly) {
        argumentEvent.server.get().let { server ->
          server.getMemberById(Utils.extractDigits(this)).orElse(
            server.getMembersByDisplayNameIgnoreCase(this).firstOrNull() ?: server.getMembersByNameIgnoreCase(this).firstOrNull() ?: server.getMembersByNameIgnoreCase(this).firstOrNull() ?: server.getMemberByDiscriminatedName(this).orElse(
              server.getMembersByNameIgnoreCase(this).first()
            )
          )
        }
      } else {
        throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.getMemberById(Utils.extractDigits(this)).orElse(
              server.getMembersByDisplayNameIgnoreCase(this).firstOrNull() ?: server.getMembersByNameIgnoreCase(this).firstOrNull() ?: server.getMembersByNameIgnoreCase(this).firstOrNull() ?: server.getMemberByDiscriminatedName(this).orElse(
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
fun OptionalMultiValueArgument<*>.channel(searchMutualGuilds: Boolean = false): Argument<ServerChannel?> {
  return (this as Argument<ServerChannel?>).apply {
    argumentValidator = {
      if (guildOnly) {
        argumentEvent.server.get().let { server ->
          server.getChannelById(Utils.extractDigits(this)).orElse(null) ?: server.getChannelsByNameIgnoreCase(this).first()
        }
      } else {
        throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.getChannelById(Utils.extractDigits(this)).orElse(null) ?: server.getChannelsByNameIgnoreCase(this).firstOrNull()
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
inline fun <reified R : ServerChannel> OptionalMultiValueArgument<*>.channel(vararg types: KClass<out R>, searchMutualGuilds: Boolean = false): Argument<R?> {
  return (this as Argument<R?>).apply {
    argumentValidator = {
      val channel = if (guildOnly) {
        argumentEvent.server.get().let { server ->
          server.getChannelById(Utils.extractDigits(this)).orElse(null) ?: server.getChannelsByNameIgnoreCase(this).first()
        }
      } else {
        throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.getChannelById(Utils.extractDigits(this)).orElse(null) ?: server.getChannelsByNameIgnoreCase(this).firstOrNull()
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
 * Retrieves a [ServerTextChannel] based on the argument value(s).
 *
 * Use [textChannels] to convert each value separately.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing the retrieved nullable [ServerTextChannel] value.
 */
fun OptionalMultiValueArgument<*>.textChannel(searchMutualGuilds: Boolean = false): Argument<ServerTextChannel?> {
  return (this as Argument<ServerTextChannel?>).apply {
    argumentValidator = {
      if (guildOnly) {
        argumentEvent.server.get().let { server ->
          server.getTextChannelById(Utils.extractDigits(this)).orElse(null) ?: server.getTextChannelsByNameIgnoreCase(this).first()
        }
      } else {
        throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.getTextChannelById(Utils.extractDigits(this)).orElse(null) ?: server.getTextChannelsByNameIgnoreCase(this).firstOrNull()
          }
        }
      }
    }
  }
}

/**
 * Retrieves a [ServerVoiceChannel] based on the argument value(s).
 *
 * Use [voiceChannels] to convert each value separately.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing the retrieved nullable [ServerVoiceChannel] value.
 */
fun OptionalMultiValueArgument<*>.voiceChannel(searchMutualGuilds: Boolean = false): Argument<ServerVoiceChannel?> {
  return (this as Argument<ServerVoiceChannel?>).apply {
    argumentValidator = {
      if (guildOnly) {
        argumentEvent.server.get().let { server ->
          server.getVoiceChannelById(Utils.extractDigits(this)).orElse(null) ?: server.getVoiceChannelsByNameIgnoreCase(this).first()
        }
      } else {
        throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.getVoiceChannelById(Utils.extractDigits(this)).orElse(null) ?: server.getVoiceChannelsByNameIgnoreCase(this).firstOrNull()
          }
        }
      }
    }
  }
}

/**
 * Retrieves a [ServerThreadChannel] based on the argument value(s).
 *
 * Use [threadChannels] to convert each value separately.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing the retrieved nullable [ServerThreadChannel] value.
 */
fun OptionalMultiValueArgument<*>.threadChannel(searchMutualGuilds: Boolean = false): Argument<ServerThreadChannel?> {
  return (this as Argument<ServerThreadChannel?>).apply {
    argumentValidator = {
      if (guildOnly) {
        argumentEvent.server.get().getThreadChannelById(Utils.extractDigits(this)).get()
      } else {
        throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.getThreadChannelById(Utils.extractDigits(this)).orElse(null)
          }
        }
      }
    }
  }
}

/**
 * Retrieves a [ServerStageVoiceChannel] based on the argument value(s).
 *
 * Use [stageChannels] to convert each value separately.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing the retrieved nullable [ServerStageVoiceChannel] value.
 */
fun OptionalMultiValueArgument<*>.stageChannel(searchMutualGuilds: Boolean = false): Argument<ServerStageVoiceChannel?> {
  return (this as Argument<ServerStageVoiceChannel?>).apply {
    argumentValidator = {
      if (guildOnly) {
        argumentEvent.server.get().getStageVoiceChannelById(Utils.extractDigits(this)).get()
      } else {
        throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.getStageVoiceChannelById(Utils.extractDigits(this)).orElse(null)
          }
        }
      }
    }
  }
}

/**
 * Retrieves a [ServerForumChannel] based on the argument value(s).
 *
 * Use [forumChannels] to convert each value separately.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing the retrieved nullable [ServerForumChannel] value.
 */
fun OptionalMultiValueArgument<*>.forumChannel(searchMutualGuilds: Boolean = false): Argument<ServerForumChannel?> {
  return (this as Argument<ServerForumChannel?>).apply {
    argumentValidator = {
      if (guildOnly) {
        argumentEvent.server.get().let { server ->
          server.getForumChannelById(Utils.extractDigits(this)).orElse(null) ?: server.getForumChannelsByNameIgnoreCase(this).first()
        }
      } else {
        throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.getForumChannelById(Utils.extractDigits(this)).orElse(null) ?: server.getForumChannelsByNameIgnoreCase(this).firstOrNull()
          }
        }
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
fun OptionalMultiValueArgument<*>.category(searchMutualGuilds: Boolean = false): Argument<ChannelCategory?> {
  return (this as Argument<ChannelCategory?>).apply {
    argumentValidator = {
      if (guildOnly) {
        argumentEvent.server.get().let { server ->
          server.getChannelCategoryById(this).orElse(null) ?: server.getChannelCategoriesByNameIgnoreCase(this).first()
        }
      } else {
        throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.getChannelCategoryById(this).orElse(null) ?: server.getChannelCategoriesByNameIgnoreCase(this).firstOrNull()
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
fun OptionalMultiValueArgument<*>.role(searchMutualGuilds: Boolean = false): Argument<Role?> {
  return (this as Argument<Role?>).apply {
    argumentValidator = {
      if (guildOnly) {
        argumentEvent.server.get().let { server ->
          server.getRoleById(Utils.extractDigits(this)).orElse(null) ?: server.getRolesByNameIgnoreCase(this).first()
        }
      } else {
        throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.getRoleById(Utils.extractDigits(this)).orElse(null) ?: server.getRolesByNameIgnoreCase(this).firstOrNull()
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
fun OptionalMultiValueArgument<*>.message(searchMutualGuilds: Boolean = false, includePrivateChannels: Boolean = false): Argument<Message?> {
  return (this as Argument<Message?>).apply {
    argumentValidator = {
      val matchResult = DiscordRegexPattern.MESSAGE_LINK.toRegex().matchEntire(this)
      if(matchResult == null) {
        argumentEvent.channel.getMessageById(this).get()
      } else {
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
}

/**
 * Retrieves a [CustomEmoji] based on the argument value(s).
 *
 * Use [customEmojis] to convert each value separately.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing the retrieved nullable [CustomEmoji] value.
 */
fun OptionalMultiValueArgument<*>.customEmoji(searchMutualGuilds: Boolean = false): Argument<CustomEmoji?> {
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
 * Converts the argument value(s) to a [Snowflake].
 *
 * Use [snowflakes] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable [Snowflake] value.
 */
fun OptionalMultiValueArgument<*>.snowflake(): Argument<Snowflake?> {
  return (this as Argument<Snowflake?>).apply {
    argumentValidator = {
      Snowflake(toLong().takeIf { it > 0 }!!)
    }
  }
}

/**
 * Converts the argument value(s) to a [URL].
 *
 * Use [urls] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable [URL] value.
 */
fun OptionalMultiValueArgument<*>.url(): Argument<URL?> {
  return (this as Argument<URL?>).apply {
    argumentValidator = { URL(this) }
  }
}

/**
 * Converts the argument value(s) to a [Duration].
 *
 * Use [durations] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable [Duration] value.
 */
fun OptionalMultiValueArgument<*>.duration(): Argument<Duration?> {
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
fun OptionalMultiValueArgument<*>.date(locale: Locale = Locale.ENGLISH): Argument<LocalDate?> {
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
fun OptionalMultiValueArgument<*>.color(): Argument<Color?> {
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
fun OptionalMultiValueArgument<*>.unicodeEmoji(): Argument<Emoji?> {
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
inline fun <reified T : Enum<T>> OptionalMultiValueArgument<*>.enum(): Argument<T?> {
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