package me.blast.utils.cooldown

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class CooldownManager {
  private val serverCooldown: MutableMap<String, ConcurrentHashMap.KeySetView<Pair<Long, Cooldown>, Boolean>> = mutableMapOf()
  private val channelCooldown: MutableMap<String, ConcurrentHashMap.KeySetView<Pair<Long, Cooldown>, Boolean>> = mutableMapOf()
  private val userCooldown: MutableMap<String, ConcurrentHashMap.KeySetView<Pair<Long, Cooldown>, Boolean>> = mutableMapOf()
  private val coroutineScope = CoroutineScope(Dispatchers.Default)
  
  fun isServerOnCooldown(command: String, id: Long, duration: Long) = isOnCooldown(serverCooldown, command, id, duration)
  
  fun isChannelOnCooldown(command: String, id: Long, duration: Long) = isOnCooldown(channelCooldown, command, id, duration)
  
  fun isUserOnCooldown(command: String, id: Long, duration: Long) = isOnCooldown(userCooldown, command, id, duration)
  
  fun getServerCooldown(command: String, id: Long) = getCooldown(serverCooldown, command, id)
  
  fun getChannelCooldown(command: String, id: Long) = getCooldown(channelCooldown, command, id)
  
  fun getUserCooldown(command: String, id: Long) = getCooldown(userCooldown, command, id)
  
  private fun getCooldown(map: MutableMap<String, ConcurrentHashMap.KeySetView<Pair<Long, Cooldown>, Boolean>>, command: String, id: Long) = map[command]?.find { it.first == id }?.second
  
  private fun isOnCooldown(map: MutableMap<String, ConcurrentHashMap.KeySetView<Pair<Long, Cooldown>, Boolean>>, command: String, id: Long, duration: Long): Boolean {
    if (map[command] == null) {
      map[command] = ConcurrentHashMap.newKeySet<Pair<Long, Cooldown>>().apply {
        add(id to applyCooldown(map, command, id, duration))
      }
      return false
    }
    if (map[command]?.isEmpty() == true) {
      map[command]?.add(id to applyCooldown(map, command, id, duration))
      return false
    }
    if (map[command]?.any { it.first == id } == true) {
      return true
    }
    return false
  }
  
  private fun applyCooldown(map: MutableMap<String, ConcurrentHashMap.KeySetView<Pair<Long, Cooldown>, Boolean>>, command: String, id: Long, duration: Long): Cooldown {
    return Cooldown(
      coroutineScope.launch {
        delay(duration)
        val valueToRemove = map[command]?.find { it.first == id }
        map[command]?.remove(valueToRemove)
      },
      System.currentTimeMillis() + duration
    )
  }
}