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

package fr.loudo.narrativecraft.client.editors.dialog;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.widgets.DialogPreviewEntry;
import fr.loudo.narrativecraft.client.editors.widgets.DialogPreviewPanel;
import fr.loudo.narrativecraft.client.editors.widgets.DialogSetupAdvancedPanel;
import fr.loudo.narrativecraft.client.screens.ClearScreen;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.dialog.DialogRenderer3D;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.network.BiStopEditorMaker;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.CustomFont;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.UtilsClient;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class ClientGlobalDialogEditorMaker implements EditorMaker {

    private static final String DEFAULT_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

    private final Minecraft minecraft = Minecraft.getInstance();
    private final ClientPlayerSession playerSession =
            ClientNarrativeCraftMod.getInstance().getPlayerSession();
    private final DialogData working =
            new DialogData(NarrativeCraftMod.getInstance().getGlobalDialogData());

    private DialogRenderer3D renderer;
    private Button closeButton;
    private Button saveButton;

    private final DialogPreviewPanel previewPanel = new DialogPreviewPanel(this::toggleAdvancedPanel, DEFAULT_TEXT);
    private final DialogSetupAdvancedPanel advancedPanel = new DialogSetupAdvancedPanel(previewPanel);

    @Override
    public void init() {
        closeButton = Button.builder(Component.literal(CustomFont.CROSS), b -> openCloseConfirm())
                .bounds(5, 5, 20, 20)
                .build();
        saveButton = Button.builder(Component.literal(CustomFont.SAVE), b -> save())
                .bounds(30, 5, 20, 20)
                .build();
        advancedPanel.setData(working);
    }

    public void registerEntityId(int entityId) {
        if (minecraft.level == null) return;
        Entity entity = minecraft.level.getEntity(entityId);
        if (entity == null) return;
        renderer = new DialogRenderer3D(working, entity);
        renderer.onStopped(() -> playerSession.removeDialog3D(renderer));
        renderer.start(DEFAULT_TEXT);
        playerSession.addDialog3D(renderer);
        previewPanel.setEntries(List.of(new DialogPreviewEntry("Global", working, renderer)));
    }

    private boolean stopping = false;

    @Override
    public void stop() {
        if (stopping) return;
        stopping = true;
        if (renderer != null) {
            renderer.stop();
            renderer = null;
        }
        minecraft.setScreen(null);
        Services.PACKET.sendToServer(BiStopEditorMaker.INSTANCE);
    }

    @Override
    public void tick() {
        advancedPanel.tick();
    }

    @Override
    public void teleportToEditorOrigin() {}

    @Override
    public NarrativeEnvironment getEnvironment() {
        return NarrativeEnvironment.DEVELOPMENT;
    }

    private void save() {
        NarrativeCraftMod.getInstance().setGlobalDialogData(working);
        try {
            NarrativeCraftMod.getInstance().getFile().saveGlobalDialogData(working);
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to save global dialog data", e);
        }
    }

    private void openCloseConfirm() {
        ConfirmScreen confirmScreen = new ConfirmScreen(
                accepted -> {
                    if (accepted) save();
                    stop();
                    playerSession.setEditor(null);
                },
                Translation.message("screen.confirm.title"),
                Translation.message("screen.confirm.save"));
        minecraft.setScreen(confirmScreen);
    }

    private void toggleAdvancedPanel(DialogData data) {
        if (advancedPanel.isVisible()) {
            advancedPanel.setVisible(false);
        } else {
            advancedPanel.setData(data);
            advancedPanel.setVisible(true);
        }
    }

    public void render(GuiGraphics graphics, float deltaTracker) {
        int[] mousePos = UtilsClient.getScaledMousePos();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        closeButton.setPosition(5, 5);
        saveButton.setPosition(closeButton.getX() + closeButton.getWidth() + 5, 5);

        closeButton.render(
                graphics, mousePos[0], mousePos[1], Minecraft.getInstance().getDeltaFrameTime());
        saveButton.render(
                graphics, mousePos[0], mousePos[1], Minecraft.getInstance().getDeltaFrameTime());

        previewPanel.render(graphics, screenWidth, screenHeight, mousePos[0], mousePos[1]);
        if (advancedPanel.isVisible()) {
            advancedPanel.render(graphics, mousePos[0], mousePos[1]);
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button, boolean isDoubleClick) {
        if (!clearScreenOpened()) return;
        if (advancedPanel.isVisible() && advancedPanel.mouseClicked(mouseX, mouseY, button)) {
            previewPanel.unfocusAll();
            return;
        }
        if (closeButton.mouseClicked(mouseX, mouseY, button)) return;
        if (saveButton.mouseClicked(mouseX, mouseY, button)) return;
        if (previewPanel.mouseClicked(mouseX, mouseY, button)) {
            advancedPanel.unfocusAll();
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (!clearScreenOpened()) return;
        previewPanel.mouseReleased();
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!clearScreenOpened()) return;
        previewPanel.mouseDragged((mouseY));
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        previewPanel.keyPressed(keyCode, scanCode, modifiers);
        if (previewPanel.isAnyBoxFocused()) return;
        if (advancedPanel.isVisible()) {
            advancedPanel.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public void charTyped(char codePoint, int modifiers) {
        previewPanel.charTyped(codePoint, modifiers);
        if (advancedPanel.isVisible()) {
            advancedPanel.charTyped(codePoint, modifiers);
        }
    }

    private boolean clearScreenOpened() {
        return minecraft.screen instanceof ClearScreen;
    }
}
