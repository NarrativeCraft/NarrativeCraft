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

package fr.loudo.narrativecraft.files;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.OperationResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NarrativeCraftFileRegistry {

    private static final NarrativeCraftFileRegistry INSTANCE = new NarrativeCraftFileRegistry();

    private final Map<Class<? extends NarrativeEntry<?>>, NarrativeCraftFileEditor<?>> registry = new HashMap<>();

    public <T extends NarrativeEntry<?>> void register(Class<T> entryClass, NarrativeCraftFileEditor<T> editor) {
        registry.put(entryClass, editor);
    }

    @SuppressWarnings("unchecked")
    public <T extends NarrativeEntry<?>> NarrativeCraftFileEditor<T> getEditor(Class<T> entryClass) {
        return (NarrativeCraftFileEditor<T>) registry.get(entryClass);
    }

    @SuppressWarnings("unchecked")
    public <T extends NarrativeEntry<?>> RankedNarrativeCraftFileEditor<T> getRankedEditor(Class<T> entryClass) {
        if (registry.get(entryClass) instanceof RankedNarrativeCraftFileEditor<?> rankedEditor) {
            return (RankedNarrativeCraftFileEditor<T>) rankedEditor;
        }
        throw new IllegalStateException(entryClass.getSimpleName() + " has no ranked file editor");
    }

    @SuppressWarnings("unchecked")
    private <T extends NarrativeEntry<?>> NarrativeCraftFileEditor<T> getEditor(T entry) {
        return (NarrativeCraftFileEditor<T>) registry.get(entry.getClass());
    }

    public <T extends NarrativeEntry<?>> OperationResult create(T entry) {
        NarrativeCraftFileEditor<T> editor = getEditor(entry);
        if (editor == null) return NarrativeCraftFileEditor.storageFailure(entry);
        return editor.create(entry);
    }

    public <T extends NarrativeEntry<?>> OperationResult edit(T existing, T updated) {
        NarrativeCraftFileEditor<T> editor = getEditor(existing);
        if (editor == null) return NarrativeCraftFileEditor.storageFailure(existing);
        return editor.edit(existing, updated);
    }

    public <T extends NarrativeEntry<?>> OperationResult delete(T entry) {
        NarrativeCraftFileEditor<T> editor = getEditor(entry);
        if (editor == null) return NarrativeCraftFileEditor.storageFailure(entry);
        return editor.delete(entry);
    }

    public <T extends NarrativeEntry<?>> List<DeserializationResult<T>> load(Class<T> entryClass) {
        if (getEditor(entryClass) instanceof RootEntryFileEditor<T> rootEditor) {
            return rootEditor.load();
        }
        throw new IllegalStateException(entryClass.getSimpleName() + " has no root file editor");
    }

    @SuppressWarnings("unchecked")
    public <T extends NarrativeEntry<?>, P extends NarrativeEntry<?>> List<DeserializationResult<T>> load(
            Class<T> entryClass, P parent) {
        if (getEditor(entryClass) instanceof ChildEntryFileEditor<?, ?> childEditor) {
            return ((ChildEntryFileEditor<T, P>) childEditor).load(parent);
        }
        throw new IllegalStateException(entryClass.getSimpleName() + " has no child file editor");
    }

    public static NarrativeCraftFileRegistry getInstance() {
        return INSTANCE;
    }
}
