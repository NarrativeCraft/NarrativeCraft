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

package fr.loudo.narrativecraft.narrative.inkTag.actions.sound;

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkCommand;
import fr.loudo.narrativecraft.api.inkAction.Side;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.utils.VolumeAudio;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

@InkCommand(
        keyword = "sound",
        description = "Plays, stops, or fades a sound effect or music track on the client, with optional looping.",
        syntax =
                "sound <type:string> <action:string> <name:string> [volume:float=1.0] [pitch:float=1.0] [--loop] [fadeTime:float=0]",
        side = Side.CLIENT)
public class SoundInkAction extends InkAction {

    private SoundManager soundManager;
    private SoundInkInstance soundInstance;
    private String soundAction;
    private String identifier;
    private String soundName;
    private float volume;
    private float currentVolume;
    private float pitch;
    private boolean looping;

    public enum SoundType {
        SFX,
        SONG,
        STOP
    }

    private SoundType soundType;

    @Override
    public void tick() {
        if (soundInstance == null || soundManager == null) {
            isRunning = false;
            return;
        }
        if (!soundManager.isActive(soundInstance) && soundAction.equals("play")) {
            isRunning = false;
            return;
        }
        if (totalTick == 0 || !isRunning) return;
        tick++;
        if (tick > totalTick && soundAction.equals("stop")) {
            soundManager.stop(soundInstance);
            isRunning = false;
        }
    }

    @Override
    public void partialTick(float partialTick) {
        if (soundInstance == null || soundManager == null || totalTick == 0 || !isRunning) return;
        double t = Mth.clamp((tick + partialTick) / totalTick, 0.0, 1.0);
        soundInstance.setFading(t < 1.0);
        currentVolume =
                soundAction.equals("play") ? (float) Mth.lerp(t, 0.0, volume) : (float) Mth.lerp(t, volume, 0.0);
        if (soundAction.equals("play")) {
            ((VolumeAudio) soundManager).narrativecraft$setVolume(soundInstance, currentVolume);
        } else {
            ((VolumeAudio) soundManager).narrativecraft$setVolume(soundInstance.getIdentifier(), currentVolume);
        }
        if (currentVolume <= 0.0f && soundAction.equals("stop")) {
            soundManager.stop(soundInstance);
        }
    }

    @Override
    public void stop() {
        if (soundManager != null && soundInstance != null) soundManager.stop(soundInstance);
        isRunning = false;
    }

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
        soundManager = Minecraft.getInstance().getSoundManager();
        soundInstance = createSoundInstance();

        if (soundType == SoundType.STOP && soundName.equals("all")) {
            for (InkAction inkAction : playerSession.getActiveClientInkActions()) {
                if (!(inkAction instanceof SoundInkAction soundInkAction)) continue;
                soundInkAction.stop();
            }
            isRunning = false;
            return InkActionResult.ok();
        }

        switch (soundAction) {
            case "play" -> soundManager.play(soundInstance);
            case "stop" -> {
                for (InkAction inkAction : playerSession.getActiveClientInkActions()) {
                    if (!(inkAction instanceof SoundInkAction soundInkAction)) continue;

                    if (soundName.equals("all") && soundInkAction.soundType == this.soundType) {
                        soundInkAction.stop();
                    } else if (soundInkAction
                                    .soundInstance
                                    .getIdentifier()
                                    .compareTo(this.soundInstance.getIdentifier())
                            == 0) {
                        this.volume = soundInkAction.currentVolume;
                        soundInkAction.totalTick = 0;
                    }
                }
            }
        }
        if (soundName.equals("all")) {
            isRunning = false;
        }

        return InkActionResult.ok();
    }

    private SoundInkInstance createSoundInstance() {
        return new SoundInkInstance(
                Identifier.fromNamespaceAndPath(identifier, soundName),
                SoundSource.MASTER,
                volume,
                pitch,
                SoundInstance.createUnseededRandom(),
                looping,
                0,
                SoundInstance.Attenuation.NONE,
                0,
                0,
                0,
                true);
    }
}
