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

import fr.loudo.narrativecraft.files.EntryChange;
import fr.loudo.narrativecraft.files.NarrativeCraftFileRegistry;
import fr.loudo.narrativecraft.files.RankedNarrativeCraftFileEditor;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.utils.UtilsServer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractRankedNarrativeEntryEditor<T extends NarrativeEntryPayload, E extends NarrativeEntry<T>>
        extends AbstractNarrativeEntryEditor<T, E> {

    private final Class<E> entryClass;

    protected AbstractRankedNarrativeEntryEditor(Class<E> entryClass) {
        this.entryClass = entryClass;
    }

    protected abstract int getRank(E entry);

    protected abstract E withRank(E entry, int rank);

    protected int getNextRank(NarrativeManager<E> siblings) {
        return getLastRank(siblings) + 1;
    }

    private int getLastRank(NarrativeManager<E> siblings) {
        int lastRank = 0;
        for (E sibling : siblings.getList()) {
            lastRank = Math.max(lastRank, getRank(sibling));
        }
        return lastRank;
    }

    private RankedNarrativeCraftFileEditor<E> getStorage() {
        return NarrativeCraftFileRegistry.getInstance().getRankedEditor(entryClass);
    }

    @Override
    protected OperationResult validate(E entry, NarrativeManager<E> siblings) {
        OperationResult validation = super.validate(entry, siblings);
        if (validation.isFailure()) return validation;

        int highestRank = siblings.getById(entry.getId()) == null ? getNextRank(siblings) : getLastRank(siblings);
        int rank = getRank(entry);
        if (rank < 1 || rank > highestRank) {
            return OperationResult.failure("error.invalid_rank", rank, highestRank);
        }
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

        List<EntryChange<E>> changes = new ArrayList<>();
        changes.add(new EntryChange<>(existing, updated));
        int oldRank = getRank(existing);
        int newRank = getRank(updated);
        if (newRank > oldRank) {
            changes.addAll(shift(siblings, oldRank + 1, newRank, -1));
        } else if (newRank < oldRank) {
            changes.addAll(shift(siblings, newRank, oldRank - 1, 1));
        }

        OperationResult storage = getStorage().editAll(changes);
        if (storage.isFailure()) return storage;

        for (EntryChange<E> change : changes) {
            apply(change.existing(), change.updated());
        }
        for (EntryChange<E> change : changes) {
            E entry = change.existing();
            UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.edit(entry.getId(), entry.toPayload()));
        }
        return OperationResult.success();
    }

    @Override
    public OperationResult delete(UUID entryId, T payload) {
        NarrativeManager<E> siblings = getSiblings(payload);
        E existing = siblings == null ? null : siblings.getById(entryId);
        if (existing == null) return notFound(payload.getName());

        List<EntryChange<E>> shiftedEntries = shift(siblings, getRank(existing) + 1, Integer.MAX_VALUE, -1);

        OperationResult storage = getStorage().deleteAndShift(existing, shiftedEntries);
        if (storage.isFailure()) return storage;

        siblings.remove(existing);
        for (EntryChange<E> change : shiftedEntries) {
            apply(change.existing(), change.updated());
        }
        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.delete(existing.getId(), existing.toPayload()));
        for (EntryChange<E> change : shiftedEntries) {
            E entry = change.existing();
            UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.edit(entry.getId(), entry.toPayload()));
        }
        return OperationResult.success();
    }

    private List<EntryChange<E>> shift(NarrativeManager<E> siblings, int fromRank, int toRank, int offset) {
        List<EntryChange<E>> changes = new ArrayList<>();
        for (E sibling : siblings.getList()) {
            int rank = getRank(sibling);
            if (rank < fromRank || rank > toRank) continue;
            changes.add(new EntryChange<>(sibling, withRank(sibling, rank + offset)));
        }
        return changes;
    }
}
