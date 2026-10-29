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
import fr.loudo.narrativecraft.api.client.studio.menu.NarrativeMenuClient;
import fr.loudo.narrativecraft.api.client.studio.menu.NarrativeMenuRegistryClient;
import fr.loudo.narrativecraft.client.studio.menu.EditMenu;
import fr.loudo.narrativecraft.client.studio.menu.FileMenu;
import fr.loudo.narrativecraft.client.studio.menu.HelpMenu;
import fr.loudo.narrativecraft.client.studio.menu.WindowsMenu;
import java.util.*;
import net.minecraft.resources.Identifier;

public class NarrativeMenuRegistryImpl implements NarrativeMenuRegistryClient {

    private static final NarrativeMenuRegistryImpl INSTANCE = new NarrativeMenuRegistryImpl();

    private final Map<Identifier, NarrativeMenuClient> builtInMenus = new LinkedHashMap<>();
    private final Map<Identifier, NarrativeMenuClient> addonMenus = new LinkedHashMap<>();
    private boolean frozen;

    private NarrativeMenuRegistryImpl() {}

    public static void initialize() {
        INSTANCE.registerBuiltIn(new FileMenu());
        INSTANCE.registerBuiltIn(new EditMenu());
        INSTANCE.registerBuiltIn(new WindowsMenu());
        INSTANCE.registerBuiltIn(new HelpMenu());
    }

    @Override
    public synchronized void register(NarrativeMenuClient menu) {
        Objects.requireNonNull(menu, "menu");
        Identifier id = Objects.requireNonNull(menu.id(), "menu id");
        if (id.getNamespace().equals(NarrativeCraftMod.MOD_ID)) {
            throw new IllegalArgumentException(
                    "Cannot register menu " + id + ": the namespace " + NarrativeCraftMod.MOD_ID + " is reserved");
        }
        add(addonMenus, menu);
    }

    /**
     * Registers a NarrativeCraft menu, listed before every addon menu.
     */
    public synchronized void registerBuiltIn(NarrativeMenuClient menu) {
        add(builtInMenus, menu);
    }

    private void add(Map<Identifier, NarrativeMenuClient> menus, NarrativeMenuClient menu) {
        Identifier id = menu.id();
        if (frozen) {
            throw new IllegalStateException(
                    "Cannot register menu " + id + ": menus must be registered before the studio opens");
        }
        if (builtInMenus.containsKey(id) || addonMenus.containsKey(id)) {
            throw new IllegalArgumentException("A menu with the id " + id + " is already registered");
        }
        menus.put(id, menu);
    }

    /**
     * Locks the registry, then returns the built-in menus followed by the addon menus, in registration order.
     */
    public synchronized List<NarrativeMenuClient> freeze() {
        frozen = true;
        List<NarrativeMenuClient> menus = new ArrayList<>(builtInMenus.values());
        menus.addAll(addonMenus.values());
        return List.copyOf(menus);
    }

    public static NarrativeMenuRegistryImpl getInstance() {
        return INSTANCE;
    }
}
