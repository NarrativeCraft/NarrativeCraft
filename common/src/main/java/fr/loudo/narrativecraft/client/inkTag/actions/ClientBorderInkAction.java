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

package fr.loudo.narrativecraft.client.inkTag.actions;

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Interpolation;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.client.gui.GuiGraphicsExtractorExtension;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.narrative.inkTag.actions.BorderInkAction;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

public class ClientBorderInkAction extends BorderInkAction {

    private float upInterpolated, rightInterpolated, downInterpolated, leftInterpolated;

    @Override
    public void tick() {
        if (!isRunning()) return;
        if (verb.equals("out") && tick >= totalTick) {
            stop();
            return;
        }
        if (tick < totalTick) {
            tick++;
        }
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, float partialTick) {
        if (!isRunning()) return;

        float upRender = up;
        float rightRender = right;
        float downRender = down;
        float leftRender = left;

        if (totalTick > 0 && tick <= totalTick) {
            double t = Mth.clamp((tick + partialTick) / totalTick, 0.0, 1.0);
            t = Interpolation.applyEasing(easingType, t);
            if (verb.equals("in")) {
                upRender = (float) Interpolation.lerp(0, up, t);
                rightRender = (float) Interpolation.lerp(0, right, t);
                downRender = (float) Interpolation.lerp(0, down, t);
                leftRender = (float) Interpolation.lerp(0, left, t);
            } else if (verb.equals("out")) {
                upRender = (float) Interpolation.lerp(up, 0, t);
                rightRender = (float) Interpolation.lerp(right, 0, t);
                downRender = (float) Interpolation.lerp(down, 0, t);
                leftRender = (float) Interpolation.lerp(left, 0, t);
            }
        }

        upInterpolated = upRender;
        rightInterpolated = rightRender;
        downInterpolated = downRender;
        leftInterpolated = leftRender;

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        GuiGraphicsExtractorExtension graphicsExtension = new GuiGraphicsExtractorExtension(guiGraphics);

        graphicsExtension.fill(0, 0, width, upRender, color);
        graphicsExtension.fill(width - rightRender, 0, width, height, color);
        graphicsExtension.fill(0, height - downRender, width, height, color);
        graphicsExtension.fill(0, 0, leftRender, height, color);
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        ClientPlayerSession session = (ClientPlayerSession) playerSession;

        if (verb.equals("out")) {
            for (InkAction active : session.getActiveClientInkActions()) {
                if (active instanceof ClientBorderInkAction existing) {
                    up = existing.upInterpolated;
                    right = existing.rightInterpolated;
                    down = existing.downInterpolated;
                    left = existing.leftInterpolated;
                    color = existing.color;
                    existing.stop();
                    break;
                }
            }
            if (up == 0 && right == 0 && down == 0 && left == 0) {
                return InkActionResult.singleOk();
            }
            return InkActionResult.ok();
        }

        if (verb.equals("clear") || (up == 0 && right == 0 && down == 0 && left == 0)) {
            for (InkAction active : session.getActiveClientInkActions()) {
                if (active instanceof ClientBorderInkAction) {
                    active.stop();
                }
            }
            return InkActionResult.singleOk();
        }

        if (totalTick == 0) {
            upInterpolated = up;
            rightInterpolated = right;
            downInterpolated = down;
            leftInterpolated = left;
        }

        return InkActionResult.ok();
    }
}
