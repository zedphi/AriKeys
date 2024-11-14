package eu.asangarin.arikeys.forge.mixin;

import net.minecraft.client.option.KeyBinding;
import net.minecraftforge.client.settings.KeyMappingLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface AKKeyboardForgeMixin {
	@Accessor("KEY_TO_BINDINGS")
	static KeyMappingLookup getKeyBindings() {
		throw new AssertionError();
	}
}
