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

package fr.loudo.narrativecraft.client.editors.cutscene.layers;

import fr.loudo.narrativecraft.api.client.editors.cutscene.ClientCutsceneLayerPlayer;
import fr.loudo.narrativecraft.api.client.editors.cutscene.ClientCutsceneLayerRegistry;
import fr.loudo.narrativecraft.api.client.editors.cutscene.ClientCutsceneLayerType;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ClientCutsceneLayerRegistryImpl implements ClientCutsceneLayerRegistry {

    private final Map<String, ClientCutsceneLayerType<?>> types = new HashMap<>();

    @Override
    public void register(ClientCutsceneLayerType<?> type) {
        String typeId = normalize(type.getTypeId());
        if (types.containsKey(typeId)) {
            throw new IllegalStateException("ClientCutsceneLayerType '" + typeId + "' is already registered");
        }
        types.put(typeId, type);
    }

    @Override
    public void unregister(String typeId) {
        types.remove(normalize(typeId));
    }

    @Override
    public ClientCutsceneLayerType<?> getType(String typeId) {
        return types.get(normalize(typeId));
    }

    @Override
    public ClientCutsceneLayerPlayer createPlayer(CutsceneLayer layer) {
        ClientCutsceneLayerType<?> type = getType(layer.getTypeId());
        if (type == null) return null;
        return createPlayer(type, layer);
    }

    @Override
    public Keyframe createDefaultKeyframe(CutsceneLayer layer, int tick) {
        ClientCutsceneLayerType<?> type = getType(layer.getTypeId());
        if (type == null) return null;
        return createDefaultKeyframe(type, layer, tick);
    }

    private static <L extends CutsceneLayer> ClientCutsceneLayerPlayer createPlayer(
            ClientCutsceneLayerType<L> type, CutsceneLayer layer) {
        return type.createPlayer(type.getLayerClass().cast(layer));
    }

    private static <L extends CutsceneLayer> Keyframe createDefaultKeyframe(
            ClientCutsceneLayerType<L> type, CutsceneLayer layer, int tick) {
        return type.createDefaultKeyframe(type.getLayerClass().cast(layer), tick);
    }

    private static String normalize(String typeId) {
        return typeId.toLowerCase(Locale.ROOT);
    }
}
