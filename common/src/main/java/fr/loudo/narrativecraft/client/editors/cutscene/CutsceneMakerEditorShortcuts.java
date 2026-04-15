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

package fr.loudo.narrativecraft.client.editors.cutscene;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayerType;
import fr.loudo.narrativecraft.editors.cutscene.layers.CutsceneLayerType;
import fr.loudo.narrativecraft.narrative.cutscene.CutsceneSerializer;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneSave;
import fr.loudo.narrativecraft.platform.Services;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

public class CutsceneMakerEditorShortcuts {

    private static final int MAX_HISTORY = 50;

    public interface EditorAction {
        void undo();

        void redo();
    }

    private record KeyframeCopy(CutsceneLayer layer, int tickOffset, JsonObject data) {}

    private final ClientCutsceneMakerEditor editor;
    private final Deque<EditorAction> undoStack = new ArrayDeque<>();
    private final Deque<EditorAction> redoStack = new ArrayDeque<>();
    private final List<KeyframeCopy> clipboard = new ArrayList<>();

    public CutsceneMakerEditorShortcuts(ClientCutsceneMakerEditor editor) {
        this.editor = editor;
    }

    public boolean handleKeyPressed(KeyEvent event) {
        boolean ctrl = Minecraft.getInstance().hasControlDown();
        int keyCode = event.key();

        if (ctrl && keyCode == GLFW.GLFW_KEY_S) {
            save();
            return true;
        }
        if (ctrl && keyCode == GLFW.GLFW_KEY_Z) {
            undo();
            return true;
        }
        if (ctrl && keyCode == GLFW.GLFW_KEY_Y) {
            redo();
            return true;
        }
        if (ctrl && keyCode == GLFW.GLFW_KEY_C) {
            copy();
            return true;
        }
        if (ctrl && keyCode == GLFW.GLFW_KEY_V) {
            paste();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            deleteSelected();
            return true;
        }
        return false;
    }

    public void pushAction(EditorAction action) {
        undoStack.push(action);
        if (undoStack.size() > MAX_HISTORY) undoStack.removeLast();
        redoStack.clear();
    }

    public void recordMoveAction(Map<Keyframe, Integer> originalTicks) {
        Map<Keyframe, Integer> finalTicks = new HashMap<>();
        for (Keyframe keyframe : originalTicks.keySet()) {
            finalTicks.put(keyframe, keyframe.getTick());
        }

        boolean moved = originalTicks.entrySet().stream()
                .anyMatch(entry -> !entry.getValue().equals(finalTicks.get(entry.getKey())));
        if (!moved) return;

        Map<Keyframe, Integer> snapshotOriginal = Map.copyOf(originalTicks);
        Map<Keyframe, Integer> snapshotFinal = Map.copyOf(finalTicks);

        pushAction(new EditorAction() {
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

    private void save() {
        String layersJson = CutsceneSerializer.serializeLayers(editor.getEditorLayers());
        Services.PACKET.sendToServer(new C2SCutsceneSave(editor.getCutscene(), layersJson));
    }

    private void undo() {
        if (undoStack.isEmpty()) return;
        EditorAction action = undoStack.pop();
        action.undo();
        redoStack.push(action);
    }

    private void redo() {
        if (redoStack.isEmpty()) return;
        EditorAction action = redoStack.pop();
        action.redo();
        undoStack.push(action);
    }

    private void copy() {
        List<Keyframe> selected = editor.getSelectedKeyframes();
        if (selected.isEmpty()) return;

        int earliestTick = selected.stream().mapToInt(Keyframe::getTick).min().getAsInt();
        clipboard.clear();

        for (Keyframe keyframe : selected) {
            ICutsceneLayerType rawType = keyframe.getLayer().getType();
            if (!(rawType instanceof CutsceneLayerType layerType)) continue;
            JsonObject data = layerType.serializeKeyframe(keyframe);
            if (data == null) continue;
            int tickOffset = keyframe.getTick() - earliestTick;
            clipboard.add(new KeyframeCopy(keyframe.getLayer(), tickOffset, data));
        }
    }

    private void paste() {
        if (clipboard.isEmpty()) return;
        int playheadTick = editor.getPlayHeadTick();
        List<Keyframe> pastedKeyframes = new ArrayList<>();

        for (KeyframeCopy copy : clipboard) {
            ICutsceneLayerType rawType = copy.layer().getType();
            if (!(rawType instanceof CutsceneLayerType layerType)) continue;

            JsonObject json = JsonParser.parseString(copy.data().toString()).getAsJsonObject();
            json.addProperty("tick", playheadTick + copy.tickOffset());

            Keyframe pasted = layerType.deserializeKeyframe(copy.layer(), json);
            if (pasted == null) continue;

            copy.layer().addKeyframe(pasted);
            pastedKeyframes.add(pasted);
        }

        if (pastedKeyframes.isEmpty()) return;

        pushAction(new EditorAction() {
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

    private void deleteSelected() {
        List<Keyframe> selected = editor.getSelectedKeyframes();
        if (selected.isEmpty()) return;

        List<Keyframe> toDelete = new ArrayList<>(selected);
        for (Keyframe keyframe : toDelete) {
            keyframe.getLayer().removeKeyframe(keyframe);
        }
        editor.clearSelection();

        pushAction(new EditorAction() {
            @Override
            public void undo() {
                for (Keyframe keyframe : toDelete) {
                    keyframe.setSelected(false);
                    keyframe.getLayer().addKeyframe(keyframe);
                }
            }

            @Override
            public void redo() {
                for (Keyframe keyframe : toDelete) {
                    keyframe.getLayer().removeKeyframe(keyframe);
                }
            }
        });
    }
}
