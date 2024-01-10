@file:Suppress("UNCHECKED_CAST", "unused")

package me.blast.command.argument.extensions

import me.blast.command.argument.Argument
import me.blast.command.argument.Multiple
import me.blast.utils.entity.Snowflake
import me.blast.utils.Utils
import me.blast.utils.Utils.hasValue
import me.blast.utils.Utils.toNullable
import me.blast.utils.extensions.throwUnless
import net.fellbaum.jemoji.Emoji
import net.fellbaum.jemoji.EmojiManager
import org.javacord.api.entity.Mentionable
import org.javacord.api.entity.channel.*
import org.javacord.api.entity.emoji.CustomEmoji
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.user.User
import org.javacord.api.util.DiscordRegexPattern
import java.awt.Color
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Duration
import java.util.*
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass

/**
 * Converts the argument values to integers.
 *
 * Use [int] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved [Int] values.
 */
fun Multiple<*>.ints(): Argument<List<Int>> {
  return (this as Argument<List<Int>>).apply {
    argumentListValidator = {
      map {
        it.toInt()
      }
    }
    argumentReturnValue = Int::class
  }
}

/**
 * Converts the argument values to unsigned integers.
 *
 * Use [uInt] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved [UInt] values.
 */
fun Multiple<*>.uInts(): Argument<List<UInt>> {
  return (this as Argument<List<UInt>>).apply {
    argumentListValidator = {
      map {
        it.toUInt()
      }
    }
    argumentReturnValue = UInt::class
  }
}

/**
 * Converts the argument values to long values.
 *
 * Use [long] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved [Long] values.
 */
fun Multiple<*>.longs(): Argument<List<Long>> {
  return (this as Argument<List<Long>>).apply {
    argumentListValidator = {
      map {
        it.toLong()
      }
    }
    argumentReturnValue = Long::class
  }
}

/**
 * Converts the argument values to unsigned long values.
 *
 * Use [uLong] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved [ULong] values.
 */
fun Multiple<*>.uLongs(): Argument<List<ULong>> {
  return (this as Argument<List<ULong>>).apply {
    argumentListValidator = {
      map {
        it.toULong()
      }
    }
    argumentReturnValue = ULong::class
  }
}

/**
 * Converts the argument values to float values.
 *
 * Use [float] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved [Float] values.
 */
fun Multiple<*>.floats(): Argument<List<Float>> {
  return (this as Argument<List<Float>>).apply {
    argumentListValidator = {
      map {
        it.toFloat()
      }
    }
    argumentReturnValue = Float::class
  }
}

/**
 * Converts the argument values to double values.
 *
 * Use [double] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved [Double] values.
 */
fun Multiple<*>.doubles(): Argument<List<Double>> {
  return (this as Argument<List<Double>>).apply {
    argumentListValidator = {
      map {
        it.toDouble()
      }
    }
    argumentReturnValue = Double::class
  }
}

/**
 * Retrieves [User]s based on the argument values.
 *
 * Use [user] to convert multiple values into a single one.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing a list with retrieved [User] values.
 */
fun Multiple<*>.users(searchMutualGuilds: Boolean = false): Argument<List<User>> {
  return (this as Argument<List<User>>).apply {
    argumentListValidator = {
      map { query ->
        argumentServer.let { server ->
          server.members.firstOrNull {
            if(contains("#"))
              it.discriminatedName.equals(query, true)
            else
              it.idAsString == query ||
              it.getNickname(server).toNullable().equals(query, true) ||
              it.name.equals(query, true) ||
              it.globalName.toNullable().equals(query, true)
          }
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.members.firstOrNull {
              if(contains("#"))
                it.discriminatedName.equals(query, true)
              else
                it.idAsString == query ||
                it.getNickname(server).toNullable().equals(query, true) ||
                it.name.equals(query, true) ||
                it.globalName.toNullable().equals(query, true)
            }
          }
        }
      }
    }
    argumentReturnValue = User::class
  }
}

/**
 * Retrieves [ServerChannel]s based on the argument values.
 *
 * Use [channel] to convert multiple values into a single one.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing a list with retrieved [ServerChannel] values.
 */
