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

package fr.loudo.narrativecraft.narrative.inkTag.actions;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkCommand;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.api.utils.Side;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.editors.cameraangle.CameraAngleMakerEditorMaker;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngle;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraView;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraViewDialogSetup;
import fr.loudo.narrativecraft.narrative.cameraangle.CharacterPlacement;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.network.cameraangle.BiCameraAngleEnter;
import fr.loudo.narrativecraft.network.cameraangle.S2CEnterCameraView;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.UUID;
import net.minecraft.world.entity.Entity;

@InkCommand(
        keyword = "camera",
        description = "Transitions the camera to a pre-configured angle defined in the scene editor.",
        syntax = "camera <parentName:string> <childName:string>",
        side = Side.SERVER)
public class CameraAngleInkAction extends InkAction {

    private CameraAngle cameraAngle;
    private CameraView cameraView;

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        String parentName = cmd.getString("parentName");
        String childName = cmd.getString("childName");

        Scene concreteScene = (Scene) scene;
        cameraAngle = concreteScene.getCameraAngleManager().getByName(parentName);
        if (cameraAngle == null) {
            return InkActionResult.error(Translation.message(
                            NOT_EXISTS_KEY, Translation.message("camera_angle").getString(), parentName)
                    .getString());
        }
        cameraView = cameraAngle.getCameraByName(childName);
        if (cameraView == null) {
            return InkActionResult.error(Translation.message(
                            NOT_EXISTS_KEY, Translation.message("camera_angle").getString(), childName)
                    .getString());
        }
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        PlayerSession session = (PlayerSession) playerSession;
        StoryHandler storyHandler = session.getStoryHandler();
        if (storyHandler != null) {
            registerDialogData(storyHandler);
        }
        EditorMaker editorMaker = session.getEditor();
        boolean enterEditorMaker = editorMaker == null;
        if (editorMaker instanceof CameraAngleMakerEditorMaker cameraAngleMakerEditorMaker) {
            enterEditorMaker =
                    !cameraAngleMakerEditorMaker.getCameraAngle().getId().equals(cameraAngle.getId());
        } else {
            enterEditorMaker = true;
        }
        if (enterEditorMaker) {
            Services.PACKET.sendToPlayer(
                    playerSession.getPlayer(), new BiCameraAngleEnter(cameraAngle, NarrativeEnvironment.PRODUCTION));
            CameraAngleMakerEditorMaker editor =
                    new CameraAngleMakerEditorMaker(cameraAngle, session, NarrativeEnvironment.PRODUCTION);
            session.openEditor(editor);
            if (storyHandler != null) {
                for (CharacterPlacement characterPlacement : editor.getCharacterPlacements()) {
                    if (characterPlacement.isTemplate()) continue;
                    Entity entity = editor.getEntityForPlacement(characterPlacement.getId());
                    if (entity == null) continue;
                    storyHandler.registerEntity(characterPlacement.getCharacterStory(), entity);
                }
            }
        }
        Services.PACKET.sendToPlayer(playerSession.getPlayer(), new S2CEnterCameraView(cameraView.getId()));
        playerSession.setGameplayMode(false);
        return InkActionResult.singleOk();
    }

    private void registerDialogData(StoryHandler storyHandler) {
        DialogData global = NarrativeCraftMod.getInstance().getGlobalDialogData();
        for (CharacterPlacement placement : cameraAngle.getCharacterPlacements()) {
            if (!(placement.getCharacterStory() instanceof ICharacterStory character)) continue;
            CameraViewDialogSetup setup = findSetupForPlacement(placement.getId());
            DialogData cameraData = setup != null ? setup.getDialogData() : null;
            DialogData resolved = DialogData.resolve(global, character.getDialogData(), cameraData);
            storyHandler.registerDialogDataForCharacter(character, resolved);
        }
    }

    private CameraViewDialogSetup findSetupForPlacement(UUID placementId) {
        for (CameraViewDialogSetup setup : cameraView.getDialogSetups()) {
            if (setup.getCharacterPlacementId().equals(placementId)) return setup;
        }
        return null;
    }
}
