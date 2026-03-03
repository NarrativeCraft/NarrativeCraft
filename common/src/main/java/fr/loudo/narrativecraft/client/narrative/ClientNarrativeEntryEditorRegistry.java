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

package fr.loudo.narrativecraft.client.narrative;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.NarrativeEntryPayload;
import java.util.HashMap;
import java.util.Map;

/**
 * Editor manager <b>client-side</b>.
 * When implementing {@link ClientNarrativeEntryEditor} from your {@link NarrativeEntry} heritor, you need to register it on {@link ClientNarrativeEditorsRegister#register()}.
 * using {@link #register(Class, ClientNarrativeEntryEditor)}.
 * <br>
 * The difference between the <b>server-side</b> one is that it only handles with the client managers instead of the world files.
 *
 * @see NarrativeCraftMod#commonInit()
 */
public class ClientNarrativeEntryEditorRegistry {

    private static final ClientNarrativeEntryEditorRegistry INSTANCE = new ClientNarrativeEntryEditorRegistry();

    private final Map<Class<? extends NarrativeEntryPayload>, ClientNarrativeEntryEditor<?, ?>> registry =
            new HashMap<>();

    private ClientNarrativeEntryEditorRegistry() {}

    public static ClientNarrativeEntryEditorRegistry getInstance() {
        return INSTANCE;
    }

    public <T extends NarrativeEntryPayload, E extends NarrativeEntry<T>> void register(
            Class<T> entryClass, ClientNarrativeEntryEditor<T, E> editor) {
        registry.put(entryClass, editor);
    }

    @SuppressWarnings("unchecked")
    public <T extends NarrativeEntryPayload, E extends NarrativeEntry<T>>
            ClientNarrativeEntryEditor<T, E> getClientEditor(T entry) {
        if (entry == null) return null;
        return (ClientNarrativeEntryEditor<T, E>) registry.get(entry.getClass());
    }

    public <T extends NarrativeEntryPayload, E extends NarrativeEntry<T>> void add(T entry) {
        ClientNarrativeEntryEditor<T, E> editor = getClientEditor(entry);
        if (editor != null) editor.add(entry);
    }

    public <T extends NarrativeEntryPayload, E extends NarrativeEntry<T>> void edit(T entry) {
        ClientNarrativeEntryEditor<T, E> editor = getClientEditor(entry);
        if (editor != null) editor.edit(entry);
    }

    public <T extends NarrativeEntryPayload, E extends NarrativeEntry<T>> void delete(T entry) {
        ClientNarrativeEntryEditor<T, E> editor = getClientEditor(entry);
        if (editor != null) editor.delete(entry);
    }
}
