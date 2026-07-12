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

import fr.loudo.narrativecraft.api.dialog.ITextEffectRegistry;
import fr.loudo.narrativecraft.api.editors.ICutsceneLayerRegistry;
import fr.loudo.narrativecraft.api.events.IEventBus;
import fr.loudo.narrativecraft.api.inkAction.InkTagDispatcher;
import fr.loudo.narrativecraft.api.managers.IChapterManager;
import fr.loudo.narrativecraft.api.managers.ICharacterManager;
import fr.loudo.narrativecraft.api.managers.IPlayerSessionManager;
import fr.loudo.narrativecraft.api.managers.IRecordingManager;
import fr.loudo.narrativecraft.api.narrative.IStoryHandlerManager;
import fr.loudo.narrativecraft.api.recording.action.IActionRegistry;

public class NarrativeCraftAPI {

    public static final int VERSION = 2;

    private static volatile NarrativeCraftAPI INSTANCE;

    private final String modId;
    private final IActionRegistry actionRegistry;
    private final ICutsceneLayerRegistry cutsceneLayerRegistry;
    private final ITextEffectRegistry textEffectRegistry;
    private final IStoryHandlerManager storyHandlerManager;
    private final IEventBus eventBus;
    private final IRecordingManager recordingManager;
    private final IPlayerSessionManager playerSessionManager;
    private final IChapterManager chapterManager;
    private final ICharacterManager characterManager;
    private final InkTagDispatcher inkTagDispatcher;
    private final AddonRegistry addonRegistry;

    private NarrativeCraftAPI(
            String modId,
            IActionRegistry actionRegistry,
            ICutsceneLayerRegistry cutsceneLayerRegistry,
            ITextEffectRegistry textEffectRegistry,
            IStoryHandlerManager storyHandlerManager,
            IEventBus eventBus,
            IRecordingManager recordingManager,
            IPlayerSessionManager playerSessionManager,
            IChapterManager chapterManager,
            ICharacterManager characterManager,
            InkTagDispatcher inkTagDispatcher) {
        this.modId = modId;
        this.actionRegistry = actionRegistry;
        this.cutsceneLayerRegistry = cutsceneLayerRegistry;
        this.textEffectRegistry = textEffectRegistry;
        this.storyHandlerManager = storyHandlerManager;
        this.eventBus = eventBus;
        this.recordingManager = recordingManager;
        this.playerSessionManager = playerSessionManager;
        this.chapterManager = chapterManager;
        this.characterManager = characterManager;
        this.inkTagDispatcher = inkTagDispatcher;
        this.addonRegistry = new AddonRegistry();
    }

    static void initialize(
            String modId,
            IActionRegistry actionRegistry,
            ICutsceneLayerRegistry cutsceneLayerRegistry,
            ITextEffectRegistry textEffectRegistry,
            IStoryHandlerManager storyHandlerManager,
            IEventBus eventBus,
            IRecordingManager recordingManager,
            IPlayerSessionManager playerSessionManager,
            IChapterManager chapterManager,
            ICharacterManager characterManager,
            InkTagDispatcher inkTagDispatcher) {
        INSTANCE = new NarrativeCraftAPI(
                modId,
                actionRegistry,
                cutsceneLayerRegistry,
                textEffectRegistry,
                storyHandlerManager,
                eventBus,
                recordingManager,
                playerSessionManager,
                chapterManager,
                characterManager,
                inkTagDispatcher);
    }

    public static NarrativeCraftAPI getInstance() {
        NarrativeCraftAPI instance = INSTANCE;
        if (instance == null) {
            throw new IllegalStateException(
                    "NarrativeCraft API is not initialized yet. "
                            + "Make sure your mod declares a dependency on NarrativeCraft and accesses the API in your mod initializer.");
        }
        return instance;
    }

    public AddonContext createAddon(
            String modId, String name, String description, String author, String homeLink, int apiVersion) {
        AddonContext context = new AddonContext(modId, name, description, author, homeLink, apiVersion);
        addonRegistry.add(context);
        return context;
    }

    public AddonRegistry getAddonRegistry() {
        return addonRegistry;
    }

    public String getModId() {
        return modId;
    }

    IActionRegistry getActionRegistry() {
        return actionRegistry;
    }

    ICutsceneLayerRegistry getCutsceneLayerRegistry() {
        return cutsceneLayerRegistry;
    }

    ITextEffectRegistry getTextEffectRegistry() {
        return textEffectRegistry;
    }

    public IStoryHandlerManager getStoryHandlerManager() {
        return storyHandlerManager;
    }

    IEventBus getEventBus() {
        return eventBus;
    }

    public IRecordingManager getRecordingManager() {
        return recordingManager;
    }

    public IPlayerSessionManager getPlayerSessionManager() {
        return playerSessionManager;
    }

    public IChapterManager getChapterManager() {
        return chapterManager;
    }

    public ICharacterManager getCharacterManager() {
        return characterManager;
    }

    InkTagDispatcher getInkTagDispatcher() {
        return inkTagDispatcher;
    }
}
