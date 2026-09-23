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

package fr.loudo.narrativecraft.narrative.chapter;

import com.mojang.serialization.Codec;
import fr.loudo.narrativecraft.api.narrative.chapter.IChapter;
import fr.loudo.narrativecraft.managers.SceneManager;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import java.util.UUID;

public class Chapter extends NarrativeEntry<ChapterPayload> implements IChapter {

    public static final Codec<Chapter> CODEC = entryCodec(ChapterPayload.CODEC, Chapter::fromPayload);

    private int chapterIndex;
    private final SceneManager sceneManager = new SceneManager();

    public Chapter(UUID id, String name, int chapterIndex) {
        super(id, name);
        this.chapterIndex = chapterIndex;
    }

    public Chapter(String name, int chapterIndex) {
        super(name);
        this.chapterIndex = chapterIndex;
    }

    public static Chapter fromPayload(UUID id, ChapterPayload payload) {
        return new Chapter(id, payload.getName(), payload.getChapterIndex());
    }

    public int getChapterIndex() {
        return chapterIndex;
    }

    public void setChapterIndex(int chapterIndex) {
        this.chapterIndex = chapterIndex;
    }

    public SceneManager getSceneManager() {
        return sceneManager;
    }

    public String formattedName() {
        return chapterIndex + " - " + name;
    }

    @Override
    public ChapterPayload toPayload() {
        return new ChapterPayload(name, chapterIndex);
    }

    @Override
    public String toFileName() {
        return chapterIndex + "_" + getNormalizedName();
    }
}
