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

package fr.loudo.narrativecraft.client.studio.shortcut;

import fr.loudo.narrativecraft.api.client.studio.shortcut.NarrativeShortcutClient;
import fr.loudo.narrativecraft.client.network.C2SRequestTree;
import fr.loudo.narrativecraft.client.studio.NarrativeStudioClient;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.registry.NarrativeShortcutRegistryImpl;
import fr.loudo.narrativecraft.util.Util;
import imgui.flag.ImGuiKey;
import net.minecraft.resources.Identifier;

public final class StudioShortcutsClient {

    public static final NarrativeShortcutClient SAVE =
            register(Util.buildId("shortcut.save"), ImGuiKey.ImGuiMod_Ctrl | ImGuiKey.S, () -> {});

    public static final NarrativeShortcutClient UNDO =
            register(Util.buildId("shortcut.undo"), ImGuiKey.ImGuiMod_Ctrl | ImGuiKey.Z, () -> {});
    public static final NarrativeShortcutClient REDO =
            register(Util.buildId("shortcut.redo"), ImGuiKey.ImGuiMod_Ctrl | ImGuiKey.Y, () -> {});
    public static final NarrativeShortcutClient NEW =
            register(Util.buildId("shortcut.new"), ImGuiKey.ImGuiMod_Ctrl | ImGuiKey.N, () -> {
                NarrativeStudioClient.getInstance().getTabsManager().openNew();
            });
    public static final NarrativeShortcutClient REFRESH =
            register(Util.buildId("shortcut.refresh"), ImGuiKey.ImGuiMod_Ctrl | ImGuiKey.R, () -> {
                Services.PACKET.sendToServer(new C2SRequestTree(
                        NarrativeStudioClient.getInstance().getAssets().getHash()));
            });

    private StudioShortcutsClient() {}

    private static NarrativeShortcutClient register(Identifier id, int chord, Runnable action) {
        NarrativeShortcutClient shortcut = NarrativeShortcutClient.of(id, chord, action);
        NarrativeShortcutRegistryImpl.getInstance().registerBuiltIn(shortcut);
        return shortcut;
    }

    public static void initialize() {}

    public static String hint(NarrativeShortcutClient shortcut) {
        return NarrativeShortcutRegistryImpl.getInstance().hint(shortcut.id());
    }
}
