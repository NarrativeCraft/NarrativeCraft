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

package fr.loudo.narrativecraft.network.handlers;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.cameraangle.ClientCameraAngleMakerEditorMaker;
import fr.loudo.narrativecraft.client.editors.cutscene.ClientCutsceneMakerEditorMaker;
import fr.loudo.narrativecraft.client.editors.cutscene.CutsceneMakerEditorPlayHead;
import fr.loudo.narrativecraft.client.editors.interaction.ClientInteractionMakerEditorMaker;
import fr.loudo.narrativecraft.client.narrative.ClientNarrativeEntryEditorRegistry;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.dialog.DialogRenderer2D;
import fr.loudo.narrativecraft.dialog.DialogRenderer3D;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.inkTag.InkTagDispatcherImpl;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.S2CPlayerSession;
import fr.loudo.narrativecraft.network.S2CToastMessage;
import fr.loudo.narrativecraft.network.cameraangle.S2CCameraAngleCharacterCaptured;
import fr.loudo.narrativecraft.network.cameraangle.S2CCameraAngleEditorData;
import fr.loudo.narrativecraft.network.cameraangle.S2CCameraAnglePlacementEntitySpawned;
import fr.loudo.narrativecraft.network.cutscene.BiCutscenePlayHeadPacket;
import fr.loudo.narrativecraft.network.cutscene.S2CCutsceneEditorData;
import fr.loudo.narrativecraft.network.dialog.S2CDialogTest;
import fr.loudo.narrativecraft.network.inkAction.S2CRunInkAction;
import fr.loudo.narrativecraft.network.interaction.S2CInteractionEditorData;
import fr.loudo.narrativecraft.utils.UtilsClient;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class ClientPacketHandler {

    public static final Minecraft MINECRAFT = Minecraft.getInstance();

    public static void narrativeEntry(BiSyncNarrativeEntryPacket packet) {
        switch (packet.action()) {
            case ADD -> ClientNarrativeEntryEditorRegistry.getInstance().add(packet.entryId(), packet.entry());
            case EDIT -> ClientNarrativeEntryEditorRegistry.getInstance().edit(packet.entryId(), packet.entry());
            case DELETE -> ClientNarrativeEntryEditorRegistry.getInstance().delete(packet.entryId(), packet.entry());
        }
    }

    public static void clearNarrativeData() {
        ClientNarrativeCraftMod.getInstance().getChapterManager().clear();
        ClientNarrativeCraftMod.getInstance().getCharacterManager().clear();
    }

    public static void clearScreen() {
        MINECRAFT.setScreen(null);
    }

    public static void setSession(S2CPlayerSession packet) {

        ChapterManager chapterManager = ClientNarrativeCraftMod.getInstance().getChapterManager();
        Chapter chapter = chapterManager.getById(packet.chapterId());
        if (chapter == null) return;

        Scene scene = chapter.getSceneManager().getById(packet.sceneId());
        if (scene == null) return;

        ClientNarrativeCraftMod.getInstance().getPlayerSession().apply(chapter, scene);
    }

    public static void showToast(S2CToastMessage packet) {
        UtilsClient.sendToast(packet.title(), packet.message());
    }

    public static void loadCutsceneEditorData(S2CCutsceneEditorData packet) {
        ClientCutsceneMakerEditorMaker cutsceneMakerEditor =
                ClientNarrativeCraftMod.getInstance().getCutsceneMakerEditor();
        if (!(cutsceneMakerEditor instanceof ClientCutsceneMakerEditorMaker editor)) return;
        editor.loadLayers(packet.layersJson());
    }

    public static void loadCameraAngleEditorData(S2CCameraAngleEditorData packet) {
        ClientCameraAngleMakerEditorMaker editor =
                ClientNarrativeCraftMod.getInstance().getCameraAngleMakerEditor();
        if (editor == null) return;
        editor.loadData(packet.dataJson());
    }

    public static void addCameraAngleCharacter(S2CCameraAngleCharacterCaptured packet) {
        ClientCameraAngleMakerEditorMaker editor =
                ClientNarrativeCraftMod.getInstance().getCameraAngleMakerEditor();
        if (editor == null) return;
        editor.addCharacterPlacementFromJson(packet.placementJson());
    }

    public static void onPlacementEntitySpawned(S2CCameraAnglePlacementEntitySpawned packet) {
        ClientCameraAngleMakerEditorMaker editor =
                ClientNarrativeCraftMod.getInstance().getCameraAngleMakerEditor();
        if (editor == null) return;
        editor.registerPlacementEntityId(packet.placementId(), packet.entityId());
    }

    public static void loadInteractionEditorData(S2CInteractionEditorData packet) {
        ClientInteractionMakerEditorMaker editor =
                ClientNarrativeCraftMod.getInstance().getInteractionMakerEditor();
        if (editor == null) return;
        editor.loadData(packet.getDataJson());
    }

    public static void handleDialogTest(S2CDialogTest packet) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();

        switch (packet.mode()) {
            case "stop" -> {
                new ArrayList<>(session.getActiveDialog2DRenderers()).forEach(session::removeDialog2D);
                new ArrayList<>(session.getActiveDialog3DRenderers()).forEach(session::removeDialog3D);
            }
            case "2d" -> {
                DialogData data = new DialogData();
                data.setScale(2f);
                data.setWidth(200f);
                DialogRenderer2D renderer = new DialogRenderer2D(data);
                renderer.onStopped(() -> session.removeDialog2D(renderer));
                renderer.start(packet.text());
                session.addDialog2D(renderer);
            }
            case "3d" -> {
                Entity entity = MINECRAFT.level != null ? MINECRAFT.level.getEntity(packet.entityId()) : null;
                if (entity == null) return;
                DialogData data = new DialogData();
                data.setPaddingY(7);
                data.setPaddingX(5);
                DialogRenderer3D renderer = new DialogRenderer3D(data, entity);
                renderer.onStopped(() -> session.removeDialog3D(renderer));
                renderer.start(packet.text());
                session.addDialog3D(renderer);
            }
        }
    }

    public static void updatePlayHeadCutscene(BiCutscenePlayHeadPacket packet) {

        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        if (!(session.getEditor() instanceof ClientCutsceneMakerEditorMaker editor)) return;

        CutsceneMakerEditorPlayHead playHead = editor.getPlayHead();
        float ratio = (float) packet.tick() / editor.getTotalTick();
        playHead.setRatio(ratio);
        if (ratio == 1.0f) {
            editor.getControl().pause();
        }
    }

    public static void runInkAction(S2CRunInkAction packet) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();

        InkAction action = InkTagDispatcherImpl.getInstance().instantiate(packet.keyword());
        if (action == null) return;

        ParsedCommand cmd = ParsedCommand.fromJson(packet.parsedArgsJson());
        action.setInstanceId(packet.instanceId());

        var validationResult = action.validate(cmd, session.getScene());
        if (validationResult.isError()) {
            NarrativeCraftMod.LOGGER.error(
                    "Client failed to validate Ink action '{}': {}", packet.keyword(), validationResult.errorMessage());
            return;
        }

        action.execute(null); // PlayerSession is null on client, actions must not use it
        session.addClientInkAction(action);
    }

    public static void stopAllInkActions() {
        ClientNarrativeCraftMod.getInstance().getPlayerSession().stopAllClientInkActions();
    }
}
