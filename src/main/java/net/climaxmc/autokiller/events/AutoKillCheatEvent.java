package net.climaxmc.autokiller.events;

import java.util.UUID;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AutoKillCheatEvent extends Event {

  private final UUID playerId;
  private final int vl;
  private final String alert;

  public AutoKillCheatEvent(UUID playerId, int vl, String alert) {
    this.playerId = playerId;
    this.vl = vl;
    this.alert = alert;
  }

  public UUID getPlayerId() {
    return playerId;
  }

  public int getVl() {
    return vl;
  }

  public String getAlert() {
    return alert;
  }

  public static final HandlerList handlers = new HandlerList();

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
