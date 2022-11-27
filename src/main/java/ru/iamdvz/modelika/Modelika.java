package ru.iamdvz.modelika;

import org.bukkit.plugin.java.JavaPlugin;
import ru.iamdvz.modelika.listeners.MSListener;

public final class Modelika extends JavaPlugin {
    private static Modelika instance;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new MSListener(), this);

    }

    public static Modelika getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
    }
}
