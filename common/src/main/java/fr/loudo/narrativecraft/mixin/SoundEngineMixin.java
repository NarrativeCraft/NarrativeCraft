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

package fr.loudo.narrativecraft.mixin;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.audio.Channel;
import fr.loudo.narrativecraft.client.inkTag.actions.sound.SoundInkInstance;
import fr.loudo.narrativecraft.utils.VolumeAudio;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin implements VolumeAudio {

    @Shadow
    protected abstract float calculateVolume(SoundInstance sound);

    @Shadow
    @Final
    private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;

    @Shadow
    private boolean loaded;

    @Shadow
    @Final
    private Multimap<SoundSource, SoundInstance> instanceBySource;

    @Unique
    private boolean narrativecraft$paused;

    @Inject(method = "tick(Z)V", at = @At("HEAD"))
    private void narrativecraft$pauseSoundInkInstances(boolean paused, CallbackInfo callbackInfo) {
        if (!this.loaded || paused == this.narrativecraft$paused) return;
        this.narrativecraft$paused = paused;
        for (Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> entry : this.instanceToChannel.entrySet()) {
            if (entry.getKey() instanceof SoundInkInstance) {
                entry.getValue().execute(paused ? Channel::pause : Channel::unpause);
            }
        }
    }

    @Override
    public void narrativecraft$setVolume(SoundInstance soundInstance, float volume) {
        if (this.loaded) {
            ChannelAccess.ChannelHandle channelHandle = this.instanceToChannel.get(soundInstance);
            if (channelHandle != null) {
                channelHandle.execute((channel) -> channel.setVolume(volume * this.calculateVolume(soundInstance)));
            }
        }
    }

    @Override
    public void narrativecraft$setVolume(Identifier source, float volume) {
        for (SoundInstance instance : this.instanceBySource.get(SoundSource.MASTER)) {
            if (source == null || instance.getIdentifier().equals(source)) {
                narrativecraft$setVolume(instance, volume);
            }
        }
    }

    /*
        Don't override volume of a sound ink action when changing volume in client settings
        Otherwise, sounds from sound ink are at max volume and broken
    */
    @Redirect(
            method = "refreshCategoryVolume",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
    private void narrativecraft$updateCategoryVolume(
            Map<SoundInstance, ChannelAccess.ChannelHandle> instance,
            BiConsumer<? super SoundInstance, ? super ChannelAccess.ChannelHandle> v) {
        instance.forEach((soundInstance, channelHandle) -> {
            if (soundInstance instanceof SoundInkInstance soundInkInstance) {
                if (soundInkInstance.isFading()) return;
            }
            float f = this.calculateVolume(soundInstance);
            channelHandle.execute((channel) -> channel.setVolume(f));
        });
    }
}
