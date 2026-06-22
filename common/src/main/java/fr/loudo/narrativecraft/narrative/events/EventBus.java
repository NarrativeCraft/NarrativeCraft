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

package fr.loudo.narrativecraft.narrative.events;

import fr.loudo.narrativecraft.api.events.Event;
import fr.loudo.narrativecraft.api.events.EventListener;
import fr.loudo.narrativecraft.api.events.IEventBus;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus implements IEventBus {

    private final Map<Class<? extends Event>, List<EventListener<? extends Event>>> listeners =
            new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <E extends Event> void post(E event) {
        if (event == null) return;

        Class<?> eventClass = event.getClass();

        for (Map.Entry<Class<? extends Event>, List<EventListener<? extends Event>>> entry : listeners.entrySet()) {
            if (entry.getKey().isAssignableFrom(eventClass)) {
                List<EventListener<? extends Event>> eventListeners = entry.getValue();

                for (EventListener<? extends Event> listener : eventListeners) {
                    ((EventListener<E>) listener).handle(event);
                }
            }
        }
    }

    @Override
    public <E extends Event> void register(Class<E> eventType, EventListener<? super E> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    @Override
    public <E extends Event> void unregister(Class<E> eventType, EventListener<? super E> listener) {
        List<EventListener<? extends Event>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }
}
