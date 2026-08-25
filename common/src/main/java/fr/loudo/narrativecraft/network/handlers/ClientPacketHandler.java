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

package fr.loudo.narrativecraft.network.handlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.cameraangle.ClientCameraAngleMakerEditorMaker;
import fr.loudo.narrativecraft.client.editors.cutscene.ClientCutsceneMakerEditorMaker;
import fr.loudo.narrativecraft.client.editors.dialog.ClientCharacterDialogEditorMaker;
import fr.loudo.narrativecraft.client.editors.dialog.ClientGlobalDialogEditorMaker;
import fr.loudo.narrativecraft.client.editors.interaction.ClientInteractionMakerEditorMaker;
import fr.loudo.narrativecraft.client.editors.widgets.DialogFieldSet;
import fr.loudo.narrativecraft.client.narrative.ClientNarrativeEntryEditorRegistry;
import fr.loudo.narrativecraft.client.rendering.ImageTexture;
import fr.loudo.narrativecraft.client.screens.mainScreen.MainScreen;
import fr.loudo.narrativecraft.client.screens.story.ChoiceScreen;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.client.settings.ClientStoryLocales;
import fr.loudo.narrativecraft.client.settings.ClientStoryTranslations;
import fr.loudo.narrativecraft.client.settings.ClientStoryVariables;
import fr.loudo.narrativecraft.client.settings.NarrativeClientSettings;
import fr.loudo.narrativecraft.dialog.*;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngle;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngleDeserializer;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraView;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.interaction.Interaction;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.network.*;
import fr.loudo.narrativecraft.network.cameraangle.*;
import fr.loudo.narrativecraft.network.cutscene.BiCutsceneEnter;
import fr.loudo.narrativecraft.network.cutscene.BiCutscenePlayHeadPacket;
import fr.loudo.narrativecraft.network.cutscene.S2CCutsceneEditorData;
import fr.loudo.narrativecraft.network.dialog.S2CDialogEditorEntitySpawned;
import fr.loudo.narrativecraft.network.dialog.S2CDialogTest;
import fr.loudo.narrativecraft.network.inkAction.S2CRunInkAction;
import fr.loudo.narrativecraft.network.interaction.BiInteractionEnter;
import fr.loudo.narrativecraft.network.interaction.S2CInteractionEditorData;
import fr.loudo.narrativecraft.network.interaction.S2CInteractionLeave;
import fr.loudo.narrativecraft.network.mainScreen.BiMainScreenEnter;
import fr.loudo.narrativecraft.network.mainScreen.S2CMainScreenData;
import fr.loudo.narrativecraft.network.mainScreen.S2COpenMainScreen;
import fr.loudo.narrativecraft.network.story.*;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.UtilsClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ClientPacketHandler {

    public static final Minecraft MINECRAFT = Minecraft.getInstance();

    private static final int PENDING_DIALOGUE_MAX_TICKS = 20;

    private static S2CShowDialogue pendingDialogue;
    private static int pendingDialogueTicks;

    public static void narrativeEntry(BiSyncNarrativeEntryPacket packet) {
        switch (packet.action()) {
            case ADD -> ClientNarrativeEntryEditorRegistry.getInstance().add(packet.entryId(), packet.entry());
            case EDIT -> ClientNarrativeEntryEditorRegistry.getInstance().edit(packet.entryId(), packet.entry());
            case DELETE -> ClientNarrativeEntryEditorRegistry.getInstance().delete(packet.entryId(), packet.entry());
        }
    }

    public static void cutsceneState(BiCutsceneEnter packet) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        Chapter chapter =
                ClientNarrativeCraftMod.getInstance().getChapterManager().getById(packet.getChapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.getSceneId());
        if (scene == null) return;
        Cutscene cutscene = scene.getCutsceneManager().getById(packet.getCutsceneId());
        if (cutscene == null) return;

        ClientCutsceneMakerEditorMaker cutsceneEditor =
                new ClientCutsceneMakerEditorMaker(cutscene, packet.getEnvironment());
        cutsceneEditor.init();
        session.setEditor(cutsceneEditor);
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
        if (cutsceneMakerEditor == null) return;
        cutsceneMakerEditor.loadLayers(packet.layersJson());
        cutsceneMakerEditor.applyManualMaxTick(packet.manualMaxTick());
    }

    public static void loadCameraAngleEditorData(S2CCameraAngleEditorData packet) {
        EditorMaker editor =
                ClientNarrativeCraftMod.getInstance().getPlayerSession().getEditor();
        if (!(editor instanceof ClientCameraAngleMakerEditorMaker editorMaker)) return;
        editorMaker.loadData(packet.dataJson());
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

    public static void onDialogEditorEntitySpawned(S2CDialogEditorEntitySpawned packet) {
        EditorMaker editor =
                ClientNarrativeCraftMod.getInstance().getPlayerSession().getEditor();
        if (editor instanceof ClientGlobalDialogEditorMaker globalEditor) {
            globalEditor.registerEntityId(packet.entityId());
        } else if (editor instanceof ClientCharacterDialogEditorMaker characterEditor) {
            characterEditor.registerEntityId(packet.entityId());
        }
    }

    public static void loadInteractionEditorData(S2CInteractionEditorData packet) {
        ClientInteractionMakerEditorMaker editor =
                ClientNarrativeCraftMod.getInstance().getInteractionMakerEditor(packet.getInteractionId());
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

        if (editor.getTotalTick() > 0 && packet.tick() >= editor.getTotalTick()) {
            editor.getControl().pause();
        }
    }

    public static void runInkAction(S2CRunInkAction packet) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();

        InkAction action =
                ClientNarrativeCraftMod.getInstance().getInkTagDispatcher().instantiate(packet.keyword());
        if (action == null) return;

        ParsedCommand cmd = ParsedCommand.fromJson(packet.parsedArgsJson());
        action.setInstanceId(packet.instanceId());

        var validationResult = action.validate(cmd, session.getScene());
        if (validationResult.isError()) {
            NarrativeCraftMod.LOGGER.error(
                    "Client failed to validate Ink action '{}': {}", packet.keyword(), validationResult.errorMessage());
            return;
        }

        action.execute(session);
        session.addClientInkAction(action);
    }

    public static void storyLocales(S2CStoryLocales packet) {
        ClientStoryLocales.set(packet.locales(), packet.defaultLocale());

        if (NarrativeClientSettings.storyLocale.isEmpty()) {
            NarrativeClientSettings.storyLocale =
                    ClientStoryLocales.resolve(MINECRAFT.getLanguageManager().getSelected());
            try {
                NarrativeClientSettings.save();
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.error("Failed to save user settings!", e);
            }
        }

        Services.PACKET.sendToServer(
                new C2SSetStoryLocale(ClientStoryLocales.resolve(NarrativeClientSettings.storyLocale)));
    }

    public static void storyTranslations(S2CStoryTranslations packet) {
        ClientStoryTranslations.set(packet.entries());
    }

    public static void storyVariables(S2CStoryVariables packet) {
        ClientStoryVariables.set(packet.variables());
    }

    public static void applyStoryLocale(S2CSetStoryLocale packet) {
        NarrativeClientSettings.storyLocale = packet.locale();
        try {
            NarrativeClientSettings.save();
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to save user settings!", e);
        }
    }

    public static void ensureLocalExists(S2CEnsureLocalExists packet) {
        if (!packet.locales().contains(NarrativeClientSettings.storyLocale)) {
            NarrativeClientSettings.storyLocale = packet.defaultLocale();
            try {
                NarrativeClientSettings.save();
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.error("Failed to save user settings!", e);
            }
        }
    }

    public static void stopAllInkActions() {
        ClientNarrativeCraftMod.getInstance().getPlayerSession().stopAllClientInkActions();
    }

    public static void stopStory() {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        pendingDialogue = null;
        session.clear();
        ClientNarrativeCraftMod.getInstance().getPlayerSession().setInStory(false);
        ClientStoryVariables.clear();
        ImageTexture.clearCache();
    }

    public static void dialogStop() {

        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        pendingDialogue = null;
        DialogRenderer dialogRenderer = session.getMainDialog();
        if (dialogRenderer == null) {
            Services.PACKET.sendToServer(new C2SDialogueFinished());
            return;
        }

        dialogRenderer.stop();
    }

    public static void enterCameraView(S2CEnterCameraView packet) {
        if (MINECRAFT.player == null) return;
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();

        EditorMaker editor = session.getEditor();
        if (!(editor instanceof ClientCameraAngleMakerEditorMaker clientAngleMaker)) return;

        CameraView cameraView = clientAngleMaker.getCameraViews().stream()
                .filter(c -> c.getId().equals(packet.cameraViewId()))
                .findFirst()
                .orElse(null);
        if (cameraView == null) return;

        clientAngleMaker.enterPreview(cameraView);
    }

    public static void showDialogue(S2CShowDialogue packet) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();

        pendingDialogue = null;
        if (session.getMainDialog() == null
                && packet.entityId() != S2CShowDialogue.NO_ENTITY
                && !entityAvailable(packet.entityId())) {
            pendingDialogue = packet;
            pendingDialogueTicks = 0;
            return;
        }

        displayDialogue(packet);
    }

    public static void tickPendingDialogue() {
        if (pendingDialogue == null) return;

        pendingDialogueTicks++;
        if (!entityAvailable(pendingDialogue.entityId()) && pendingDialogueTicks < PENDING_DIALOGUE_MAX_TICKS) return;

        S2CShowDialogue packet = pendingDialogue;
        pendingDialogue = null;
        displayDialogue(packet);
    }

    private static boolean entityAvailable(int entityId) {
        return MINECRAFT.level != null && MINECRAFT.level.getEntity(entityId) != null;
    }

    private static void displayDialogue(S2CShowDialogue packet) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();

        DialogData resolvedData = resolveDialogData(packet.dialogDataJson());

        if (session.getMainDialog() != null) {
            DialogRenderer dialogRenderer = session.getMainDialog();
            DialogData data = new DialogData();
            if (packet.entityId() != S2CShowDialogue.NO_ENTITY && entityAvailable(packet.entityId())) {
                data = resolvedData;
            }
            dialogRenderer.setData(data);
            dialogRenderer.update(packet.text());
            return;
        }

        if (packet.entityId() != S2CShowDialogue.NO_ENTITY && MINECRAFT.level != null) {
            Entity entity = MINECRAFT.level.getEntity(packet.entityId());
            if (entity != null) {
                DialogData data = resolvedData != null ? resolvedData : new DialogData();
                DialogRenderer3D renderer = new DialogRenderer3D(data, entity);
                session.setMainDialog(renderer);
                renderer.onStopped(() -> {
                    session.removeDialog3D(renderer);
                    if (renderer.equals(session.getMainDialog())) {
                        session.setMainDialog(null);
                    }
                    Services.PACKET.sendToServer(new C2SDialogueFinished());
                });
                renderer.start(packet.text());
                session.addDialog3D(renderer);
                return;
            }
        }

        DialogRenderer2D renderer = new DialogRenderer2D(new DialogData());
        session.setMainDialog(renderer);
        renderer.onStopped(() -> {
            session.removeDialog2D(renderer);
            if (renderer.equals(session.getMainDialog())) {
                session.setMainDialog(null);
            }
            Services.PACKET.sendToServer(new C2SDialogueFinished());
        });
        renderer.start(packet.text());
        session.addDialog2D(renderer);
    }

    private static DialogData resolveDialogData(String dialogDataJson) {
        if (dialogDataJson == null || dialogDataJson.isEmpty()) return null;
        try {
            JsonObject json = JsonParser.parseString(dialogDataJson).getAsJsonObject();
            return DialogDataIO.deserialize(json, DialogFieldSet.ALL);
        } catch (Exception e) {
            return null;
        }
    }

    public static void characterStoryAction(S2CCharacterStoryAction packet) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();

        CharacterManager characterManager =
                ClientNarrativeCraftMod.getInstance().getCharacterManager();
        Map<UUID, ICharacterStory> charactersInWorld = session.getCharactersInWorld();
        switch (packet.action()) {
            case ADD -> {
                if (packet.profileId() == null) return;
                ICharacterStory characterStory =
                        characterManager.resolveCharacter(packet.characterId(), session.getScene());
                if (characterStory == null) return;
                charactersInWorld.put(packet.profileId(), characterStory);
            }
            case REMOVE -> {
                if (packet.profileId() == null) {
                    charactersInWorld
                            .values()
                            .removeIf(character -> character.getId().equals(packet.characterId()));
                    return;
                }
                charactersInWorld.remove(packet.profileId());
            }
            case CLEAR -> charactersInWorld.clear();
        }
    }

    public static void showChoices(S2CShowChoices packet) {
        ChoiceScreen choiceScreen = new ChoiceScreen(packet.texts());
        ClientNarrativeCraftMod.getInstance().getPlayerSession().setChoiceScreen(choiceScreen);
        MINECRAFT.setScreen(choiceScreen);
    }

    public static void cameraAngleEnter(BiCameraAngleEnter packet) {
        Chapter chapter =
                ClientNarrativeCraftMod.getInstance().getChapterManager().getById(packet.getChapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.getSceneId());
        if (scene == null) return;
        CameraAngle cameraAngle = scene.getCameraAngleManager().getById(packet.getCameraAngleId());
        if (cameraAngle == null) return;

        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        ClientCameraAngleMakerEditorMaker cameraAngleEditor =
                new ClientCameraAngleMakerEditorMaker(cameraAngle, packet.getEnvironment());
        cameraAngleEditor.init();
        session.setEditor(cameraAngleEditor);
        Minecraft.getInstance().setScreen(null);
    }

    public static void editorClose(BiEditorClose packet) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        if (session.getEditor() == null) return;
        if (packet.editorSessionId() != session.getEditorSessionId()) return;
        session.closeEditor();
    }

    public static void editorOpened(S2CEditorOpened packet) {
        ClientNarrativeCraftMod.getInstance().getPlayerSession().setEditorSessionId(packet.editorSessionId());
    }

    public static void interactionEnter(BiInteractionEnter packet) {
        Chapter chapter =
                ClientNarrativeCraftMod.getInstance().getChapterManager().getById(packet.getChapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.getSceneId());
        if (scene == null) return;
        Interaction interaction = scene.getInteractionManager().getById(packet.getInteractionId());
        if (interaction == null) return;

        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        ClientInteractionMakerEditorMaker interactionEditor =
                new ClientInteractionMakerEditorMaker(interaction, packet.getEnvironment());
        if (packet.getEnvironment() == NarrativeEnvironment.PRODUCTION) {
            session.addInteractionSession(interaction.getId(), interactionEditor);
            return;
        }
        interactionEditor.init();
        session.setEditor(interactionEditor);
        Minecraft.getInstance().setScreen(null);
    }

    public static void interactionLeave(S2CInteractionLeave packet) {
        ClientNarrativeCraftMod.getInstance().getPlayerSession().removeInteractionSession(packet.getInteractionId());
    }

    public static void characterSkin(S2CCharacterSkin packet) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();

        UUID characterId = packet.characterId();
        try {
            NativeImage image = NativeImage.read(packet.skinBytes());
            DynamicTexture texture = new DynamicTexture(image);
            minecraft
                    .getTextureManager()
                    .register(new ResourceLocation(NarrativeCraftMod.MOD_ID, "character/" + characterId), texture);
            session.getLoadedCharactersSkin().add(characterId);
        } catch (Exception e) {
            minecraft.player.sendSystemMessage(
                    Translation.message("error.register_character_skin", packet.characterId())
                            .withStyle(ChatFormatting.RED));
            NarrativeCraftMod.LOGGER.error("Failed to register {} skin!", packet.characterId(), e);
        }
    }

    public static void clearLoadedSkins(S2CClearLoadedSkins packet) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();

        Minecraft minecraft = Minecraft.getInstance();
        TextureManager textureManager = minecraft.getTextureManager();
        for (UUID characterId : session.getLoadedCharactersSkin()) {
            textureManager.release(new ResourceLocation(NarrativeCraftMod.MOD_ID, "character/" + characterId));
        }
        session.getLoadedCharactersSkin().clear();
    }

    public static void renderSaveIcon(S2CRenderSaveIcon packet) {
        ClientNarrativeCraftMod.getInstance()
                .getPlayerSession()
                .getSaveIconRenderer()
                .start(packet.in(), packet.stay(), packet.out());
    }

    public static void receiveMainScreenData(S2CMainScreenData packet) {
        CameraAngle cameraAngle = new CameraAngle("", "", null);
        CameraAngleDeserializer.deserializeInto(packet.dataJson(), cameraAngle);
        ClientNarrativeCraftMod.getInstance().setMainScreenData(cameraAngle);
    }

    public static void openMainScreen(S2COpenMainScreen packet) {
        Minecraft.getInstance().setScreen(new MainScreen(packet.canContinue(), packet.finishedStory()));
    }

    public static void enterMainScreen(BiMainScreenEnter packet) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        CameraAngle mainScreenData = ClientNarrativeCraftMod.getInstance().getMainScreenData();
        if (mainScreenData == null) return;
        ClientCameraAngleMakerEditorMaker cameraAngleEditor =
                new ClientCameraAngleMakerEditorMaker(mainScreenData, packet.getEnvironment());
        cameraAngleEditor.init();
        session.setEditor(cameraAngleEditor);
        Minecraft.getInstance().setScreen(null);
    }

    public static void notifyClientPlayStory() {
        ClientNarrativeCraftMod.getInstance().getPlayerSession().setInStory(true);
    }

    public static void sessionClear() {
        ClientNarrativeCraftMod.getInstance().getPlayerSession().clear();
    }
}
