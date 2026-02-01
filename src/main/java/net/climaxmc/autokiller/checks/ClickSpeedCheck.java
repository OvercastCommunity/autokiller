package net.climaxmc.autokiller.checks;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.climaxmc.autokiller.AutoKiller;
import net.climaxmc.autokiller.packets.PacketBlockDigEvent;
import net.climaxmc.autokiller.util.ViaChecker;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ClickSpeedCheck extends Check implements Listener {

  public ClickSpeedCheck(AutoKiller plugin) {
    super(plugin, "Click-Speed");

    new BukkitRunnable() {
      @Override
      public void run() {
        checkForAlert();
      }
    }.runTaskTimer(plugin, 0L, 10L);
    new BukkitRunnable() {
      @Override
      public void run() {
        checkForBan();
      }
    }.runTaskTimer(plugin, 0L, 20L);
    new BukkitRunnable() {
      @Override
      public void run() {
        decreaseAllVL(1);
      }
    }.runTaskTimer(plugin, 0L, 20L * 3);
  }

  public int getCps(UUID uuid) {
    return clicks.getOrDefault(uuid, 0);
  }

  /** Click Speed Check */
  private final Map<UUID, Integer> clicks = new HashMap<>();

  // Tracking for exempt player actions
  private final List<UUID> itemThrowExempt = new ArrayList<>();
  private final List<UUID> breakingExempt = new ArrayList<>();

  @EventHandler
  public void speedCheck(PlayerInteractEvent event) {
    if (!event.getPlayer().getType().equals(EntityType.PLAYER)) return;
    if (!event.getAction().equals(Action.LEFT_CLICK_AIR)) return;

    Player player = event.getPlayer();
    UUID uuid = player.getUniqueId();

    // Some actions require a click to be ignored
    if (isExempt(player, event)) return;

    if (clicks.compute(uuid, (p, c) -> (c != null ? c : 0) + 1) == 1) {
      plugin
          .getServer()
          .getScheduler()
          .runTaskLater(
              plugin,
              () -> {
                Integer currClicks = clicks.get(uuid);

                if (currClicks >= plugin.config.getMaxSpeed()) {
                  if (!isEnabled()) {
                    resetVL(player.getUniqueId());
                    return;
                  }
                  plugin.logCheat(uuid, getName(), currClicks);
                  increaseVL(uuid, 3);
                }
                clicks.put(uuid, 0);
              },
              20L);
    }
  }

  private boolean isExempt(Player player, PlayerInteractEvent event) {
    if (!ViaChecker.isViaVersionInstalled()) return false;

    UUID uuid = player.getUniqueId();

    // Versions 1.15.2 and above create arm animations when throwing items
    // - For every item thrown the player is exempt for 1 click
    if (ViaChecker.atLeast(player, ProtocolVersion.v1_15_2) && itemThrowExempt.remove(uuid)) {
      return true;
    }

    // Versions 1.14.4 and swinging at a block below them and shifting
    // The lower player eye height creates break block and air packets at the same time
    // Player is exempt until:
    // - entity attack
    // - non-air block clicked
    // - block dig end event
    // - shifting stopped
    // This can still false if a player continues to break a restored block
    if (ViaChecker.atLeast(player, ProtocolVersion.v1_14)
        && event.getAction().equals(Action.LEFT_CLICK_AIR)
        && breakingExempt.contains(uuid)) {
      return true;
    } else {
      breakingExempt.remove(uuid);
    }

    // Fishing rods create player animation packets
    if (player.getItemInHand() != null
        && player.getItemInHand().getType().equals(Material.FISHING_ROD)) {
      return true;
    }

    return false;
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    itemThrowExempt.add(event.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onPlayerDigBlock(PacketBlockDigEvent event) {
    // Players eye height is lowered when sneaking so extra range can be achieved
    if (!event.getPlayer().isSneaking() || event.getPlayer().getLocation().getPitch() < 0) return;

    switch (event.getDigType()) {
      case START_DESTROY_BLOCK:
        breakingExempt.add(event.getPlayer().getUniqueId());
        break;
      case STOP_DESTROY_BLOCK:
      case ABORT_DESTROY_BLOCK:
        breakingExempt.remove(event.getPlayer().getUniqueId());
        break;
      default:
        break;
    }
  }

  @EventHandler
  public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
    if (!event.isSneaking()) breakingExempt.remove(event.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onDamage(EntityDamageByEntityEvent event) {
    if (!event.getDamager().getType().equals(EntityType.PLAYER)) return;
    Player player = (Player) event.getDamager();
    breakingExempt.remove(player.getUniqueId());
  }

  @Override
  protected void cleanup(UUID uuid) {
    super.cleanup(uuid);
    clicks.remove(uuid);
    itemThrowExempt.remove(uuid);
    breakingExempt.remove(uuid);
  }
}
