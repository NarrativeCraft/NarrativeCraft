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

package fr.loudo.narrativecraft.client.inkTag.actions.sound;

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.narrative.inkTag.actions.SoundInkAction;
import fr.loudo.narrativecraft.utils.VolumeAudio;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class ClientSoundInkAction extends SoundInkAction {

    private SoundManager soundManager;
    private SoundInkInstance soundInstance;
    private float currentVolume;

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
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        soundManager = Minecraft.getInstance().getSoundManager();
        soundInstance = createSoundInstance();

        if (soundType == SoundType.STOP && soundName.equals("all")) {
            for (InkAction inkAction : playerSession.getActiveClientInkActions()) {
                if (!(inkAction instanceof ClientSoundInkAction clientSoundInkAction)) continue;
                clientSoundInkAction.stop();
            }
            isRunning = false;
            return InkActionResult.ok();
        }

        switch (soundAction) {
            case "play" -> soundManager.play(soundInstance);
            case "stop" -> {
                for (InkAction inkAction : playerSession.getActiveClientInkActions()) {
                    if (!(inkAction instanceof ClientSoundInkAction clientSoundInkAction)) continue;

                    if (soundName.equals("all") && clientSoundInkAction.soundType == this.soundType) {
                        clientSoundInkAction.stop();
                    } else if (clientSoundInkAction
                                    .soundInstance
                                    .getIdentifier()
                                    .compareTo(this.soundInstance.getIdentifier())
                            == 0) {
                        if (totalTick == 0) {
                            clientSoundInkAction.stop();
                        } else {
                            this.volume = clientSoundInkAction.currentVolume > 0
                                    ? clientSoundInkAction.currentVolume
                                    : clientSoundInkAction.volume;
                            clientSoundInkAction.totalTick = 0;
                        }
                    }
                }
            }
        }
        if (soundName.equals("all") || totalTick == 0) {
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
