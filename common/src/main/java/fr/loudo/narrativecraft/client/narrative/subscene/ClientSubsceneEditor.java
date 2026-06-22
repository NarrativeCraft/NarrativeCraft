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

package fr.loudo.narrativecraft.client.narrative.subscene;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.narrative.ClientNarrativeEntryEditor;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.subscene.SubscenePayload;
import fr.loudo.narrativecraft.utils.UtilsClient;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ClientSubsceneEditor implements ClientNarrativeEntryEditor<SubscenePayload, Subscene> {

    final ChapterManager chapterManager = ClientNarrativeCraftMod.getInstance().getChapterManager();

    @Override
    public void add(UUID entryId, SubscenePayload payload) {
        Chapter chapter = chapterManager.getById(payload.getChapterId());
        if (chapter == null) return;

        Scene scene = chapter.getSceneManager().getById(payload.getSceneId());
        if (scene == null) return;

        List<Animation> animations = payload.getAnimationIds().stream()
                .map(id -> scene.getAnimationManager().getById(id))
                .filter(Objects::nonNull)
                .toList();
        Subscene subscene = new Subscene(entryId, payload.getName(), payload.getDescription(), scene, animations);
        scene.getSubsceneManager().add(subscene);

        UtilsClient.reloadListScreen();
    }

    @Override
    public void edit(UUID entryId, SubscenePayload payload) {
        Subscene subscene = resolve(entryId, payload);
        if (subscene == null) return;

        List<Animation> animations = payload.getAnimationIds().stream()
                .map(id -> subscene.getScene().getAnimationManager().getById(id))
                .filter(Objects::nonNull)
                .toList();
        subscene.setName(payload.getName());
        subscene.setDescription(payload.getDescription());
        subscene.setAnimations(animations);

        UtilsClient.reloadListScreen();
    }

    @Override
    public void delete(UUID entryId, SubscenePayload payload) {
        Chapter chapter = chapterManager.getById(payload.getChapterId());
        if (chapter == null) return;

        Scene scene = chapter.getSceneManager().getById(payload.getSceneId());
        if (scene == null) return;

        Subscene subscene = scene.getSubsceneManager().getById(entryId);
        if (subscene == null) return;

        scene.getSubsceneManager().remove(subscene);

        UtilsClient.reloadListScreen();
    }

    @Override
    public Subscene resolve(UUID entryId, SubscenePayload payload) {
        Chapter chapter = chapterManager.getById(payload.getChapterId());
        if (chapter == null) return null;

        Scene scene = chapter.getSceneManager().getById(payload.getSceneId());
        if (scene == null) return null;

        return scene.getSubsceneManager().getById(entryId);
    }
}
