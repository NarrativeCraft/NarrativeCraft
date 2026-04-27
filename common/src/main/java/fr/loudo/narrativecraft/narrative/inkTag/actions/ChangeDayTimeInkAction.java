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

@InkCommand(
        keyword = "time",
        description = "Smoothly transitions the in-game day time from one value to another, with optional easing.",
        syntax =
                "time <action:string> <from:string> [to:string] [for:float=0] [unit:string=seconds] [easing:string=smooth]",
        side = Side.CLIENT)
public class ChangeDayTimeInkAction extends InkAction {

    @Override
    public void tick() {
        // TODO: advance segmentTick and compute interpolated time when Easing is available
    }

    @Override
    public void partialTick(float partialTick) {
        // TODO: interpolate day-time tick value via Easing
    }

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        // TODO: parse action (set/add), resolve named day times, compute totalTick when Easing is available
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        // TODO: apply day-time change when implementation is complete
        isRunning = false;
        return InkActionResult.ignored();
    }
}
