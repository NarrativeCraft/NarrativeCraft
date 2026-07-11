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
import fr.loudo.narrativecraft.editors.cutscene.keyframes.CameraKeyframe;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.KeyframePosition;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class CameraKeyframeMenu extends KeyframeMenu<CameraKeyframe> {

    private static final int FIELD_LABEL_HEIGHT = 7;
    private static final int FIELD_HEIGHT = 12;
    private static final int FIELD_GAP = 4;
    private static final int FIELD_TOTAL = FIELD_LABEL_HEIGHT + FIELD_HEIGHT + FIELD_GAP;
    private static final int FIELD_COUNT = 6;

    private EditBox fieldX, fieldY, fieldZ, fieldPitch, fieldYaw, fieldRotate;
    private List<EditBox> fields;
    private EasingDropdown easingDropdown;

    public CameraKeyframeMenu(CameraKeyframe keyframe) {
        super(keyframe);
    }

    @Override
    protected void initContent() {
        KeyframePosition pos = keyframe.getPosition();
        Minecraft mc = Minecraft.getInstance();
        int fieldWidth = width - padding * 2;
        fieldX = createField(mc, fieldWidth, String.format(Locale.US, "%.2f", pos.getPosition().x));
        fieldY = createField(mc, fieldWidth, String.format(Locale.US, "%.2f", pos.getPosition().y));
        fieldZ = createField(mc, fieldWidth, String.format(Locale.US, "%.2f", pos.getPosition().z));
        fieldPitch = createField(mc, fieldWidth, String.format(Locale.US, "%.2f", pos.getRotation().x));
        fieldYaw = createField(mc, fieldWidth, String.format(Locale.US, "%.2f", pos.getRotation().y));
        fieldRotate = createField(mc, fieldWidth, String.format(Locale.US, "%.2f", pos.getRotation().z));
        fields = List.of(fieldX, fieldY, fieldZ, fieldPitch, fieldYaw, fieldRotate);
        easingDropdown = new EasingDropdown(keyframe.getEasing());
    }

    private EditBox createField(Minecraft mc, int width, String value) {
        EditBox box = new EditBox(mc.font, 0, 0, width, FIELD_HEIGHT, Component.empty());
        box.setValue(value);
        return box;
    }

    @Override
    protected int getContentHeight() {
        return FIELD_COUNT * FIELD_TOTAL + FIELD_LABEL_HEIGHT + easingDropdown.getHeight();
    }

    @Override
    protected void renderContent(
            GuiGraphicsExtractor graphics, DeltaTracker delta, int x, int y, int contentWidth, int mouseX, int mouseY) {
        String[] labels = {"X", "Y", "Z", "Pitch", "Yaw", "Rotate"};
        EditBox[] boxes = {fieldX, fieldY, fieldZ, fieldPitch, fieldYaw, fieldRotate};

        int currentY = y;
        float partialTick = delta.getGameTimeDeltaTicks();
        for (int i = 0; i < FIELD_COUNT; i++) {
            graphics.text(Minecraft.getInstance().font, labels[i], x, currentY - 2, 0xFFAAAAAA);
            currentY += FIELD_LABEL_HEIGHT;
            boxes[i].setPosition(x, currentY);
            boxes[i].setWidth(contentWidth);
            boxes[i].extractRenderState(graphics, mouseX, mouseY, partialTick);
            currentY += FIELD_HEIGHT + FIELD_GAP;
        }
        graphics.text(Minecraft.getInstance().font, "Easing", x, currentY - 2, 0xFFAAAAAA);
        currentY += FIELD_LABEL_HEIGHT;
        easingDropdown.setPosition(x, currentY);
        easingDropdown.setWidth(contentWidth);
        easingDropdown.render(graphics, mouseX, mouseY);
    }

    @Override
    protected void onContentMouseClicked(
            MouseButtonEvent event, boolean isDoubleClick, int contentX, int contentY, int contentWidth) {
        if (easingDropdown.mouseClicked(event)) {
            for (EditBox field : fields) field.setFocused(false);
            return;
        }
        easingDropdown.close();
        for (EditBox field : fields) {
            boolean hovered = event.x() >= field.getX()
                    && event.x() < field.getX() + field.getWidth()
                    && event.y() >= field.getY()
                    && event.y() < field.getY() + field.getHeight();
            field.setFocused(hovered);
            if (hovered) {
                field.mouseClicked(event, isDoubleClick);
            }
        }
    }

    @Override
    public void mouseScrolled(double amount) {
        easingDropdown.mouseScrolled(amount);
    }

    public void mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        for (EditBox field : fields) {
            if (field.isFocused()) {
                field.mouseDragged(event, dragX, dragY);
                return;
            }
        }
    }

    @Override
    public void charTyped(CharacterEvent event) {
        for (EditBox field : fields) {
            if (field.isFocused()) {
                field.charTyped(event);
                return;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        for (EditBox field : fields) {
            if (field.isFocused()) {
                field.keyPressed(event);
                return;
            }
        }
    }

    @Override
    protected void applyChanges() {
        try {
            KeyframePosition pos = keyframe.getPosition();
            double x = Double.parseDouble(fieldX.getValue());
            double y = Double.parseDouble(fieldY.getValue());
            double z = Double.parseDouble(fieldZ.getValue());
            double pitch = Double.parseDouble(fieldPitch.getValue());
            double yaw = Double.parseDouble(fieldYaw.getValue());
            float rotate = Float.parseFloat(fieldRotate.getValue());

            pos.setPosition(new Vec3(x, y, z));
            pos.setRotation(new Vec3(pitch, yaw, rotate));
        } catch (NumberFormatException ignored) {
        }
        keyframe.setEasing(easingDropdown.getSelected());
    }
}
