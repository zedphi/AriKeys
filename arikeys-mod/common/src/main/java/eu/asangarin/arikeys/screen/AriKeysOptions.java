package eu.asangarin.arikeys.screen;

import eu.asangarin.arikeys.AriKey;
import eu.asangarin.arikeys.AriKeys;
import eu.asangarin.arikeys.util.AriKeysIO;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AriKeysOptions extends GameOptionsScreen {
	public AriKey focusedMKey;
	private AriKeyControlsListWidget keyBindingListWidget;
	private ButtonWidget resetButton;

	public AriKeysOptions(Screen parent, GameOptions options) {
		super(parent, options, Text.translatable("arikeys.controls.title"));
	}

	protected void init() {
		if (client == null) return;
		this.keyBindingListWidget = new AriKeyControlsListWidget(this, this.client);
		this.addSelectableChild(this.keyBindingListWidget);
		this.resetButton = this.addDrawableChild(
				new ButtonWidget(this.width / 2 - 155, this.height - 29, 150, 20, Text.translatable("controls.resetAll"), (button) -> {
					for (AriKey keyBinding : AriKeys.getKeybinds())
						keyBinding.setBoundKey(keyBinding.getKeyCode());
					KeyBinding.updateKeysByCode();
				}));
		this.addDrawableChild(new ButtonWidget(this.width / 2 - 155 + 160, this.height - 29, 150, 20, ScreenTexts.DONE,
				(button) -> this.client.setScreen(this.parent)));
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (this.focusedMKey != null) {

			focusedMKey.setBoundKey(keyCode == GLFW.GLFW_KEY_ESCAPE ? InputUtil.UNKNOWN_KEY : InputUtil.fromKeyCode(keyCode, scanCode));
			AriKeysIO.save();

			this.focusedMKey = null;
			KeyBinding.updateKeysByCode();
			return true;
		} else {
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.focusedMKey != null) {
			focusedMKey.setBoundKey(InputUtil.Type.MOUSE.createFromCode(button));
			AriKeysIO.save();

			this.focusedMKey = null;
			KeyBinding.updateKeysByCode();
			return true;
		} else {
			return super.mouseClicked(mouseX, mouseY, button);
		}
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		this.keyBindingListWidget.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215);
		boolean canReset = false;

		for (AriKey ariKey : AriKeys.getKeybinds()) {
			if (ariKey.hasChanged()) {
				canReset = true;
				break;
			}
		}

		this.resetButton.active = canReset;
		super.render(matrices, mouseX, mouseY, delta);
	}
}
