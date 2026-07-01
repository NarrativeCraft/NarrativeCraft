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

package fr.loudo.narrativecraft.narrative.inkTag.actions;

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.EasingType;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkCommand;
import fr.loudo.narrativecraft.api.inkAction.Side;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.utils.Utils;

@InkCommand(
        keyword = "border",
        description = "Draws solid rectangles along screen edges to create cinematic letterboxing or vignettes.",
        syntax = "border <verb:string> [up:int=0] [right:int=0] [down:int=0] [left:int=0] "
                + "[color:string=000000] [opacity:float=1.0] [duration:float=0] [easing:string=smooth]",
        side = Side.CLIENT)
public class BorderInkAction extends InkAction {

    protected float up, right, down, left;
    protected int color;
    protected String verb;
    protected EasingType easingType;

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
            color = Utils.argb((int) (opacity * 255), rawColor);
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
        return InkActionResult.ignored();
    }
}
