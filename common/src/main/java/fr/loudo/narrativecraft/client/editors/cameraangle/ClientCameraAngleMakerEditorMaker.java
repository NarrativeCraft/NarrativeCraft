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

package fr.loudo.narrativecraft.client.editors.cameraangle;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.dialog.DialogRenderer3D;
import fr.loudo.narrativecraft.client.editors.widgets.DialogPreviewEntry;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.client.utils.UtilsClient;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.cameraangle.*;
import fr.loudo.narrativecraft.narrative.character.CharacterType;
import fr.loudo.narrativecraft.network.cameraangle.*;
import fr.loudo.narrativecraft.network.mainScreen.C2SMainScreenCaptureCharacter;
import fr.loudo.narrativecraft.network.mainScreen.C2SMainScreenRemovePlacement;
import fr.loudo.narrativecraft.network.mainScreen.C2SMainScreenSave;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public class ClientCameraAngleMakerEditorMaker implements EditorMaker {

    public static final String DEFAULT_DIALOG_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

    private final Minecraft minecraft = Minecraft.getInstance();
    private final CameraAngle cameraAngle;
    private final ClientPlayerSession playerSession =
            ClientNarrativeCraftMod.getInstance().getPlayerSession();
    private final NarrativeEnvironment environment;

    public enum PreviewMode {
        CAMERA,
        DIALOG
    }

    private CameraView previewCameraView;
    private PreviewMode previewMode = PreviewMode.CAMERA;
    private boolean editingCameraViewPosition = false;
    private final Map<UUID, Integer> placementEntityIds = new HashMap<>();
    private DialogRenderer3D activeDialogRenderer;

    private final List<DialogPreviewEntry> dialogPreviewEntries = new ArrayList<>();
    private final Set<DialogRenderer3D> stoppingRenderers = new HashSet<>();
    private final List<CharacterPlacement> characterPlacements = new ArrayList<>();
    private final List<TemplateReference> templateReferences = new ArrayList<>();
    private final List<CameraView> cameraViews = new ArrayList<>();

    public ClientCameraAngleMakerEditorMaker(CameraAngle cameraAngle) {
        this.cameraAngle = cameraAngle;
        this.environment = NarrativeEnvironment.DEVELOPMENT;
    }

    public ClientCameraAngleMakerEditorMaker(CameraAngle cameraAngle, NarrativeEnvironment environment) {
        this.cameraAngle = cameraAngle;
        this.environment = environment;
    }

    @Override
    public void init() {}

    @Override
    public void close() {
        exitPreview();
        minecraft.gui.setScreen(null);
    }

    @Override
    public void tick() {
        if (previewCameraView != null && !editingCameraViewPosition) {
            LocalPlayer player = minecraft.player;
            player.setPos(previewCameraView.getPosition().subtract(0, player.getEyeHeight(), 0));
            player.setXRot((float) previewCameraView.getRotation().x);
            player.setYRot((float) previewCameraView.getRotation().y);
            player.setYHeadRot((float) previewCameraView.getRotation().y);
            player.connection.send(new ServerboundMovePlayerPacket.PosRot(
                    previewCameraView.getPosition(),
                    (float) previewCameraView.getRotation().y,
                    (float) previewCameraView.getRotation().x,
                    player.onGround(),
                    false));
            UtilsClient.setHudHidden(true);
            minecraft.setCameraEntity(minecraft.player);
        }
    }

    @Override
    public NarrativeEnvironment getEnvironment() {
        return environment;
    }

    public void loadData(String json) {
        CameraAngleDeserializer.deserializeInto(json, cameraAngle);
        characterPlacements.addAll(cameraAngle.getCharacterPlacements());
        templateReferences.addAll(cameraAngle.getTemplateReferences());
        cameraViews.addAll(cameraAngle.getCameras());
        syncDialogSetups();
    }

    public void addCharacterPlacementFromJson(String placementJson) {
        CharacterPlacement placement =
                CameraAngleDeserializer.deserializeCharacterPlacementFromJson(placementJson, cameraAngle.getScene());
        if (placement != null) {
            characterPlacements.add(placement);
            syncDialogSetups();
        }
    }

    private void syncDialogSetups() {
        for (CameraView cameraView : cameraViews) {
            for (CharacterPlacement placement : characterPlacements) {
                boolean hasSetup = cameraView.getDialogSetups().stream()
                        .anyMatch(setup -> setup.getCharacterPlacementId().equals(placement.getId()));
                if (!hasSetup) {
                    cameraView.getDialogSetups().add(new CameraViewDialogSetup(placement.getId()));
                }
            }
        }
    }

    public void createCameraFromPlayer(String name) {
        if (minecraft.player == null) return;
        if (cameraExists(name)) {
            UtilsClient.sendToast(
                    Translation.message("error"),
                    Translation.message(
                            "error.already_exists",
                            Translation.message("camera_angle").getString(),
                            name));
            return;
        }
        Vec3 position = minecraft.player.position().add(0, minecraft.player.getEyeHeight(), 0);
        Vec3 rotation = new Vec3(minecraft.player.getXRot(), minecraft.player.getYRot(), 0.0);
        float fov = minecraft.options.fov().get();
        cameraViews.add(new CameraView(name, position, rotation, fov));
        syncDialogSetups();
    }

    public boolean cameraExists(String name) {
        for (CameraView cameraView : cameraViews) {
            if (cameraView.getName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public void renameCamera(CameraView cameraView, String newName) {
        if (cameraView.getName().equalsIgnoreCase(newName)) return;
        if (cameraAngle.getCameraByName(newName) != null) {
            UtilsClient.sendToast(
                    Translation.message("error"),
                    Translation.message(
                            "error.already_exists",
                            Translation.message("camera_angle").getString(),
                            newName));
            return;
        }
        cameraView.setName(newName);
    }

    public void removeCamera(CameraView cameraView) {
        if (previewCameraView == cameraView) exitPreview();
        cameraViews.remove(cameraView);
    }

    public void removeCharacterPlacement(CharacterPlacement placement) {
        characterPlacements.remove(placement);
        removeDialogSetupsForPlacement(placement.getId());
        if (cameraAngle.getScene() != null) {
            Services.PACKET.sendToServer(new C2SCameraAngleRemovePlacement(cameraAngle, placement.getId()));
        } else {
            Services.PACKET.sendToServer(new C2SMainScreenRemovePlacement(placement.getId()));
        }
    }

    public void removeTemplateReference(TemplateReference reference) {
        List<CharacterPlacement> toRemove = characterPlacements.stream()
                .filter(p -> reference.id().equals(p.getTemplateReferenceId()))
                .toList();
        for (CharacterPlacement placement : toRemove) {
            removeDialogSetupsForPlacement(placement.getId());
        }
        characterPlacements.removeAll(toRemove);
        templateReferences.remove(reference);
        Services.PACKET.sendToServer(new C2SCameraAngleRemoveTemplateReference(cameraAngle, reference.id()));
    }

    private void removeDialogSetupsForPlacement(UUID placementId) {
        for (CameraView cameraView : cameraViews) {
            cameraView.getDialogSetups().removeIf(setup -> setup.getCharacterPlacementId()
                    .equals(placementId));
        }
    }

    public void captureCharacter(UUID characterId) {
        if (cameraAngle.getScene() != null) {
            Services.PACKET.sendToServer(new C2SCameraAngleCaptureCharacter(
                    cameraAngle.getScene().getChapter().getId(),
                    cameraAngle.getScene().getId(),
                    cameraAngle.getId(),
                    characterId));
        } else {
            Services.PACKET.sendToServer(new C2SMainScreenCaptureCharacter(characterId));
        }
    }

    public void addTemplateReference(TemplateSourceType sourceType, UUID refId, String displayName) {
        TemplateReference reference = new TemplateReference(sourceType, refId, displayName);
        templateReferences.add(reference);
        Services.PACKET.sendToServer(new C2SCameraAngleAddTemplateReference(cameraAngle, reference));
    }

    public void quit(boolean saveBeforeQuit) {
        if (saveBeforeQuit) {
            save();
        }
        playerSession.requestEditorClose();
    }

    public void save() {
        cameraAngle.getCharacterPlacements().clear();
        cameraAngle.getCharacterPlacements().addAll(characterPlacements);

        cameraAngle.getTemplateReferences().clear();
        cameraAngle.getTemplateReferences().addAll(templateReferences);

        cameraAngle.getCameras().clear();
        cameraAngle.getCameras().addAll(cameraViews);

        String dataJson = CameraAngleSerializer.serializeData(cameraAngle);
        if (cameraAngle.getScene() != null) {
            Services.PACKET.sendToServer(new C2SCameraAngleSave(cameraAngle, dataJson));
        } else {
            Services.PACKET.sendToServer(new C2SMainScreenSave(dataJson));
        }
    }

    public void teleportPlayerToPlacement(Vec3 position) {
        minecraft.gui.setScreen(null);
        LocalPlayer player = minecraft.player;
        if (player == null) return;
        player.setPos(position);
        player.connection.send(new ServerboundMovePlayerPacket.Pos(position, player.onGround(), false));
    }

    public void enterPreview(CameraView cameraView) {
        this.previewCameraView = cameraView;
        minecraft.player.connection.send(new ServerboundChangeGameModePacket(GameType.SPECTATOR));
        minecraft.setCameraEntity(minecraft.player);
        playerSession.setCameraView(cameraView);
        UtilsClient.teleportPlayerTo(cameraView.getPosition(), cameraView.getRotation());
    }

    public void exitPreview() {
        exitDialogMode();
        previewMode = PreviewMode.CAMERA;
        this.previewCameraView = null;
        playerSession.setCameraView(null);
        UtilsClient.setHudHidden(false);
    }

    public void enterDialogMode() {
        if (previewCameraView == null) return;
        previewMode = PreviewMode.DIALOG;
        for (DialogRenderer3D renderer : new ArrayList<>(stoppingRenderers)) {
            playerSession.removeDialog3D(renderer);
        }
        stoppingRenderers.clear();
        DialogData global = NarrativeCraftMod.getInstance().getGlobalDialogData();
        dialogPreviewEntries.clear();
        for (CameraViewDialogSetup setup : previewCameraView.getDialogSetups()) {
            Entity entity = getEntityForPlacement(setup.getCharacterPlacementId());
            if (entity == null) continue;
            DialogData characterData = resolveCharacterDialogData(setup.getCharacterPlacementId());
            DialogData data = DialogData.resolve(global, characterData, setup.getDialogData());
            setup.setDialogData(data);
            DialogRenderer3D renderer = new DialogRenderer3D(data, entity);
            renderer.onStopped(() -> {
                if (activeDialogRenderer == renderer) activeDialogRenderer = null;
                stoppingRenderers.remove(renderer);
                playerSession.removeDialog3D(renderer);
            });
            String label = resolvePlacementLabel(setup.getCharacterPlacementId());
            DialogPreviewEntry entry = new DialogPreviewEntry(label, data, renderer);
            entry.setPreviewText(setup.getPreviewText());
            dialogPreviewEntries.add(entry);
        }
        if (!dialogPreviewEntries.isEmpty()) {
            startDialogEntry(dialogPreviewEntries.get(0));
        }
    }

    private void startDialogEntry(DialogPreviewEntry entry) {
        DialogRenderer3D renderer = entry.getRenderer();
        stoppingRenderers.remove(renderer);
        playerSession.removeDialog3D(renderer);
        String text = entry.getPreviewText().isEmpty() ? DEFAULT_DIALOG_TEXT : entry.getPreviewText();
        renderer.start(text);
        activeDialogRenderer = renderer;
        playerSession.addDialog3D(renderer);
    }

    private void stopActiveDialogRenderer() {
        if (activeDialogRenderer == null) return;
        activeDialogRenderer.stop();
        stoppingRenderers.add(activeDialogRenderer);
        activeDialogRenderer = null;
    }

    public void selectDialogPreviewEntry(DialogPreviewEntry newEntry) {
        if (newEntry.getRenderer() == activeDialogRenderer) return;
        stopActiveDialogRenderer();
        startDialogEntry(newEntry);
    }

    public void exitDialogMode() {
        previewMode = PreviewMode.CAMERA;
        stopActiveDialogRenderer();
    }

    public void registerPlacementEntityId(UUID placementId, int entityId) {
        placementEntityIds.put(placementId, entityId);
    }

    public Entity getEntityForPlacement(UUID placementId) {
        Integer entityId = placementEntityIds.get(placementId);
        if (entityId == null || minecraft.level == null) return null;
        return minecraft.level.getEntity(entityId);
    }

    public CharacterPlacement getPlacementByEntityId(int entityId) {
        UUID placementId = placementEntityIds.entrySet().stream()
                .filter(entry -> entry.getValue() == entityId)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (placementId == null) return null;
        for (CharacterPlacement characterPlacement : characterPlacements) {
            if (characterPlacement.getId().equals(placementId)) {
                return characterPlacement;
            }
        }
        return null;
    }

    private DialogData resolveCharacterDialogData(UUID placementId) {
        for (CharacterPlacement placement : characterPlacements) {
            if (placement.getId().equals(placementId) && placement.getCharacterStory() != null) {
                return placement.getCharacterStory().getDialogData();
            }
        }
        return null;
    }

    private String resolvePlacementLabel(UUID placementId) {
        for (CharacterPlacement placement : characterPlacements) {
            if (placement.getId().equals(placementId)) {
                return placement.getCharacterStory().getName();
            }
        }
        return placementId.toString().substring(0, 8);
    }

    public void editCameraPosition() {
        editingCameraViewPosition = true;
        minecraft.gui.setScreen(null);
    }

    public void acceptNewCameraPosition() {
        LocalPlayer player = minecraft.player;
        Vec3 position = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 rotation = new Vec3(player.getXRot(), player.getYRot(), previewCameraView.getRoll());
        previewCameraView.setPosition(position);
        previewCameraView.setRotation(rotation);
        editingCameraViewPosition = false;
    }

    public void stopNewCameraPosition() {
        editingCameraViewPosition = false;
    }

    public CameraView getPreviewCamera() {
        return previewCameraView;
    }

    public CameraAngle getCameraAngle() {
        return cameraAngle;
    }

    public ClientPlayerSession getPlayerSession() {
        return playerSession;
    }

    public boolean isEditingCameraViewPosition() {
        return editingCameraViewPosition;
    }

    public PreviewMode getPreviewMode() {
        return previewMode;
    }

    public List<DialogPreviewEntry> getDialogPreviewEntries() {
        return dialogPreviewEntries;
    }

    public List<TemplateReference> getTemplateReferences() {
        return templateReferences;
    }

    public List<CharacterPlacement> getCharacterPlacements() {
        return characterPlacements;
    }

    public List<CameraView> getCameraViews() {
        return cameraViews;
    }

    public record CharacterPick(CharacterType type, UUID characterId) {}

    public record TemplatePick(TemplateSourceType sourceType, UUID refId, String displayName) {}
}