fun Multiple<*>.channels(searchMutualGuilds: Boolean = false): Argument<List<ServerChannel>> {
  return (this as Argument<List<ServerChannel>>).apply {
    argumentListValidator = {
      map {
        argumentServer.channels.firstOrNull { entity ->
          entity.idAsString == it ||
          entity.name.equals(it, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.channels.firstOrNull { entity ->
              entity.idAsString == it ||
              entity.name.equals(it, true)
            }
          }
        }
      }
    }
    argumentReturnValue = ServerChannel::class
  }
}

/**
 * Retrieves [ServerChannel]s of the specified `types` based on the argument values.
 *
 * Use [channel] to convert multiple values into a single one.
 *
 * @param types types An array of classes extending [ServerChannel] representing the supported channel types.
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing a list with retrieved [ServerChannel] values.
 */
inline fun <reified R : ServerChannel> Multiple<*>.channels(vararg types: KClass<out R>, searchMutualGuilds: Boolean = false): Argument<R> {
  return (this as Argument<R>).apply {
    argumentListValidator = {
      map {
        val channel = argumentServer.channels.firstOrNull { entity ->
          entity.idAsString == it ||
          entity.name.equals(it, true)
        } ?: throwUnless(searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
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
    argumentReturnValue = ServerChannel::class
  }
}

/**
 * Retrieves [ServerTextChannel]s based on the argument values.
 *
 * Use [textChannel] to convert multiple values into a single one.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing a list with retrieved [ServerTextChannel] values.
 */
fun Multiple<*>.textChannels(searchMutualGuilds: Boolean = false): Argument<List<ServerTextChannel>> {
  return (this as Argument<List<ServerTextChannel>>).apply {
    argumentListValidator = {
      map {
        argumentServer.textChannels.firstOrNull { entity ->
          entity.idAsString == it ||
          entity.name.equals(it, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.textChannels.firstOrNull { entity ->
              entity.idAsString == it ||
              entity.name.equals(it, true)
            }
          }
        }
      }
    }
    argumentReturnValue = ServerTextChannel::class
  }
}

/**
 * Retrieves [ServerVoiceChannel]s based on the argument values.
 *
 * Use [voiceChannel] to convert multiple values into a single one.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing a list with retrieved [ServerVoiceChannel] values.
 */
fun Multiple<*>.voiceChannels(searchMutualGuilds: Boolean = false): Argument<List<ServerVoiceChannel>> {
  return (this as Argument<List<ServerVoiceChannel>>).apply {
    argumentListValidator = {
      map {
        argumentServer.voiceChannels.firstOrNull { entity ->
          entity.idAsString == it ||
          entity.name.equals(it, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.voiceChannels.firstOrNull { entity ->
              entity.idAsString == it ||
              entity.name.equals(it, true)
            }
          }
        }
      }
    }
    argumentReturnValue = ServerVoiceChannel::class
  }
}

/**
 * Retrieves [ServerThreadChannel]s based on the argument values.
 *
 * Use [threadChannel] to convert multiple values into a single one.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing a list with retrieved [ServerThreadChannel] values.
 */
fun Multiple<*>.threadChannels(searchMutualGuilds: Boolean = false): Argument<List<ServerThreadChannel>> {
  return (this as Argument<List<ServerThreadChannel>>).apply {
    argumentListValidator = {
      map {
        argumentServer.threadChannels.firstOrNull { entity ->
          entity.idAsString == it ||
          entity.name.equals(it, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.threadChannels.firstOrNull { entity ->
              entity.idAsString == it ||
              entity.name.equals(it, true)
            }
          }
        }
      }
    }
    argumentReturnValue = ServerThreadChannel::class
  }
}

/**
 * Retrieves [ServerStageVoiceChannel]s based on the argument values.
 *
 * Use [stageChannel] to convert multiple values into a single one.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing a list with retrieved [ServerStageVoiceChannel] values.
 */
fun Multiple<*>.stageChannels(searchMutualGuilds: Boolean = false): Argument<List<ServerStageVoiceChannel>> {
  return (this as Argument<List<ServerStageVoiceChannel>>).apply {
    argumentListValidator = {
      map {
        argumentServer.channels.filter { it.asServerStageVoiceChannel().isPresent }.firstOrNull { entity ->
          entity.idAsString == it ||
          entity.name.equals(it, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.channels.filter { it.asServerStageVoiceChannel().isPresent }.firstOrNull { entity ->
              entity.idAsString == it ||
              entity.name.equals(it, true)
            }
          }
        }
      }
    }
    argumentReturnValue = ServerStageVoiceChannel::class
  }
}

/**
 * Retrieves [ServerForumChannel]s based on the argument values.
 *
 * Use [forumChannel] to convert multiple values into a single one.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing a list with retrieved [ServerForumChannel] values.
 */
fun Multiple<*>.forumChannels(searchMutualGuilds: Boolean = false): Argument<List<ServerForumChannel>> {
  return (this as Argument<List<ServerForumChannel>>).apply {
    argumentListValidator = {
      map {
        argumentServer.forumChannels.firstOrNull { entity ->
          entity.idAsString == it ||
          entity.name.equals(it, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.forumChannels.firstOrNull { entity ->
              entity.idAsString == it ||
              entity.name.equals(it, true)
            }
          }
        }
      }
    }
    argumentReturnValue = ServerForumChannel::class
  }
}

/**
 * Retrieves [ChannelCategory]s based on the argument values.
 *
 * Use [category] to convert multiple values into a single one.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing a list with retrieved [ChannelCategory] values.
 */
fun Multiple<*>.categories(searchMutualGuilds: Boolean = false): Argument<List<ChannelCategory>> {
  return (this as Argument<List<ChannelCategory>>).apply {
    argumentListValidator = {
      map {
        argumentServer.channelCategories.firstOrNull { entity ->
          entity.idAsString == it ||
          entity.name.equals(it, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.channelCategories.firstOrNull { entity ->
              entity.idAsString == it ||
              entity.name.equals(it, true)
            }
          }
        }
      }
    }
    argumentReturnValue = ChannelCategory::class
  }
}

/**
 * Retrieves [Role]s based on the argument values.
 *
 * Use [role] to convert multiple values into a single one.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing a list with retrieved [Role] values.
 */
fun Multiple<*>.roles(searchMutualGuilds: Boolean = false): Argument<List<Role>> {
  return (this as Argument<List<Role>>).apply {
    argumentListValidator = {
      map {
        argumentServer.roles.firstOrNull { entity ->
          entity.idAsString == it ||
          entity.name.equals(it, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.roles.firstOrNull { entity ->
              entity.idAsString == it ||
              entity.name.equals(it, true)
            }
          }
        }
      }
    }
    argumentReturnValue = Role::class
  }
}

/**
 * Retrieves [Message]s based on the argument values.
 *
 * Use [message] to convert multiple values into a single one.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @param includePrivateChannels Whether to include messages in the private channel between the user and the bot in search. Defaults to false.
 * @return An Argument containing a list with retrieved [Message] values.
 */
fun Multiple<*>.messages(searchMutualGuilds: Boolean = false, includePrivateChannels: Boolean = false): Argument<List<Message>> {
  return (this as Argument<List<Message>>).apply {
    argumentListValidator = {
      map {
        val matchResult = DiscordRegexPattern.MESSAGE_LINK.toRegex().matchEntire(it)
        if (matchResult == null) {
          argumentChannel.getMessageById(it).get()
        } else {
          if (matchResult.groups["server"] == null) {
            require(includePrivateChannels)
            argumentUser.openPrivateChannel().get()
              .getMessageById(matchResult.groups["message"]!!.value).get()
          } else {
            try {
              argumentServer.getTextChannelById(matchResult.groups["channel"]!!.value).get().takeIf { channel -> channel.canSee(argumentUser) }!!
                .getMessageById(matchResult.groups["message"]!!.value).get()
            } catch (_: NullPointerException) {
              throw IllegalAccessException()
            } catch (_: Exception) {
              throwUnless(searchMutualGuilds) {
                argumentUser.mutualServers.find { server -> server.idAsString == matchResult.groups["server"]!!.value }!!
                  .getTextChannelById(matchResult.groups["channel"]!!.value).get().takeIf { channel -> channel.canSee(argumentUser) }!!
                  .getMessageById(matchResult.groups["message"]!!.value).get()
              }
            }
          }
        }
      }
    }
    argumentReturnValue = Message::class
  }
}

/**
 * Retrieves [Mentionable]s based on the argument values.
 *
 * Use [mentionable] to convert multiple values into a single one.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing a list with retrieved [Mentionable] values.
 */
fun Multiple<*>.mentionables(searchMutualGuilds: Boolean = false): Argument<List<Mentionable>> {
  return (this as Argument<List<Mentionable>>).apply {
    argumentListValidator = {
      map {
        val matchResult = DiscordRegexPattern.USER_MENTION.toRegex().matchEntire(it)
                          ?: DiscordRegexPattern.CHANNEL_MENTION.toRegex().matchEntire(it)
                          ?: DiscordRegexPattern.ROLE_MENTION.toRegex().matchEntire(it)
                          ?: throw IllegalArgumentException()
        
        argumentServer.getMemberById(matchResult.groups["id"]!!.value).getOrNull()
        ?: argumentServer.getChannelById(matchResult.groups["id"]!!.value).getOrNull()
        ?: argumentServer.getRoleById(matchResult.groups["id"]!!.value).getOrNull()
        ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()){
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.getMemberById(matchResult.groups["id"]!!.value).getOrNull()
            ?: server.getChannelById(matchResult.groups["id"]!!.value).getOrNull()
            ?: server.getRoleById(matchResult.groups["id"]!!.value).getOrNull()
          }
        }
      }
    }
    argumentReturnValue = Mentionable::class
  }
}

/**
 * Retrieves [CustomEmoji]s based on the argument values.
 *
 * Use [customEmoji] to convert multiple values into a single one.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing a list with retrieved [CustomEmoji] values.
 */
fun Multiple<*>.customEmojis(searchMutualGuilds: Boolean = false): Argument<List<CustomEmoji>> {
  return (this as Argument<List<CustomEmoji>>).apply {
    argumentListValidator = {
      map {
        val matchResult = DiscordRegexPattern.CUSTOM_EMOJI.toRegex().matchEntire(it) ?: throw IllegalArgumentException()
        argumentServer.getCustomEmojiById(matchResult.groups["id"]!!.value).getOrNull() ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.getCustomEmojiById(matchResult.groups["id"]!!.value).get()
          }
        }
      }
    }
    argumentReturnValue = CustomEmoji::class
  }
}

