package net.climaxmc.autokiller.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.climaxmc.autokiller.AutoKiller;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/** Packet method based on Janitor. */
public class PacketCore {
  public AutoKiller plugin;
  private static final boolean LEGACY = Bukkit.getServer().getVersion().contains("SportPaper");

  public PacketCore(AutoKiller plugin) {
    this.plugin = plugin;
    ProtocolLibrary.getProtocolManager()
        .addPacketListener(
            new PacketAdapter(plugin, PacketType.Play.Client.USE_ENTITY) {
              public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                if (player == null) {
                  return;
                }

                EnumWrappers.EntityUseAction type = null;
                int entityId = packet.getIntegers().read(0);

                try {
                  if (LEGACY) {
                    type = packet.getEntityUseActions().read(0);
                  } else {
                    Object action = packet.getModifier().read(1);
                    if (action != null) {
                      String className = action.getClass().getSimpleName();
                      type =
                          switch (className) {
                            case "" -> EnumWrappers.EntityUseAction.ATTACK;
                            case "InteractionAtLocationAction" ->
                                EnumWrappers.EntityUseAction.INTERACT_AT;
                            case "InteractionAction" -> EnumWrappers.EntityUseAction.INTERACT;
                            default -> null;
                          };
                    }
                  }
                } catch (Exception e) {
                  plugin
                      .getLogger()
                      .warning("Failed to read USE_ENTITY action type: " + e.getMessage());
                  return;
                }

                if (type == null) {
                  return;
                }

                final EnumWrappers.EntityUseAction finalType = type;

                // Run in main thread
                Bukkit.getScheduler()
                    .runTask(
                        plugin,
                        () -> {
                          Entity entity = null;
                          for (World worlds : Bukkit.getWorlds()) {
                            for (Entity entities : worlds.getEntities()) {
                              if (entities.getEntityId() == entityId) {
                                entity = entities;
                              }
                            }
                          }
                          if (entity == null) {
                            entity = player;
                          }
                          Bukkit.getServer()
                              .getPluginManager()
                              .callEvent(new PacketUseEntityEvent(finalType, player, entity));
                        });
              }
            });

    ProtocolLibrary.getProtocolManager()
        .addPacketListener(
            new PacketAdapter(plugin, PacketType.Play.Client.BLOCK_DIG) {
              public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                if (player == null) {
                  return;
                }
                // Ignore non-block break packets
                EnumWrappers.PlayerDigType digType = packet.getPlayerDigTypes().read(0);
                if (digType.ordinal() > 3) {
                  return;
                }
                Location blockLocation =
                    new Location(
                        player.getWorld(),
                        packet.getBlockPositionModifier().read(0).getX(),
                        packet.getBlockPositionModifier().read(0).getY(),
                        packet.getBlockPositionModifier().read(0).getZ());
                Bukkit.getScheduler()
                    .runTask(
                        plugin,
                        () ->
                            Bukkit.getServer()
                                .getPluginManager()
                                .callEvent(
                                    new PacketBlockDigEvent(player, digType, blockLocation)));
              }
            });

    ProtocolLibrary.getProtocolManager()
        .addPacketListener(
            new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_STATUS) {
              @Override
              public void onPacketSending(PacketEvent event) {
                event.setCancelled(false);
              }
            });
    ProtocolLibrary.getProtocolManager()
        .addPacketListener(
            new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_VELOCITY) {
              @Override
              public void onPacketSending(PacketEvent event) {
                event.setCancelled(false);
              }
            });
    ProtocolLibrary.getProtocolManager()
        .addPacketListener(
            new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_EFFECT) {
              @Override
              public void onPacketSending(PacketEvent event) {
                event.setCancelled(false);
              }
            });
    ProtocolLibrary.getProtocolManager()
        .addPacketListener(
            new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_LOOK) {
              @Override
              public void onPacketSending(PacketEvent event) {
                event.setCancelled(false);
              }
            });
    ProtocolLibrary.getProtocolManager()
        .addPacketListener(
            new PacketAdapter(plugin, PacketType.Play.Server.REL_ENTITY_MOVE) {
              @Override
              public void onPacketSending(PacketEvent event) {
                event.setCancelled(false);
              }
            });
    ProtocolLibrary.getProtocolManager()
        .addPacketListener(
            new PacketAdapter(plugin, PacketType.Play.Server.REL_ENTITY_MOVE_LOOK) {
              @Override
              public void onPacketSending(PacketEvent event) {
                event.setCancelled(false);
              }
            });
    ProtocolLibrary.getProtocolManager()
        .addPacketListener(
            new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA) {
              @Override
              public void onPacketSending(PacketEvent event) {
                event.setCancelled(false);
              }
            });
  }
}
