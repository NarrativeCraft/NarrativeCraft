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

import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayer;
import fr.loudo.narrativecraft.editors.Editor;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.network.cutscene.BiCutscenePlayHeadPacket;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.playback.Playback;
import fr.loudo.narrativecraft.session.PlayerSession;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.GameType;

public class CutsceneMakerEditor implements Editor {

    private final Cutscene cutscene;
    private final List<Playback> playbacks = new ArrayList<>();
    private final List<ICutsceneLayer> layersAdded = new ArrayList<>();
    private final PlayerSession playerSession;
    private int totalTick;
    private int currentTick;
    private boolean playing;

    public CutsceneMakerEditor(Cutscene cutscene, PlayerSession playerSession) {
        this.cutscene = cutscene;
        this.playerSession = playerSession;
    }

    public void tick() {
        if (!playing) return;

        for (Playback playback : playbacks) {
            playback.tick();
        }

        float ratio = (float) currentTick / (float) totalTick;
        Services.PACKET.sendToPlayer(playerSession.getPlayer(), new BiCutscenePlayHeadPacket(ratio, false));

        currentTick++;
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
        playerSession.changeGameMode(GameType.SPECTATOR);
        totalTick = cutscene.getMaxTick();
    }

    public void start() {
        for (Playback playback : playbacks) {
            playback.start();
        }
    }

    public void stop() {
        for (Playback playback : playbacks) {
            playback.stopAndKill();
        }
        playerSession.changeGameMode(playerSession.getLastGameType());
    }

    public void moveTo(int tick, boolean smooth) {
        currentTick = tick;
        for (Playback playback : playbacks) {
            playback.moveTo(tick, smooth);
        }
    }

    public void addLayer(ICutsceneLayer layer) {
        layersAdded.add(layer);
    }

    public void removeLayer(ICutsceneLayer layer) {
        layersAdded.remove(layer);
    }

    public void play() {
        if (currentTick >= totalTick) {
            Services.PACKET.sendToPlayer(playerSession.getPlayer(), new BiCutscenePlayHeadPacket(1.0f, true));
            return;
        }
        playing = true;
        for (Playback playback : playbacks) {
            playback.play();
        }
    }

    public void pause() {
        playing = false;
        for (Playback playback : playbacks) {
            playback.pause();
        }
    }

    public Cutscene getCutscene() {
        return cutscene;
    }

    public List<ICutsceneLayer> getLayersAdded() {
        return layersAdded;
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
