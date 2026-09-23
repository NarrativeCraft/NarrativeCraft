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

import com.google.gson.JsonObject;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayerType;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.EditorAction;
import fr.loudo.narrativecraft.client.editors.EditorHistory;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.client.utils.UtilsClient;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.cutscene.CutsceneDeserializer;
import fr.loudo.narrativecraft.narrative.cutscene.CutsceneSerializer;
import fr.loudo.narrativecraft.network.cutscene.BiCutscenePlayHeadPacket;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneControl;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneSave;
import fr.loudo.narrativecraft.platform.Services;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;

public class ClientCutsceneMakerEditorMaker implements EditorMaker {

    private record KeyframeCopy(CutsceneLayer layer, int tickOffset, JsonObject data) {}

    private final Minecraft minecraft = Minecraft.getInstance();
    private final List<CutsceneLayer> layers = new ArrayList<>();
    private final Cutscene cutscene;
    private final ClientPlayerSession playerSession =
            ClientNarrativeCraftMod.getInstance().getPlayerSession();
    private final CutsceneEditorPlayback playback;
    private final EditorHistory history = new EditorHistory();
    private final List<KeyframeCopy> clipboard = new ArrayList<>();
    private final NarrativeEnvironment environment;

    private final List<Keyframe> selectedKeyframes = new ArrayList<>();

    private int totalTick;
    private int playHeadTick = 0;
    private float previewRoll = 0f;

    public ClientCutsceneMakerEditorMaker(Cutscene cutscene, NarrativeEnvironment environment) {
        this.cutscene = cutscene;
        this.playback = new CutsceneEditorPlayback(layers, playerSession, cutscene.getMaxTick());
        this.environment = environment;
    }

    @Override
    public void init() {
        totalTick = cutscene.getMaxTick();
        playback.setTotalTick(totalTick);
        playHeadTick = 0;

        if (environment == NarrativeEnvironment.PRODUCTION) {
            startProductionPlayback();
        }
    }

    private void startProductionPlayback() {
        if (environment != NarrativeEnvironment.PRODUCTION) return;
        int firstCameraTick = cutscene.getFirstCameraTick();
        playback.play(firstCameraTick < totalTick ? firstCameraTick : 0);
    }

    public void applyManualMaxTick(int value) {
        if (value <= 0) return;
        cutscene.setManualMaxTick(value);
        totalTick = value;
        playback.setTotalTick(value);
    }

    public void save() {
        String layersJson = CutsceneSerializer.serializeLayers(layers);
        Services.PACKET.sendToServer(new C2SCutsceneSave(cutscene, layersJson));
    }

    public void quit(boolean saveBeforeQuit) {
        if (saveBeforeQuit) {
            save();
        }
        Services.PACKET.sendToServer(new C2SCutsceneControl(C2SCutsceneControl.State.QUIT));
        playerSession.closeEditor();
    }

    @Override
    public void close() {
        playback.pause();
        playback.releaseLayers();
        playerSession.getCutsceneDataSession().reset();
        UtilsClient.setHudHidden(false);
        if (environment != NarrativeEnvironment.DEVELOPMENT) return;
        minecraft.gui.setScreen(null);
        playerSession.stopAllClientInkActions();
    }

    @Override
    public void tick() {
        boolean hideGui = minecraft.gui.hud.isHidden();
        if (playback.isPlaying() && !hideGui) {
            UtilsClient.setHudHidden(true);
        } else if (!playback.isPlaying() && hideGui) {
            UtilsClient.setHudHidden(false);
        }
    }

    public void loadLayers(String layersJson) {
        playback.releaseLayers();
        layers.clear();
        selectedKeyframes.clear();
        history.clear();
        clipboard.clear();
        CutsceneDeserializer.deserializeLayers(layersJson, cutscene);
        if (cutscene.getLayers() != null) {
            layers.addAll(cutscene.getLayers());
        }
        totalTick = cutscene.getMaxTick();
        playback.setTotalTick(totalTick);
        startProductionPlayback();
    }

    public void addLayer(CutsceneLayer layer) {
        layers.add(layer);
        rebuildSortIndices();
    }

    public void removeLayer(CutsceneLayer layer) {
        layers.remove(layer);
        playback.releaseLayer(layer);
        rebuildSortIndices();
    }

    public void undo() {
        history.undo();
    }

    public void redo() {
        history.redo();
    }

