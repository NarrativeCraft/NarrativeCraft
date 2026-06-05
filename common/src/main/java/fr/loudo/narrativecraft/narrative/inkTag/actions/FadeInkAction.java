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

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkCommand;
import fr.loudo.narrativecraft.api.inkAction.Side;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.utils.FadeState;

@InkCommand(
        keyword = "fade",
        description = "Fades the screen to a solid color, holds for a duration, then fades back out.",
        syntax = "fade <fadeIn:float> <stay:float> <fadeOut:float> [color:string=000000]",
        side = Side.CLIENT)
public class FadeInkAction extends InkAction {

    protected double fadeInSeconds;
    protected double staySeconds;
    protected double fadeOutSeconds;
    protected int color;
    protected FadeState currentFadeState;

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        fadeInSeconds = cmd.getFloat("fadeIn");
        staySeconds = cmd.getFloat("stay");
        fadeOutSeconds = cmd.getFloat("fadeOut");

        String colorHex = cmd.getString("color");
        try {
            color = Integer.parseInt(colorHex, 16);
        } catch (NumberFormatException e) {
            return InkActionResult.error("Invalid hex color '" + colorHex + "'.");
        }

        if (fadeInSeconds > 2) fadeInSeconds -= 1;
        if (staySeconds > 2) staySeconds -= 1;
        if (fadeOutSeconds > 2) fadeOutSeconds -= 1;

        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        return InkActionResult.ignored();
    }
}
