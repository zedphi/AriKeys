package eu.asangarin.arikeys.screen;

import com.google.common.collect.ImmutableList;
import eu.asangarin.arikeys.AriKey;
import eu.asangarin.arikeys.AriKeys;
import eu.asangarin.arikeys.util.AriKeysIO;
import eu.asangarin.arikeys.util.ModifierKey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** TODO: Get this up-to-date, for version 1.20.6 and above */
public class AriKeyControlsListWidget extends ElementListWidget<AriKeyControlsListWidget.Entry> {
	final AriKeysOptions parent;
	int maxKeyNameLength;

	public AriKeyControlsListWidget(AriKeysOptions parent, MinecraftClient client) {
		super(client, parent.width + 45, parent.height - 52, 20, 20);
		this.parent = parent;
		String category = null;

		for (AriKey ariKey : AriKeys.getCategorySortedKeybinds()) {
			String keyCat = ariKey.getCategory();
			if (!keyCat.equals(category)) {
				category = keyCat;
				this.addEntry(new CategoryEntry(Text.literal(keyCat)));
			}

			Text text = Text.literal(ariKey.getName());
			int i = client.textRenderer.getWidth(text);
			if (i > this.maxKeyNameLength) {
				this.maxKeyNameLength = i;
			}

			this.addEntry(new KeyBindingEntry(ariKey, text));
		}

	}

	public int getRowWidth() {
		return super.getRowWidth() + 32;
	}

	public class CategoryEntry extends AriKeyControlsListWidget.Entry {
		final Text text;
		private final int textWidth;

		public CategoryEntry(Text text) {
			this.text = text;
			this.textWidth = AriKeyControlsListWidget.this.client.textRenderer.getWidth(this.text);
		}

		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			assert client.currentScreen != null;
			int width = (client.currentScreen.width / 2 - this.textWidth / 2);
			int height = y + entryHeight;
			context.drawText(client.textRenderer, this.text, width, height - 9 - 1, 16777215, false);
		}

		public List<? extends Element> children() {
			return Collections.emptyList();
		}

		public List<? extends Selectable> selectableChildren() {
			return ImmutableList.of(new Selectable() {
				public Selectable.SelectionType getType() {
					return Selectable.SelectionType.HOVERED;
				}

				public void appendNarrations(NarrationMessageBuilder builder) {
					builder.put(NarrationPart.TITLE, CategoryEntry.this.text);
				}
			});
		}
	}

	public class KeyBindingEntry extends AriKeyControlsListWidget.Entry {
		private final AriKey ariKey;
		private final Text bindingName;
		private final ButtonWidget editButton;
		private final ButtonWidget resetButton;

		KeyBindingEntry(AriKey ariKey, Text bindingName) {
			this.ariKey = ariKey;
			this.bindingName = bindingName;

			this.editButton = ButtonWidget.builder(bindingName, (button) -> AriKeyControlsListWidget.this.parent.focusedMKey = ariKey)
					.dimensions(0, 0, 135, 20).narrationSupplier(
							supplier -> ariKey.isUnbound() ? Text.translatable("narrator.controls.unbound", bindingName) : Text.translatable(
									"narrator.controls.bound", bindingName, supplier.get())).build();

			this.resetButton = ButtonWidget.builder(Text.translatable("controls.reset"), (button) -> {
				ariKey.setBoundKey(ariKey.getKeyCode(), false);
				ariKey.resetBoundModifiers();
				AriKeysIO.save();
				KeyBinding.updateKeysByCode();
			}).dimensions(0, 0, 50, 20).narrationSupplier(supplier -> Text.translatable("narrator.controls.reset", bindingName)).build();
		}

		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			boolean bl = parent.focusedMKey == this.ariKey;
			int width = x + 20 - maxKeyNameLength;
			int height = y + entryHeight / 2;
			context.drawText(client.textRenderer, this.bindingName, width, height - 9 / 2, 16777215, false);

			this.resetButton.setX(x + 210);
			this.resetButton.setY(y);
			this.resetButton.active = this.ariKey.hasChanged();
			this.resetButton.render(context, mouseX, mouseY, tickDelta);
			this.editButton.setX(x + 65);
			this.editButton.setY(y);
			MutableText editMessage = Text.empty();
			for (ModifierKey modifier : this.ariKey.getBoundModifiers()) {
				editMessage.append(Text.translatable(modifier.getTranslationKey()));
				editMessage.append(Text.literal(" + "));
			}
			editMessage.append(this.ariKey.getBoundKeyCode().getLocalizedText().copyContentOnly());
			editMessage = editMessage.copy();
			boolean bl2 = false;
			if (!this.ariKey.isUnbound()) {
				final List<KeyBinding> bindings = new ArrayList<>(List.of(client.options.allKeys));
				for (KeyBinding keyBinding : bindings) {
					if (keyBinding.getBoundKeyTranslationKey().equals(ariKey.getBoundKeyCode().getTranslationKey()) && ariKey.getBoundModifiers()
							.isEmpty()) {
						bl2 = true;
						break;
					}
				}
				for (AriKey key : AriKeys.getKeybinds()) {
					if (!key.equals(ariKey) && key.getBoundKeyCode().equals(ariKey.getBoundKeyCode())) {
						if (key.testModifiers(ariKey.getBoundModifiers())) {
							bl2 = true;
							break;
						}
					}
				}
			}

			if (bl) {
				this.editButton.setMessage((Text.literal("> ")).append(editMessage.formatted(Formatting.YELLOW)).append(" <").formatted(Formatting.YELLOW));
			} else if (bl2) {
				this.editButton.setMessage(editMessage.formatted(Formatting.RED));
			} else this.editButton.setMessage(editMessage);

			this.editButton.render(context, mouseX, mouseY, tickDelta);
		}

		public List<? extends Element> children() {
			return ImmutableList.of(this.editButton, this.resetButton);
		}

		public List<? extends Selectable> selectableChildren() {
			return ImmutableList.of(this.editButton, this.resetButton);
		}

		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (this.editButton.mouseClicked(mouseX, mouseY, button)) {
				return true;
			} else {
				return this.resetButton.mouseClicked(mouseX, mouseY, button);
			}
		}

		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			return this.editButton.mouseReleased(mouseX, mouseY, button) || this.resetButton.mouseReleased(mouseX, mouseY, button);
		}
	}

	public abstract static class Entry extends ElementListWidget.Entry<AriKeyControlsListWidget.Entry> {
	}
}
