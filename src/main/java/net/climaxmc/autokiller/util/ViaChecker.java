package net.climaxmc.autokiller.util;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ViaChecker {

  public static boolean isViaVersionInstalled() {
    Plugin plugin = Bukkit.getPluginManager().getPlugin("ViaVersion");
    return plugin != null && plugin.isEnabled();
  }

  public static ProtocolVersion getPlayerVersion(Player player) {
    if (!isViaVersionInstalled()) {
      return ProtocolVersion.v1_8;
    }

    return Via.getAPI().getPlayerProtocolVersion(player.getUniqueId());
  }

  public static boolean atLeast(Player player, ProtocolVersion version) {
    return getPlayerVersion(player).newerThanOrEqualTo(version);
  }
}
