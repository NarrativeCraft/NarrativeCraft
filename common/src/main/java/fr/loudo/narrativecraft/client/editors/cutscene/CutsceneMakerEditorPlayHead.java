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

package fr.loudo.narrativecraft.client.editors.cutscene;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class CutsceneMakerEditorPlayHead {

    public static final ResourceLocation playHeadLocation =
            ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "playhead");
    private int y, width, height;
    private int space;
    private boolean isHovered;
    private boolean isDragging;
    private float ratio = 0f;

    public CutsceneMakerEditorPlayHead(int width, int height, int space) {
        this.width = width;
        this.height = height;
        this.space = space;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, int timelineStartX, int timelineWidth) {
        int x = timelineStartX + (int) (ratio * timelineWidth) - width / 2;
        graphics.blitSprite(playHeadLocation, x, y, width, height);
        isHovered = mouseX >= x - space && mouseX <= x + space + width && mouseY >= y && mouseY <= y + height;
    }

    public void onMouseDrag(double mouseX, int timelineStartX, int timelineWidth) {
        if (!isDragging || timelineWidth <= 0) return;
        ratio = (float) Math.clamp((mouseX - timelineStartX) / (double) timelineWidth, 0.0, 1.0);
    }

    public void onClick(
            double mouseX, double mouseY, int button, int timelineStartX, int timelineWidth, int timelineY) {
        if (timelineWidth <= 0) return;
        if (timelineY >= (mouseY) + 5) return;
        if ((mouseX) <= timelineStartX) {
            return;
        }
        isDragging = true;
        ratio = (float) Math.clamp(((mouseX) - timelineStartX) / (double) timelineWidth, 0.0, 1.0);
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setRatio(float ratio) {
        this.ratio = (float) Math.clamp(ratio, 0.0, 1.0);
    }

    public float getRatio() {
        return ratio;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSpace() {
        return space;
    }

    public void setSpace(int space) {
        this.space = space;
    }

    public boolean isHovered() {
        return isHovered;
    }

    public boolean isDragging() {
        return isDragging;
    }

    public void setDragging(boolean dragging) {
        isDragging = dragging;
    }
}
