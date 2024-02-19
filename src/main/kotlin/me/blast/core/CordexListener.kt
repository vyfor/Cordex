package me.blast.core

import kotlinx.coroutines.launch
import me.blast.command.Arguments
import me.blast.command.BaseCommand
import me.blast.command.text.TextContext
import me.blast.command.slash.SlashContext
import me.blast.command.text.TextCommand
import me.blast.command.text.TextSubcommand
import me.blast.parser.ArgumentsParser
import me.blast.parser.exception.ArgumentException
import me.blast.utils.Utils.hasValue
import me.blast.utils.command.CommandUtils
import me.blast.utils.command.Embeds
import me.blast.utils.cooldown.CooldownType
import me.blast.utils.event.SlashCommandEvent
import me.blast.utils.event.TextCommandEvent
import me.blast.utils.extensions.respondEphemerally
import org.javacord.api.entity.permission.PermissionType
import org.javacord.api.entity.server.Server
import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.interaction.SlashCommandCreateListener
import org.javacord.api.listener.message.MessageCreateListener
import kotlin.jvm.optionals.getOrNull

// TODO: Reduce boilerplate
class CordexListener(private val cordex: CordexBuilder) : MessageCreateListener, SlashCommandCreateListener {
  override fun onMessageCreate(event: MessageCreateEvent) {
    if (!event.messageAuthor.isRegularUser) return
    val prefix = cordex.config.prefix(event.server.orElse(null)?.id ?: -1)
    if (event.messageContent.startsWith(prefix)) {
      executeTextCommand(event, prefix)
    }
  }
  
  override fun onSlashCommandCreate(event: SlashCommandCreateEvent) {
    executeSlashCommand(event)
  }
  
  private fun executeTextCommand(event: MessageCreateEvent, prefix: String) {
    val args = event.messageContent.substring(prefix.length).split(Regex("\\s+"))
    cordex.handler.getTextCommands()[args[0].lowercase()]?.apply {
      if (guildOnly && !event.server.hasValue()) return
      
      
      // Run command interceptor, if provided
      if (cordex.config.interceptors[name]?.invoke(TextCommandEvent(event, this)) == false) return
      if (isNsfw && (!guildOnly || !event.serverTextChannel.get().isNsfw)) {
        event.message.reply("This command can only be run in NSFW channels.")
        return
      }
      if (
        guildOnly &&
        serverCooldown.isPositive() &&
        cordex.cooldownManager.isServerOnCooldown(name, event.server.get().id, serverCooldown.inWholeMilliseconds)
      ) {
        cordex.cooldownManager.getServerCooldown(name, event.server.get().id)?.let {
          cordex.config.cooldownHandler?.invoke(TextCommandEvent(event, this), it, CooldownType.SERVER)
          ?: event.message.reply(Embeds.userHitCooldown(it.endTime - System.currentTimeMillis(), CooldownType.SERVER))
          return
        }
      }
      // User cooldown check
      if (
        userCooldown.isPositive() &&
        cordex.cooldownManager.isUserOnCooldown(name, event.messageAuthor.id, userCooldown.inWholeMilliseconds)
      ) {
        cordex.cooldownManager.getUserCooldown(name, event.messageAuthor.id)?.let {
          cordex.config.cooldownHandler?.invoke(TextCommandEvent(event, this), it, CooldownType.USER)
          ?: event.message.reply(Embeds.userHitCooldown(it.endTime - System.currentTimeMillis(), CooldownType.USER))
          return
        }
      }
      // Channel cooldown check
      if (
        channelCooldown.isPositive() &&
        cordex.cooldownManager.isChannelOnCooldown(name, event.channel.id, channelCooldown.inWholeMilliseconds)
      ) {
        cordex.cooldownManager.getChannelCooldown(name, event.channel.id)?.let {
          cordex.config.cooldownHandler?.invoke(TextCommandEvent(event, this), it, CooldownType.CHANNEL)
          ?: event.message.reply(Embeds.userHitCooldown(it.endTime - System.currentTimeMillis(), CooldownType.CHANNEL))
          return
        }
      }
      
      // Check for user's permissions in the server
      if (
        permissions != null &&
        !(event.server.get().getAllowedPermissions(event.messageAuthor.asUser().get()).let {
          it.contains(PermissionType.ADMINISTRATOR) || it.containsAll(permissions)
        })
      ) {
        event.message.reply(Embeds.missingPermissions(permissions))
        return
      }
      // Check for the bot's permissions in the server
      if (
        selfPermissions != null &&
        !(event.server.get().getAllowedPermissions(event.api.yourself).let {
          it.contains(PermissionType.ADMINISTRATOR) || it.containsAll(selfPermissions)
        })
      ) {
        event.message.reply(Embeds.missingSelfPermissions(selfPermissions))
        return
      }
      
      // Subcommand validation
      if (args.size > 1) {
        subcommands[args[1].lowercase()]?.run {
          executeTextSubcommand(this, this@apply, args, event, prefix)
          return
        }
      }
      
      Cordex.scope.launch {
        try {
          // Argument parsing and validation
          val parsedArgs = ArgumentsParser.parseTextCommand(args.drop(1), options, event, guildOnly)
          // Command execution
          Arguments(parsedArgs).execute(
            TextContext(
              event,
              event.server.getOrNull(),
              event.channel,
              event.messageAuthor.asUser().get(),
              event.message,
              prefix
            )
          )
        } catch (e: ArgumentException) {
          cordex.config.parsingErrorHandler?.invoke(TextCommandEvent(event, this@apply), e)
          ?: event.message.reply(Embeds.invalidArguments(e))
        } catch (e: Exception) {
          cordex.config.errorHandler?.invoke(TextCommandEvent(event, this@apply), e)
          Cordex.logger.error("Error occurred while executing command $name", e)
        }
      }
    } ?: run {
      // Let us agree, you wouldn't give a command a thirty character name, or would you?
      if (cordex.config.enableCommandSuggestions && args[0].length <= 30) {
        CommandUtils.findClosestCommands(args[0], cordex.handler.getTextCommands().keys, cordex.config.commandSuggestionAccuracy.maxDistance).let {
          if(it.isNotEmpty()) event.message.reply(
            Embeds.commandNotFound(
              args[0],
              it,
              prefix
            )
          )
        }
      }
    }
  }
  