/**
 * Converts the argument values to [Snowflake]s.
 *
 * Use [snowflake] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved [Snowflake] values.
 */
fun Multiple<*>.snowflakes(): Argument<List<Snowflake>> {
  return (this as Argument<List<Snowflake>>).apply {
    argumentValidator = {
      map {
        Snowflake(toLong().takeIf { it > 0 }!!)
      }
    }
    argumentReturnValue = Snowflake::class
  }
}

/**
 * Converts the argument values to [URL]s.
 *
 * Use [url] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved [URL] values.
 */
fun Multiple<*>.urls(): Argument<List<URL>> {
  return (this as Argument<List<URL>>).apply {
    argumentListValidator = {
      map {
        URL(it)
      }
    }
    argumentReturnValue = URL::class
  }
}

/**
 * Converts the argument values to [Duration]s.
 *
 * Use [duration] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved [Duration] values.
 */
fun Multiple<*>.durations(): Argument<List<Duration>> {
  return (this as Argument<List<Duration>>).apply {
    argumentListValidator = {
      map {
        Utils.parseDuration(it) ?: throw IllegalArgumentException()
      }
    }
    argumentReturnValue = Duration::class
  }
}

/**
 * Converts the argument values to [LocalDate]s.
 *
 * Use [date] to convert multiple values into a single one.
 *
 * @param locale The locale used for date parsing. Defaults to [Locale.ENGLISH].
 * @return An Argument containing a list with retrieved [LocalDate] values.
 */
