package ru.iamdvz.modelika.listeners

import com.nisovin.magicspells.events.MagicSpellsLoadedEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MSListener : Listener {
    @EventHandler
    fun onMagicSpellsLoad(event: MagicSpellsLoadedEvent?) {
        for (world in Bukkit.getServer().worlds) {
            for (entity in world.livingEntities) {
                if (entity != null && entity.scoreboardTags.contains("MODELIKA_MOB")) {
                    entity.remove()
                }
            }
        }
    }
}