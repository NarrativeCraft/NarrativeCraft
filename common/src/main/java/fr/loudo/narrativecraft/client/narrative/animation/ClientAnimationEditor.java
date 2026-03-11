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

package fr.loudo.narrativecraft.client.narrative.animation;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.narrative.ClientNarrativeEntryEditor;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.animation.AnimationPayload;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.utils.UtilsClient;
import java.util.UUID;

public class ClientAnimationEditor implements ClientNarrativeEntryEditor<AnimationPayload, Animation> {

    final ChapterManager chapterManager = ClientNarrativeCraftMod.getInstance().getChapterManager();

    @Override
    public void add(UUID entryId, AnimationPayload payload) {

        Chapter chapter = chapterManager.getById(payload.getChapterId());
        if (chapter == null) return;

        Scene scene = chapter.getSceneManager().getById(payload.getSceneId());
        if (scene == null) return;

        Animation animation = new Animation(entryId, payload.getName(), scene);
        scene.getAnimationManager().add(animation);

        UtilsClient.reloadListScreen();
    }

    @Override
    public void edit(UUID entryId, AnimationPayload payload) {

        Animation animation = resolve(entryId, payload);
        if (animation == null) return;

        animation.setName(payload.getName());

        UtilsClient.reloadListScreen();
    }

    @Override
    public void delete(UUID entryId, AnimationPayload payload) {

        // uhhhh duplicated code but I need to access manager from the scene
        Chapter chapter = chapterManager.getById(payload.getChapterId());
        if (chapter == null) return;

        Scene scene = chapter.getSceneManager().getById(payload.getSceneId());
        if (scene == null) return;

        Animation animation = scene.getAnimationManager().getById(entryId);
        if (animation == null) return;

        scene.getAnimationManager().remove(animation);

        UtilsClient.reloadListScreen();
    }

    @Override
    public Animation resolve(UUID entryId, AnimationPayload payload) {

        Chapter chapter = chapterManager.getById(payload.getChapterId());
        if (chapter == null) return null;

        Scene scene = chapter.getSceneManager().getById(payload.getSceneId());
        if (scene == null) return null;

        return scene.getAnimationManager().getById(entryId);
    }
}
