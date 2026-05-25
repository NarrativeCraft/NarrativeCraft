package fr.loudo.narrativecraft.client.editors.widgets;

import fr.loudo.narrativecraft.utils.Translation;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public class ToggleButton extends AbstractButton {

    private boolean value;
    private final Consumer<Boolean> onChange;

    public ToggleButton(int x, int y, int width, int height, boolean initialValue, Consumer<Boolean> onChange) {
        super(x, y, width, height, Component.empty());
        this.value = initialValue;
        this.onChange = onChange;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        value = !value;
        if (onChange != null) onChange.accept(value);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        int x = getX(), y = getY(), width = getWidth(), height = getHeight();
        graphics.fill(x, y, x + width, y + height, isHovered ? 0xFF555555 : 0xFF333333);
        String text = value
                ? Translation.message("widget.toggle.on").getString()
                : Translation.message("widget.toggle.off").getString();
        graphics.text(mc.font, text, x + width / 2 - mc.font.width(text) / 2, y + (height - 8) / 2, 0xFFFFFFFF);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {}
}
