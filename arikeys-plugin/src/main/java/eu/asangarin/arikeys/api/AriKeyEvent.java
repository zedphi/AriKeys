package eu.asangarin.arikeys.api;

import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;

@Getter
public abstract class AriKeyEvent extends PlayerEvent {
	private final NamespacedKey id;
	private final boolean registered;

	protected AriKeyEvent(Player player, NamespacedKey id, boolean registered) {
		super(player);
		this.id = id;
		this.registered = registered;
	}
}
