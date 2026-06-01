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
import fr.loudo.narrativecraft.dialog.DialogRenderer3D;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public class DialogPreviewPanel {

    private static final int PANEL_WIDTH = 170;
    private static final int PADDING = 6;
    private static final int ROW_HEIGHT = 14;
    private static final int PADDING_RIGHT = 10;
    private static final int EDITBOX_HEIGHT = 10;
    private static final int FIELD_GAP = 3;

    private final Consumer<DialogData> onToggleAdvanced;
    private final String defaultDialogText;

    private DialogFieldSet fieldSet = DialogFieldSet.ALL;
    private List<DialogPreviewEntry> entries = new ArrayList<>();
    private int selectedIndex = 0;

    private EditBox previewTextBox;
    private EditBox offsetXBox;
    private EditBox offsetYBox;
    private EditBox scaleBox;
    private EditBox backgroundColorBox;
    private EditBox opacityBox;
    private EditBox textColorBox;

    private int advancedButtonY = 0;

    public DialogPreviewPanel(Consumer<DialogData> onToggleAdvanced, String defaultDialogText) {
        this.onToggleAdvanced = onToggleAdvanced;
        this.defaultDialogText = defaultDialogText;
    }

    public void setFieldSet(DialogFieldSet fieldSet) {
        this.fieldSet = fieldSet;
        rebuildEditBoxes();
    }

    public void setEntries(List<DialogPreviewEntry> entries) {
        this.entries = entries != null ? entries : new ArrayList<>();
        this.selectedIndex = 0;
        rebuildEditBoxes();
    }

    private void rebuildEditBoxes() {
        Minecraft mc = Minecraft.getInstance();
        DialogPreviewEntry entry = getSelectedEntry();

        previewTextBox = makeEditBox(mc, 256, entry != null ? entry.getPreviewText() : "");
        previewTextBox.setResponder(text -> {
            DialogPreviewEntry current = getSelectedEntry();
            if (current == null) return;
            current.setPreviewText(text);
            DialogRenderer3D renderer = current.getRenderer();
            if (renderer != null) {
                renderer.update(text.isEmpty() ? defaultDialogText : text);
            }
        });

        if (entry != null) {
            DialogData data = entry.getData();

            if (fieldSet == DialogFieldSet.ALL || fieldSet == DialogFieldSet.CAMERA_VIEW) {
                offsetXBox = makeEditBox(mc, 16, String.format(java.util.Locale.ROOT, "%.2f", data.getOffsetX()));
                offsetXBox.setResponder(text -> {
                    DialogPreviewEntry s = getSelectedEntry();
                    if (s == null) return;
                    try {
                        s.getData().setOffsetX(Float.parseFloat(text));
                    } catch (NumberFormatException ignored) {
                    }
                });

                offsetYBox = makeEditBox(mc, 16, String.format(java.util.Locale.ROOT, "%.2f", data.getOffsetY()));
                offsetYBox.setResponder(text -> {
                    DialogPreviewEntry s = getSelectedEntry();
                    if (s == null) return;
                    try {
                        s.getData().setOffsetY(Float.parseFloat(text));
                    } catch (NumberFormatException ignored) {
                    }
                });

                scaleBox = makeEditBox(mc, 16, String.format(java.util.Locale.ROOT, "%.2f", data.getScale()));
                scaleBox.setResponder(text -> {
                    DialogPreviewEntry s = getSelectedEntry();
                    if (s == null) return;
                    try {
                        s.getData().setScale(Float.parseFloat(text));
                    } catch (NumberFormatException ignored) {
                    }
                });
            } else {
                offsetXBox = null;
                offsetYBox = null;
                scaleBox = null;
            }

            if (fieldSet == DialogFieldSet.ALL || fieldSet == DialogFieldSet.CHARACTER) {
                backgroundColorBox = makeEditBox(mc, 6, String.format("%06X", data.getBackgroundColor() & 0xFFFFFF));
                backgroundColorBox.setResponder(text -> {
                    DialogPreviewEntry s = getSelectedEntry();
                    if (s == null) return;
                    try {
                        int rgb = (int) Long.parseLong(text, 16) & 0xFFFFFF;
                        int alpha = (s.getData().getBackgroundColor() >> 24) & 0xFF;
                        s.getData().setBackgroundColor((alpha << 24) | rgb);
                    } catch (NumberFormatException ignored) {
                    }
                });

                int alpha = (data.getBackgroundColor() >> 24) & 0xFF;
                opacityBox = makeEditBox(mc, 4, String.format(java.util.Locale.ROOT, "%.2f", alpha / 255f));
                opacityBox.setResponder(text -> {
                    DialogPreviewEntry s = getSelectedEntry();
                    if (s == null) return;
                    try {
                        float value = Float.parseFloat(text);
                        if (value < 0f || value > 1f) return;
                        int newAlpha = Math.round(value * 255f);
                        int rgb = s.getData().getBackgroundColor() & 0xFFFFFF;
                        s.getData().setBackgroundColor((newAlpha << 24) | rgb);
                    } catch (NumberFormatException ignored) {
                    }
                });

                textColorBox = makeEditBox(mc, 6, String.format("%06X", data.getTextColor() & 0xFFFFFF));
                textColorBox.setResponder(text -> {
                    DialogPreviewEntry s = getSelectedEntry();
                    if (s == null) return;
                    try {
                        int rgb = (int) Long.parseLong(text, 16) & 0xFFFFFF;
                        s.getData().setTextColor(0xFF000000 | rgb);
                    } catch (NumberFormatException ignored) {
                    }
                });
            } else {
                backgroundColorBox = null;
                opacityBox = null;
                textColorBox = null;
            }
        } else {
            offsetXBox = null;
            offsetYBox = null;
            scaleBox = null;
            backgroundColorBox = null;
            opacityBox = null;
            textColorBox = null;
        }
    }

    private EditBox makeEditBox(Minecraft mc, int maxLength, String value) {
        EditBox box = new EditBox(mc.font, 0, 0, PANEL_WIDTH - PADDING * 2, EDITBOX_HEIGHT, Component.empty());
        box.setMaxLength(maxLength);
        box.setValue(value);
        return box;
    }

    public void render(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        if (entries.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        int panelX = getPanelX(screenWidth);
        int panelY = 35;
        int contentHeight = computeContentHeight(entries.size());

        graphics.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelY + contentHeight + 1, 0xFFAAAAAA);
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + contentHeight, ARGB.color(220, 0, 0, 0));

        int y = panelY + PADDING;

        graphics.text(
                mc.font,
                Translation.message("screen.dialog_preview.title").getString(),
                panelX + PADDING,
                y,
                0xFFFFFFFF);
        y += ROW_HEIGHT + 2;

        for (int i = 0; i < entries.size(); i++) {
            DialogPreviewEntry entry = entries.get(i);
            boolean selected = i == selectedIndex;
            int rowColor = selected
                    ? 0xFF4466AA
                    : (isOver(mouseX, mouseY, panelX + PADDING, y, PANEL_WIDTH - PADDING * 2, ROW_HEIGHT - 2)
                            ? 0xFF334455
                            : 0xFF222222);
            graphics.fill(panelX + PADDING, y, panelX + PANEL_WIDTH - PADDING, y + ROW_HEIGHT - 2, rowColor);
            graphics.text(mc.font, entry.getLabel(), panelX + PADDING + 2, y + 2, selected ? 0xFFFFFFFF : 0xFFAAAAAA);
            y += ROW_HEIGHT;
        }

        y += 4;
        if (getSelectedEntry() == null) return;

        y = renderEditBoxRow(
                graphics,
                mc,
                Translation.message("screen.dialog_preview.field.text").getString(),
                panelX,
                y,
                previewTextBox,
                mouseX,
                mouseY);
        if (fieldSet == DialogFieldSet.ALL || fieldSet == DialogFieldSet.CAMERA_VIEW) {
            y = renderEditBoxRow(
                    graphics,
                    mc,
                    Translation.message("screen.dialog_preview.field.offset_x").getString(),
                    panelX,
                    y,
                    offsetXBox,
                    mouseX,
                    mouseY);
            y = renderEditBoxRow(
                    graphics,
                    mc,
                    Translation.message("screen.dialog_preview.field.offset_y").getString(),
                    panelX,
                    y,
                    offsetYBox,
                    mouseX,
                    mouseY);
            y = renderEditBoxRow(
                    graphics,
                    mc,
                    Translation.message("screen.dialog_preview.field.scale").getString(),
                    panelX,
                    y,
                    scaleBox,
                    mouseX,
                    mouseY);
        }
        if (fieldSet == DialogFieldSet.ALL || fieldSet == DialogFieldSet.CHARACTER) {
            y = renderEditBoxRow(
                    graphics,
                    mc,
                    Translation.message("screen.dialog_preview.field.bg_color").getString(),
                    panelX,
                    y,
                    backgroundColorBox,
                    mouseX,
                    mouseY);
            y = renderEditBoxRow(
                    graphics,
                    mc,
                    Translation.message("screen.dialog_preview.field.opacity").getString(),
                    panelX,
                    y,
                    opacityBox,
                    mouseX,
                    mouseY);
            y = renderEditBoxRow(
                    graphics,
                    mc,
                    Translation.message("screen.dialog_preview.field.text_color")
                            .getString(),
                    panelX,
                    y,
                    textColorBox,
                    mouseX,
                    mouseY);
        }

        if (fieldSet != DialogFieldSet.CAMERA_VIEW) {
            advancedButtonY = y;
            int advBtnWidth = PANEL_WIDTH - PADDING * 2;
            boolean advHover = isOver(mouseX, mouseY, panelX + PADDING, y, advBtnWidth, 10);
            graphics.fill(
                    panelX + PADDING, y, panelX + PADDING + advBtnWidth, y + 10, advHover ? 0xFF555577 : 0xFF333355);
            graphics.text(mc.font, "...", panelX + PANEL_WIDTH / 2 - mc.font.width("...") / 2, y + 1, 0xFFFFFFFF);
        } else {
            advancedButtonY = -1;
        }
    }

    private int renderEditBoxRow(
            GuiGraphicsExtractor graphics,
            Minecraft mc,
            String label,
            int panelX,
            int y,
            EditBox box,
            int mouseX,
            int mouseY) {
        graphics.text(mc.font, label, panelX + PADDING, y, 0xFFCCCCCC);
        y += ROW_HEIGHT - 2;
        if (box != null) {
            box.setX(panelX + PADDING);
            box.setY(y);
            box.extractRenderState(graphics, mouseX, mouseY, 0);
        }
        return y + EDITBOX_HEIGHT + FIELD_GAP;
    }

    public boolean mouseClicked(MouseButtonEvent event) {
        if (entries.isEmpty()) return false;
        Minecraft mc = Minecraft.getInstance();
        int panelX = getPanelX(mc.getWindow().getGuiScaledWidth());
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();

        int y = 35 + PADDING + ROW_HEIGHT + 2;
        for (int i = 0; i < entries.size(); i++) {
            if (isOver(mouseX, mouseY, panelX + PADDING, y, PANEL_WIDTH - PADDING * 2, ROW_HEIGHT - 2)) {
                if (selectedIndex != i) {
                    selectedIndex = i;
                    rebuildEditBoxes();
                }
                return true;
            }
            y += ROW_HEIGHT;
        }

        if (getSelectedEntry() == null) return false;

        unfocusAll();

        if (tryFocusBox(previewTextBox, mouseX, mouseY, event)) return true;
        if (tryFocusBox(offsetXBox, mouseX, mouseY, event)) return true;
        if (tryFocusBox(offsetYBox, mouseX, mouseY, event)) return true;
        if (tryFocusBox(scaleBox, mouseX, mouseY, event)) return true;
        if (tryFocusBox(backgroundColorBox, mouseX, mouseY, event)) return true;
        if (tryFocusBox(opacityBox, mouseX, mouseY, event)) return true;
        if (tryFocusBox(textColorBox, mouseX, mouseY, event)) return true;

        int advBtnWidth = PANEL_WIDTH - PADDING * 2;
        if (fieldSet != DialogFieldSet.CAMERA_VIEW
                && advancedButtonY > 0
                && isOver(mouseX, mouseY, panelX + PADDING, advancedButtonY, advBtnWidth, 10)) {
            DialogPreviewEntry entry = getSelectedEntry();
            if (entry != null && onToggleAdvanced != null) {
                onToggleAdvanced.accept(entry.getData());
            }
            return true;
        }

        return false;
    }

    private boolean tryFocusBox(EditBox box, int mouseX, int mouseY, MouseButtonEvent event) {
        if (box == null) return false;
        if (isOver(mouseX, mouseY, box.getX(), box.getY(), box.getWidth(), box.getHeight())) {
            box.setFocused(true);
            box.mouseClicked(event, false);
            return true;
        }
        return false;
    }

    public void unfocusAll() {
        setBoxFocus(previewTextBox, false);
        setBoxFocus(offsetXBox, false);
        setBoxFocus(offsetYBox, false);
        setBoxFocus(scaleBox, false);
        setBoxFocus(backgroundColorBox, false);
        setBoxFocus(opacityBox, false);
        setBoxFocus(textColorBox, false);
    }

    private void setBoxFocus(EditBox box, boolean focused) {
        if (box != null) box.setFocused(focused);
    }

    public void mouseReleased() {}

    public boolean mouseDragged(double mouseY) {
        return false;
    }

    public void keyPressed(KeyEvent event) {
        forwardKeyToAll(event);
    }

    public void charTyped(CharacterEvent event) {
        forwardCharToAll(event);
    }

    private void forwardKeyToAll(KeyEvent event) {
        forwardKeyToFocused(previewTextBox, event);
        forwardKeyToFocused(offsetXBox, event);
        forwardKeyToFocused(offsetYBox, event);
        forwardKeyToFocused(scaleBox, event);
        forwardKeyToFocused(backgroundColorBox, event);
        forwardKeyToFocused(opacityBox, event);
        forwardKeyToFocused(textColorBox, event);
    }

    private void forwardKeyToFocused(EditBox box, KeyEvent event) {
        if (box != null && box.isFocused()) box.keyPressed(event);
    }

    private void forwardCharToAll(CharacterEvent event) {
        forwardCharToFocused(previewTextBox, event);
        forwardCharToFocused(offsetXBox, event);
        forwardCharToFocused(offsetYBox, event);
        forwardCharToFocused(scaleBox, event);
        forwardCharToFocused(backgroundColorBox, event);
        forwardCharToFocused(opacityBox, event);
        forwardCharToFocused(textColorBox, event);
    }

    private void forwardCharToFocused(EditBox box, CharacterEvent event) {
        if (box != null && box.isFocused()) box.charTyped(event);
    }

    public boolean isAnyBoxFocused() {
        return isFocused(previewTextBox)
                || isFocused(offsetXBox)
                || isFocused(offsetYBox)
                || isFocused(scaleBox)
                || isFocused(backgroundColorBox)
                || isFocused(opacityBox)
                || isFocused(textColorBox);
    }

    private boolean isFocused(EditBox box) {
        return box != null && box.isFocused();
    }

    private int computeContentHeight(int entryCount) {
        int height = PADDING;
        height += ROW_HEIGHT + 2;
        if (entryCount == 0) {
            height += ROW_HEIGHT + PADDING;
            return height;
        }
        height += entryCount * ROW_HEIGHT;
        height += 4;
        int fieldCount;
        boolean hasAdvancedButton;
        if (fieldSet == DialogFieldSet.CHARACTER) {
            fieldCount = 4;
            hasAdvancedButton = true;
        } else if (fieldSet == DialogFieldSet.CAMERA_VIEW) {
            fieldCount = 4;
            hasAdvancedButton = false;
        } else {
            fieldCount = 7;
            hasAdvancedButton = true;
        }
        height += fieldCount * ((ROW_HEIGHT - 2) + EDITBOX_HEIGHT + FIELD_GAP);
        if (hasAdvancedButton) height += 10 + PADDING;
        return height;
    }

    private int getPanelX(int screenWidth) {
        return screenWidth - PANEL_WIDTH - PADDING_RIGHT;
    }

    private DialogPreviewEntry getSelectedEntry() {
        if (entries.isEmpty() || selectedIndex >= entries.size()) return null;
        return entries.get(selectedIndex);
    }

    private boolean isOver(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    public DialogRenderer3D getCurrentDialog() {
        DialogPreviewEntry current = getSelectedEntry();
        if (current == null) return null;
        return current.getRenderer();
    }
}
