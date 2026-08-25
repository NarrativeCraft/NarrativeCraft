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

package fr.loudo.narrativecraft.session;

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.narrative.IStoryHandler;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.network.BiEditorClose;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public class AbstractPlayerSession implements IPlayerSession {

    protected Chapter chapter;
    protected Scene scene;
    protected EditorMaker editorMaker;
    protected int editorSessionId = BiEditorClose.UNIDENTIFIED_SESSION;
    protected final Map<UUID, EditorMaker> interactionSessions = new LinkedHashMap<>();

    public AbstractPlayerSession(Chapter chapter, Scene scene) {
        this.chapter = chapter;
        this.scene = scene;
    }

    public void apply(Chapter chapter, Scene scene) {
        this.chapter = chapter;
        this.scene = scene;
    }

    public void clear() {
        clearInteractionSession();
        chapter = null;
        scene = null;
        editorMaker = null;
        editorSessionId = BiEditorClose.UNIDENTIFIED_SESSION;
    }

    public boolean sessionSet() {
        return chapter != null && scene != null;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public EditorMaker getEditor() {
        return editorMaker;
    }

    public void setEditor(EditorMaker editorMaker) {
        this.editorMaker = editorMaker;
    }

    public void closeEditor() {
        setEditor(null);
    }

    public void addInteractionSession(UUID interactionId, EditorMaker interactionSession) {
        EditorMaker previousSession = interactionSessions.put(interactionId, interactionSession);
        if (previousSession != null && previousSession != interactionSession) {
            previousSession.close();
        }
        interactionSession.init();
    }

    public void removeInteractionSession(UUID interactionId) {
        EditorMaker interactionSession = interactionSessions.remove(interactionId);
        if (interactionSession == null) return;
        interactionSession.close();
    }

    public void clearInteractionSession() {
        for (UUID interactionId : new ArrayList<>(interactionSessions.keySet())) {
            removeInteractionSession(interactionId);
        }
    }

    public EditorMaker getInteractionSession(UUID interactionId) {
        return interactionSessions.get(interactionId);
    }

    public Collection<EditorMaker> getInteractionSessions() {
        return Collections.unmodifiableCollection(interactionSessions.values());
    }

    public int getEditorSessionId() {
        return editorSessionId;
    }

    public void setEditorSessionId(int editorSessionId) {
        this.editorSessionId = editorSessionId;
    }

    @Override
    public ServerPlayer getPlayer() {
        return null;
    }

    @Override
    public boolean isGameplayMode() {
        return false;
    }

    @Override
    public void setGameplayMode(boolean gameplayMode) {}

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public List<InkAction> getActiveClientInkActions() {
        return List.of();
    }

    @Override
    public IStoryHandler getStoryHandler() {
        return null;
    }
}
