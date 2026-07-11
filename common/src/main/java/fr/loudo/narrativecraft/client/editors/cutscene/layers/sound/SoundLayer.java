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

package fr.loudo.narrativecraft.client.editors.cutscene.layers.sound;

import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.SoundKeyframe;
import fr.loudo.narrativecraft.editors.cutscene.layers.CutsceneLayerType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;

public class SoundLayer extends CutsceneLayer {
    private final Map<Integer, SimpleSoundInstance> playedSounds = new HashMap<>();

    public SoundLayer(CutsceneLayerType layerType) {
        super(layerType);
    }

    @Override
    public String getTypeId() {
        return SoundLayerType.ID;
    }

    @Override
    public SoundKeyframe createDefaultKeyframe(int tick) {
        return new SoundKeyframe(this, tick);
    }

    @Override
    public boolean execute(float tick) {
        for (SoundKeyframe keyframe : getSortedKeyframes(SoundKeyframe.class)) {
            if (tick < keyframe.getTick()) {
                stopSound(keyframe.getTick());
            } else if ((int) tick == keyframe.getTick() && !playedSounds.containsKey(keyframe.getTick())) {
                playKeyframe(keyframe);
            }
        }
        return false;
    }

    @Override
    public void stop() {
        for (int keyframeTick : new ArrayList<>(playedSounds.keySet())) {
            stopSound(keyframeTick);
        }
    }

    private void playKeyframe(SoundKeyframe keyframe) {
        if (keyframe.getSoundId().isBlank()) return;

        SimpleSoundInstance soundInstance = new SimpleSoundInstance(
                Identifier.parse(keyframe.getSoundId()),
                SoundSource.MASTER,
                keyframe.getVolume(),
                keyframe.getPitch(),
                SoundInstance.createUnseededRandom(),
                false,
                0,
                SoundInstance.Attenuation.NONE,
                0,
                0,
                0,
                true);

        Minecraft.getInstance().getSoundManager().play(soundInstance);
        playedSounds.put(keyframe.getTick(), soundInstance);
    }

    private void stopSound(int keyframeTick) {
        SimpleSoundInstance soundInstance = playedSounds.remove(keyframeTick);
        if (soundInstance == null) return;

        Minecraft.getInstance().getSoundManager().stop(soundInstance);
    }
}
