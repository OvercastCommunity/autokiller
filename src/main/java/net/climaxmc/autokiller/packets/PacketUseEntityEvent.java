package net.climaxmc.autokiller.packets;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PacketUseEntityEvent extends Event {

  private final WrapperPlayClientInteractEntity.InteractAction action;
  private final Player attacker;
  private final Entity attacked;
  private static final HandlerList handlers = new HandlerList();

  public PacketUseEntityEvent(
      WrapperPlayClientInteractEntity.InteractAction action, Player attacker, Entity attacked) {
    this.action = action;
    this.attacker = attacker;
    this.attacked = attacked;
  }

  public WrapperPlayClientInteractEntity.InteractAction getAction() {
    return this.action;
  }

  public Player getAttacker() {
    return this.attacker;
  }

  public Entity getAttacked() {
    return this.attacked;
  }

  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
