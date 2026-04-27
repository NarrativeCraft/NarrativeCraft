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
import fr.loudo.narrativecraft.api.inkAction.InkActionUtil;
import fr.loudo.narrativecraft.api.inkAction.InkCommand;
import fr.loudo.narrativecraft.api.inkAction.Side;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;

@InkCommand(
        keyword = "wait",
        description = "Pauses the story for a given duration before advancing to the next line.",
        syntax = "wait <duration:float> <unit:string>",
        side = Side.SERVER)
public class CooldownInkAction extends InkAction {

    @Override
    public void tick() {
        if (!isRunning) return;
        tick++;
        if (tick >= totalTick) isRunning = false;
    }

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        float duration = cmd.getFloat("duration");
        String unit = cmd.getString("unit");

        double seconds = InkActionUtil.getSecondsFromTimeValue(duration, unit);
        if (seconds == -1) {
            return InkActionResult.error("Invalid time unit '" + unit + "'. Use second(s), minute(s), or hour(s).");
        }
        if (seconds > 2) seconds -= 1;

        totalTick = (int) (seconds * 20);
        blocking = true;
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        return InkActionResult.block();
    }
}
