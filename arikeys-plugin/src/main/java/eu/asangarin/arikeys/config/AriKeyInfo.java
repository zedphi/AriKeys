package eu.asangarin.arikeys.config;

import eu.asangarin.arikeys.AriKeysPlugin;
import eu.asangarin.arikeys.compat.MythicMobsCompat;
import eu.asangarin.arikeys.util.ModifierKey;
import eu.asangarin.arikeys.util.PressType;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@Getter
public class AriKeyInfo {
	private static final KeyExecutor DEFAULT = (type) -> {};

	private final NamespacedKey id;
	private final String name, category, command, mythicPress, mythicRelease;
	private final int def;
	private final Set<ModifierKey> modifiers;
	private final KeyExecutor executor;

	private AriKeyInfo(NamespacedKey id, String name, String category, int def, Set<ModifierKey> modifiers, String command, String mythicPress, String mythicRelease, KeyExecutor executor) {
		this.id = id;
		this.name = name;
		this.category = category;
		this.def = def;
		this.modifiers = modifiers;
		this.command = command;
		this.mythicPress = mythicPress;
		this.mythicRelease = mythicRelease;
		this.executor = executor;
	}

	// Using a static method to insert KeyInfo verification code.
	public static @Nullable AriKeyInfo from(ConfigurationSection config) {
		if (config.contains("Id")) {
			NamespacedKey key = NamespacedKey.fromString(config.getString("Id", ""), AriKeysPlugin.get());
			if (key == null) return null;

			String name;
			String category;
			int defaultKey;

			if (config.contains("Name") && config.contains("DefaultKey") && config.contains("Category")) {
				name = config.getString("Name");
				category = config.getString("Category");
				defaultKey = config.getInt("DefaultKey");
			} else if (key.getNamespace().equals("minecraft")) {
				name = "name";
				category = "category";
				defaultKey = 0;
			} else {
				return null;
			}

			Set<ModifierKey> modifiers = new HashSet<>();
			for (String modifier : config.getStringList("Modifiers")) {
				ModifierKey modKey = ModifierKey.fromString(modifier);
				if (modKey != ModifierKey.NONE) modifiers.add(modKey);
			}

			return new AriKeyInfo(key, name, category, defaultKey, modifiers, config.getString("RunCommand", ""),
					config.getString("SkillPress", ""), config.getString("SkillRelease", ""), DEFAULT);
		}

		return null;
	}

	public boolean runCommand(Player player) {
		if (command == null || command.isEmpty()) return false;

		final boolean isAdmin = command.startsWith("!");
		String cmd = (isAdmin ? command.substring(1) : command).replace("%player%", player.getName());
		if (AriKeysPlugin.get().papi) cmd = PlaceholderAPI.setPlaceholders(player, cmd);

		if (isAdmin) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
		else Bukkit.dispatchCommand(player, cmd);
		return true;
	}

	public boolean hasCommand() {
		return checkStr(command);
	}

	public boolean hasMM(boolean press) {
		return press ? checkStr(mythicPress) : checkStr(mythicRelease);
	}

	private boolean checkStr(String str) {
		return str != null && !str.isEmpty();
	}

	public void mmSkill(Player player, boolean press) {
		if (!AriKeysPlugin.get().mm) return;
		MythicMobsCompat.runSkill(press ? mythicPress : mythicRelease, player);
	}

	public static class Builder {
		NamespacedKey id;
		String name, category, command, mythicPress, mythicRelease;
		int def;
		Set<ModifierKey> modifiers = new HashSet<>();
		KeyExecutor executor;

		public Builder setId(NamespacedKey identifier) {
			this.id = identifier;
			return this;
		}

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder setCategory(String category) {
			this.category = category;
			return this;
		}

		public Builder setCommand(String command) {
			this.command = command;
			return this;
		}

		public Builder setDefaultKey(int defKeyId) {
			this.def = defKeyId;
			return this;
		}

		public Builder addModifier(ModifierKey modKey) {
			this.modifiers.add(modKey);
			return this;
		}

		public Builder setMythicPressSkill(String skill) {
			this.mythicPress = skill;
			return this;
		}

		public Builder setMythicReleaseSkill(String skill) {
			this.mythicRelease = skill;
			return this;
		}

		public Builder setKeyExecutor(KeyExecutor executor) {
			this.executor = executor;
			return this;
		}

		public AriKeyInfo build() {
			return new AriKeyInfo(id, name, category, def, modifiers, command, mythicPress, mythicRelease, executor);
		}
	}

	@FunctionalInterface
	public interface KeyExecutor {
		void execute(PressType type);
	}
}
