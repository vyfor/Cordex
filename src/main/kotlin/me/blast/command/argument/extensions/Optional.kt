@file:Suppress("UNCHECKED_CAST", "unused")

package me.blast.command.argument.extensions

import me.blast.command.argument.Argument
import me.blast.command.argument.OptionalArg
import me.blast.utils.Utils
import me.blast.utils.Utils.hasValue
import me.blast.utils.Utils.toNullable
import me.blast.utils.entity.Snowflake
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
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
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
    argumentReturnValue = Int::class
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
    argumentReturnValue = UInt::class
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
    argumentReturnValue = Long::class
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
    argumentReturnValue = ULong::class
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
    argumentReturnValue = Float::class
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
    argumentReturnValue = Double::class
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
      if (startsWith("<@") && endsWith(">")) {
        val id = this.substring(2, length - 1).toLong()
        argumentServer.getMemberById(id).toNullable() ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.getMemberById(id).get()
          }
        }
      } else {
        argumentServer.let { server ->
          server.members.firstOrNull {
            if(contains("#"))
              it.discriminatedName.equals(this, true)
            else
              it.idAsString == this ||
              it.getNickname(server).toNullable().equals(this, true) ||
              it.name.equals(this, true) ||
              it.globalName.toNullable().equals(this, true)
          }
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.members.firstOrNull {
              if(contains("#"))
                it.discriminatedName.equals(this, true)
              else
                it.idAsString == this ||
                it.getNickname(server).toNullable().equals(this, true) ||
                it.name.equals(this, true) ||
                it.globalName.toNullable().equals(this, true)
            }
          }
        }
      }
    }
    argumentReturnValue = User::class
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
      if (startsWith("<#") && endsWith(">")) {
        val id = this.substring(2, length - 1).toLong()
        argumentServer.getChannelById(id).toNullable() ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.getChannelById(id).get()
          }
        }
      } else {
        argumentServer.channels.firstOrNull {
          it.idAsString == this ||
          it.name.equals(this, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.channels.firstOrNull {
              it.idAsString == this ||
              it.name.equals(this, true)
            }
          }
        }
      }
    }
    argumentReturnValue = ServerChannel::class
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
fun OptionalArg<*>.channel(vararg types: KClass<out ServerChannel>, searchMutualGuilds: Boolean = false): Argument<ServerChannel?> {
  return (this as Argument<ServerChannel?>).apply {
    argumentValidator = {
      val channel = if (startsWith("<#") && endsWith(">")) {
        val id = this.substring(2, length - 1).toLong()
        argumentServer.getChannelById(id).toNullable() ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.getChannelById(id).get()
          }
        }
      } else {
        argumentServer.channels.firstOrNull {
          it.idAsString == this ||
          it.name.equals(this, true)
        } ?: throwUnless(searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.channels.firstOrNull {
              it.idAsString == this ||
              it.name.equals(this, true)
            }
          }
        }
      }
      if (types.any { it.isInstance(channel) }) {
        channel
      } else {
        throw IllegalArgumentException()
      }
    }
    argumentReturnValue = ServerChannel::class
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
      if (startsWith("<#") && endsWith(">")) {
        val id = this.substring(2, length - 1).toLong()
        argumentServer.getTextChannelById(id).toNullable() ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.getTextChannelById(id).get()
          }
        }
      } else {
        argumentServer.textChannels.firstOrNull {
          it.idAsString == this ||
          it.name.equals(this, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.textChannels.firstOrNull {
              it.idAsString == this ||
              it.name.equals(this, true)
            }
          }
        }
      }
    }
    argumentReturnValue = ServerTextChannel::class
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
      if (startsWith("<#") && endsWith(">")) {
        val id = this.substring(2, length - 1).toLong()
        argumentServer.getVoiceChannelById(id).toNullable() ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.getVoiceChannelById(id).get()
          }
        }
      } else {
        argumentServer.voiceChannels.firstOrNull {
          it.idAsString == this ||
          it.name.equals(this, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.voiceChannels.firstOrNull {
              it.idAsString == this ||
              it.name.equals(this, true)
            }
          }
        }
      }
    }
    argumentReturnValue = ServerVoiceChannel::class
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
      if (startsWith("<#") && endsWith(">")) {
        val id = this.substring(2, length - 1).toLong()
        argumentServer.getThreadChannelById(id).toNullable() ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.getThreadChannelById(id).get()
          }
        }
      } else {
        argumentServer.threadChannels.firstOrNull {
          it.idAsString == this ||
          it.name.equals(this, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.threadChannels.firstOrNull {
              it.idAsString == this ||
              it.name.equals(this, true)
            }
          }
        }
      }
    }
    argumentReturnValue = ServerThreadChannel::class
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
      if (startsWith("<#") && endsWith(">")) {
        val id = this.substring(2, length - 1).toLong()
        argumentServer.getStageVoiceChannelById(id).toNullable() ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.getStageVoiceChannelById(id).get()
          }
        }
      } else {
        argumentServer.channels.filter { it.asServerStageVoiceChannel().isPresent }.firstOrNull {
          it.idAsString == this ||
          it.name.equals(this, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.channels.filter { it.asServerStageVoiceChannel().isPresent }.firstOrNull {
              it.idAsString == this ||
              it.name.equals(this, true)
            }
          }
        }
      }
    }
    argumentReturnValue = ServerStageVoiceChannel::class
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
      if (startsWith("<#") && endsWith(">")) {
        val id = this.substring(2, length - 1).toLong()
        argumentServer.getForumChannelById(id).toNullable() ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.getForumChannelById(id).get()
          }
        }
      } else {
        argumentServer.forumChannels.firstOrNull {
          it.idAsString == this ||
          it.name.equals(this, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.forumChannels.firstOrNull {
              it.idAsString == this ||
              it.name.equals(this, true)
            }
          }
        }
      }
    }
    argumentReturnValue = ServerForumChannel::class
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
      if (startsWith("<#") && endsWith(">")) {
        val id = this.substring(2, length - 1).toLong()
        argumentServer.getChannelCategoryById(id).toNullable() ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.getChannelCategoryById(id).get()
          }
        }
      } else {
        argumentServer.channelCategories.firstOrNull {
          it.idAsString == this ||
          it.name.equals(this, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.channelCategories.firstOrNull {
              it.idAsString == this ||
              it.name.equals(this, true)
            }
          }
        }
      }
    }
    argumentReturnValue = ChannelCategory::class
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
      if (startsWith("<&") && endsWith(">")) {
        val id = this.substring(3, length - 1).toLong()
        argumentServer.getRoleById(id).toNullable() ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.getRoleById(id).get()
          }
        }
      } else {
        argumentServer.roles.firstOrNull {
          it.idAsString == this ||
          it.name.equals(this, true)
        } ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
          argumentUser.mutualServers.firstNotNullOf { server ->
            server.roles.firstOrNull {
              it.idAsString == this ||
              it.name.equals(this, true)
            }
          }
        }
      }
    }
    argumentReturnValue = Role::class
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
        argumentChannel.getMessageById(this).get()
      } else {
        if (matchResult.groups["server"] == null) {
          require(includePrivateChannels)
          argumentUser.openPrivateChannel().get()
            .getMessageById(matchResult.groups["message"]!!.value).get()
        } else {
          try {
            argumentServer.getTextChannelById(matchResult.groups["channel"]!!.value).get().takeIf { it.canSee(argumentUser) }!!
              .getMessageById(matchResult.groups["message"]!!.value).get()
          } catch (_: NullPointerException) {
            throw IllegalAccessException()
          } catch (_: Exception) {
            throwUnless(searchMutualGuilds) {
              argumentUser.mutualServers.find { it.idAsString == matchResult.groups["server"]!!.value }!!
                .getTextChannelById(matchResult.groups["channel"]!!.value).get().takeIf { it.canSee(argumentUser) }!!
                .getMessageById(matchResult.groups["message"]!!.value).get()
            }
          }
        }
      }
    }
    argumentReturnValue = Message::class
  }
}

