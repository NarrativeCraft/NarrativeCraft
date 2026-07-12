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
import fr.loudo.narrativecraft.editors.cutscene.keyframes.TextKeyframe;
import fr.loudo.narrativecraft.utils.UtilsClient;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;

public class TextKeyframeMenu extends KeyframeMenu<TextKeyframe> {

    private static final int FIELD_LABEL_HEIGHT = 7;
    private static final int FIELD_HEIGHT = 70;

    private MultiLineEditBox tagsBox;

    public TextKeyframeMenu(TextKeyframe keyframe) {
        super(keyframe);
    }

    @Override
    protected void initContent() {
        width = 250;
        int fieldWidth = width - padding * 2;
        tagsBox = new MultiLineEditBox(
                Minecraft.getInstance().font,
                0,
                0,
                fieldWidth,
                FIELD_HEIGHT,
                Component.literal("text id create \"Hello\""),
                Component.empty());
        tagsBox.setValue(String.join("\n", keyframe.getTags()));
    }

    @Override
    protected int getContentHeight() {
        return FIELD_LABEL_HEIGHT + FIELD_HEIGHT;
    }

    @Override
    protected void renderContent(
            GuiGraphics graphics, float delta, int x, int y, int contentWidth, int mouseX, int mouseY) {
        graphics.drawString(Minecraft.getInstance().font, "Tags", x, y - 2, 0xFFAAAAAA);
        tagsBox.setPosition(x, y + FIELD_LABEL_HEIGHT);
        tagsBox.render(graphics, mouseX, mouseY, Minecraft.getInstance().getDeltaFrameTime());
    }

    @Override
    protected boolean onContentMouseClicked(
            double mouseX,
            double mouseY,
            int button,
            boolean isDoubleClick,
            int contentX,
            int contentY,
            int contentWidth) {
        boolean hovered = isHovered(mouseX, mouseY, tagsBox);
        tagsBox.setFocused(hovered);
        if (hovered) tagsBox.mouseClicked(mouseX, mouseY, button);
        return hovered;
    }

    @Override
    public boolean mouseScrolled(double amount) {
        int[] mousePosition = UtilsClient.getScaledMousePos();
        return tagsBox.mouseScrolled(mousePosition[0], mousePosition[1], amount);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (tagsBox.isFocused()) return tagsBox.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (tagsBox.isFocused()) return tagsBox.charTyped(codePoint, modifiers);
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (tagsBox.isFocused()) return tagsBox.keyPressed(keyCode, scanCode, modifiers);
        return false;
    }

    @Override
    protected void applyChanges() {
        List<String> tags = new ArrayList<>();
        for (String line : tagsBox.getValue().split("\n")) {
            if (line.isBlank()) continue;
            tags.add(line.trim());
        }
        keyframe.setTags(tags);
    }
}
