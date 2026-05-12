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

package fr.loudo.narrativecraft.session;

import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import net.minecraft.server.level.ServerPlayer;

public class AbstractPlayerSession implements IPlayerSession {

    private Chapter chapter;
    private Scene scene;
    private EditorMaker editorMaker;

    public AbstractPlayerSession(Chapter chapter, Scene scene) {
        this.chapter = chapter;
        this.scene = scene;
    }

    public void apply(Chapter chapter, Scene scene) {
        this.chapter = chapter;
        this.scene = scene;
    }

    public void clear() {
        chapter = null;
        scene = null;
        editorMaker = null;
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
}