/**
 * Retrieves a [Mentionable] based on the argument value(s).
 *
 * Use [mentionables] to convert each value separately.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing the retrieved nullable [Mentionable] value.
 */
fun OptionalArg<*>.mentionable(searchMutualGuilds: Boolean = false): Argument<Mentionable?> {
  return (this as Argument<Mentionable?>).apply {
    argumentValidator = {
      val matchResult = DiscordRegexPattern.USER_MENTION.toRegex().matchEntire(this)
                        ?: DiscordRegexPattern.CHANNEL_MENTION.toRegex().matchEntire(this)
                        ?: DiscordRegexPattern.ROLE_MENTION.toRegex().matchEntire(this)
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
    argumentReturnValue = Mentionable::class
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
      argumentServer.getCustomEmojiById(matchResult.groups["id"]!!.value).getOrNull() ?: throwUnless(!guildOnly && searchMutualGuilds && argumentChannel.asPrivateChannel().hasValue()) {
        argumentUser.mutualServers.firstNotNullOf { server ->
          server.getCustomEmojiById(matchResult.groups["id"]!!.value).get()
        }
      }
    }
    argumentReturnValue = CustomEmoji::class
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
    argumentReturnValue = Snowflake::class
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
    argumentReturnValue = URL::class
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
    argumentReturnValue = Duration::class
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
    argumentReturnValue = LocalDate::class
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
    argumentReturnValue = LocalDateTime::class
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
    argumentReturnValue = Color::class
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
    argumentReturnValue = Emoji::class
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
    argumentReturnValue = T::class
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
    argumentChoices = values.mapValues {
      it.value.toString()
    }
    argumentValidator = {
      values[if(ignoreCase) lowercase() else this]!!
    }
    argumentReturnValue = Map::class
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
    argumentChoices = values.associate {
      it.first to it.second.toString()
    }
    argumentValidator = {
      mapOf(*values)[if(ignoreCase) lowercase() else this]!!
    }
    argumentReturnValue = Map::class
  }
}