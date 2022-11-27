package ru.iamdvz.modelika.listeners;

import com.nisovin.magicspells.events.MagicSpellsLoadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MSListener implements Listener {
    @EventHandler
    public void onMagicSpellsLoad(MagicSpellsLoadedEvent event) {
        for (World world : Bukkit.getServer().getWorlds()) {
            for (Entity entity : world.getLivingEntities()) {
                if (entity != null && entity.getScoreboardTags().contains("MODELIKA_MOB")) {
                    entity.remove();
                }
            }
        }
    }
}
