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

package fr.loudo.narrativecraft.client.narrative.scene;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.narrative.ClientNarrativeEntryEditor;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.scene.ScenePayload;
import fr.loudo.narrativecraft.utils.UtilsClient;
import java.util.UUID;

public class ClientSceneEditor implements ClientNarrativeEntryEditor<ScenePayload, Scene> {

    final ChapterManager chapterManager = ClientNarrativeCraftMod.getInstance().getChapterManager();

    @Override
    public Scene resolve(UUID entryId, ScenePayload payload) {
        Chapter chapter = chapterManager.getById(payload.getChapterId());
        if (chapter == null) return null;
        return chapter.getSceneManager().getById(entryId);
    }

    @Override
    public void add(UUID entryId, ScenePayload payload) {
        Chapter chapter = chapterManager.getById(payload.getChapterId());
        if (chapter == null) return;

        Scene scene = new Scene(entryId, payload.getName(), payload.getDescription(), chapter, payload.getRank());

        scene.getChapter().getSceneManager().add(scene);
        UtilsClient.reloadListScreen();
    }

    @Override
    public void edit(UUID entryId, ScenePayload payload) {
        Scene scene = resolve(entryId, payload);
        if (scene == null) return;

        int oldRank = scene.getRank();
        scene.setName(payload.getName());
        scene.setDescription(payload.getDescription());
        scene.setRank(payload.getRank());

        if (oldRank != payload.getRank()) {
            scene.getChapter().getSceneManager().forceSort();
        }

        UtilsClient.reloadListScreen();
    }

    @Override
    public void delete(UUID entryId, ScenePayload payload) {
        Scene scene = resolve(entryId, payload);
        if (scene == null) return;

        scene.getChapter().getSceneManager().remove(scene);
        UtilsClient.reloadListScreen();
    }
}
