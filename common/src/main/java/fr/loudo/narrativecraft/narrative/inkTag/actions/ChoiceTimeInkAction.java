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
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.api.utils.Side;

@InkCommand(
        keyword = "timed_choice",
        description = "Set a time limit for making the next decision",
        side = Side.CLIENT,
        syntax = "timed_choice "
                + "[seconds:float=10] [default_answer_idx:int=-1] "
                + "[easing:string=linear] [fade_duration:float=2.0] "
                + "[line_offset_y:float=20.0] [line_width:float=150.0] [line_height:float=1.0] "
                + "[line_color:string=ffffff] "
                + "[outline_color:string=000000] [outline_padding:float=1.0] "
                + "[--no_text]")
public class ChoiceTimeInkAction extends InkAction {

    protected float seconds;
    protected int defaultAnswerIndex;
    protected EasingType easingType;
    protected float fadeDuration;
    protected float lineOffsetY;
    protected float lineWidth;
    protected float lineHeight;
    protected int lineColor;
    protected int outlineColor;
    protected float outlinePadding;
    protected boolean showText;

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        seconds = cmd.getFloat("seconds");
        defaultAnswerIndex = cmd.getInt("default_answer_idx");
        if (seconds < 1) {
            return InkActionResult.error("Seconds must be greater than 0.");
        }
        if (defaultAnswerIndex > 4) {
            return InkActionResult.error(
                    "You can't have more than 4 answers. Default answer index must be between 1 and 4.");
        }

        String easing = cmd.getString("easing");
        try {
            easingType = EasingType.valueOf(easing.toUpperCase());
        } catch (IllegalArgumentException e) {
            return InkActionResult.error("Invalid easing '" + easing + "'.");
        }

        fadeDuration = cmd.getFloat("fade_duration");
        if (fadeDuration < 0) {
            return InkActionResult.error("Fade duration can't be negative.");
        }
        if (fadeDuration > seconds) {
            return InkActionResult.error("Fade duration can't be longer than the choice time.");
        }

        lineOffsetY = cmd.getFloat("line_offset_y");

        lineWidth = cmd.getFloat("line_width");
        if (lineWidth <= 0) {
            return InkActionResult.error("Line width must be greater than 0.");
        }

        lineHeight = cmd.getFloat("line_height");
        if (lineHeight <= 0) {
            return InkActionResult.error("Line height must be greater than 0.");
        }

        String lineColorHex = cmd.getString("line_color");
        try {
            lineColor = Integer.parseInt(lineColorHex, 16);
        } catch (NumberFormatException e) {
            return InkActionResult.error("Invalid hex color '" + lineColorHex + "'.");
        }

        String outlineColorHex = cmd.getString("outline_color");
        try {
            outlineColor = Integer.parseInt(outlineColorHex, 16);
        } catch (NumberFormatException e) {
            return InkActionResult.error("Invalid hex color '" + outlineColorHex + "'.");
        }

        outlinePadding = cmd.getFloat("outline_padding");
        if (outlinePadding < 0) {
            return InkActionResult.error("Outline padding can't be negative.");
        }

        showText = !cmd.flag("no_text");

        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        return InkActionResult.ignored();
    }
}
