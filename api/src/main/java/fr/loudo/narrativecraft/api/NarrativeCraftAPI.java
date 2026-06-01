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

package fr.loudo.narrativecraft.api;

import fr.loudo.narrativecraft.api.dialog.ITextEffectRegistry;
import fr.loudo.narrativecraft.api.editors.ICutsceneLayerRegistry;
import fr.loudo.narrativecraft.api.events.IEventBus;
import fr.loudo.narrativecraft.api.inkAction.InkTagDispatcher;
import fr.loudo.narrativecraft.api.managers.IRecordingManager;
import fr.loudo.narrativecraft.api.recording.action.IActionRegistry;

public class NarrativeCraftAPI {

    private static final NarrativeCraftAPI INSTANCE = new NarrativeCraftAPI();

    private String modId;
    private IActionRegistry actionRegistry;
    private ICutsceneLayerRegistry cutsceneLayerRegistry;
    private ITextEffectRegistry textEffectRegistry;
    private IEventBus eventBus;
    private IRecordingManager recordingManager;
    private InkTagDispatcher inkTagDispatcher;

    public IActionRegistry getActionRegistry() {
        return actionRegistry;
    }

    public ICutsceneLayerRegistry getCutsceneLayerRegistry() {
        return cutsceneLayerRegistry;
    }

    void setCutsceneLayerRegistry(ICutsceneLayerRegistry cutsceneLayerRegistry) {
        this.cutsceneLayerRegistry = cutsceneLayerRegistry;
    }

    public ITextEffectRegistry getTextEffectRegistry() {
        return textEffectRegistry;
    }

    void setTextEffectRegistry(ITextEffectRegistry textEffectRegistry) {
        this.textEffectRegistry = textEffectRegistry;
    }

    void setActionRegistry(IActionRegistry actionRegistry) {
        this.actionRegistry = actionRegistry;
    }

    public IEventBus getEventBus() {
        return eventBus;
    }

    void setEventBus(IEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public IRecordingManager getRecordingManager() {
        return recordingManager;
    }

    void setRecordingManager(IRecordingManager recordingManager) {
        this.recordingManager = recordingManager;
    }

    public static NarrativeCraftAPI getInstance() {
        return INSTANCE;
    }

    public String getModId() {
        return modId;
    }

    void setModId(String modId) {
        this.modId = modId;
    }

    public InkTagDispatcher getInkTagDispatcher() {
        return inkTagDispatcher;
    }

    void setInkTagDispatcher(InkTagDispatcher inkTagDispatcher) {
        this.inkTagDispatcher = inkTagDispatcher;
    }
}
