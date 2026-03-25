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

package fr.loudo.narrativecraft.editors.cutscene;

import fr.loudo.narrativecraft.editors.Editor;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.playback.Playback;
import fr.loudo.narrativecraft.session.PlayerSession;
import java.util.ArrayList;
import java.util.List;

public class CutsceneEditor implements Editor {

    private final Cutscene cutscene;
    private final List<Playback> playbacks = new ArrayList<>();
    private final PlayerSession playerSession;
    private int totalTick;
    private int currentTick;
    private boolean playing;

    public CutsceneEditor(Cutscene cutscene, PlayerSession playerSession) {
        this.cutscene = cutscene;
        this.playerSession = playerSession;
    }

    public void tick() {
        if (!playing) return;

        for (Playback playback : playbacks) {
            playback.tick();
        }
    }

    public void init() {
        for (Subscene subscene : cutscene.getSubscenes()) {
            for (Animation animation : subscene.getAnimations()) {
                animation.initialize();
                playbacks.add(new Playback(animation, playerSession.getPlayer()));
            }
        }
        for (Animation animation : cutscene.getAnimations()) {
            animation.initialize();
            playbacks.add(new Playback(animation, playerSession.getPlayer()));
        }
        for (Playback playback : playbacks) {
            playback.start();
        }
    }

    public void stop() {
        for (Playback playback : playbacks) {
            playback.stop();
        }
    }

    public void play() {
        playing = true;
    }

    public void pause() {
        playing = false;
    }

    public Cutscene getCutscene() {
        return cutscene;
    }

    public PlayerSession getPlayerSession() {
        return playerSession;
    }

    public int getTotalTick() {
        return totalTick;
    }

    public void setTotalTick(int totalTick) {
        this.totalTick = totalTick;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }
}
