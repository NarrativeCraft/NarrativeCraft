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

package fr.loudo.narrativecraft.playback;

import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.recording.RecordingData;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public class Playback {

    private final UUID id = UUID.randomUUID();
    private final Animation animation;
    private final ServerPlayer requester;
    private final List<PlaybackContext> contexts = new ArrayList<>();
    private int tick = 0;
    private int totalTicks = 0;
    private boolean isPlaying;

    public Playback(Animation animation, ServerPlayer requester) {
        this.animation = animation;
        this.requester = requester;
    }

    public void start() {
        for (RecordingData recordingData : animation.getRecordingDataList()) {
            totalTicks += recordingData.getActions().size();
            contexts.add(new PlaybackContext(this, recordingData, requester.level()));
        }

        isPlaying = true;
        for (PlaybackContext context : contexts) {
            context.start();
        }
    }

    public void stop() {
        isPlaying = false;
        for (PlaybackContext context : contexts) {
            context.stop();
        }
    }

    public void tick() {
        if (!isPlaying) return;

        if (tick == totalTicks) {
            stop();
            return;
        }

        for (PlaybackContext context : contexts) {
            context.tick();
        }

        tick++;
    }

    public UUID getId() {
        return id;
    }

    public Animation getAnimation() {
        return animation;
    }

    public ServerPlayer getRequester() {
        return requester;
    }

    public int getTick() {
        return tick;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}
