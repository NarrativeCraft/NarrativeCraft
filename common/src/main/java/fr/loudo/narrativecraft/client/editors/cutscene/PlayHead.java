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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class PlayHead {

    public static final Identifier playHeadLocation =
            Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "playhead");
    private int y, width, height;
    private boolean isHovered;
    private boolean isDragging;
    private float ratio = 0f;

    public PlayHead(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int timelineStartX, int timelineWidth) {
        int x = timelineStartX + (int) (ratio * timelineWidth) - width / 2;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, playHeadLocation, x, y, width, height);
        isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void onMouseDrag(double mouseX, int timelineStartX, int timelineWidth) {
        if (!isDragging || timelineWidth <= 0) return;
        ratio = (float) Math.clamp((mouseX - timelineStartX) / (double) timelineWidth, 0.0, 1.0);
    }

    public void onClick(MouseButtonEvent event, int timelineStartX, int timelineWidth, int timelineY) {
        if (timelineWidth <= 0) return;
        if (timelineY >= event.y() + 5) return;
        if (event.x() <= timelineStartX) {
            ratio = 0.0f;
            return;
        }
        ratio = (float) Math.clamp((event.x() - timelineStartX) / (double) timelineWidth, 0.0, 1.0);
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
