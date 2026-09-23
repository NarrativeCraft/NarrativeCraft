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

package fr.loudo.narrativecraft.client.editors.cutscene;

import fr.loudo.narrativecraft.api.client.editors.cutscene.ClientCutsceneLayerPlayer;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;

public class CutsceneEditorPlayback {

    private final List<CutsceneLayer> layers;
    private final Map<CutsceneLayer, ClientCutsceneLayerPlayer> layerPlayers = new IdentityHashMap<>();
    private final ClientPlayerSession playerSession;

    private float currentTick = 0f;
    private int totalTick;
    private boolean playing = false;
    private boolean dragPause = false;

    public CutsceneEditorPlayback(List<CutsceneLayer> layers, ClientPlayerSession playerSession, int totalTick) {
        this.layers = layers;
        this.playerSession = playerSession;
        this.totalTick = totalTick;
    }

    public void play(float fromTick) {
        currentTick = fromTick;
        playing = true;
    }

    public void pause() {
        playing = false;
        playerSession.getCutsceneDataSession().setFov(-1f);
        playerSession.getCutsceneDataSession().setKeyframePosition(null);
        for (ClientCutsceneLayerPlayer layerPlayer : layerPlayers.values()) {
            layerPlayer.stop();
        }
    }

    public void releaseLayer(CutsceneLayer layer) {
        ClientCutsceneLayerPlayer layerPlayer = layerPlayers.remove(layer);
        if (layerPlayer != null) {
            layerPlayer.stop();
        }
    }

    public void releaseLayers() {
        for (ClientCutsceneLayerPlayer layerPlayer : layerPlayers.values()) {
            layerPlayer.stop();
        }
        layerPlayers.clear();
    }

    public void tick(DeltaTracker delta) {
        if (!playing || dragPause || totalTick <= 0 || Minecraft.getInstance().isPaused()) return;

        currentTick += delta.getGameTimeDeltaTicks();

        if (currentTick >= totalTick) {
            currentTick = totalTick;
            pause();
            return;
        }

        executeKeyframes();
    }

    private void executeKeyframes() {
        Set<String> executedTypes = new HashSet<>();
        for (CutsceneLayer layer : layers) {
            String typeId = layer.getTypeId();
            if (executedTypes.contains(typeId)) continue;
            ClientCutsceneLayerPlayer layerPlayer = getLayerPlayer(layer);
            if (layerPlayer != null && layerPlayer.execute(currentTick)) {
                executedTypes.add(typeId);
            }
        }
    }

    private ClientCutsceneLayerPlayer getLayerPlayer(CutsceneLayer layer) {
        return layerPlayers.computeIfAbsent(layer, unplayedLayer -> ClientNarrativeCraftMod.getInstance()
                .getCutsceneLayerRegistry()
                .createPlayer(unplayedLayer));
    }

    public void seekTo(float tick) {
        currentTick = tick;
    }

    public boolean isPlaying() {
        return playing;
    }

    public float getCurrentTick() {
        return currentTick;
    }

    public boolean isDragPause() {
        return dragPause;
    }

    public void setDragPause(boolean dragPause) {
        this.dragPause = dragPause;
    }

    public void setTotalTick(int totalTick) {
        this.totalTick = totalTick;
    }
}