    public void recordKeyframeMove(Map<Keyframe, Integer> originalTicks) {
        Map<Keyframe, Integer> finalTicks = new HashMap<>();
        for (Keyframe keyframe : originalTicks.keySet()) {
            finalTicks.put(keyframe, keyframe.getTick());
        }
        if (finalTicks.equals(originalTicks)) return;

        Map<Keyframe, Integer> snapshotOriginal = Map.copyOf(originalTicks);
        Map<Keyframe, Integer> snapshotFinal = Map.copyOf(finalTicks);

        history.record(new EditorAction() {
            @Override
            public void undo() {
                snapshotOriginal.forEach(Keyframe::setTick);
            }

            @Override
            public void redo() {
                snapshotFinal.forEach(Keyframe::setTick);
            }
        });
    }

    public void copySelection() {
        if (selectedKeyframes.isEmpty()) return;

        int earliestTick =
                selectedKeyframes.stream().mapToInt(Keyframe::getTick).min().getAsInt();
        clipboard.clear();

        for (Keyframe keyframe : selectedKeyframes) {
            ICutsceneLayerType layerType = keyframe.getLayer().getType();
            JsonObject data = layerType.serializeKeyframe(keyframe);
            if (data == null) continue;
            clipboard.add(new KeyframeCopy(keyframe.getLayer(), keyframe.getTick() - earliestTick, data));
        }
    }

    public void paste() {
        if (clipboard.isEmpty()) return;
        List<Keyframe> pastedKeyframes = new ArrayList<>();

        for (KeyframeCopy copy : clipboard) {
            if (!layers.contains(copy.layer())) continue;
            JsonObject json = copy.data().deepCopy();
            json.addProperty("tick", playHeadTick + copy.tickOffset());

            Keyframe pasted = copy.layer().getType().deserializeKeyframe(copy.layer(), json);
            if (pasted == null) continue;

            copy.layer().addKeyframe(pasted);
            pastedKeyframes.add(pasted);
        }

        if (pastedKeyframes.isEmpty()) return;

        history.record(new EditorAction() {
            @Override
            public void undo() {
                for (Keyframe keyframe : pastedKeyframes) {
                    keyframe.getLayer().removeKeyframe(keyframe);
                }
            }

            @Override
            public void redo() {
                for (Keyframe keyframe : pastedKeyframes) {
                    keyframe.getLayer().addKeyframe(keyframe);
                }
            }
        });
    }

    public void deleteSelection() {
        if (selectedKeyframes.isEmpty()) return;

        List<Keyframe> deletedKeyframes = new ArrayList<>(selectedKeyframes);
        for (Keyframe keyframe : deletedKeyframes) {
            keyframe.getLayer().removeKeyframe(keyframe);
        }
        clearSelection();

        history.record(new EditorAction() {
            @Override
            public void undo() {
                for (Keyframe keyframe : deletedKeyframes) {
                    keyframe.getLayer().addKeyframe(keyframe);
                }
            }

            @Override
            public void redo() {
                for (Keyframe keyframe : deletedKeyframes) {
                    keyframe.getLayer().removeKeyframe(keyframe);
                }
            }
        });
    }

    public void clearSelection() {
        for (Keyframe keyframe : selectedKeyframes) {
            keyframe.setSelected(false);
        }
        selectedKeyframes.clear();
    }

    public void startPlayback() {
        if (playback.getCurrentTick() >= totalTick) {
            setPlayHeadTick(0);
        }
        playback.play(playback.getCurrentTick());
    }

    public void pausePlayback() {
        playback.pause();
        setPreviewRoll(0f);
    }

    private void rebuildSortIndices() {
        for (int index = 0; index < layers.size(); index++) {
            layers.get(index).setSortIndex(index);
        }
    }

    public int getPlayHeadTick() {
        return playHeadTick;
    }

    public void setPlayHeadTick(int tick) {
        playHeadTick = (int) Math.clamp(tick, 0, totalTick);
        playback.seekTo(playHeadTick);
        Services.PACKET.sendToServer(new BiCutscenePlayHeadPacket(playHeadTick));
    }

    public float getTick() {
        return playback.getCurrentTick();
    }

    public int getTotalTick() {
        return totalTick;
    }

    public Cutscene getCutscene() {
        return cutscene;
    }

    public CutsceneEditorPlayback getPlayback() {
        return playback;
    }

    public EditorHistory getHistory() {
        return history;
    }

    public boolean hasClipboard() {
        return !clipboard.isEmpty();
    }

    public float getPreviewRoll() {
        return previewRoll;
    }

    public void setPreviewRoll(float roll) {
        this.previewRoll = roll;
    }

    public List<CutsceneLayer> getLayers() {
        return layers;
    }

    public List<Keyframe> getSelectedKeyframes() {
        return selectedKeyframes;
    }

    @Override
    public NarrativeEnvironment getEnvironment() {
        return environment;
    }
}