fun Multiple<*>.dates(locale: Locale = Locale.ENGLISH): Argument<List<LocalDate>> {
  return (this as Argument<List<LocalDate>>).apply {
    argumentListValidator = {
      map {
        Utils.parseDate(it, locale)?.toLocalDate() ?: throw IllegalArgumentException()
      }
    }
    argumentReturnValue = LocalDate::class
  }
}

/**
 * Converts the argument values to [LocalDateTime]s.
 *
 * Use [dateTime] to convert multiple values into a single one.
 *
 * @param locale The locale used for date parsing. Defaults to [Locale.ENGLISH].
 * @return An Argument containing a list with retrieved [LocalDateTime] values.
 */
fun Multiple<*>.dateTimes(locale: Locale = Locale.ENGLISH): Argument<List<LocalDateTime>> {
  return (this as Argument<List<LocalDateTime>>).apply {
    argumentListValidator = {
      map {
        Utils.parseDate(it, locale) ?: throw IllegalArgumentException()
      }
    }
    argumentReturnValue = LocalDateTime::class
  }
}

/**
 * Converts the argument values to [Color]s.
 *
 * Use [color] to convert multiple values into a single one.
 *
 * @returnAn Argument containing a list with retrieved [Color] values.
 */
fun Multiple<*>.colors(): Argument<List<Color>> {
  return (this as Argument<List<Color>>).apply {
    argumentListValidator = {
      map {
        Color::class.java.getField(it)[null] as? Color ?: Color.decode(it)
      }
    }
    argumentReturnValue = Color::class
  }
}

