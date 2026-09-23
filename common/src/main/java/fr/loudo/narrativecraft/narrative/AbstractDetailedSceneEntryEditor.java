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

import fr.loudo.narrativecraft.files.NarrativeCraftFileRegistry;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import java.util.UUID;

public abstract class AbstractDetailedSceneEntryEditor<
                T extends SceneEntryPayload,
                D extends NarrativeEntryDetail,
                E extends NarrativeEntry<T> & DetailedNarrativeEntry<D>>
        extends AbstractSceneEntryEditor<T, E> {

    protected abstract E create(UUID entryId, T payload, Scene scene);

    @Override
    protected final E build(UUID entryId, T payload, Scene scene, E existing) {
        E entry = create(entryId, payload, scene);
        if (existing != null) {
            entry.setDetail(existing.getDetail());
        }
        return entry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public OperationResult saveDetail(UUID entryId, T payload, NarrativeEntryDetail detail) {
        E existing = resolve(entryId, payload);
        if (existing == null) return notFound(payload.getName());
        if (!NarrativeEntryType.fromPayload(payload).acceptsDetail(detail)) {
            return OperationResult.failure("error.invalid_data", existing.getName());
        }
        return saveDetail(existing, (D) detail);
    }

    public OperationResult saveDetail(E existing, D detail) {
        E updated = build(existing.getId(), existing.toPayload(), existing);
        updated.setDetail(detail);

        OperationResult storage = NarrativeCraftFileRegistry.getInstance().edit(existing, updated);
        if (storage.isFailure()) return storage;

        existing.setDetail(detail);
        return OperationResult.success();
    }
}
