package net.climaxmc.autokiller.packets;

import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PacketBlockDigEvent extends Event {

  private final Player player;
  private final DiggingAction digType;
  private final Location blockLocation;

  private static final HandlerList handlers = new HandlerList();

  public PacketBlockDigEvent(Player player, DiggingAction digType, Location blockLocation) {
    this.player = player;
    this.digType = digType;
    this.blockLocation = blockLocation;
  }

  public Player getPlayer() {
    return this.player;
  }

  public DiggingAction getDigType() {
    return digType;
  }

  public Location getBlockLocation() {
    return this.blockLocation;
  }

  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
