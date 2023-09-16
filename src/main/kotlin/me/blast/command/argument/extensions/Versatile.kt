@file:Suppress("UNCHECKED_CAST", "unused")

package me.blast.command.argument.extensions

import me.blast.command.argument.Argument
import me.blast.command.argument.FinalizedArg
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
fun FinalizedArg<*>.int(): Argument<Int?> {
  return (this as Argument<Int?>).apply {
    argumentListValidator = {
      map {
        it.toInt()
      }
    }
  }
}

/**
 * Converts the argument value(s) to an unsigned integer.
 *
 * Use [uInts] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable [UInt] value.
 */
fun FinalizedArg<*>.uInt(): Argument<UInt?> {
  return (this as Argument<UInt?>).apply {
    argumentListValidator = {
      map {
        it.toUInt()
      }
    }
  }
}

/**
 * Converts the argument value(s) to a long.
 *
 * Use [longs] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable [Long] value.
 */
fun FinalizedArg<*>.long(): Argument<Long?> {
  return (this as Argument<Long?>).apply {
    argumentListValidator = {
      map {
        it.toLong()
      }
    }
  }
}

/**
 * Converts the argument value(s) to an unsigned long.
 *
 * Use [uLongs] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable [Long] value.
 */
fun FinalizedArg<*>.uLong(): Argument<ULong?> {
  return (this as Argument<ULong?>).apply {
    argumentListValidator = {
      map {
        it.toULong()
      }
    }
  }
}

/**
 * Converts the argument value(s) to a float.
 *
 * Use [floats] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable [Float] value.
 */
fun FinalizedArg<*>.float(): Argument<Float?> {
  return (this as Argument<Float?>).apply {
    argumentListValidator = {
      map {
        it.toFloat()
      }
    }
  }
}

/**
 * Converts the argument value(s) to a double.
 *
 * Use [doubles] to convert each value separately.
 *
 * @return An Argument containing the retrieved nullable [Double] value.
 */
