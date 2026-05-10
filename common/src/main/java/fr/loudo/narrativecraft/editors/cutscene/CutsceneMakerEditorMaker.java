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
import fr.loudo.narrativecraft.client.editors.cutscene.layers.camera.CameraLayer;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.CameraKeyframe;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.cutscene.CutsceneSerializer;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.network.cutscene.BiCutscenePlayHeadPacket;
import fr.loudo.narrativecraft.network.cutscene.S2CCutsceneEditorData;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.playback.Playback;
import fr.loudo.narrativecraft.session.PlayerSession;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CutsceneMakerEditorMaker implements EditorMaker {

    private final Cutscene cutscene;
    private final List<Playback> playbacks = new ArrayList<>();
    private final List<ICutsceneLayer> layersAdded = new ArrayList<>();
    private final PlayerSession playerSession;
    private int totalTick;
    private int currentTick;
    private boolean playing;
    private NarrativeEnvironment environment;

    public CutsceneMakerEditorMaker(Cutscene cutscene, PlayerSession playerSession) {
        this.cutscene = cutscene;
        this.playerSession = playerSession;
        this.environment = NarrativeEnvironment.DEVELOPMENT;
    }

    public CutsceneMakerEditorMaker(Cutscene cutscene, PlayerSession playerSession, NarrativeEnvironment environment) {
        this(cutscene, playerSession);
        this.environment = environment;
    }

    public void tick() {
        if (!playing) return;

        for (Playback playback : playbacks) {
            playback.tick();
        }

        Services.PACKET.sendToPlayer(playerSession.getPlayer(), new BiCutscenePlayHeadPacket(currentTick));

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
        teleportToEditorOrigin();

        if (cutscene.getEditorLayers() != null && !cutscene.getEditorLayers().isEmpty()) {
            String layersJson = CutsceneSerializer.serializeLayers(cutscene.getEditorLayers());
            Services.PACKET.sendToPlayer(
                    playerSession.getPlayer(), new S2CCutsceneEditorData(cutscene.getId(), layersJson));
        }
    }

    public void teleportToEditorOrigin() {
        ServerPlayer player = playerSession.getPlayer();
        Vec3 position = null;

        if (!playbacks.isEmpty()) {
            Playback playback = playbacks.get(0);
            position = playback.getFirstPosition();
        } else {
            for (ICutsceneLayer layer : layersAdded) {
                if (layer instanceof CameraLayer cameraLayer) {
                    List<CameraKeyframe> cameraKeyframes = cameraLayer.getSortedCameraKeyframes();
                    if (cameraKeyframes.isEmpty()) continue;
                    CameraKeyframe keyframe = cameraKeyframes.get(0);
                    position = keyframe.getPosition().getPosition();
                    break;
                }
            }
        }
        if (position != null) {
            player.connection.teleport(position.x, position.y, position.z, player.getYRot(), player.getXRot());
        }
    }

    @Override
    public NarrativeEnvironment getEnvironment() {
        return environment;
    }

    public void start() {
        for (Playback playback : playbacks) {
            playback.start(Collections.singletonList(playerSession.getPlayer()));
        }
    }

    public void stop() {
        if (environment == NarrativeEnvironment.PRODUCTION) {
            for (Playback playback : playbacks) {
                playback.stop();
            }
        } else {
            for (Playback playback : playbacks) {
                playback.stopAndKill();
            }
            playerSession.changeGameMode(playerSession.getLastGameType());
        }
    }

    public void moveTo(int tick) {
        for (Playback playback : playbacks) {
            if (tick < currentTick) {
                playback.rewindTo(tick);
            } else {
                playback.moveTo(tick);
            }
        }
        currentTick = tick;
    }

    public void addLayer(ICutsceneLayer layer) {
        layersAdded.add(layer);
    }

    public void removeLayer(ICutsceneLayer layer) {
        layersAdded.remove(layer);
    }

    public void play() {
        if (currentTick >= totalTick) {
            Services.PACKET.sendToPlayer(playerSession.getPlayer(), new BiCutscenePlayHeadPacket(totalTick));
            return;
        }
        playing = true;
        for (Playback playback : playbacks) {
            playback.play();
        }
    }

    public boolean isFinished() {
        return currentTick >= totalTick;
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

    public List<Playback> getPlaybacks() {
        return playbacks;
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
