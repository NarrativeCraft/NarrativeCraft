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

package fr.loudo.narrativecraft.narrative.scene;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.AbstractRankedNarrativeEntryEditor;
import fr.loudo.narrativecraft.narrative.NarrativeEntryResolver;
import fr.loudo.narrativecraft.narrative.NarrativeManager;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import java.util.UUID;

public class SceneEditor extends AbstractRankedNarrativeEntryEditor<ScenePayload, Scene> {

    private final NarrativeEntryResolver resolver =
            NarrativeCraftMod.getInstance().getEntryResolver();

    public SceneEditor() {
        super(Scene.class);
    }

    @Override
    protected String getTypeKey() {
        return "scene";
    }

    @Override
    protected NarrativeManager<Scene> getSiblings(ScenePayload payload) {
        Chapter chapter = resolver.chapter(payload.getChapterId());
        if (chapter == null) return null;
        return chapter.getSceneManager();
    }

    @Override
    protected Scene build(UUID entryId, ScenePayload payload, Scene existing) {
        Chapter chapter = resolver.chapter(payload.getChapterId());
        int rank = existing == null ? getNextRank(chapter.getSceneManager()) : payload.getRank();
        return new Scene(entryId, payload.getName(), chapter, rank);
    }

    @Override
    protected void copyAttributes(Scene target, Scene source) {
        target.setRank(source.getRank());
    }

    @Override
    protected int getRank(Scene entry) {
        return entry.getRank();
    }

    @Override
    protected Scene withRank(Scene entry, int rank) {
        return new Scene(entry.getId(), entry.getName(), entry.getChapter(), rank);
    }
}
