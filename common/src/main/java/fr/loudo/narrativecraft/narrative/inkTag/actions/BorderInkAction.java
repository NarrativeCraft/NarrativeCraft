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

package fr.loudo.narrativecraft.narrative.inkTag.actions;

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.EasingType;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Interpolation;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkCommand;
import fr.loudo.narrativecraft.api.inkAction.Side;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.client.gui.GuiGraphicsExtractorExtension;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

@InkCommand(
        keyword = "border",
        description = "Draws solid rectangles along screen edges to create cinematic letterboxing or vignettes.",
        syntax = "border <verb:string> [up:int=0] [right:int=0] [down:int=0] [left:int=0] "
                + "[color:string=000000] [opacity:float=1.0] [duration:float=0] [easing:string=smooth]",
        side = Side.CLIENT)
public class BorderInkAction extends InkAction {

    private float up, right, down, left;
    private float upInterpolated, rightInterpolated, downInterpolated, leftInterpolated;
    private int color;
    private String verb;
    private EasingType easingType;

    @Override
    public void tick() {
        if (!isRunning) return;
        if (verb.equals("out") && tick >= totalTick) {
            isRunning = false;
            return;
        }
        if (tick < totalTick) {
            tick++;
        }
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, float partialTick) {
        if (!isRunning) return;

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
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        verb = cmd.getString("verb");
        if (!verb.equals("in") && !verb.equals("out") && !verb.equals("set") && !verb.equals("clear")) {
            return InkActionResult.error("Unknown border verb '" + verb + "' (expected 'in', 'out', 'set' or 'clear')");
        }

        if (!verb.equals("out") && !verb.equals("clear")) {
            up = cmd.getInt("up");
            right = cmd.getInt("right");
            down = cmd.getInt("down");
            left = cmd.getInt("left");

            String colorHex = cmd.getString("color");
            int rawColor;
            try {
                rawColor = Integer.parseInt(colorHex, 16);
            } catch (NumberFormatException e) {
                return InkActionResult.error("Invalid hex color '" + colorHex + "'");
            }
            float opacity = cmd.getFloat("opacity");
            color = ARGB.color((int) (opacity * 255), rawColor);
        }

        float duration = cmd.getFloat("duration");
        if ((verb.equals("in") || verb.equals("out")) && duration > 0) {
            totalTick = (int) (duration * 20.0);
            String easingStr = cmd.getString("easing");
            try {
                easingType = EasingType.valueOf(easingStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return InkActionResult.error("Invalid easing '" + easingStr + "'");
            }
        }

        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        ClientPlayerSession session = (ClientPlayerSession) playerSession;

        if (verb.equals("out")) {
            for (InkAction active : session.getActiveClientInkActions()) {
                if (active instanceof BorderInkAction existing) {
                    up = existing.upInterpolated;
                    right = existing.rightInterpolated;
                    down = existing.downInterpolated;
                    left = existing.leftInterpolated;
                    color = existing.color;
                    existing.isRunning = false;
                    break;
                }
            }
            if (up == 0 && right == 0 && down == 0 && left == 0) {
                isRunning = false;
                return InkActionResult.ok();
            }
            return InkActionResult.ok();
        }

        if (verb.equals("clear") || (up == 0 && right == 0 && down == 0 && left == 0)) {
            for (InkAction active : session.getActiveClientInkActions()) {
                if (active instanceof BorderInkAction) {
                    active.stop();
                }
            }
            isRunning = false;
            return InkActionResult.ok();
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
