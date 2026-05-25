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

package fr.loudo.narrativecraft.client.editors.dialog;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.widgets.DialogFieldSet;
import fr.loudo.narrativecraft.client.editors.widgets.DialogPreviewEntry;
import fr.loudo.narrativecraft.client.editors.widgets.DialogPreviewPanel;
import fr.loudo.narrativecraft.client.editors.widgets.DialogSetupAdvancedPanel;
import fr.loudo.narrativecraft.client.screens.ClearScreen;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.dialog.DialogRenderer3D;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.network.BiStopEditorMaker;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.CustomFont;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.UtilsClient;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class ClientCharacterDialogEditorMaker implements EditorMaker {

    private static final String DEFAULT_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

    private final Minecraft minecraft = Minecraft.getInstance();
    private final ClientPlayerSession playerSession =
            ClientNarrativeCraftMod.getInstance().getPlayerSession();
    private final ICharacterStory character;
    private final Runnable onSave;
    private final DialogData working;

    private DialogRenderer3D renderer;
    private DialogData rendererData;
    private Button closeButton;
    private Button saveButton;

    private final DialogPreviewPanel previewPanel = new DialogPreviewPanel(this::toggleAdvancedPanel, DEFAULT_TEXT);
    private final DialogSetupAdvancedPanel advancedPanel = new DialogSetupAdvancedPanel(previewPanel);

    public ClientCharacterDialogEditorMaker(ICharacterStory character, Runnable onSave) {
        this.character = character;
        this.onSave = onSave;
        this.working = new DialogData(character.getDialogData());
    }

    @Override
    public void init() {
        closeButton = Button.builder(Component.literal("✖"), b -> openCloseConfirm())
                .bounds(5, 5, 20, 20)
                .build();
        saveButton = Button.builder(Component.literal(CustomFont.SAVE), b -> save())
                .bounds(30, 5, 20, 20)
                .build();
        previewPanel.setFieldSet(DialogFieldSet.CHARACTER);
        advancedPanel.setFieldSet(DialogFieldSet.CHARACTER);
        advancedPanel.setData(working);
    }

    public void registerEntityId(int entityId) {
        if (minecraft.level == null) return;
        Entity entity = minecraft.level.getEntity(entityId);
        if (entity == null) return;
        rendererData =
                new DialogData(DialogData.from(NarrativeCraftMod.getInstance().getGlobalDialogData(), working));
        renderer = new DialogRenderer3D(rendererData, entity);
        renderer.onStopped(() -> playerSession.removeDialog3D(renderer));
        renderer.start(DEFAULT_TEXT);
        playerSession.addDialog3D(renderer);
        previewPanel.setEntries(List.of(new DialogPreviewEntry(character.getName(), working, renderer)));
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
        character.setDialogData(working);
        onSave.run();
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

    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (rendererData != null) {
            rendererData.copyFrom(
                    DialogData.from(NarrativeCraftMod.getInstance().getGlobalDialogData(), working));
        }
        int[] mousePos = UtilsClient.getScaledMousePos();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        closeButton.setPosition(5, 5);
        saveButton.setPosition(closeButton.getX() + closeButton.getWidth() + 5, 5);

        closeButton.extractRenderState(graphics, mousePos[0], mousePos[1], deltaTracker.getGameTimeDeltaTicks());
        saveButton.extractRenderState(graphics, mousePos[0], mousePos[1], deltaTracker.getGameTimeDeltaTicks());

        previewPanel.render(graphics, screenWidth, screenHeight, mousePos[0], mousePos[1]);
        if (advancedPanel.isVisible()) {
            advancedPanel.render(graphics, mousePos[0], mousePos[1]);
        }
    }

    public void mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!clearScreenOpened()) return;
        if (advancedPanel.isVisible() && advancedPanel.mouseClicked(event)) {
            previewPanel.unfocusAll();
            return;
        }
        if (closeButton.mouseClicked(event, isDoubleClick)) return;
        if (saveButton.mouseClicked(event, isDoubleClick)) return;
        if (previewPanel.mouseClicked(event)) {
            advancedPanel.unfocusAll();
        }
    }

    public void mouseReleased(MouseButtonEvent event) {
        if (!clearScreenOpened()) return;
        previewPanel.mouseReleased();
    }

    public void mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (!clearScreenOpened()) return;
        previewPanel.mouseDragged(event.y());
    }

    public void keyPressed(KeyEvent event) {
        previewPanel.keyPressed(event);
        if (previewPanel.isAnyBoxFocused()) return;
        if (advancedPanel.isVisible()) {
            advancedPanel.keyPressed(event);
        }
    }

    public void charTyped(CharacterEvent event) {
        previewPanel.charTyped(event);
        if (advancedPanel.isVisible()) {
            advancedPanel.charTyped(event);
        }
    }

    private boolean clearScreenOpened() {
        return minecraft.screen instanceof ClearScreen;
    }
}
