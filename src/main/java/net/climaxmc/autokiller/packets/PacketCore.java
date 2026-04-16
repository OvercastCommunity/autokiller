package net.climaxmc.autokiller.packets;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import net.climaxmc.autokiller.AutoKiller;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/** Packet method based on Janitor. */
public class PacketCore implements PacketListener {
  public AutoKiller plugin;

  public PacketCore(AutoKiller plugin) {
    this.plugin = plugin;
  }

  @Override
  public void onPacketReceive(PacketReceiveEvent event) {
    Player player = event.getPlayer();
    if (player == null) {
      return;
    }

    if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
      WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
      WrapperPlayClientInteractEntity.InteractAction action = packet.getAction();
      if (action == null) {
        return;
      }
      int entityId = packet.getEntityId();
      final WrapperPlayClientInteractEntity.InteractAction finalAction = action;

      // Run in main thread
      Bukkit.getScheduler()
          .runTask(
              plugin,
              () -> {
                World world = player.getWorld();
                Entity entity = null;
                for (Entity e : world.getEntities()) {
                  if (e.getEntityId() == entityId) {
                    entity = e;
                    break;
                  }
                }
                if (entity == null) {
                  entity = player;
                }
                Bukkit.getServer()
                    .getPluginManager()
                    .callEvent(new PacketUseEntityEvent(finalAction, player, entity));
              });

    } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
      WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
      DiggingAction action = packet.getAction();
      if (action.getId() > 3) {
        return;
      }
      Vector3i pos = packet.getBlockPosition();

      Bukkit.getScheduler()
          .runTask(
              plugin,
              () -> {
                Location blockLocation = new Location(player.getWorld(), pos.x, pos.y, pos.z);
                Bukkit.getServer()
                    .getPluginManager()
                    .callEvent(new PacketBlockDigEvent(player, action, blockLocation));
              });
    }
  }
}
