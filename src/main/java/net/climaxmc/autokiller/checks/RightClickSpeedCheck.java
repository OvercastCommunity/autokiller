package net.climaxmc.autokiller.checks;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.climaxmc.autokiller.AutoKiller;
import net.climaxmc.autokiller.util.ViaChecker;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RightClickSpeedCheck extends Check implements Listener {

    public RightClickSpeedCheck(AutoKiller plugin) {
        super(plugin, "RClick-Speed");

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

    /**
     * Click Speed Check
     */

    private final Map<UUID, Integer> clicks = new HashMap<>();

    // Tracking for exempt player actions
    private final Map<UUID, Long> debounce = new HashMap<>();

    @EventHandler
    public void speedCheck(PlayerInteractEvent event) {
        if (!event.getPlayer().getType().equals(EntityType.PLAYER)) return;
        // Only check for right click events
        if (!(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (isExempt(player, event)) return;

        if (clicks.compute(uuid, (p, c) -> (c != null ? c : 0) + 1) == 1) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
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
            }, 20L);
        }
    }

    private boolean isExempt(Player player, PlayerInteractEvent event) {
        UUID uuid = player.getUniqueId();

        long now = System.currentTimeMillis();
        boolean holdingItem = player.getItemInHand() != null;

        // Players on versions where shifting lowers their eye height cause double packets
        // A right click block then right click air packet is sent back to back when the block
        // is out of range of a non-shifting player on native server version
        // TODO: idk if this version is the right one
        if (ViaChecker.atLeast(player, ProtocolVersion.v1_14) && player.isSneaking() && holdingItem) {
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                debounce.put(uuid, now);
            } else if (event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                Long debounceTime = debounce.remove(uuid);
                if (debounceTime != null && now - debounceTime <= 5) {
                    return true;
                }
            }
        }

        // Fishing rods create player animation packets
        if (holdingItem && player.getItemInHand().getType().equals(Material.FISHING_ROD)) {
            return true;
        }

        return false;
    }

    @Override
    protected void cleanup(UUID uuid) {
        super.cleanup(uuid);
        clicks.remove(uuid);
        debounce.remove(uuid);
    }
}
