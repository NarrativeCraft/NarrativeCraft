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
        keyword = "sound",
        description = "Plays, stops, or fades a sound effect or music track on the client, with optional looping.",
        syntax =
                "sound <type:string> <action:string> <name:string> [volume:float=1.0] [pitch:float=1.0] [--loop] [fadeTime:float=0]",
        side = Side.CLIENT)
public class SoundInkAction extends InkAction {

    public enum SoundType {
        SFX,
        SONG,
        STOP
    }

    protected SoundType soundType;
    protected String soundAction;
    protected String identifier;
    protected String soundName;
    protected float volume;
    protected float pitch;
    protected boolean looping;

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        String rawType = cmd.getString("type").toUpperCase();
        try {
            soundType = SoundType.valueOf(rawType);
        } catch (IllegalArgumentException e) {
            return InkActionResult.error("Unknown sound type '" + rawType + "'. Use: sfx, song, or sound.");
        }

        soundAction = cmd.getString("action");
        if (!soundAction.equals("play") && !soundAction.equals("stop")) {
            return InkActionResult.error("Sound action must be 'play' or 'stop'.");
        }

        String rawName = cmd.getString("name");
        if (rawName.contains(":")) {
            String[] parts = rawName.split(":", 2);
            identifier = parts[0];
            soundName = parts[1];
        } else {
            identifier = "minecraft";
            soundName = rawName;
        }

        volume = cmd.getFloat("volume");
        pitch = cmd.getFloat("pitch");
        looping = cmd.flag("loop");
        double fadeTime = cmd.getFloat("fadeTime");
        totalTick = (int) (fadeTime * 20.0);
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        return InkActionResult.ignored();
    }
}
