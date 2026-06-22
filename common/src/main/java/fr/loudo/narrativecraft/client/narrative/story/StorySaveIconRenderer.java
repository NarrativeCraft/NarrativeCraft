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

package fr.loudo.narrativecraft.client.narrative.story;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Interpolation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public class StorySaveIconRenderer {

    private static final Identifier SAVE_ICON = Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "save");
    private static final int ICON_SIZE = 16;
    private static final int MARGIN = 20;

    private double inTicks;
    private double stayTicks;
    private double outTicks;
    private double elapsedTicks;
    private boolean active;

    public void start(double in, double stay, double out) {
        this.inTicks = in * 20.0;
        this.stayTicks = stay * 20.0;
        this.outTicks = out * 20.0;
        this.elapsedTicks = 0.0;
        this.active = true;
    }

    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (!active) return;

        elapsedTicks += deltaTracker.getGameTimeDeltaPartialTick(true);

        double total = inTicks + stayTicks + outTicks;
        if (elapsedTicks >= total) {
            active = false;
            return;
        }

        int alpha;
        if (elapsedTicks < inTicks) {
            double t = inTicks > 0 ? elapsedTicks / inTicks : 1.0;
            alpha = (int) Interpolation.lerp(0.0, 255.0, t);
        } else if (elapsedTicks < inTicks + stayTicks) {
            alpha = 255;
        } else {
            double t = outTicks > 0 ? (elapsedTicks - inTicks - stayTicks) / outTicks : 1.0;
            alpha = (int) Interpolation.lerp(255.0, 0.0, t);
        }

        int x = guiGraphics.guiWidth() - ICON_SIZE - MARGIN;
        int y = guiGraphics.guiHeight() - ICON_SIZE - MARGIN;

        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED, SAVE_ICON, x, y, ICON_SIZE, ICON_SIZE, ARGB.color(alpha, 0xFFFFFF));
    }
}
