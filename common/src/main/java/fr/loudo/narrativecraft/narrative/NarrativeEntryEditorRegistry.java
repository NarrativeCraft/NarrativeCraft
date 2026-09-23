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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Editor manager <b>server-side</b>.
 * When implementing {@link NarrativeEntryEditor} from your {@link NarrativeEntry} heritor, you need to register it on {@link NarrativeEditorsRegister#register()}.
 * using {@link #register(Class, NarrativeEntryEditor)}.
 * <br>
 * This is the single entry point for mutating narrative entries: editors validate, write to disk, apply in memory
 * then broadcast, and report the outcome as an {@link OperationResult}.
 */
public class NarrativeEntryEditorRegistry {

    private static final NarrativeEntryEditorRegistry INSTANCE = new NarrativeEntryEditorRegistry();

    private final Map<Class<? extends NarrativeEntryPayload>, NarrativeEntryEditor<?, ?>> registry = new HashMap<>();

    private NarrativeEntryEditorRegistry() {}

    public static NarrativeEntryEditorRegistry getInstance() {
        return INSTANCE;
    }

    public <T extends NarrativeEntryPayload, E extends NarrativeEntry<T>> void register(
            Class<T> entryClass, NarrativeEntryEditor<T, E> editor) {
        registry.put(entryClass, editor);
    }

    @SuppressWarnings("unchecked")
    public <T extends NarrativeEntryPayload, E extends NarrativeEntry<T>> NarrativeEntryEditor<T, E> getEditor(
            T entry) {
        if (entry == null) return null;
        return (NarrativeEntryEditor<T, E>) registry.get(entry.getClass());
    }

    public <E extends NarrativeEntryEditor<?, ?>> E getEditor(Class<E> editorClass) {
        for (NarrativeEntryEditor<?, ?> editor : registry.values()) {
            if (editorClass.isInstance(editor)) return editorClass.cast(editor);
        }
        throw new IllegalStateException(editorClass.getSimpleName() + " is not registered");
    }

    public <T extends NarrativeEntryPayload, E extends NarrativeEntry<T>> OperationResult add(UUID entryId, T entry) {
        NarrativeEntryEditor<T, E> editor = getEditor(entry);
        if (editor == null) return OperationResult.failure("error.unsupported_entry");
        return editor.add(entryId, entry);
    }

    public <T extends NarrativeEntryPayload, E extends NarrativeEntry<T>> OperationResult edit(UUID entryId, T entry) {
        NarrativeEntryEditor<T, E> editor = getEditor(entry);
        if (editor == null) return OperationResult.failure("error.unsupported_entry");
        return editor.edit(entryId, entry);
    }

    public <T extends NarrativeEntryPayload, E extends NarrativeEntry<T>> OperationResult delete(
            UUID entryId, T entry) {
        NarrativeEntryEditor<T, E> editor = getEditor(entry);
        if (editor == null) return OperationResult.failure("error.unsupported_entry");
        return editor.delete(entryId, entry);
    }
}
