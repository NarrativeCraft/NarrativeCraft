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

package fr.loudo.narrativecraft.api;

import fr.loudo.narrativecraft.api.dialog.ITextEffect;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayerType;
import fr.loudo.narrativecraft.api.events.Event;
import fr.loudo.narrativecraft.api.events.EventListener;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddonContext {

    private static final Logger LOGGER = LoggerFactory.getLogger("NarrativeCraft");

    private final NarrativeCraftAPI api;
    private final String modId;
    private final String name;
    private final String description;
    private final String author;
    private final String homeLink;
    private final int apiVersion;
    private final State state;

    AddonContext(String modId, String name, String description, String author, String homeLink, int apiVersion) {
        this.api = NarrativeCraftAPI.getInstance();
        this.modId = modId;
        this.name = name;
        this.description = description;
        this.author = author;
        this.homeLink = homeLink;
        this.apiVersion = apiVersion;

        if (apiVersion == NarrativeCraftAPI.VERSION) {
            this.state = State.ENABLED;
        } else {
            this.state = State.DISABLED;
            LOGGER.warn(
                    "Addon \"{}\" ({}) is DISABLED, built for API version {}, current is {}.",
                    name,
                    modId,
                    apiVersion,
                    NarrativeCraftAPI.VERSION);
        }
    }

    public void registerRecordingAction(String id, IntFunction<AbstractAction> factory) {
        if (state == State.DISABLED) return;
        api.getActionRegistry().register(id, factory);
    }

    public <T extends InkAction> void registerInkAction(Class<T> clazz, Supplier<T> factory) {
        if (state == State.DISABLED) return;
        api.getInkTagDispatcher().register(clazz, factory);
    }

    public <E extends Event> void registerEvent(Class<E> eventType, EventListener<? super E> listener) {
        if (state == State.DISABLED) return;
        api.getEventBus().register(eventType, listener);
    }

    public <E extends Event> void unregisterEvent(Class<E> eventType, EventListener<? super E> listener) {
        if (state == State.DISABLED) return;
        api.getEventBus().unregister(eventType, listener);
    }

    public void registerCutsceneLayer(ICutsceneLayerType type) {
        if (state == State.DISABLED) return;
        api.getCutsceneLayerRegistry().register(type);
    }

    public void registerTextEffect(String name, ITextEffect effect) {
        if (state == State.DISABLED) return;
        api.getTextEffectRegistry().register(name, effect);
    }

    public NarrativeCraftAPI getApi() {
        return api;
    }

    public String getModId() {
        return modId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public String getHomeLink() {
        return homeLink;
    }

    public int getApiVersion() {
        return apiVersion;
    }

    public State getState() {
        return state;
    }

    public boolean isEnabled() {
        return state == State.ENABLED;
    }

    public boolean isDisabled() {
        return state == State.DISABLED;
    }

    public enum State {
        ENABLED,
        DISABLED
    }
}