  private fun executeTextSubcommand(command: TextSubcommand, parentCommand: TextCommand, args: List<String>, event: MessageCreateEvent, prefix: String): Boolean {
    if (command.guildOnly && !event.server.hasValue()) return false
    
    command.apply {
      if (isNsfw && (!guildOnly || !event.serverTextChannel.get().isNsfw)) {
        event.message.reply("This subcommand can only be run in NSFW channels.")
        return false
      }
      
      if (guildOnly) {
        // Check for user's permissions in the server
        if (permissions == null) {
          if (
            parentCommand.permissions != null &&
            !(event.server.get().getAllowedPermissions(event.messageAuthor.asUser().get()).let {
              it.contains(PermissionType.ADMINISTRATOR) || it.containsAll(parentCommand.permissions)
            })
          ) {
            event.message.reply(Embeds.missingPermissions(parentCommand.permissions))
            return false
          }
        } else if (
          !(event.server.get().getAllowedPermissions(event.messageAuthor.asUser().get()).let {
            it.contains(PermissionType.ADMINISTRATOR) || it.containsAll(permissions)
          })
        ) {
          event.message.reply(Embeds.missingPermissions(permissions))
          return false
        }
        // Check for the bot's permissions in the server
        if (selfPermissions == null) {
          if (
            parentCommand.selfPermissions != null &&
            !(event.server.get().getAllowedPermissions(event.messageAuthor.asUser().get()).let {
              it.contains(PermissionType.ADMINISTRATOR) || it.containsAll(parentCommand.selfPermissions)
            })
          ) {
            event.message.reply(Embeds.missingPermissions(parentCommand.selfPermissions))
            return false
          }
        } else if (
          !(event.server.get().getAllowedPermissions(event.messageAuthor.asUser().get()).let {
            it.contains(PermissionType.ADMINISTRATOR) || it.containsAll(selfPermissions)
          })
        ) {
          event.message.reply(Embeds.missingPermissions(selfPermissions))
          return false
        }
      }
      
      Cordex.scope.launch {
        try {
          // Argument parsing and validation
          val parsedArgs = ArgumentsParser.parseTextCommand(args.drop(2), options, event, guildOnly)
          // Command execution
          Arguments(parsedArgs).execute(
            parentCommand.name,
            TextContext(
              event,
              event.server.getOrNull(),
              event.channel,
              event.messageAuthor.asUser().get(),
              event.message,
              prefix
            )
          )
        } catch (e: ArgumentException) {
          cordex.config.parsingErrorHandler?.invoke(TextCommandEvent(event, parentCommand), e)
          ?: event.message.reply(Embeds.invalidArguments(e))
        } catch (e: Exception) {
          cordex.config.errorHandler?.invoke(TextCommandEvent(event, parentCommand), e)
          Cordex.logger.error("Error occurred while executing command ${parentCommand.name} ${command.name}", e)
        }
      }
    }
    return false
  }
  
