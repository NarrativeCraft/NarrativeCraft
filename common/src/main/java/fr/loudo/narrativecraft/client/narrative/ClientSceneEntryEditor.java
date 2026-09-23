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

package fr.loudo.narrativecraft.client.narrative;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.NarrativeEntryResolver;
import fr.loudo.narrativecraft.narrative.NarrativeManager;
import fr.loudo.narrativecraft.narrative.SceneEntryPayload;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import java.util.UUID;

public abstract class ClientSceneEntryEditor<T extends SceneEntryPayload, E extends NarrativeEntry<T>>
        implements ClientNarrativeEntryEditor<T, E> {

    protected final NarrativeEntryResolver resolver =
            ClientNarrativeCraftMod.getInstance().getEntryResolver();

    protected abstract NarrativeManager<E> getManager(Scene scene);

    protected abstract E create(UUID entryId, T payload, Scene scene);

    protected abstract void copyAttributes(E target, E source);

    @Override
    public void add(UUID entryId, T payload) {
        Scene scene = resolver.scene(payload);
        if (scene == null) return;
        getManager(scene).add(create(entryId, payload, scene));
    }

    @Override
    public void edit(UUID entryId, T payload) {
        Scene scene = resolver.scene(payload);
        if (scene == null) return;
        E entry = getManager(scene).getById(entryId);
        if (entry == null) return;
        entry.setName(payload.getName());
        copyAttributes(entry, create(entryId, payload, scene));
    }

    @Override
    public void delete(UUID entryId, T payload) {
        Scene scene = resolver.scene(payload);
        if (scene == null) return;
        E entry = getManager(scene).getById(entryId);
        if (entry == null) return;
        getManager(scene).remove(entry);
    }

    @Override
    public E resolve(UUID entryId, T payload) {
        Scene scene = resolver.scene(payload);
        if (scene == null) return null;
        return getManager(scene).getById(entryId);
    }
}
