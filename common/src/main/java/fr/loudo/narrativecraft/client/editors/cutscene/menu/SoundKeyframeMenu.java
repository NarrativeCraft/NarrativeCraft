/*
 * NarrativeCraft - Create narrative games inside Minecraft. No coding, no game engine, only text and logic.
 * Copyright (c) 2025 LOUDO and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package fr.loudo.narrativecraft.client.editors.cutscene.menu;

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.KeyframeMenu;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.SoundKeyframe;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class SoundKeyframeMenu extends KeyframeMenu<SoundKeyframe> {

    private static final int FIELD_LABEL_HEIGHT = 7;
    private static final int FIELD_HEIGHT = 12;
    private static final int FIELD_GAP = 4;
    private static final int FIELD_COUNT = 3;

    private EditBox fieldSoundId;
    private EditBox fieldVolume;
    private EditBox fieldPitch;
    private List<EditBox> fields;

    public SoundKeyframeMenu(SoundKeyframe keyframe) {
        super(keyframe);
    }

    @Override
    protected void initContent() {
        fieldSoundId = createField(keyframe.getSoundId());
        fieldVolume = createField(format(keyframe.getVolume()));
        fieldPitch = createField(format(keyframe.getPitch()));
        fields = List.of(fieldSoundId, fieldVolume, fieldPitch);
    }

    private EditBox createField(String value) {
        EditBox field =
                new EditBox(Minecraft.getInstance().font, 0, 0, width - padding * 2, FIELD_HEIGHT, Component.empty());
        field.setValue(value);
        return field;
    }

    private String format(float value) {
        return String.format(Locale.US, "%.2f", value);
    }

    @Override
    protected int getContentHeight() {
        return (FIELD_LABEL_HEIGHT + FIELD_HEIGHT) * FIELD_COUNT + FIELD_GAP * (FIELD_COUNT - 1);
    }

    @Override
    protected void renderContent(
            GuiGraphics graphics, DeltaTracker delta, int x, int y, int contentWidth, int mouseX, int mouseY) {
        renderField(graphics, delta, fieldSoundId, "Sound ID", x, getFieldY(y, 0), contentWidth, mouseX, mouseY);
        renderField(graphics, delta, fieldVolume, "Volume", x, getFieldY(y, 1), contentWidth, mouseX, mouseY);
        renderField(graphics, delta, fieldPitch, "Pitch", x, getFieldY(y, 2), contentWidth, mouseX, mouseY);
    }

    private void renderField(
            GuiGraphics graphics,
            DeltaTracker delta,
            EditBox field,
            String label,
            int x,
            int y,
            int contentWidth,
            int mouseX,
            int mouseY) {
        graphics.drawString(Minecraft.getInstance().font, label, x, y - 2, 0xFFAAAAAA);
        field.setPosition(x, y + FIELD_LABEL_HEIGHT);
        field.setWidth(contentWidth);
        field.render(graphics, mouseX, mouseY, delta.getGameTimeDeltaTicks());
    }

    private int getFieldY(int y, int index) {
        return y + index * (FIELD_LABEL_HEIGHT + FIELD_HEIGHT + FIELD_GAP);
    }

    @Override
    protected void onContentMouseClicked(
            double mouseX,
            double mouseY,
            int button,
            boolean isDoubleClick,
            int contentX,
            int contentY,
            int contentWidth) {
        for (EditBox field : fields) {
            boolean hovered = mouseX >= field.getX()
                    && mouseX < field.getX() + field.getWidth()
                    && mouseY >= field.getY()
                    && mouseY < field.getY() + field.getHeight();
            field.setFocused(hovered);
            if (hovered) field.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for (EditBox field : fields) {
            if (field.isFocused()) field.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        for (EditBox field : fields) {
            if (field.isFocused()) field.charTyped(codePoint, modifiers);
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        for (EditBox field : fields) {
            if (field.isFocused()) field.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    protected void applyChanges() {
        keyframe.setSoundId(fieldSoundId.getValue().trim());
        keyframe.setVolume(parseFloatOr(fieldVolume, keyframe.getVolume()));
        keyframe.setPitch(parseFloatOr(fieldPitch, keyframe.getPitch()));
    }

    private float parseFloatOr(EditBox field, float fallback) {
        try {
            return Float.parseFloat(field.getValue());
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }
}