  private fun executeSlashCommand(event: SlashCommandCreateEvent) {
    cordex.handler.getSlashCommands()[event.slashCommandInteraction.commandName]?.apply {
      if (guildOnly && !event.slashCommandInteraction.server.hasValue()) return
      
      if (isNsfw && (!guildOnly || !event.slashCommandInteraction.channel.get().asServerTextChannel().get().isNsfw)) {
        event.slashCommandInteraction.respondEphemerally("This command can only be run in NSFW channels.")
        return
      }
      
      if (guildOnly) {
        // User permission check is unnecessary
        // Check for the bot's permissions in the server
        if (
          selfPermissions != null &&
          !(
            event.slashCommandInteraction.server.get().isAdmin(event.api.yourself) ||
            event.slashCommandInteraction.server.get().getAllowedPermissions(event.api.yourself).containsAll(selfPermissions)
           )
        ) {
          event.slashCommandInteraction.respondEphemerally(Embeds.missingSelfPermissions(selfPermissions))
          return
        }
      }
      if (applyCooldown) {
        // Server cooldown check
        if (
          guildOnly &&
          serverCooldown.isPositive() &&
          cordex.cooldownManager.isServerOnCooldown(name, event.slashCommandInteraction.server.get().id, serverCooldown.inWholeMilliseconds)
        ) {
          cordex.cooldownManager.getServerCooldown(name, event.slashCommandInteraction.server.get().id)?.let {
            cordex.config.cooldownHandler?.invoke(SlashCommandEvent(event, this), it, CooldownType.SERVER)
            ?: event.slashCommandInteraction.respondEphemerally(Embeds.userHitCooldown(it.endTime - System.currentTimeMillis(), CooldownType.SERVER))
            return
          }
        }
        // User cooldown check
        if (
          userCooldown.isPositive() &&
          cordex.cooldownManager.isUserOnCooldown(name, event.slashCommandInteraction.user.id, userCooldown.inWholeMilliseconds)
        ) {
          cordex.cooldownManager.getUserCooldown(name, event.slashCommandInteraction.user.id)?.let {
            cordex.config.cooldownHandler?.invoke(SlashCommandEvent(event, this), it, CooldownType.USER)
            ?: event.slashCommandInteraction.respondEphemerally(Embeds.userHitCooldown(it.endTime - System.currentTimeMillis(), CooldownType.USER))
            return
          }
        }
        // Channel cooldown check
        if (channelCooldown.isPositive()) event.slashCommandInteraction.channel.ifPresent { channel ->
          if (
            cordex.cooldownManager.isChannelOnCooldown(name, channel.id, channelCooldown.inWholeMilliseconds)
          ) {
            cordex.cooldownManager.getChannelCooldown(name, channel.id)?.let {
              cordex.config.cooldownHandler?.invoke(SlashCommandEvent(event, this), it, CooldownType.CHANNEL)
              ?: event.slashCommandInteraction.respondEphemerally(Embeds.userHitCooldown(it.endTime - System.currentTimeMillis(), CooldownType.CHANNEL))
              return@ifPresent
            }
          }
        }
      }
      
      Cordex.scope.launch {
        // Argument parsing and validation
        val parsedArgs = ArgumentsParser.parseSlashCommand(options, event)
        // Command execution
        Arguments(parsedArgs).execute(
          SlashContext(
            event,
            event.slashCommandInteraction,
            event.slashCommandInteraction.server.getOrNull(),
            event.slashCommandInteraction.channel.getOrNull(),
            event.slashCommandInteraction.user
          )
        )
      }
    }
  }
}