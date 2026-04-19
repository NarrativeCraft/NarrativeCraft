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

package fr.loudo.narrativecraft.client.editors.cameraangle;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.widgets.FovSliderWidget;
import fr.loudo.narrativecraft.client.editors.widgets.RollSliderWidget;
import fr.loudo.narrativecraft.client.screens.ClearScreen;
import fr.loudo.narrativecraft.client.screens.narrative.cameraangle.CameraAngleCameraNameScreen;
import fr.loudo.narrativecraft.client.screens.narrative.cameraangle.CameraAngleCharacterPickerScreen;
import fr.loudo.narrativecraft.client.screens.narrative.cameraangle.CameraAngleEditorMenuScreen;
import fr.loudo.narrativecraft.client.screens.narrative.cameraangle.CameraAngleTemplatePickerScreen;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.editors.Editor;
import fr.loudo.narrativecraft.narrative.cameraangle.*;
import fr.loudo.narrativecraft.narrative.character.CharacterType;
import fr.loudo.narrativecraft.network.cameraangle.*;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.CustomFont;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.UtilsClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientCameraAngleMakerEditor implements Editor {

    private static final int BUTTON_WIDTH = 20;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 10;

    private final Minecraft minecraft = Minecraft.getInstance();
    private final CameraAngle cameraAngle;
    private final ClientPlayerSession playerSession =
            ClientNarrativeCraftMod.getInstance().getPlayerSession();
    private final List<Button> buttons = new ArrayList<>();
    private final RollSliderWidget rollWidget = new RollSliderWidget();
    private final FovSliderWidget fovWidget = new FovSliderWidget();

    private CameraView previewCameraView;
    private boolean renderingHud = true;
    private boolean editingCameraViewPosition = false;

    public ClientCameraAngleMakerEditor(CameraAngle cameraAngle) {
        this.cameraAngle = cameraAngle;
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

        buttons.add(Button.builder(Component.literal("✖"), b -> openQuitConfirm())
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
    }

    public void tick() {
        if (previewCameraView != null && !editingCameraViewPosition) {
            LocalPlayer player = minecraft.player;
            player.setPos(previewCameraView.getPosition().subtract(0, player.getEyeHeight(), 0));
            player.setXRot((float) previewCameraView.getRotation().x);
            player.setYRot((float) previewCameraView.getRotation().y);
            player.setYHeadRot((float) previewCameraView.getRotation().y);
            player.connection.send(new ServerboundMovePlayerPacket.PosRot(
                    previewCameraView.getPosition(),
                    (float) previewCameraView.getRotation().x,
                    (float) previewCameraView.getRotation().y,
                    player.onGround(),
                    false));
        }
    }

    public void loadData(String json) {
        CameraAngleDeserializer.deserializeInto(json, cameraAngle);
    }

    public void addCharacterPlacementFromJson(String placementJson) {
        CharacterPlacement placement =
                CameraAngleDeserializer.deserializeCharacterPlacementFromJson(placementJson, cameraAngle.getScene());
        if (placement != null) {
            cameraAngle.getCharacterPlacements().add(placement);
        }
    }

    private void openAddCameraScreen() {
        minecraft.setScreen(new CameraAngleCameraNameScreen(
                Translation.message("screen.camera_angle_editor.camera_name_prompt"),
                "",
                null,
                this::createCameraFromPlayer,
                minecraft.screen));
    }

    private void createCameraFromPlayer(String name) {
        if (minecraft.player == null) return;
        if (cameraAngle.getCameraByName(name) != null) {
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
        cameraAngle.getCameras().add(new CameraView(name, position, rotation, fov));
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

    public void recaptureCamera(CameraView cameraView) {
        if (minecraft.player == null) return;
        cameraView.setPosition(minecraft.player.position());
        cameraView.setRotation(new Vec3(minecraft.player.getXRot(), minecraft.player.getYRot(), cameraView.getRoll()));
    }

    public void removeCamera(CameraView cameraView) {
        if (previewCameraView == cameraView) exitPreview();
        cameraAngle.getCameras().remove(cameraView);
    }

    public void removeCharacterPlacement(CharacterPlacement placement) {
        cameraAngle.getCharacterPlacements().remove(placement);
        Services.PACKET.sendToServer(new C2SCameraAngleRemovePlacement(cameraAngle, placement.getId()));
    }

    public void removeTemplateReference(TemplateReference reference) {
        cameraAngle.getTemplateReferences().remove(reference);
        Services.PACKET.sendToServer(new C2SCameraAngleRemoveTemplateReference(cameraAngle, reference.id()));
    }

    private void openCharacterPicker() {
        minecraft.setScreen(new CameraAngleCharacterPickerScreen(cameraAngle.getScene(), minecraft.screen, pick -> {
            Services.PACKET.sendToServer(new C2SCameraAngleCaptureCharacter(
                    cameraAngle.getScene().getChapter().getId(),
                    cameraAngle.getScene().getId(),
                    cameraAngle.getId(),
                    pick.characterId()));
        }));
    }

    private void openTemplatePicker() {
        minecraft.setScreen(new CameraAngleTemplatePickerScreen(cameraAngle.getScene(), minecraft.screen, pick -> {
            addTemplateReference(pick.sourceType(), pick.refId());
        }));
    }

    public void addTemplateReference(TemplateSourceType sourceType, UUID refId) {
        TemplateReference reference = new TemplateReference(sourceType, refId);
        cameraAngle.getTemplateReferences().add(reference);
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
                    Services.PACKET.sendToServer(new C2SCameraAngleControl(C2SCameraAngleControl.State.QUIT));
                    playerSession.setEditor(null);
                    exitPreview();
                    minecraft.setScreen(null);
                },
                Translation.message("screen.confirm.title"),
                Translation.message("screen.confirm.save"));
        minecraft.setScreen(confirmScreen);
    }

    private void save() {
        String dataJson = CameraAngleSerializer.serializeData(cameraAngle);
        Services.PACKET.sendToServer(new C2SCameraAngleSave(cameraAngle, dataJson));
    }

    public void enterPreview(CameraView cameraView) {
        this.previewCameraView = cameraView;
        minecraft.player.connection.send(new ServerboundChangeGameModePacket(GameType.SPECTATOR));
        playerSession.setCameraView(cameraView);
        Vec3 position = cameraView.getPosition();
        Vec3 rotation = cameraView.getRotation();
        LocalPlayer player = minecraft.player;
        player.setPos(position);
        player.setXRot((float) rotation.x);
        player.setYRot((float) rotation.y);
        player.setYHeadRot((float) rotation.y);
        player.connection.send(new ServerboundMovePlayerPacket.PosRot(
                position, (float) rotation.x, (float) rotation.y, player.onGround(), false));

        rollWidget.setValue(cameraView.getRoll());
        rollWidget.setVisible(true);
        fovWidget.setValue(cameraView.getFov());
        fovWidget.setVisible(true);
    }

    public void exitPreview() {
        this.previewCameraView = null;
        playerSession.setCameraView(null);
        rollWidget.setVisible(false);
        fovWidget.setVisible(false);
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

    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
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
                b.extractRenderState(graphics, mousePos[0], mousePos[1], deltaTracker.getGameTimeDeltaTicks());
            }
            quitButton.extractRenderState(graphics, mousePos[0], mousePos[1], deltaTracker.getGameTimeDeltaTicks());
            saveButton.extractRenderState(graphics, mousePos[0], mousePos[1], deltaTracker.getGameTimeDeltaTicks());
        } else if (!editingCameraViewPosition) {
            leavePreviewButton.setPosition(5, 5);
            editPositionButton.setPosition(leavePreviewButton.getX() + leavePreviewButton.getWidth() + 5, 5);

            leavePreviewButton.extractRenderState(
                    graphics, mousePos[0], mousePos[1], deltaTracker.getGameTimeDeltaTicks());
            editPositionButton.extractRenderState(
                    graphics, mousePos[0], mousePos[1], deltaTracker.getGameTimeDeltaTicks());
        } else {
            acceptPositionButton.setPosition(5, 5);
            acceptPositionButton.extractRenderState(
                    graphics, mousePos[0], mousePos[1], deltaTracker.getGameTimeDeltaTicks());

            stopPositionButton.setPosition(acceptPositionButton.getX() + acceptPositionButton.getWidth() + 5, 5);
            stopPositionButton.extractRenderState(
                    graphics, mousePos[0], mousePos[1], deltaTracker.getGameTimeDeltaTicks());
        }

        rollWidget.render(graphics, screenWidth, screenHeight, mousePos[0], mousePos[1]);
        fovWidget.render(graphics, screenWidth, screenHeight, mousePos[0], mousePos[1]);
    }

    public void mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!renderingHud || !clearScreenOpened()) return;
        if (rollWidget.mouseClicked(event)) return;
        if (fovWidget.mouseClicked(event)) return;

        boolean inPreview = previewCameraView != null;

        if (!inPreview) {
            for (int i = 0; i < 6; i++) {
                buttons.get(i).mouseClicked(event, isDoubleClick);
            }
        } else if (!editingCameraViewPosition) {
            buttons.get(6).mouseClicked(event, isDoubleClick);
            buttons.get(7).mouseClicked(event, isDoubleClick);
        } else {
            buttons.get(8).mouseClicked(event, isDoubleClick);
            buttons.get(9).mouseClicked(event, isDoubleClick);
        }
    }

    public void mouseReleased(MouseButtonEvent event) {
        if (!renderingHud || !clearScreenOpened()) return;
        rollWidget.mouseReleased();
        fovWidget.mouseReleased();
    }

    public void mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (!renderingHud || !clearScreenOpened()) return;
        if (rollWidget.mouseDragged(event.y())) return;
        fovWidget.mouseDragged(event.y());
    }

    private boolean clearScreenOpened() {
        return minecraft.screen != null && minecraft.screen instanceof ClearScreen;
    }

    public void keyPressed(KeyEvent event) {}

    public void charTyped(CharacterEvent event) {}

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

    public record CharacterPick(CharacterType type, UUID characterId) {}

    public record TemplatePick(TemplateSourceType sourceType, UUID refId) {}
}
