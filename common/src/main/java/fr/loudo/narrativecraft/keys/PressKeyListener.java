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

package fr.loudo.narrativecraft.keys;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.cameraangle.ClientCameraAngleMakerEditorMaker;
import fr.loudo.narrativecraft.client.editors.cutscene.ClientCutsceneMakerEditorMaker;
import fr.loudo.narrativecraft.client.narrative.ui.ClientNarrativeUIActionRegistry;
import fr.loudo.narrativecraft.client.screens.NarrativeEntryListScreen;
import fr.loudo.narrativecraft.client.screens.narrative.scene.SceneMenuScreen;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;

public class PressKeyListener {

    public static void onKeyPressed(Minecraft minecraft) {
        if (ModKeys.STORY_MANAGER.consumeClick()) {
            ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
            NarrativeEntryListScreen<Chapter> entryListScreen = new NarrativeEntryListScreen<>(
                    Translation.message("chapter"),
                    ClientNarrativeCraftMod.getInstance().getChapterManager().getList(),
                    Chapter.class,
                    "");
            if (session.sessionSet()) {
                SceneMenuScreen screen = new SceneMenuScreen(
                        session.getScene(),
                        ClientNarrativeUIActionRegistry.getInstance()
                                .showListSubScreen(session.getChapter(), entryListScreen));
                minecraft.setScreen(screen);
            } else {
                minecraft.setScreen(entryListScreen);
            }
        }

        if (ModKeys.HIDE_EDITOR_MAKER_HUD.consumeClick()) {
            EditorMaker editor =
                    ClientNarrativeCraftMod.getInstance().getPlayerSession().getEditor();
            if (editor == null) return;
            if (editor instanceof ClientCutsceneMakerEditorMaker clientCutsceneMakerEditorMaker) {
                clientCutsceneMakerEditorMaker.toggleHud();
            }
            if (editor instanceof ClientCameraAngleMakerEditorMaker cameraAngleMakerEditorMaker) {
                cameraAngleMakerEditorMaker.toggleHud();
            }
        }

        if (ModKeys.TOGGLE_CAMERA_ROLL.consumeClick()) {
            ClientCutsceneMakerEditorMaker editor =
                    ClientNarrativeCraftMod.getInstance().getCutsceneMakerEditor();
            if (editor == null) return;

            editor.getRollWidget().toggle();
        }
    }
}
