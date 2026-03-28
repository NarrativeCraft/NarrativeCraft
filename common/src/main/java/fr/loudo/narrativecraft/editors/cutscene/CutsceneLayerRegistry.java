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

import fr.loudo.narrativecraft.api.editors.ICutsceneLayerRegistry;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CutsceneLayerRegistry implements ICutsceneLayerRegistry {

    private final Map<String, ICutsceneLayer> layers = new HashMap<>();

    public void register(String id, ICutsceneLayer cutsceneLayer) {
        id = id.toLowerCase();
        ICutsceneLayer existing = layers.get(id);
        if (existing != null) {
            throw new IllegalStateException("CutsceneLayer " + id + " already exists");
        }

        layers.put(id, cutsceneLayer);
    }

    public void unregister(String id) {
        id = id.toLowerCase();
        ICutsceneLayer layer = layers.get(id);
        if (layer != null) {
            layers.remove(id);
        }
    }

    public ICutsceneLayer getLayer(String id) {
        id = id.toLowerCase();
        return layers.get(id);
    }

    public List<ICutsceneLayer> getLayers() {
        return new ArrayList<>(layers.values());
    }
}
