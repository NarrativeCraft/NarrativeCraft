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

package fr.loudo.narrativecraft.narrative;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Editor manager <b>server-side</b>.
 * When implementing {@link NarrativeEntryEditor} from your {@link NarrativeEntry} heritor, you need to register it on {@link NarrativeEditorsRegister#register()}.
 * using {@link #register(Class, NarrativeEntryEditor)}.
 * <br>
 * This class should be used to deal with managers <b>and</b> world files using {@link NarrativeCraftFile}.
 *
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

    public <T extends NarrativeEntryPayload, E extends NarrativeEntry<T>> void add(T entry, UUID playerId) {
        NarrativeEntryEditor<T, E> editor = getEditor(entry);
        if (editor != null) editor.add(entry, playerId);
    }

    public <T extends NarrativeEntryPayload, E extends NarrativeEntry<T>> void edit(T entry, UUID playerId) {
        NarrativeEntryEditor<T, E> editor = getEditor(entry);
        if (editor != null) editor.edit(entry, playerId);
    }

    public <T extends NarrativeEntryPayload, E extends NarrativeEntry<T>> void delete(T entry, UUID playerId) {
        NarrativeEntryEditor<T, E> editor = getEditor(entry);
        if (editor != null) editor.delete(entry, playerId);
    }
}
