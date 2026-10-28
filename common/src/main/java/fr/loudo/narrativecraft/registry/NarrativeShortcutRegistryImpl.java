/*
 * NarrativeCraft - Create narrative games inside Minecraft. No coding, no game engine, only text and logic.
 * Copyright (c) 2026 LOUDO and contributors
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

package fr.loudo.narrativecraft.registry;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.client.studio.shortcut.NarrativeShortcutClient;
import fr.loudo.narrativecraft.api.client.studio.shortcut.NarrativeShortcutRegistryClient;
import imgui.ImGui;
import imgui.flag.ImGuiKey;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class NarrativeShortcutRegistryImpl implements NarrativeShortcutRegistryClient {

    private static final NarrativeShortcutRegistryImpl INSTANCE = new NarrativeShortcutRegistryImpl();

    private final Map<Identifier, NarrativeShortcutClient> shortcuts = new LinkedHashMap<>();
    private final Map<Identifier, String> hints = new HashMap<>();
    private boolean frozen;

    private NarrativeShortcutRegistryImpl() {}

    @Override
    public synchronized void register(NarrativeShortcutClient shortcut) {
        Objects.requireNonNull(shortcut, "shortcut");
        Identifier id = Objects.requireNonNull(shortcut.id(), "shortcut id");
        if (id.getNamespace().equals(NarrativeCraftMod.MOD_ID)) {
            throw new IllegalArgumentException(
                    "Cannot register shortcut " + id + ": the namespace " + NarrativeCraftMod.MOD_ID + " is reserved");
        }
        add(shortcut);
    }

    /**
     * Registers a NarrativeCraft shortcut.
     */
    public synchronized void registerBuiltIn(NarrativeShortcutClient shortcut) {
        add(shortcut);
    }

    private void add(NarrativeShortcutClient shortcut) {
        Identifier id = shortcut.id();
        int chord = shortcut.chord();
        if (frozen) {
            throw new IllegalStateException(
                    "Cannot register shortcut " + id + ": shortcuts must be registered before the studio opens");
        }
        if (shortcuts.containsKey(id)) {
            throw new IllegalArgumentException("A shortcut with the id " + id + " is already registered");
        }
        if ((chord & ~ImGuiKey.ImGuiMod_Mask_) == ImGuiKey.None) {
            throw new IllegalArgumentException("Cannot register shortcut " + id + ": its chord has no key");
        }
        for (NarrativeShortcutClient other : shortcuts.values()) {
            if (other.chord() == chord) {
                throw new IllegalArgumentException(
                        "Cannot register shortcut " + id + ": its chord is already used by " + other.id());
            }
        }
        shortcuts.put(id, shortcut);
    }

    @Override
    public synchronized @Nullable String hint(Identifier id) {
        NarrativeShortcutClient shortcut = shortcuts.get(id);
        if (shortcut == null) return null;
        if (ImGui.getCurrentContext().isNotValidPtr()) {
            throw new IllegalStateException("Cannot get the hint of shortcut " + id + ": ImGui is not initialized yet");
        }
        return hints.computeIfAbsent(id, key -> chordName(shortcut.chord()));
    }

    /**
     * Locks the registry, then returns the shortcuts in registration order.
     */
    public synchronized List<NarrativeShortcutClient> freeze() {
        frozen = true;
        return List.copyOf(shortcuts.values());
    }

    // imgui-java does not bind ImGui::GetKeyChordName
    private static String chordName(int chord) {
        StringBuilder name = new StringBuilder();
        if ((chord & ImGuiKey.ImGuiMod_Ctrl) != 0) name.append("Ctrl+");
        if ((chord & ImGuiKey.ImGuiMod_Shift) != 0) name.append("Shift+");
        if ((chord & ImGuiKey.ImGuiMod_Alt) != 0) name.append("Alt+");
        if ((chord & ImGuiKey.ImGuiMod_Super) != 0) name.append("Super+");
        return name.append(ImGui.getKeyName(chord & ~ImGuiKey.ImGuiMod_Mask_)).toString();
    }

    public static NarrativeShortcutRegistryImpl getInstance() {
        return INSTANCE;
    }
}
