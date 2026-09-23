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
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import fr.loudo.narrativecraft.utils.UtilsServer;
import java.util.UUID;
import net.minecraft.network.chat.Component;

public abstract class AbstractNarrativeEntryEditor<T extends NarrativeEntryPayload, E extends NarrativeEntry<T>>
        implements NarrativeEntryEditor<T, E> {

    protected abstract String getTypeKey();

    protected abstract NarrativeManager<E> getSiblings(T payload);

    protected abstract E build(UUID entryId, T payload, E existing);

    protected abstract void copyAttributes(E target, E source);

    @Override
    public E resolve(UUID entryId, T payload) {
        NarrativeManager<E> siblings = getSiblings(payload);
        if (siblings == null) return null;
        return siblings.getById(entryId);
    }

    @Override
    public OperationResult add(UUID entryId, T payload) {
        NarrativeManager<E> siblings = getSiblings(payload);
        if (siblings == null) return OperationResult.failure("error.parent_not_found", payload.getName());
        if (siblings.getById(entryId) != null) return alreadyExists(payload.getName());

        E entry = build(entryId, payload, null);
        OperationResult validation = validate(entry, siblings);
        if (validation.isFailure()) return validation;

        OperationResult storage = NarrativeCraftFileRegistry.getInstance().create(entry);
        if (storage.isFailure()) return storage;

        siblings.add(entry);
        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.add(entry.getId(), entry.toPayload()));
        return OperationResult.success();
    }

    @Override
    public OperationResult edit(UUID entryId, T payload) {
        NarrativeManager<E> siblings = getSiblings(payload);
        E existing = siblings == null ? null : siblings.getById(entryId);
        if (existing == null) return notFound(payload.getName());

        E updated = build(entryId, payload, existing);
        OperationResult validation = validate(updated, siblings);
        if (validation.isFailure()) return validation;

        OperationResult storage = NarrativeCraftFileRegistry.getInstance().edit(existing, updated);
        if (storage.isFailure()) return storage;

        apply(existing, updated);
        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.edit(existing.getId(), existing.toPayload()));
        return OperationResult.success();
    }

    @Override
    public OperationResult delete(UUID entryId, T payload) {
        NarrativeManager<E> siblings = getSiblings(payload);
        E existing = siblings == null ? null : siblings.getById(entryId);
        if (existing == null) return notFound(payload.getName());

        OperationResult storage = NarrativeCraftFileRegistry.getInstance().delete(existing);
        if (storage.isFailure()) return storage;

        siblings.remove(existing);
        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.delete(existing.getId(), existing.toPayload()));
        return OperationResult.success();
    }

    protected void apply(E target, E source) {
        target.setName(source.getName());
        copyAttributes(target, source);
    }

    protected OperationResult validate(E entry, NarrativeManager<E> siblings) {
        String name = entry.getName();
        if (name == null || name.isBlank()) {
            return OperationResult.failure("error.must_have_name");
        }
        if (!name.matches(Utils.NO_SPECIAL_CHARACTERS)) {
            return OperationResult.failure("error.no_special_characters");
        }
        for (E sibling : siblings.getList()) {
            if (sibling.getId().equals(entry.getId())) continue;
            if (sibling.getNormalizedName().equals(entry.getNormalizedName())) {
                return alreadyExists(sibling.getName());
            }
        }
        return OperationResult.success();
    }

    protected Component getTypeLabel() {
        return Translation.message(getTypeKey());
    }

    protected OperationResult alreadyExists(String name) {
        return OperationResult.failure("error.already_exists", getTypeLabel(), name);
    }

    protected OperationResult notFound(String name) {
        return OperationResult.failure("error.not_exists", getTypeLabel(), name);
    }
}
