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

package fr.loudo.narrativecraft.narrative;

import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngle;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.interaction.Interaction;
import fr.loudo.narrativecraft.narrative.npc.Npc;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import java.util.UUID;
import java.util.function.Function;

public class NarrativeEntryResolver {

    private final ChapterManager chapterManager;

    public NarrativeEntryResolver(ChapterManager chapterManager) {
        this.chapterManager = chapterManager;
    }

    public Chapter chapter(UUID chapterId) {
        return chapterManager.getById(chapterId);
    }

    public Scene scene(UUID chapterId, UUID sceneId) {
        Chapter chapter = chapter(chapterId);
        if (chapter == null) return null;
        return chapter.getSceneManager().getById(sceneId);
    }

    public Scene scene(SceneEntryPayload payload) {
        return scene(payload.getChapterId(), payload.getSceneId());
    }

    public <E extends NarrativeEntry<?>> E inScene(
            UUID chapterId, UUID sceneId, Function<Scene, NarrativeManager<E>> manager, UUID entryId) {
        Scene scene = scene(chapterId, sceneId);
        if (scene == null) return null;
        return manager.apply(scene).getById(entryId);
    }

    public Animation animation(UUID chapterId, UUID sceneId, UUID animationId) {
        return inScene(chapterId, sceneId, Scene::getAnimationManager, animationId);
    }

    public Subscene subscene(UUID chapterId, UUID sceneId, UUID subsceneId) {
        return inScene(chapterId, sceneId, Scene::getSubsceneManager, subsceneId);
    }

    public Cutscene cutscene(UUID chapterId, UUID sceneId, UUID cutsceneId) {
        return inScene(chapterId, sceneId, Scene::getCutsceneManager, cutsceneId);
    }

    public CameraAngle cameraAngle(UUID chapterId, UUID sceneId, UUID cameraAngleId) {
        return inScene(chapterId, sceneId, Scene::getCameraAngleManager, cameraAngleId);
    }

    public Interaction interaction(UUID chapterId, UUID sceneId, UUID interactionId) {
        return inScene(chapterId, sceneId, Scene::getInteractionManager, interactionId);
    }

    public Npc npc(UUID chapterId, UUID sceneId, UUID npcId) {
        return inScene(chapterId, sceneId, Scene::getNpcManager, npcId);
    }

    public Npc npc(UUID npcId) {
        for (Chapter chapter : chapterManager.getList()) {
            for (Scene scene : chapter.getSceneManager().getList()) {
                Npc npc = scene.getNpcManager().getById(npcId);
                if (npc != null) return npc;
            }
        }
        return null;
    }
}
