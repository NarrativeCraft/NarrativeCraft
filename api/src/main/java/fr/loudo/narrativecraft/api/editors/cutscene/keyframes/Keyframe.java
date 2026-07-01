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

package fr.loudo.narrativecraft.api.editors.cutscene.keyframes;

import fr.loudo.narrativecraft.api.NarrativeCraftAPI;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.utils.MathUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public abstract class Keyframe {

    public static final ResourceLocation KEYFRAME_SPRITE =
            new ResourceLocation(NarrativeCraftAPI.getInstance().getModId(), "textures/gui/sprites/keyframe.png");
    public static final ResourceLocation KEYFRAME_SELECTED_SPRITE = new ResourceLocation(
            NarrativeCraftAPI.getInstance().getModId(), "textures/gui/sprites/keyframe-selected.png");
    public static final int SIZE = 7;

    protected int x;
    protected int y;
    protected int tick;
    protected boolean isSelected;
    protected final CutsceneLayer layer;

    public Keyframe(CutsceneLayer layer, int tick) {
        this.layer = layer;
        this.tick = tick;
    }

    public void click(double mouseX, double mouseY, int button, boolean isDoubleClick) {
        isSelected = true;
    }

    public void drag(double mouseX, int timelineStartX, int timelineWidth, int maxTick) {
        drag(mouseX, timelineStartX, timelineWidth, (float) maxTick, 0f);
    }

    public void drag(double mouseX, int timelineStartX, int timelineWidth, float visibleTicks, float viewStartTick) {
        if (timelineWidth <= 0 || visibleTicks <= 0) return;
        tick = (int) MathUtils.clamp(
                viewStartTick + (mouseX - timelineStartX) / timelineWidth * visibleTicks,
                0,
                viewStartTick + visibleTicks);
    }

    public void render(GuiGraphics graphics, float delta) {
        ResourceLocation sprite = isSelected ? KEYFRAME_SELECTED_SPRITE : KEYFRAME_SPRITE;
        graphics.blit(sprite, x, y, 0, 0, SIZE, SIZE, SIZE, SIZE);
    }

    public abstract KeyframeMenu<?> createMenu();

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + SIZE && mouseY >= y && mouseY <= y + SIZE;
    }

    public void setLayerPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public CutsceneLayer getLayer() {
        return layer;
    }
}
