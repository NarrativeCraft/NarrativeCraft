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

package fr.loudo.narrativecraft.file;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import java.util.HashMap;
import java.util.Map;

public class NarrativeCraftFileRegistry {

    private static final NarrativeCraftFileRegistry INSTANCE = new NarrativeCraftFileRegistry();

    private final Map<Class<? extends NarrativeEntry>, NarrativeCraftFileEditor<?>> registry = new HashMap<>();

    public <T extends NarrativeEntry> void register(Class<T> entryClass, NarrativeCraftFileEditor<T> editor) {
        registry.put(entryClass, editor);
    }

    @SuppressWarnings("unchecked")
    public <T extends NarrativeEntry> NarrativeCraftFileEditor<T> getEditor(T entry) {
        if (entry == null) return null;
        return (NarrativeCraftFileEditor<T>) registry.get(entry.getClass());
    }

    public <T extends NarrativeEntry> int create(T entry) {
        NarrativeCraftFileEditor<T> editor = getEditor(entry);
        if (editor != null) return editor.create(entry);
        return NarrativeCraftFileEditor.OPERATION_FAILED;
    }

    public <T extends NarrativeEntry> int edit(T entry) {
        NarrativeCraftFileEditor<T> editor = getEditor(entry);
        if (editor != null) return editor.edit(entry);
        return NarrativeCraftFileEditor.OPERATION_FAILED;
    }

    public <T extends NarrativeEntry> int delete(T entry) {
        NarrativeCraftFileEditor<T> editor = getEditor(entry);
        if (editor != null) return editor.delete(entry);
        return NarrativeCraftFileEditor.OPERATION_FAILED;
    }

    public static NarrativeCraftFileRegistry getInstance() {
        return INSTANCE;
    }
}
