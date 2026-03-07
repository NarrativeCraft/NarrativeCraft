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

package fr.loudo.narrativecraft.client.narrative.ui;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.screens.AbstractNarrativeEntryEditScreen;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.screens.Screen;

public class ClientNarrativeUIActionRegistry {

    private static final ClientNarrativeUIActionRegistry INSTANCE = new ClientNarrativeUIActionRegistry();

    private final Map<Class<? extends NarrativeEntry<?>>, ClientNarrativeUIAction<?>> registry = new HashMap<>();

    private ClientNarrativeUIActionRegistry() {}

    public static ClientNarrativeUIActionRegistry getInstance() {
        return INSTANCE;
    }

    public <T extends NarrativeEntry<?>> void register(Class<T> entryClass, ClientNarrativeUIAction<T> editor) {
        registry.put(entryClass, editor);
    }

    @SuppressWarnings("unchecked")
    public <T extends NarrativeEntry<?>> ClientNarrativeUIAction<T> getClientEditor(T entry) {
        if (entry == null) return null;
        return (ClientNarrativeUIAction<T>) registry.get(entry.getClass());
    }

    public <T extends NarrativeEntry<?>> Screen showListSubScreen(T entry, Screen parent) {
        ClientNarrativeUIAction<T> editor = getClientEditor(entry);
        if (editor != null) return editor.subListSubScreen(entry, parent);
        return null;
    }

    public <T extends NarrativeEntry<?>> boolean hasSubScreen(T entry) {
        ClientNarrativeUIAction<T> editor = getClientEditor(entry);
        if (editor != null) return editor.hasSubScreen();
        return false;
    }

    @SuppressWarnings("unchecked")
    public <T extends NarrativeEntry<?>> AbstractNarrativeEntryEditScreen<T> showCreateScreen(
            Class<? extends NarrativeEntry<?>> entryClass, Screen lastScreen, NarrativeEntry<?> parent) {
        ClientNarrativeUIAction<T> editor = (ClientNarrativeUIAction<T>) registry.get(entryClass);
        if (editor != null) return editor.showCreateScreen(parent, lastScreen);
        return null;
    }

    public <T extends NarrativeEntry<?>> AbstractNarrativeEntryEditScreen<T> showEditScreen(
            T entry, Screen lastScreen) {
        ClientNarrativeUIAction<T> editor = getClientEditor(entry);
        if (editor != null) return editor.showEditScreen(entry, lastScreen);
        return null;
    }
}
