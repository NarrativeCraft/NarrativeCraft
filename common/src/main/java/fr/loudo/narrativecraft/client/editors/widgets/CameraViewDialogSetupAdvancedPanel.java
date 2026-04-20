/*
 * NarrativeCraft - Create your own stories, easily, and freely in Minecraft.
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

package fr.loudo.narrativecraft.client.editors.widgets;

import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraViewDialogSetup;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public class CameraViewDialogSetupAdvancedPanel {

    private static final int PANEL_X = 5;
    private static final int PANEL_WIDTH = 172;
    private static final int PANEL_PADDING = 4;
    private static final int ROW_HEIGHT = 16;
    private static final int ROW_GAP = 2;
    private static final int LABEL_WIDTH = 84;

    private CameraViewDialogSetup setup;
    private boolean visible = false;

    private EditBox widthBox;
    private EditBox paddingXBox;
    private EditBox paddingYBox;
    private EditBox letterSpacingBox;
    private EditBox lineGapBox;
    private EditBox scrollSpeedBox;
    private EditBox skipSecondsBox;
    private EditBox backgroundImageBox;
    private EditBox letterSoundBox;

    private int tailVisibleRowY = -1;
    private int autoSkipRowY = -1;
    private int soundMutedRowY = -1;
    private int alignmentRowY = -1;
    private int closeRowY = -1;

    public void setSetup(CameraViewDialogSetup setup) {
        this.setup = setup;
        rebuildEditBoxes();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (!visible) unfocusAll();
    }

    public boolean isVisible() {
        return visible;
    }

    private void rebuildEditBoxes() {
        if (setup == null) return;
        Minecraft mc = Minecraft.getInstance();
        DialogData data = setup.getDialogData();

        widthBox = makeBox(mc, 16, String.format("%.2f", data.getWidth()), text -> {
            try {
                data.setWidth(Float.parseFloat(text));
            } catch (NumberFormatException ignored) {
            }
        });
        paddingXBox = makeBox(mc, 16, String.format("%.2f", data.getPaddingX()), text -> {
            try {
                data.setPaddingX(Float.parseFloat(text));
            } catch (NumberFormatException ignored) {
            }
        });
        paddingYBox = makeBox(mc, 16, String.format("%.2f", data.getPaddingY()), text -> {
            try {
                data.setPaddingY(Float.parseFloat(text));
            } catch (NumberFormatException ignored) {
            }
        });
        letterSpacingBox = makeBox(mc, 16, String.format("%.2f", data.getLetterSpacing()), text -> {
            try {
                data.setLetterSpacing(Float.parseFloat(text));
            } catch (NumberFormatException ignored) {
            }
        });
        lineGapBox = makeBox(mc, 16, String.format("%.2f", data.getLineGap()), text -> {
            try {
                data.setLineGap(Float.parseFloat(text));
            } catch (NumberFormatException ignored) {
            }
        });
        scrollSpeedBox = makeBox(mc, 16, String.format("%.2f", data.getScrollSpeed()), text -> {
            try {
                data.setScrollSpeed(Float.parseFloat(text));
            } catch (NumberFormatException ignored) {
            }
        });
        skipSecondsBox = data.isAutoSkipEnabled()
                ? makeBox(mc, 16, String.format("%.2f", data.getAutoSkipSeconds()), text -> {
                    try {
                        data.setAutoSkipSeconds(Float.parseFloat(text));
                    } catch (NumberFormatException ignored) {
                    }
                })
                : null;
        backgroundImageBox = makeBox(
                mc,
                256,
                data.getBackgroundImage() != null ? data.getBackgroundImage().toString() : "",
                text -> {
                    if (text.isEmpty()) {
                        data.setBackgroundImage(null);
                        return;
                    }
                    try {
                        data.setBackgroundImage(Identifier.parse(text));
                    } catch (Exception ignored) {
                    }
                });
        letterSoundBox = makeBox(
                mc, 256, data.getLetterSound() != null ? data.getLetterSound().toString() : "", text -> {
                    if (text.isEmpty()) {
                        data.setLetterSound(null);
                        return;
                    }
                    try {
                        data.setLetterSound(Identifier.parse(text));
                    } catch (Exception ignored) {
                    }
                });
    }

    private EditBox makeBox(Minecraft mc, int maxLength, String value, Consumer<String> responder) {
        int editBoxWidth = PANEL_WIDTH - PANEL_PADDING * 2 - LABEL_WIDTH - 2;
        EditBox box = new EditBox(mc.font, 0, 0, editBoxWidth, 10, Component.empty());
        box.setMaxLength(maxLength);
        box.setValue(value);
        box.setResponder(responder);
        return box;
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!visible || setup == null) return;
        Minecraft mc = Minecraft.getInstance();
        DialogData data = setup.getDialogData();

        int panelTop = 5;
        int panelHeight = computePanelHeight(data);
        int panelBottom = panelTop + panelHeight;

        graphics.fill(PANEL_X, panelTop, PANEL_X + PANEL_WIDTH, panelBottom, ARGB.color(210, 0, 0, 0));
        graphics.fill(PANEL_X, panelTop, PANEL_X + PANEL_WIDTH, panelTop + 1, 0xFFAAAAAA);
        graphics.fill(PANEL_X, panelBottom - 1, PANEL_X + PANEL_WIDTH, panelBottom, 0xFFAAAAAA);
        graphics.fill(PANEL_X, panelTop, PANEL_X + 1, panelBottom, 0xFFAAAAAA);
        graphics.fill(PANEL_X + PANEL_WIDTH - 1, panelTop, PANEL_X + PANEL_WIDTH, panelBottom, 0xFFAAAAAA);

        int contentX = PANEL_X + PANEL_PADDING;
        int editBoxX = contentX + LABEL_WIDTH + 2;
        int editBoxWidth = PANEL_WIDTH - PANEL_PADDING * 2 - LABEL_WIDTH - 2;
        int y = 14;

        y = renderFieldRow(graphics, mc, "Width:", contentX, editBoxX, y, widthBox, mouseX, mouseY);
        y = renderFieldRow(graphics, mc, "Padding X:", contentX, editBoxX, y, paddingXBox, mouseX, mouseY);
        y = renderFieldRow(graphics, mc, "Padding Y:", contentX, editBoxX, y, paddingYBox, mouseX, mouseY);
        y = renderFieldRow(graphics, mc, "Letter Spacing:", contentX, editBoxX, y, letterSpacingBox, mouseX, mouseY);
        y = renderFieldRow(graphics, mc, "Line Gap:", contentX, editBoxX, y, lineGapBox, mouseX, mouseY);
        y = renderFieldRow(graphics, mc, "Scroll Speed:", contentX, editBoxX, y, scrollSpeedBox, mouseX, mouseY);

        tailVisibleRowY = y;
        y = renderBoolRow(
                graphics,
                mc,
                "Tail Visible:",
                contentX,
                editBoxX,
                editBoxWidth,
                y,
                data.isTailVisible(),
                mouseX,
                mouseY);

        autoSkipRowY = y;
        y = renderBoolRow(
                graphics,
                mc,
                "Auto Skip:",
                contentX,
                editBoxX,
                editBoxWidth,
                y,
                data.isAutoSkipEnabled(),
                mouseX,
                mouseY);

        if (data.isAutoSkipEnabled()) {
            y = renderFieldRow(graphics, mc, "Skip Seconds:", contentX, editBoxX, y, skipSecondsBox, mouseX, mouseY);
        }

        soundMutedRowY = y;
        y = renderBoolRow(
                graphics, mc, "Sound Muted:", contentX, editBoxX, editBoxWidth, y, data.isSoundMuted(), mouseX, mouseY);

        alignmentRowY = y;
        y = renderAlignmentRow(graphics, mc, contentX, editBoxX, editBoxWidth, y, data, mouseX, mouseY);

        y = renderFieldRow(graphics, mc, "BG Image:", contentX, editBoxX, y, backgroundImageBox, mouseX, mouseY);
        y = renderFieldRow(graphics, mc, "Letter Sound:", contentX, editBoxX, y, letterSoundBox, mouseX, mouseY);

        y += ROW_GAP * 2;
        closeRowY = y;
        boolean closeHover = isOver(mouseX, mouseY, contentX, y, 90, ROW_HEIGHT);
        graphics.fill(contentX, y, contentX + 90, y + ROW_HEIGHT, closeHover ? 0xFF666666 : 0xFF444444);
        String closeText = "Close";
        graphics.text(
                mc.font, closeText, contentX + 45 - mc.font.width(closeText) / 2, y + (ROW_HEIGHT - 8) / 2, 0xFFFFFFFF);
    }

    private int renderFieldRow(
            GuiGraphicsExtractor graphics,
            Minecraft mc,
            String label,
            int contentX,
            int editBoxX,
            int y,
            EditBox box,
            int mouseX,
            int mouseY) {
        graphics.text(mc.font, label, contentX, y + 3, 0xFFCCCCCC);
        if (box != null) {
            box.setX(editBoxX);
            box.setY(y);
            box.extractRenderState(graphics, mouseX, mouseY, 0);
        }
        return y + ROW_HEIGHT + ROW_GAP;
    }

    private int renderBoolRow(
            GuiGraphicsExtractor graphics,
            Minecraft mc,
            String label,
            int contentX,
            int editBoxX,
            int editBoxWidth,
            int y,
            boolean value,
            int mouseX,
            int mouseY) {
        graphics.text(mc.font, label, contentX, y + 3, 0xFFCCCCCC);
        boolean hover = isOver(mouseX, mouseY, editBoxX, y, editBoxWidth, ROW_HEIGHT);
        graphics.fill(editBoxX, y, editBoxX + editBoxWidth, y + ROW_HEIGHT, hover ? 0xFF555555 : 0xFF333333);
        String valueText = value ? "On" : "Off";
        graphics.text(
                mc.font,
                valueText,
                editBoxX + editBoxWidth / 2 - mc.font.width(valueText) / 2,
                y + (ROW_HEIGHT - 8) / 2,
                0xFFFFFFFF);
        return y + ROW_HEIGHT + ROW_GAP;
    }

    private int renderAlignmentRow(
            GuiGraphicsExtractor graphics,
            Minecraft mc,
            int contentX,
            int editBoxX,
            int editBoxWidth,
            int y,
            DialogData data,
            int mouseX,
            int mouseY) {
        graphics.text(mc.font, "Alignment:", contentX, y + 3, 0xFFCCCCCC);
        DialogData.TextAlignment current = data.getTextAlignment();
        DialogData.TextAlignment[] alignments = DialogData.TextAlignment.values();
        int buttonWidth = editBoxWidth / 3 - 1;
        for (int i = 0; i < alignments.length; i++) {
            DialogData.TextAlignment alignment = alignments[i];
            int btnX = editBoxX + i * (buttonWidth + 1);
            boolean active = current == alignment;
            boolean hover = !active && isOver(mouseX, mouseY, btnX, y, buttonWidth, ROW_HEIGHT);
            graphics.fill(
                    btnX,
                    y,
                    btnX + buttonWidth,
                    y + ROW_HEIGHT,
                    active ? 0xFF4466AA : (hover ? 0xFF555555 : 0xFF333333));
            String letter = alignment.name().substring(0, 1);
            graphics.text(
                    mc.font,
                    letter,
                    btnX + buttonWidth / 2 - mc.font.width(letter) / 2,
                    y + (ROW_HEIGHT - 8) / 2,
                    0xFFFFFFFF);
        }
        return y + ROW_HEIGHT + ROW_GAP;
    }

    public boolean mouseClicked(MouseButtonEvent event) {
        if (!visible || setup == null) return false;
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();
        DialogData data = setup.getDialogData();
        if (!isOver(mouseX, mouseY, PANEL_X, 5, PANEL_WIDTH, computePanelHeight(data))) return false;

        int contentX = PANEL_X + PANEL_PADDING;
        int editBoxX = contentX + LABEL_WIDTH + 2;
        int editBoxWidth = PANEL_WIDTH - PANEL_PADDING * 2 - LABEL_WIDTH - 2;

        unfocusAll();

        if (tryFocus(widthBox, mouseX, mouseY, event)) return true;
        if (tryFocus(paddingXBox, mouseX, mouseY, event)) return true;
        if (tryFocus(paddingYBox, mouseX, mouseY, event)) return true;
        if (tryFocus(letterSpacingBox, mouseX, mouseY, event)) return true;
        if (tryFocus(lineGapBox, mouseX, mouseY, event)) return true;
        if (tryFocus(scrollSpeedBox, mouseX, mouseY, event)) return true;
        if (skipSecondsBox != null && tryFocus(skipSecondsBox, mouseX, mouseY, event)) return true;
        if (tryFocus(backgroundImageBox, mouseX, mouseY, event)) return true;
        if (tryFocus(letterSoundBox, mouseX, mouseY, event)) return true;

        if (tailVisibleRowY >= 0 && isOver(mouseX, mouseY, editBoxX, tailVisibleRowY, editBoxWidth, ROW_HEIGHT)) {
            data.setTailVisible(!data.isTailVisible());
            return true;
        }
        if (autoSkipRowY >= 0 && isOver(mouseX, mouseY, editBoxX, autoSkipRowY, editBoxWidth, ROW_HEIGHT)) {
            data.setAutoSkipEnabled(!data.isAutoSkipEnabled());
            rebuildEditBoxes();
            return true;
        }
        if (soundMutedRowY >= 0 && isOver(mouseX, mouseY, editBoxX, soundMutedRowY, editBoxWidth, ROW_HEIGHT)) {
            data.setSoundMuted(!data.isSoundMuted());
            return true;
        }
        if (alignmentRowY >= 0) {
            DialogData.TextAlignment[] alignments = DialogData.TextAlignment.values();
            int buttonWidth = editBoxWidth / 3 - 1;
            for (int i = 0; i < alignments.length; i++) {
                int btnX = editBoxX + i * (buttonWidth + 1);
                if (isOver(mouseX, mouseY, btnX, alignmentRowY, buttonWidth, ROW_HEIGHT)) {
                    data.setTextAlignment(alignments[i]);
                    return true;
                }
            }
        }
        if (closeRowY >= 0 && isOver(mouseX, mouseY, contentX, closeRowY, 90, ROW_HEIGHT)) {
            visible = false;
            return true;
        }
        return true;
    }

    public void keyPressed(KeyEvent event) {
        forwardKeyToFocused(widthBox, event);
        forwardKeyToFocused(paddingXBox, event);
        forwardKeyToFocused(paddingYBox, event);
        forwardKeyToFocused(letterSpacingBox, event);
        forwardKeyToFocused(lineGapBox, event);
        forwardKeyToFocused(scrollSpeedBox, event);
        forwardKeyToFocused(skipSecondsBox, event);
        forwardKeyToFocused(backgroundImageBox, event);
        forwardKeyToFocused(letterSoundBox, event);
    }

    public void charTyped(CharacterEvent event) {
        forwardCharToFocused(widthBox, event);
        forwardCharToFocused(paddingXBox, event);
        forwardCharToFocused(paddingYBox, event);
        forwardCharToFocused(letterSpacingBox, event);
        forwardCharToFocused(lineGapBox, event);
        forwardCharToFocused(scrollSpeedBox, event);
        forwardCharToFocused(skipSecondsBox, event);
        forwardCharToFocused(backgroundImageBox, event);
        forwardCharToFocused(letterSoundBox, event);
    }

    public boolean isAnyBoxFocused() {
        return isFocused(widthBox)
                || isFocused(paddingXBox)
                || isFocused(paddingYBox)
                || isFocused(letterSpacingBox)
                || isFocused(lineGapBox)
                || isFocused(scrollSpeedBox)
                || isFocused(skipSecondsBox)
                || isFocused(backgroundImageBox)
                || isFocused(letterSoundBox);
    }

    private void unfocusAll() {
        setFocus(widthBox, false);
        setFocus(paddingXBox, false);
        setFocus(paddingYBox, false);
        setFocus(letterSpacingBox, false);
        setFocus(lineGapBox, false);
        setFocus(scrollSpeedBox, false);
        setFocus(skipSecondsBox, false);
        setFocus(backgroundImageBox, false);
        setFocus(letterSoundBox, false);
    }

    private void setFocus(EditBox box, boolean focused) {
        if (box != null) box.setFocused(focused);
    }

    private boolean tryFocus(EditBox box, int mouseX, int mouseY, MouseButtonEvent event) {
        if (box == null) return false;
        if (isOver(mouseX, mouseY, box.getX(), box.getY(), box.getWidth(), box.getHeight())) {
            box.setFocused(true);
            box.mouseClicked(event, false);
            return true;
        }
        return false;
    }

    private void forwardKeyToFocused(EditBox box, KeyEvent event) {
        if (box != null && box.isFocused()) box.keyPressed(event);
    }

    private void forwardCharToFocused(EditBox box, CharacterEvent event) {
        if (box != null && box.isFocused()) box.charTyped(event);
    }

    private boolean isFocused(EditBox box) {
        return box != null && box.isFocused();
    }

    private int computePanelHeight(DialogData data) {
        int rowCount = 6 + 3 + 1 + 2;
        if (data.isAutoSkipEnabled()) rowCount++;
        return 14 + rowCount * (ROW_HEIGHT + ROW_GAP) + ROW_GAP * 2 + ROW_HEIGHT + PANEL_PADDING;
    }

    private boolean isOver(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
