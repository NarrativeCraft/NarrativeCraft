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

package fr.loudo.narrativecraft.files;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import java.io.File;

public class NarrativeCraftFileUtil {

    public static File getChaptersFolder() {
        return NarrativeCraftMod.getInstance().getFile().getInit().getChaptersDirectory();
    }

    public static File getCharactersFolder() {
        return NarrativeCraftMod.getInstance().getFile().getInit().getCharactersDirectory();
    }

    public static File getScenesFolder(Chapter chapter) {
        File chaptersFolder = getChaptersFolder();
        File chapterFolder = new File(chaptersFolder, chapter.toFileName());
        return new File(chapterFolder, NarrativeCraftFileDefault.SCENES_FOLDER_NAME);
    }

    public static File getSceneFolder(Scene scene) {
        File scenesFolder = getScenesFolder(scene.getChapter());
        return new File(scenesFolder, scene.toFileName());
    }

    public static File getAnimationsFolder(Scene scene) {
        File sceneFolder = getSceneFolder(scene);
        return new File(sceneFolder, NarrativeCraftFileDefault.ANIMATIONS_FOLDER_NAME);
    }

    public static File getSubscenesFolder(Scene scene) {
        File sceneFolder = getSceneFolder(scene);
        return new File(sceneFolder, NarrativeCraftFileDefault.SUBSCENES_FOLDER_NAME);
    }

    public static File getCutscenesFolder(Scene scene) {
        File sceneFolder = getSceneFolder(scene);
        return new File(sceneFolder, NarrativeCraftFileDefault.CUTSCENES_FOLDER_NAME);
    }

    public static File getCameraAnglesFolder(Scene scene) {
        File sceneFolder = getSceneFolder(scene);
        return new File(sceneFolder, NarrativeCraftFileDefault.CAMERA_ANGLES_FOLDER_NAME);
    }

    public static File getNpcFolder(Scene scene) {
        File sceneFolder = getSceneFolder(scene);
        return new File(sceneFolder, NarrativeCraftFileDefault.NPC_FOLDER_NAME);
    }
}
