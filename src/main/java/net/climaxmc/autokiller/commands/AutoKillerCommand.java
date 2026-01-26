package net.climaxmc.autokiller.commands;

import net.climaxmc.autokiller.AutoKiller;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AutoKillerCommand implements CommandExecutor {

    private final AutoKiller plugin;
    public AutoKillerCommand(AutoKiller plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (player.hasPermission("autokiller.staff")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    plugin.config.reloadConfig();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&6AutoKiller&8] &7Configuration reloaded!"));
                }
            } else if (args.length == 2) {
                //
            } else {
                player.sendMessage(ChatColor.RED + "/ak <reload>");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
        }

        return true;
    }
}
