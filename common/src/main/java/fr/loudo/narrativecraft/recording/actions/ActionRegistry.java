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

package fr.loudo.narrativecraft.recording.actions;

import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.api.recording.action.ActionType;
import fr.loudo.narrativecraft.api.recording.action.IActionRegistry;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

public class ActionRegistry implements IActionRegistry {

    public static final int MAX_ACTION_SIZE = 255;

    private final Map<String, ActionType> registry = new HashMap<>();

    public ActionType register(String id, IntFunction<AbstractAction> factory) {
        id = id.toLowerCase();
        ActionType action = new ActionType(id, factory);
        registry.put(id, action);
        if (registry.size() >= MAX_ACTION_SIZE) {
            throw new RuntimeException("Max action size reached!");
        }
        return action;
    }

    public ActionType get(String id) {
        return registry.get(id.toLowerCase());
    }

    @Override
    public AbstractAction createAction(String id, int tick) {
        ActionType action = registry.get(id.toLowerCase());
        if (action == null) return null;

        return action.factory().apply(tick);
    }

    public Collection<ActionType> getActionsType() {
        return registry.values();
    }
}
