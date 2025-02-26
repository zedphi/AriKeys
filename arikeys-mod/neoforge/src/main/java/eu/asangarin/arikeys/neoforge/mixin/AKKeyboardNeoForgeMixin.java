package eu.asangarin.arikeys.neoforge.mixin;

import net.minecraft.client.option.KeyBinding;
import net.neoforged.neoforge.client.settings.KeyMappingLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface AKKeyboardNeoForgeMixin {
	@Accessor("KEY_TO_BINDINGS")
	static KeyMappingLookup getKeyBindings() {
		throw new AssertionError();
	}
}
