@file:Suppress("UNCHECKED_CAST", "unused")

package me.blast.command.argument.extensions

import me.blast.command.argument.Argument
import me.blast.command.argument.OptionalArg
import me.blast.utils.entity.Snowflake
import me.blast.utils.Utils
import me.blast.utils.Utils.hasValue
import me.blast.utils.extensions.throwUnless
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
import kotlin.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass

/**
 * Converts the argument value(s) to an integer.
 *
 * Use [ints] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable [Int] value.
 */
fun OptionalArg<*>.int(): Argument<Int?> {
  return (this as Argument<Int?>).apply {
    argumentValidator = { toInt() }
  }
}

/**
 * Converts the argument value(s) to an unsigned integer.
 *
 * Use [uInts] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable [UInt] value.
 */
fun OptionalArg<*>.uInt(): Argument<UInt?> {
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
fun OptionalArg<*>.long(): Argument<Long?> {
  return (this as Argument<Long?>).apply {
    argumentValidator = { toLong() }
  }
}

/**
 * Converts the argument value(s) to an unsigned long.
 *
 * Use [uLongs] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable [Long] value.
 */
fun OptionalArg<*>.uLong(): Argument<ULong?> {
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
fun OptionalArg<*>.float(): Argument<Float?> {
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
fun OptionalArg<*>.double(): Argument<Double?> {
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
fun OptionalArg<*>.user(searchMutualGuilds: Boolean = false): Argument<User?> {
  return (this as Argument<User?>).apply {
    argumentValidator = {
      argumentEvent.server.get().let { server ->
        if (contains("#")) {
          server.members.firstOrNull {
            it.idAsString == this ||
            it.getDisplayName(server).equals(this, true)
          }
        } else {
          server.members.firstOrNull {
            it.discriminatedName.equals(this, true) ||
            it.getDisplayName(server).equals(this, true)
          }
        }
      } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
        argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
          server.getMemberById(Utils.extractDigits(this)).getOrElse {
            if (contains("#")) {
              server.members.firstOrNull {
                it.idAsString == this ||
                it.getDisplayName(server).equals(this, true)
              }
            } else {
              server.members.firstOrNull {
                it.discriminatedName.equals(this, true) ||
                it.getDisplayName(server).equals(this, true)
              }
            }
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
fun OptionalArg<*>.channel(searchMutualGuilds: Boolean = false): Argument<ServerChannel?> {
  return (this as Argument<ServerChannel?>).apply {
    argumentValidator = {
      argumentEvent.server.get().channels.firstOrNull {
        it.idAsString == this ||
        it.name.equals(this, true)
      } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
        argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
          server.channels.firstOrNull {
            it.idAsString == this ||
            it.name.equals(this, true)
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
inline fun <reified R : ServerChannel> OptionalArg<*>.channel(vararg types: KClass<out R>, searchMutualGuilds: Boolean = false): Argument<R?> {
  return (this as Argument<R?>).apply {
    argumentValidator = {
      val channel = argumentEvent.server.get().channels.firstOrNull {
        it.idAsString == this ||
        it.name.equals(this, true)
      } ?: throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
        argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
          server.channels.firstOrNull {
            it.idAsString == this ||
            it.name.equals(this, true)
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
fun OptionalArg<*>.textChannel(searchMutualGuilds: Boolean = false): Argument<ServerTextChannel?> {
  return (this as Argument<ServerTextChannel?>).apply {
    argumentValidator = {
      argumentEvent.server.get().textChannels.firstOrNull {
        it.idAsString == this ||
        it.name.equals(this, true)
      } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
        argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
          server.textChannels.firstOrNull {
            it.idAsString == this ||
            it.name.equals(this, true)
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
fun OptionalArg<*>.voiceChannel(searchMutualGuilds: Boolean = false): Argument<ServerVoiceChannel?> {
  return (this as Argument<ServerVoiceChannel?>).apply {
    argumentValidator = {
      argumentEvent.server.get().voiceChannels.firstOrNull {
        it.idAsString == this ||
        it.name.equals(this, true)
      } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
        argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
          server.voiceChannels.firstOrNull {
            it.idAsString == this ||
            it.name.equals(this, true)
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
fun OptionalArg<*>.threadChannel(searchMutualGuilds: Boolean = false): Argument<ServerThreadChannel?> {
  return (this as Argument<ServerThreadChannel?>).apply {
    argumentValidator = {
      argumentEvent.server.get().threadChannels.firstOrNull {
        it.idAsString == this ||
        it.name.equals(this, true)
      } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
        argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
          server.threadChannels.firstOrNull {
            it.idAsString == this ||
            it.name.equals(this, true)
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
fun OptionalArg<*>.stageChannel(searchMutualGuilds: Boolean = false): Argument<ServerStageVoiceChannel?> {
  return (this as Argument<ServerStageVoiceChannel?>).apply {
    argumentValidator = {
      argumentEvent.server.get().channels.filter { it.asServerStageVoiceChannel().isPresent }.firstOrNull {
        it.idAsString == this ||
        it.name.equals(this, true)
      } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
        argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
          server.channels.filter { it.asServerStageVoiceChannel().isPresent }.firstOrNull {
            it.idAsString == this ||
            it.name.equals(this, true)
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
fun OptionalArg<*>.forumChannel(searchMutualGuilds: Boolean = false): Argument<ServerForumChannel?> {
  return (this as Argument<ServerForumChannel?>).apply {
    argumentValidator = {
      argumentEvent.server.get().forumChannels.firstOrNull {
        it.idAsString == this ||
        it.name.equals(this, true)
      } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
        argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
          server.forumChannels.firstOrNull {
            it.idAsString == this ||
            it.name.equals(this, true)
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
fun OptionalArg<*>.category(searchMutualGuilds: Boolean = false): Argument<ChannelCategory?> {
  return (this as Argument<ChannelCategory?>).apply {
    argumentValidator = {
      argumentEvent.server.get().channelCategories.firstOrNull {
        it.idAsString == this ||
        it.name.equals(this, true)
      } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
        argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
          server.channelCategories.firstOrNull {
            it.idAsString == this ||
            it.name.equals(this, true)
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
fun OptionalArg<*>.role(searchMutualGuilds: Boolean = false): Argument<Role?> {
  return (this as Argument<Role?>).apply {
    argumentValidator = {
      argumentEvent.server.get().roles.firstOrNull {
        it.idAsString == this ||
        it.name.equals(this, true)
      } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
        argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
          server.roles.firstOrNull {
            it.idAsString == this ||
            it.name.equals(this, true)
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
fun OptionalArg<*>.message(searchMutualGuilds: Boolean = false, includePrivateChannels: Boolean = false): Argument<Message?> {
  return (this as Argument<Message?>).apply {
    argumentValidator = {
      val matchResult = DiscordRegexPattern.MESSAGE_LINK.toRegex().matchEntire(this)
      if (matchResult == null) {
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
fun OptionalArg<*>.customEmoji(searchMutualGuilds: Boolean = false): Argument<CustomEmoji?> {
  return (this as Argument<CustomEmoji?>).apply {
    argumentValidator = {
      val matchResult = DiscordRegexPattern.CUSTOM_EMOJI.toRegex().matchEntire(this) ?: throw IllegalArgumentException()
      argumentEvent.server.get().getCustomEmojiById(matchResult.groups["id"]!!.value).getOrNull() ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
        argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
          server.getCustomEmojiById(matchResult.groups["id"]!!.value).get()
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
fun OptionalArg<*>.snowflake(): Argument<Snowflake?> {
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
fun OptionalArg<*>.url(): Argument<URL?> {
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
fun OptionalArg<*>.duration(): Argument<Duration?> {
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
fun OptionalArg<*>.date(locale: Locale = Locale.ENGLISH): Argument<LocalDate?> {
  return (this as Argument<LocalDate?>).apply {
    argumentValidator = {
      Utils.parseDate(this, locale)?.toLocalDate() ?: throw IllegalArgumentException()
    }
  }
}

/**
 * Converts the argument value(s) to a [LocalDateTime].
 *
 * Use [dateTimes] to convert each value separately.
 *
 * @param locale The locale used for date parsing. Defaults to [Locale.ENGLISH].
 * @return An Argument containing the retrieved nullable [LocalDateTime] value.
 */
fun OptionalArg<*>.dateTime(locale: Locale = Locale.ENGLISH): Argument<LocalDateTime?> {
  return (this as Argument<LocalDateTime?>).apply {
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
fun OptionalArg<*>.color(): Argument<Color?> {
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
fun OptionalArg<*>.unicodeEmoji(): Argument<Emoji?> {
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
inline fun <reified T : Enum<T>> OptionalArg<*>.enum(): Argument<T?> {
  return (this as Argument<T?>).apply {
    argumentValidator = {
      enumValueOf<T>(uppercase().replace(" ", "_"))
    }
  }
}

/**
 * Maps argument value(s) to corresponding [T] value(s) from the given map.
 *
 * Use [maps] to convert each value separately.
 *
 * @param ignoreCase Whether to convert the provided value(s) to lowercase before retrieving.
 * @return An Argument containing the retrieved nullable [T] value.
 */
fun <T> OptionalArg<*>.map(values: Map<String, T>, ignoreCase: Boolean = false): Argument<T?> {
  return (this as Argument<T?>).apply {
    argumentValidator = {
      values[if(ignoreCase) lowercase() else this]!!
    }
  }
}

/**
 * Maps argument value(s) to corresponding [T] value(s) from the given array of pairs.
 *
 * Use [maps] to convert each value separately.
 *
 * @param ignoreCase Whether to convert the provided value(s) to lowercase before retrieving.
 * @return An Argument containing the retrieved nullable [T] value.
 */
fun <T> OptionalArg<*>.map(vararg values: Pair<String, T>, ignoreCase: Boolean = false): Argument<T?> {
  return (this as Argument<T?>).apply {
    argumentValidator = {
      mapOf(*values)[if(ignoreCase) lowercase() else this]!!
    }
  }
}