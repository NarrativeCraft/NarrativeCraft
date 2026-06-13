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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;

@InkCommand(
        keyword = "time",
        description = "Changes the in-game day time instantly or transitions smoothly from one value to another.",
        syntax =
                "time <action:string> <from:string> [to:string] [for:float=0] [unit:string=seconds] [easing:string=smooth]",
        side = Side.SERVER)
public class ChangeDayTimeInkAction extends InkAction {

    private String action;
    private long fromTick;
    private long toTick;
    private EasingType easingType;
    private ServerLevel level;

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        action = cmd.getString("action");
        if (!action.equals("set") && !action.equals("add")) {
            return InkActionResult.error("Unknown time action '" + action + "' (expected 'set' or 'add')");
        }

        fromTick = parseDayTime(cmd.getString("from"));
        if (fromTick == -1) {
            return InkActionResult.error("Invalid time value '" + cmd.getString("from") + "'");
        }

        if (action.equals("add")) {
            return InkActionResult.ok();
        }

        String toStr = cmd.getString("to");
        float forDuration = cmd.getFloat("for");

        if (toStr == null || toStr.isEmpty() || forDuration == 0) {
            toTick = fromTick;
            return InkActionResult.ok();
        }

        toTick = parseDayTime(toStr);
        if (toTick == -1) {
            return InkActionResult.error("Invalid time value '" + toStr + "'");
        }

        String unit = cmd.getString("unit");
        double forSeconds =
                switch (unit) {
                    case "minutes" -> forDuration * 60.0;
                    case "hours" -> forDuration * 3600.0;
                    default -> forDuration;
                };
        totalTick = (int) (forSeconds * 20.0);

        String easingStr = cmd.getString("easing");
        try {
            easingType = EasingType.valueOf(easingStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return InkActionResult.error("Invalid easing '" + easingStr + "'");
        }

        return InkActionResult.ok();
    }

    @Override
    public void tick() {
        if (!isRunning || level == null || totalTick == 0) return;
        tick++;
        double t = Mth.clamp((double) tick / totalTick, 0.0, 1.0);
        t = Interpolation.applyEasing(easingType, t);
        changeTime((long) Interpolation.lerp(fromTick, toTick, t));
        if (tick >= totalTick) {
            isRunning = false;
        }
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        level = (ServerLevel) playerSession.getPlayer().level();

        if (action.equals("add")) {
            changeTime(level.getDayTime() + fromTick);
            isRunning = false;
            return InkActionResult.ok();
        }

        if (totalTick == 0) {
            changeTime(fromTick);
            isRunning = false;
            return InkActionResult.ok();
        }

        return InkActionResult.ok();
    }

    private void changeTime(long newTime) {
        level.setDayTime(newTime);
    }

    private static long parseDayTime(String dayTime) {
        return switch (dayTime) {
            case "day" -> 1000L;
            case "noon" -> 6000L;
            case "night" -> 13000L;
            case "midnight" -> 18000L;
            default -> {
                try {
                    yield Long.parseLong(dayTime);
                } catch (NumberFormatException e) {
                    yield -1L;
                }
            }
        };
    }
}
