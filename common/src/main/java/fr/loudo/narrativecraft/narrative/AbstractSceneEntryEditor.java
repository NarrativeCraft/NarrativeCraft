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

package fr.loudo.narrativecraft.narrative;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import java.util.UUID;

public abstract class AbstractSceneEntryEditor<T extends SceneEntryPayload, E extends NarrativeEntry<T>>
        extends AbstractNarrativeEntryEditor<T, E> {

    protected final NarrativeEntryResolver resolver =
            NarrativeCraftMod.getInstance().getEntryResolver();

    protected abstract NarrativeManager<E> getManager(Scene scene);

    protected abstract E build(UUID entryId, T payload, Scene scene, E existing);

    @Override
    protected NarrativeManager<E> getSiblings(T payload) {
        Scene scene = resolver.scene(payload);
        if (scene == null) return null;
        return getManager(scene);
    }

    @Override
    protected E build(UUID entryId, T payload, E existing) {
        return build(entryId, payload, resolver.scene(payload), existing);
    }
}