/**
 * Converts the argument values to [Emoji]s.
 *
 * Use [unicodeEmoji] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved [Emoji] values.
 */
fun Multiple<*>.unicodeEmojis(): Argument<List<Emoji>> {
  return (this as Argument<List<Emoji>>).apply {
    argumentListValidator = {
      map {
        EmojiManager.getEmoji(it).get()
      }
    }
    argumentReturnValue = Emoji::class
  }
}

/**
 * Converts the argument values to values of the given enum class.
 *
 * Use [enum] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved enum values.
 */
inline fun <reified T : Enum<T>> Multiple<*>.enums(): Argument<T> {
  return (this as Argument<T>).apply {
    argumentListValidator = {
      map {
        enumValueOf<T>(it.uppercase().replace(" ", "_"))
      }
    }
    argumentReturnValue = T::class
  }
}

/**
 * Maps the argument values to corresponding [T] value(s) from the given map.
 *
 * Use [map] to convert multiple values into a single one.
 *
 * @param ignoreCase Whether to convert the provided values to lowercase before retrieving.
 * @return An Argument containing a list with retrieved [T] values.
 */
fun <T> Multiple<*>.maps(values: Map<String, T>, ignoreCase: Boolean = false): Argument<List<T>> {
  return (this as Argument<List<T>>).apply {
    argumentChoices = values.mapValues {
      it.value.toString()
    }
    argumentListValidator = {
      map {
        values[if(ignoreCase) it.lowercase() else it]!!
      }
    }
    argumentReturnValue = Map::class
  }
}

/**
 * Maps the argument values to corresponding [T] value(s) from the given array of pairs.
 *
 * Use [map] to convert multiple values into a single one.
 *
 * @param ignoreCase Whether to convert the provided values to lowercase before retrieving.
 * @return An Argument containing a list with retrieved [T] values.
 */
fun <T> Multiple<*>.maps(vararg values: Pair<String, T>, ignoreCase: Boolean = false): Argument<List<T>> {
  return (this as Argument<List<T>>).apply {
    argumentChoices = values.associate {
      it.first to it.second.toString()
    }
    argumentListValidator = {
      map {
        mapOf(*values)[if(ignoreCase) it.lowercase() else it]!!
      }
    }
    argumentReturnValue = Map::class
  }
}

/**
 * Joins the argument values into one string.
 *
 * @return An Argument containing the combined value.
 */
fun <T> Multiple<List<T>>.combine(separator: String = " "): Argument<String> {
  return (this as Argument<String>).apply {
    argumentListValidator = {
      joinToString(separator)
    }
  }
}