fun FinalizedArg<*>.double(): Argument<Double?> {
  return (this as Argument<Double?>).apply {
    argumentListValidator = {
      map {
        it.toDouble()
      }
    }
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
fun FinalizedArg<*>.user(searchMutualGuilds: Boolean = false): Argument<User?> {
  return (this as Argument<User?>).apply {
    argumentListValidator = {
      map {
        argumentEvent.server.get().let { server ->
          if (contains("#")) {
            server.members.firstOrNull { entity ->
              entity.idAsString == it ||
              entity.getDisplayName(server).equals(it, true)
            }
          } else {
            server.members.firstOrNull { entity ->
              entity.discriminatedName.equals(it, true) ||
              entity.getDisplayName(server).equals(it, true)
            }
          }
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.getMemberById(Utils.extractDigits(it)).getOrElse {
              if (contains("#")) {
                server.members.firstOrNull { entity ->
                  entity.idAsString == it ||
                  entity.getDisplayName(server).equals(it, true)
                }
              } else {
                server.members.firstOrNull { entity ->
                  entity.discriminatedName.equals(it, true) ||
                  entity.getDisplayName(server).equals(it, true)
                }
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
fun FinalizedArg<*>.channel(searchMutualGuilds: Boolean = false): Argument<ServerChannel?> {
  return (this as Argument<ServerChannel?>).apply {
    argumentListValidator = {
      map {
        argumentEvent.server.get().channels.firstOrNull { entity ->
          entity.idAsString == it ||
          entity.name.equals(it, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.channels.firstOrNull { entity ->
              entity.idAsString == it ||
              entity.name.equals(it, true)
            }
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
inline fun <reified R : ServerChannel> FinalizedArg<*>.channel(vararg types: KClass<out R>, searchMutualGuilds: Boolean = false): Argument<R> {
  return (this as Argument<R>).apply {
    argumentListValidator = {
      map {
        val channel = argumentEvent.server.get().channels.firstOrNull { entity ->
          entity.idAsString == it ||
          entity.name.equals(it, true)
        } ?: throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.channels.firstOrNull { entity ->
              entity.idAsString == it ||
              entity.name.equals(it, true)
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
}

/**
 * Retrieves a [ServerTextChannel] based on the argument value(s).
 *
 * Use [textChannels] to convert each value separately.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing the retrieved nullable [ServerTextChannel] value.
 */
fun FinalizedArg<*>.textChannel(searchMutualGuilds: Boolean = false): Argument<ServerTextChannel?> {
  return (this as Argument<ServerTextChannel?>).apply {
    argumentListValidator = {
      map {
        argumentEvent.server.get().textChannels.firstOrNull { entity ->
          entity.idAsString == it ||
          entity.name.equals(it, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.textChannels.firstOrNull { entity ->
              entity.idAsString == it ||
              entity.name.equals(it, true)
            }
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
fun FinalizedArg<*>.voiceChannel(searchMutualGuilds: Boolean = false): Argument<ServerVoiceChannel?> {
  return (this as Argument<ServerVoiceChannel?>).apply {
    argumentListValidator = {
      map {
        argumentEvent.server.get().voiceChannels.firstOrNull { entity ->
          entity.idAsString == it ||
          entity.name.equals(it, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.voiceChannels.firstOrNull { entity ->
              entity.idAsString == it ||
              entity.name.equals(it, true)
            }
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
fun FinalizedArg<*>.threadChannel(searchMutualGuilds: Boolean = false): Argument<ServerThreadChannel?> {
  return (this as Argument<ServerThreadChannel?>).apply {
    argumentListValidator = {
      map {
        argumentEvent.server.get().threadChannels.firstOrNull { entity ->
          entity.idAsString == it ||
          entity.name.equals(it, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.threadChannels.firstOrNull { entity ->
              entity.idAsString == it ||
              entity.name.equals(it, true)
            }
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
fun FinalizedArg<*>.stageChannel(searchMutualGuilds: Boolean = false): Argument<ServerStageVoiceChannel?> {
  return (this as Argument<ServerStageVoiceChannel?>).apply {
    argumentListValidator = {
      map {
        argumentEvent.server.get().channels.filter { it.asServerStageVoiceChannel().isPresent }.firstOrNull { entity ->
          entity.idAsString == it ||
          entity.name.equals(it, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.channels.filter { it.asServerStageVoiceChannel().isPresent }.firstOrNull { entity ->
              entity.idAsString == it ||
              entity.name.equals(it, true)
            }
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
fun FinalizedArg<*>.forumChannel(searchMutualGuilds: Boolean = false): Argument<ServerForumChannel?> {
  return (this as Argument<ServerForumChannel?>).apply {
    argumentListValidator = {
      map {
        argumentEvent.server.get().forumChannels.firstOrNull { entity ->
          entity.idAsString == it ||
          entity.name.equals(it, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.forumChannels.firstOrNull { entity ->
              entity.idAsString == it ||
              entity.name.equals(it, true)
            }
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
fun FinalizedArg<*>.category(searchMutualGuilds: Boolean = false): Argument<ChannelCategory?> {
  return (this as Argument<ChannelCategory?>).apply {
    argumentListValidator = {
      map {
        argumentEvent.server.get().channelCategories.firstOrNull { entity ->
          entity.idAsString == it ||
          entity.name.equals(it, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.channelCategories.firstOrNull { entity ->
              entity.idAsString == it ||
              entity.name.equals(it, true)
            }
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
fun FinalizedArg<*>.role(searchMutualGuilds: Boolean = false): Argument<Role?> {
  return (this as Argument<Role?>).apply {
    argumentListValidator = {
      map {
        argumentEvent.server.get().roles.firstOrNull { entity ->
          entity.idAsString == it ||
          entity.name.equals(it, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.roles.firstOrNull { entity ->
              entity.idAsString == it ||
              entity.name.equals(it, true)
            }
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
fun FinalizedArg<*>.message(searchMutualGuilds: Boolean = false, includePrivateChannels: Boolean = false): Argument<Message?> {
  return (this as Argument<Message?>).apply {
    argumentListValidator = {
      map {
        val matchResult = DiscordRegexPattern.MESSAGE_LINK.toRegex().matchEntire(it)
        if (matchResult == null) {
          argumentEvent.channel.getMessageById(it).get()
        } else {
          if (matchResult.groups["server"] == null) {
            require(includePrivateChannels)
            argumentEvent.messageAuthor.asUser().get().openPrivateChannel().get()
              .getMessageById(matchResult.groups["message"]!!.value).get()
          } else {
            try {
              argumentEvent.server.get().getTextChannelById(matchResult.groups["channel"]!!.value).get().takeIf { channel -> channel.canSee(argumentEvent.messageAuthor.asUser().get()) }!!
                .getMessageById(matchResult.groups["message"]!!.value).get()
            } catch (_: NullPointerException) {
              throw IllegalAccessException()
            } catch (_: Exception) {
              throwUnless(searchMutualGuilds) {
                argumentEvent.messageAuthor.asUser().get().mutualServers.find { server -> server.idAsString == matchResult.groups["server"]!!.value }!!
                  .getTextChannelById(matchResult.groups["channel"]!!.value).get().takeIf { channel -> channel.canSee(argumentEvent.messageAuthor.asUser().get()) }!!
                  .getMessageById(matchResult.groups["message"]!!.value).get()
              }
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
fun FinalizedArg<*>.customEmoji(searchMutualGuilds: Boolean = false): Argument<CustomEmoji?> {
  return (this as Argument<CustomEmoji?>).apply {
    argumentListValidator = {
      map {
        val matchResult = DiscordRegexPattern.CUSTOM_EMOJI.toRegex().matchEntire(it) ?: throw IllegalArgumentException()
        argumentEvent.server.get().getCustomEmojiById(matchResult.groups["id"]!!.value).getOrNull() ?: throwUnless(!guildOnly && searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
          argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
            server.getCustomEmojiById(matchResult.groups["id"]!!.value).get()
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
fun FinalizedArg<*>.snowflake(): Argument<Snowflake?> {
  return (this as Argument<Snowflake?>).apply {
    argumentValidator = {
      map {
        Snowflake(toLong().takeIf { it > 0 }!!)
      }
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
fun FinalizedArg<*>.url(): Argument<URL?> {
  return (this as Argument<URL?>).apply {
    argumentListValidator = {
      map {
        URL(it)
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
fun FinalizedArg<*>.duration(): Argument<Duration?> {
  return (this as Argument<Duration?>).apply {
    argumentListValidator = {
      map {
        Utils.parseDuration(it) ?: throw IllegalArgumentException()
      }
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
fun FinalizedArg<*>.date(locale: Locale = Locale.ENGLISH): Argument<LocalDate?> {
  return (this as Argument<LocalDate?>).apply {
    argumentListValidator = {
      map {
        Utils.parseDate(it, locale)?.toLocalDate() ?: throw IllegalArgumentException()
      }
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
fun FinalizedArg<*>.dateTime(locale: Locale = Locale.ENGLISH): Argument<LocalDateTime?> {
  return (this as Argument<LocalDateTime?>).apply {
    argumentListValidator = {
      map {
        Utils.parseDate(it, locale) ?: throw IllegalArgumentException()
      }
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
fun FinalizedArg<*>.color(): Argument<Color?> {
  return (this as Argument<Color?>).apply {
    argumentListValidator = {
      map {
        Color::class.java.getField(it)[null] as? Color ?: Color.decode(it)
      }
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
fun FinalizedArg<*>.unicodeEmoji(): Argument<Emoji?> {
  return (this as Argument<Emoji?>).apply {
    argumentListValidator = {
      map {
        EmojiManager.getEmoji(it).get()
      }
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
inline fun <reified T : Enum<T>> FinalizedArg<*>.enum(): Argument<T?> {
  return (this as Argument<T?>).apply {
    argumentListValidator = {
      map {
        enumValueOf<T>(it.uppercase().replace(" ", "_"))
      }
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
fun <T> FinalizedArg<*>.map(values: Map<String, T>, ignoreCase: Boolean = false): Argument<T?> {
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
fun <T> FinalizedArg<*>.map(vararg values: Pair<String, T>, ignoreCase: Boolean = false): Argument<T?> {
  return (this as Argument<T?>).apply {
    argumentValidator = {
      mapOf(*values)[if(ignoreCase) lowercase() else this]!!
    }
  }
}
