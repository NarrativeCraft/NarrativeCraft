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

package fr.loudo.narrativecraft.narrative.scene;

import fr.loudo.narrativecraft.managers.AnimationManager;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.subscene.SubsceneManager;
import java.util.UUID;

public class Scene extends NarrativeEntry<ScenePayload> {

    private final Chapter chapter;
    private final AnimationManager animationManager = new AnimationManager();
    private final SubsceneManager subsceneManager = new SubsceneManager();

    private int rank;

    public Scene(UUID id, String name, String description, Chapter chapter, int rank) {
        super(id, name, description);
        this.chapter = chapter;
        this.rank = rank;
    }

    public Scene(String name, String description, Chapter chapter) {
        super(name, description);
        this.chapter = chapter;
    }

    public Scene(String name, String description, Chapter chapter, int rank) {
        super(name, description);
        this.chapter = chapter;
        this.rank = rank;
    }

    public int getChapterIndex() {
        return chapter.getChapterIndex();
    }

    public Chapter getChapter() {
        return chapter;
    }

    public AnimationManager getAnimationManager() {
        return animationManager;
    }

    public SubsceneManager getSubsceneManager() {
        return subsceneManager;
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public String formattedName() {
        return name;
    }

    @Override
    public String toRawJson() {
        return String.format(
                "{\"id\": \"%s\",\"name\":\"%s\",\"description\":\"%s\", \"chapterId\":\"%s\",\"rank\":\"%s\"}",
                id.toString(), name, description, chapter.getId(), rank);
    }

    @Override
    public ScenePayload toPayload() {
        return new ScenePayload(name, description, chapter.getId(), rank);
    }

    @Override
    public String toFileName() {
        return chapter.getChapterIndex() + "_" + rank + "_" + name.toLowerCase();
    }
}
