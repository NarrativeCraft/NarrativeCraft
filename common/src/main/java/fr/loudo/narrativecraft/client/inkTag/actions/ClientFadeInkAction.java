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

package fr.loudo.narrativecraft.client.inkTag.actions;

import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.narrative.inkTag.actions.FadeInkAction;
import fr.loudo.narrativecraft.utils.FadeState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class ClientFadeInkAction extends FadeInkAction {

    @Override
    public void tick() {
        if (!isRunning) return;
        tick++;
        if (tick >= totalTick) {
            tick = 0;
            if (currentFadeState == FadeState.FADE_IN) {
                currentFadeState = FadeState.STAY;
                totalTick = (int) (staySeconds * 20.0);
            } else if (currentFadeState == FadeState.STAY) {
                currentFadeState = FadeState.FADE_OUT;
                totalTick = (int) (fadeOutSeconds * 20.0);
            } else if (currentFadeState == FadeState.FADE_OUT) {
                isRunning = false;
            }
        }
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, float partialTick) {
        if (!isRunning) return;
        double t = Mth.clamp((tick + partialTick) / totalTick, 0.0, 1.0);
        int opacity = 255;
        if (currentFadeState == FadeState.FADE_IN) {
            opacity = (int) Mth.lerp(t, 0, 255);
        } else if (currentFadeState == FadeState.FADE_OUT) {
            opacity = (int) Mth.lerp(t, 255, 0);
        }
        guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), ARGB.color(opacity, color));
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        if (fadeInSeconds == 0 && staySeconds == 0 && fadeOutSeconds == 0) {
            isRunning = false;
            return InkActionResult.ok();
        }
        currentFadeState = FadeState.FADE_IN;
        totalTick = (int) (fadeInSeconds * 20.0);
        return InkActionResult.ok();
    }
}
