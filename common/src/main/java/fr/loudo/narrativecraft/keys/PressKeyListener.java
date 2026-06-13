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

import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.cameraangle.ClientCameraAngleMakerEditorMaker;
import fr.loudo.narrativecraft.client.editors.cutscene.ClientCutsceneMakerEditorMaker;
import fr.loudo.narrativecraft.client.editors.interaction.ClientInteractionMakerEditorMaker;
import fr.loudo.narrativecraft.client.narrative.ui.ClientNarrativeUIActionRegistry;
import fr.loudo.narrativecraft.client.screens.NarrativeEntryListScreen;
import fr.loudo.narrativecraft.client.screens.narrative.scene.SceneMenuScreen;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.dialog.DialogRenderer;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.interaction.InteractionPoint;
import fr.loudo.narrativecraft.network.story.C2SDialogueFinished;
import fr.loudo.narrativecraft.network.story.C2SPlayStitchStory;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class PressKeyListener {

    public static void onKeyPressed(Minecraft minecraft) {
        if (ModKeys.STORY_MANAGER.consumeClick()) {
            ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
            if (session.isInStory()) return;
            if (!minecraft.player.hasPermissions(2)) return;
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

        ModKeys.handleKeyPress(
                InputConstants.MOUSE_BUTTON_LEFT,
                minecraft.mouseHandler.isLeftPressed(),
                PressKeyListener::handleAdvanceStory,
                PressKeyListener::handleInteractionPoints);

        ModKeys.handleKeyPress(
                InputConstants.MOUSE_BUTTON_RIGHT,
                minecraft.mouseHandler.isRightPressed(),
                PressKeyListener::handleInteractionPoints);
    }

    private static void handleAdvanceStory() {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        DialogRenderer dialogRenderer = session.getMainDialog();
        if (dialogRenderer != null) {
            if (dialogRenderer.isAnimating()) return;
            if (!dialogRenderer.isTextFinished()) {
                dialogRenderer.forceFinishText();
                return;
            }

            Services.PACKET.sendToServer(new C2SDialogueFinished());
        }
    }

    private static void handleInteractionPoints() {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        if (!(session.getEditor() instanceof ClientInteractionMakerEditorMaker interactionEditor)) return;
        if (interactionEditor.getEnvironment() == NarrativeEnvironment.DEVELOPMENT) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        for (InteractionPoint point : interactionEditor.getInteraction().getPoints()) {
            if (point.isOneTimeClick() && session.hasClickedInteractionPoint(point.getId())) continue;
            if (point.canSee(player) && point.canClick(player)) {
                if (point.isOneTimeClick()) session.addClickedInteractionPoint(point.getId());
                Services.PACKET.sendToServer(
                        new C2SPlayStitchStory(point.getStitchName(), point.getId(), point.isOneTimeClick()));
                return;
            }
        }
    }
}
