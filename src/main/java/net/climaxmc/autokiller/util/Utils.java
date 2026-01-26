package net.climaxmc.autokiller.util;

import net.climaxmc.autokiller.AutoKiller;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Utils {

    private final AutoKiller plugin;

    public Utils(AutoKiller plugin) {
        this.plugin = plugin;
    }

    public static int getPing(Player player) {
        return player.spigot().getPing();
    }
}
