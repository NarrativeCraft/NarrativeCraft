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

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.widgets.*;
import fr.loudo.narrativecraft.client.screens.ClearScreen;
import fr.loudo.narrativecraft.client.screens.narrative.cameraangle.CameraAngleCameraNameScreen;
import fr.loudo.narrativecraft.client.screens.narrative.cameraangle.CameraAngleCharacterPickerScreen;
import fr.loudo.narrativecraft.client.screens.narrative.cameraangle.CameraAngleEditorMenuScreen;
import fr.loudo.narrativecraft.client.screens.narrative.cameraangle.CameraAngleTemplatePickerScreen;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.dialog.DialogRenderer3D;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.keys.ModKeys;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.cameraangle.*;
import fr.loudo.narrativecraft.narrative.character.CharacterType;
import fr.loudo.narrativecraft.network.C2SChangeGamemodePacket;
import fr.loudo.narrativecraft.network.cameraangle.*;
import fr.loudo.narrativecraft.network.mainScreen.C2SMainScreenCaptureCharacter;
import fr.loudo.narrativecraft.network.mainScreen.C2SMainScreenRemovePlacement;
import fr.loudo.narrativecraft.network.mainScreen.C2SMainScreenSave;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.CustomFont;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.UtilsClient;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public class ClientCameraAngleMakerEditorMaker implements EditorMaker {

    private static final int BUTTON_WIDTH = 20;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 10;
    public static final String DEFAULT_DIALOG_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

    private final Minecraft minecraft = Minecraft.getInstance();
    private final CameraAngle cameraAngle;
    private final ClientPlayerSession playerSession =
            ClientNarrativeCraftMod.getInstance().getPlayerSession();
    private final List<Button> buttons = new ArrayList<>();
    private final RollSliderWidget rollWidget = new RollSliderWidget();
    private final FovSliderWidget fovWidget = new FovSliderWidget();
    private final NarrativeEnvironment environment;

    public enum PreviewMode {
        CAMERA,
        DIALOG
    }

    private CameraView previewCameraView;
    private PreviewMode previewMode = PreviewMode.CAMERA;
    private boolean renderingHud = true;
    private boolean editingCameraViewPosition = false;
    private final Map<UUID, Integer> placementEntityIds = new HashMap<>();
    private DialogRenderer3D activeDialogRenderer;
    private final DialogPreviewPanel dialogPreviewPanel =
            new DialogPreviewPanel(this::toggleAdvancedPanel, DEFAULT_DIALOG_TEXT);
    private final DialogSetupAdvancedPanel advancedPanel = new DialogSetupAdvancedPanel(dialogPreviewPanel);

    {
        dialogPreviewPanel.setFieldSet(DialogFieldSet.CAMERA_VIEW);
        advancedPanel.setFieldSet(DialogFieldSet.CAMERA_VIEW);
        dialogPreviewPanel.setOnSelectionChanged(this::onDialogSelectionChanged);
    }

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

    public void init() {
        buttons.clear();
        buttons.add(Button.builder(Component.literal(CustomFont.CAMERA), b -> openAddCameraScreen())
                .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Translation.message("screen.camera_angle_editor.add_camera")))
                .build());
        buttons.add(Button.builder(Component.literal(CustomFont.CHARACTER), b -> openCharacterPicker())
                .tooltip(Tooltip.create(Translation.message("screen.camera_angle_editor.add_character")))
                .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        buttons.add(Button.builder(Component.literal(CustomFont.CHARACTER_TEMPLATE), b -> openTemplatePicker())
                .tooltip(Tooltip.create(Translation.message("screen.camera_angle_editor.add_template")))
                .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        buttons.add(Button.builder(Component.literal(CustomFont.BURGER_MENU), b -> openMenu())
                .tooltip(Tooltip.create(Translation.message("screen.camera_angle_editor.open_menu")))
                .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        buttons.add(Button.builder(Component.literal(CustomFont.CROSS), b -> openQuitConfirm())
                .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        buttons.add(Button.builder(Component.literal(CustomFont.SAVE), b -> save())
                .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        buttons.add(Button.builder(Translation.message("screen.camera_angle_editor.leave_preview"), b -> exitPreview())
                .bounds(0, 0, 100, BUTTON_HEIGHT)
                .build());
        buttons.add(Button.builder(
                        Translation.message("screen.camera_angle_editor.edit_position"), b -> editCameraPosition())
                .bounds(0, 0, 100, BUTTON_HEIGHT)
                .build());
        buttons.add(Button.builder(Component.literal(CustomFont.CHECK), b -> acceptNewCameraPosition())
                .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        buttons.add(Button.builder(Component.literal(CustomFont.UNDO), b -> stopNewCameraPosition())
                .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        buttons.add(Button.builder(Component.literal(CustomFont.DIALOG), button -> {
                    if (previewMode == PreviewMode.CAMERA) {
                        enterDialogMode();
                    } else {
                        exitDialogMode();
                    }
                })
                .bounds(0, 0, 20, 20)
                .build());

        // Make template character button not active if on main screen editor
        buttons.get(2).active = cameraAngle.getScene() != null;
        if (cameraAngle.getScene() == null) {
            buttons.get(2).setTooltip(Tooltip.create(Translation.message("screen.main_screen.disabled_template")));
        }
    }

    @Override
    public void close() {
        exitPreview();
        minecraft.setScreen(null);
    }

    public void tick() {
        if (previewCameraView != null && !editingCameraViewPosition) {
            LocalPlayer player = minecraft.player;
            player.setPos(previewCameraView.getPosition().subtract(0, player.getEyeHeight(), 0));
            player.setXRot((float) previewCameraView.getRotation().x);
            player.setYRot((float) previewCameraView.getRotation().y);
            player.setYHeadRot((float) previewCameraView.getRotation().y);
            player.connection.send(new ServerboundMovePlayerPacket.PosRot(
                    previewCameraView.getPosition().x,
                    previewCameraView.getPosition().y,
                    previewCameraView.getPosition().z,
                    (float) previewCameraView.getRotation().y,
                    (float) previewCameraView.getRotation().x,
                    player.onGround()));
            Minecraft.getInstance().options.hideGui = true;
            minecraft.setCameraEntity(minecraft.player);
        }
        advancedPanel.tick();
    }

    @Override
    public void teleportToEditorOrigin() {}

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

    private void openAddCameraScreen() {
        if (cameraAngle.getScene() != null) {
            minecraft.setScreen(new CameraAngleCameraNameScreen(
                    Translation.message("screen.camera_angle_editor.camera_name_prompt"),
                    "",
                    null,
                    this::createCameraFromPlayer,
                    minecraft.screen));
        } else {
            createCameraFromPlayer("main");
        }
    }

    private void createCameraFromPlayer(String name) {
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

    private void openCharacterPicker() {
        minecraft.setScreen(new CameraAngleCharacterPickerScreen(cameraAngle.getScene(), minecraft.screen, pick -> {
            if (cameraAngle.getScene() != null) {
                Services.PACKET.sendToServer(new C2SCameraAngleCaptureCharacter(
                        cameraAngle.getScene().getChapter().getId(),
                        cameraAngle.getScene().getId(),
                        cameraAngle.getId(),
                        pick.characterId()));
            } else {
                Services.PACKET.sendToServer(new C2SMainScreenCaptureCharacter(pick.characterId()));
            }
        }));
    }

    private void openTemplatePicker() {
        minecraft.setScreen(new CameraAngleTemplatePickerScreen(cameraAngle.getScene(), minecraft.screen, pick -> {
            addTemplateReference(pick.sourceType(), pick.refId(), pick.displayName());
        }));
    }

    public void addTemplateReference(TemplateSourceType sourceType, UUID refId, String displayName) {
        TemplateReference reference = new TemplateReference(sourceType, refId, displayName);
        templateReferences.add(reference);
        Services.PACKET.sendToServer(new C2SCameraAngleAddTemplateReference(cameraAngle, reference));
    }

    public void openMenu() {
        minecraft.setScreen(new CameraAngleEditorMenuScreen(this, minecraft.screen));
    }

    private void openQuitConfirm() {
        ConfirmScreen confirmScreen = new ConfirmScreen(
                b -> {
                    if (b) {
                        save();
                    }
                    playerSession.requestEditorClose();
                },
                Translation.message("screen.confirm.title"),
                Translation.message("screen.confirm.save"));
        minecraft.setScreen(confirmScreen);
    }

    private void save() {
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
        minecraft.setScreen(null);
        LocalPlayer player = minecraft.player;
        if (player == null) return;
        player.setPos(position);
        player.connection.send(
                new ServerboundMovePlayerPacket.Pos(position.x, position.y, position.z, player.onGround()));
    }

    public void enterPreview(CameraView cameraView) {
        this.previewCameraView = cameraView;
        Services.PACKET.sendToServer(new C2SChangeGamemodePacket(GameType.SPECTATOR));
        minecraft.setCameraEntity(minecraft.player);
        playerSession.setCameraView(cameraView);
        UtilsClient.teleportPlayerTo(cameraView.getPosition(), cameraView.getRotation());
        rollWidget.setValue(cameraView.getRoll());
        rollWidget.setVisible(true);
        fovWidget.setValue(cameraView.getFov());
        fovWidget.setVisible(true);
    }

    public void exitPreview() {
        exitDialogMode();
        previewMode = PreviewMode.CAMERA;
        this.previewCameraView = null;
        playerSession.setCameraView(null);
        rollWidget.setVisible(false);
        fovWidget.setVisible(false);
        Minecraft.getInstance().options.hideGui = false;
    }

    public void enterDialogMode() {
        if (previewCameraView == null) return;
        previewMode = PreviewMode.DIALOG;
        rollWidget.setVisible(false);
        fovWidget.setVisible(false);
        for (DialogRenderer3D renderer : new ArrayList<>(stoppingRenderers)) {
            playerSession.removeDialog3D(renderer);
        }
        stoppingRenderers.clear();
        DialogData global = NarrativeCraftMod.getInstance().getGlobalDialogData();
        List<DialogPreviewEntry> entries = new ArrayList<>();
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
            entries.add(entry);
        }
        dialogPreviewPanel.setEntries(entries);
        if (!entries.isEmpty()) {
            startDialogEntry(entries.get(0));
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

    private void onDialogSelectionChanged(DialogPreviewEntry newEntry) {
        if (newEntry.getRenderer() == activeDialogRenderer) return;
        stopActiveDialogRenderer();
        startDialogEntry(newEntry);
    }

    public void exitDialogMode() {
        previewMode = PreviewMode.CAMERA;
        stopActiveDialogRenderer();
        if (previewCameraView != null) {
            rollWidget.setVisible(true);
            fovWidget.setVisible(true);
        }
        advancedPanel.setVisible(false);
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

    public void openAdvancedPanel(DialogData data) {
        advancedPanel.setData(data);
        advancedPanel.setVisible(true);
    }

    public boolean advancedPanelVisible() {
        return advancedPanel.isVisible();
    }

    public void toggleAdvancedPanel(DialogData data) {
        if (advancedPanel.isVisible()) {
            advancedPanel.setVisible(false);
        } else {
            advancedPanel.setData(data);
            advancedPanel.setVisible(true);
        }
    }

    public void closeAdvancedPanel() {
        advancedPanel.setVisible(false);
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

    private void editCameraPosition() {
        editingCameraViewPosition = true;
        minecraft.setScreen(null);
    }

    private void acceptNewCameraPosition() {
        LocalPlayer player = minecraft.player;
        Vec3 position = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 rotation = new Vec3(player.getXRot(), player.getYRot(), previewCameraView.getRoll());
        previewCameraView.setPosition(position);
        previewCameraView.setRotation(rotation);
        editingCameraViewPosition = false;
    }

    private void stopNewCameraPosition() {
        editingCameraViewPosition = false;
        minecraft.levelRenderer.allChanged();
    }

    public CameraView getPreviewCamera() {
        return previewCameraView;
    }

    public void renderAnchorPoint(PoseStack poseStack) {
        if (previewCameraView == null || previewMode != PreviewMode.DIALOG) return;
        dialogPreviewPanel.renderAnchorPoint(poseStack);
    }

    @Override
    public void render(GuiGraphics graphics, float deltaTracker) {
        if (environment != NarrativeEnvironment.DEVELOPMENT) return;
        if (previewCameraView != null) {
            previewCameraView.setRoll(rollWidget.getValue());
            previewCameraView.setFov(fovWidget.getValue());
        }
        if (!renderingHud) return;

        int[] mousePos = UtilsClient.getScaledMousePos();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        Button quitButton = buttons.get(4);
        Button saveButton = buttons.get(5);
        Button leavePreviewButton = buttons.get(6);
        Button editPositionButton = buttons.get(7);
        Button acceptPositionButton = buttons.get(8);
        Button stopPositionButton = buttons.get(9);

        quitButton.setPosition(5, 5);
        saveButton.setPosition(quitButton.getX() + quitButton.getWidth() + 5, 5);

        boolean inPreview = previewCameraView != null;

        if (!inPreview) {
            int totalWidth = 4 * BUTTON_WIDTH + 3 * BUTTON_GAP;
            int startX = screenWidth / 2 - totalWidth / 2;
            int y = screenHeight - BUTTON_HEIGHT - 30;
            for (int i = 0; i < 4; i++) {
                Button b = buttons.get(i);
                b.setPosition(startX + i * (BUTTON_WIDTH + BUTTON_GAP), y);
                b.render(
                        graphics,
                        mousePos[0],
                        mousePos[1],
                        Minecraft.getInstance().getDeltaFrameTime());
            }
            quitButton.render(
                    graphics, mousePos[0], mousePos[1], Minecraft.getInstance().getDeltaFrameTime());
            saveButton.render(
                    graphics, mousePos[0], mousePos[1], Minecraft.getInstance().getDeltaFrameTime());
        } else if (!editingCameraViewPosition) {
            leavePreviewButton.setPosition(5, 5);
            editPositionButton.setPosition(leavePreviewButton.getX() + leavePreviewButton.getWidth() + 5, 5);

            leavePreviewButton.render(
                    graphics, mousePos[0], mousePos[1], Minecraft.getInstance().getDeltaFrameTime());
            editPositionButton.render(
                    graphics, mousePos[0], mousePos[1], Minecraft.getInstance().getDeltaFrameTime());

            if (previewMode == PreviewMode.DIALOG) {
                dialogPreviewPanel.render(graphics, screenWidth, screenHeight, mousePos[0], mousePos[1]);
            }

            buttons.get(10).setPosition(screenWidth - 25, 5);
            buttons.get(10).setWidth(20);
            buttons.get(10)
                    .render(
                            graphics,
                            mousePos[0],
                            mousePos[1],
                            Minecraft.getInstance().getDeltaFrameTime());
        } else {
            acceptPositionButton.setPosition(5, 5);
            acceptPositionButton.render(
                    graphics, mousePos[0], mousePos[1], Minecraft.getInstance().getDeltaFrameTime());

            stopPositionButton.setPosition(acceptPositionButton.getX() + acceptPositionButton.getWidth() + 5, 5);
            stopPositionButton.render(
                    graphics, mousePos[0], mousePos[1], Minecraft.getInstance().getDeltaFrameTime());
        }

        if (previewMode == PreviewMode.CAMERA) {
            rollWidget.render(graphics, screenWidth, screenHeight, mousePos[0], mousePos[1]);
            fovWidget.render(graphics, screenWidth, screenHeight, mousePos[0], mousePos[1]);
        }

        if (previewMode == PreviewMode.DIALOG) {
            advancedPanel.render(graphics, mousePos[0], mousePos[1]);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button, boolean isDoubleClick) {
        if (environment != NarrativeEnvironment.DEVELOPMENT) return;
        if (!renderingHud || !clearScreenOpened()) return;
        if (advancedPanel.mouseClicked(mouseX, mouseY, button)) {
            dialogPreviewPanel.unfocusAll();
            return;
        }
        if (rollWidget.mouseClicked(mouseX, mouseY, button)) return;
        if (fovWidget.mouseClicked(mouseX, mouseY, button)) return;

        boolean inPreview = previewCameraView != null;

        if (!inPreview) {
            for (int i = 0; i < 6; i++) {
                buttons.get(i).mouseClicked(mouseX, mouseY, button);
            }
        } else if (!editingCameraViewPosition) {
            buttons.get(6).mouseClicked(mouseX, mouseY, button);
            buttons.get(7).mouseClicked(mouseX, mouseY, button);
            if (previewMode == PreviewMode.DIALOG) {
                if (dialogPreviewPanel.mouseClicked(mouseX, mouseY, button)) {
                    advancedPanel.unfocusAll();
                }
            }
        } else {
            buttons.get(8).mouseClicked(mouseX, mouseY, button);
            buttons.get(9).mouseClicked(mouseX, mouseY, button);
        }

        buttons.get(10).mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (environment != NarrativeEnvironment.DEVELOPMENT) return;
        if (!renderingHud || !clearScreenOpened()) return;
        rollWidget.mouseReleased();
        fovWidget.mouseReleased();
        if (previewMode == PreviewMode.DIALOG) dialogPreviewPanel.mouseReleased();
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (environment != NarrativeEnvironment.DEVELOPMENT) return;
        if (!renderingHud || !clearScreenOpened()) return;
        if (previewMode == PreviewMode.DIALOG) {
            if (dialogPreviewPanel.mouseDragged((mouseY))) return;
        }
        if (rollWidget.mouseDragged((mouseY))) return;
        fovWidget.mouseDragged((mouseY));
    }

    public void mouseScrolled(double deltaX, double deltaY) {
        if (environment != NarrativeEnvironment.DEVELOPMENT) return;
        if (!renderingHud || !clearScreenOpened()) return;
        if (previewCameraView == null || editingCameraViewPosition || previewMode != PreviewMode.DIALOG) return;
        int[] mousePos = UtilsClient.getScaledMousePos();
        dialogPreviewPanel.mouseScrolled(deltaY, mousePos[0], mousePos[1]);
    }

    private boolean clearScreenOpened() {
        return minecraft.screen != null && minecraft.screen instanceof ClearScreen;
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (environment != NarrativeEnvironment.DEVELOPMENT) return;
        if (previewMode == PreviewMode.DIALOG) {
            dialogPreviewPanel.keyPressed(keyCode, scanCode, modifiers);
            if (dialogPreviewPanel.isAnyBoxFocused()) return;
        }
        if (advancedPanel.isVisible()) {
            advancedPanel.keyPressed(keyCode, scanCode, modifiers);
            if (advancedPanel.isAnyBoxFocused()) return;
        }
        if (previewCameraView != null
                && !editingCameraViewPosition
                && keyCode
                        == ModKeys.TOGGLE_DIALOG_MODE_CAMERA_ANGLE
                                .getDefaultKey()
                                .getValue()
                && cameraAngle.getScene() != null) {
            if (previewMode == PreviewMode.CAMERA) {
                enterDialogMode();
            } else {
                exitDialogMode();
            }
        }
    }

    public void toggleHud() {
        renderingHud = !renderingHud;
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        if (environment != NarrativeEnvironment.DEVELOPMENT) return;
        if (previewMode == PreviewMode.DIALOG) {
            dialogPreviewPanel.charTyped(codePoint, modifiers);
        }
        if (advancedPanel.isVisible()) {
            advancedPanel.charTyped(codePoint, modifiers);
        }
    }

    public CameraAngle getCameraAngle() {
        return cameraAngle;
    }

    public ClientPlayerSession getPlayerSession() {
        return playerSession;
    }

    public boolean isRenderingHud() {
        return renderingHud;
    }

    public void setRenderingHud(boolean renderingHud) {
        this.renderingHud = renderingHud;
    }

    public boolean isEditingCameraViewPosition() {
        return editingCameraViewPosition;
    }

    public PreviewMode getPreviewMode() {
        return previewMode;
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
