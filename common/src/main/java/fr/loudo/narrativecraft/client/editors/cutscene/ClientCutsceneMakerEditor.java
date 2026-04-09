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

package fr.loudo.narrativecraft.client.editors.cutscene;

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.KeyframeMenu;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayer;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.editors.Editor;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.network.cutscene.BiCutscenePlayHeadPacket;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneControl;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.UtilsClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import java.util.ArrayList;
import java.util.List;

/**
 * The main container of CutsceneEditor but for the client, it handles all the rendering and server communication.
 */
public class ClientCutsceneMakerEditor implements Editor {

    private static final int LAYER_HEIGHT = 20;
    private static final int LAYER_GAP = 85;
    private static final int LAYERS_START_Y_OFFSET = 80;
    private static final int ADD_NEW_LAYER_BUTTON_OFFSET = 13;

    private final Minecraft mc = Minecraft.getInstance();
    private final List<CutsceneMakerEditorLayer> editorLayers = new ArrayList<>();
    private final Cutscene cutscene;
    private final ClientPlayerSession playerSession =
            ClientNarrativeCraftMod.getInstance().getPlayerSession();
    private final List<Button> buttons = new ArrayList<>();
    private final CutsceneMakerEditorLayerSelector layerSelector =
            new CutsceneMakerEditorLayerSelector(this, 90, 120, ARGB.color(190, 0, 0, 0));
    private final CutsceneMakerEditorPlayHead playHead = new CutsceneMakerEditorPlayHead(11, 90, 5);
    private final CutsceneMakerEditorControl control;
    private final CutsceneEditorPlayback playback;

    private Button addLayerButton;
    private int tick, totalTick;
    private int scrollOffset = 0;
    private Keyframe selectedKeyframe;
    private Keyframe draggingKeyframe;
    private KeyframeMenu<?> openMenu;

    public ClientCutsceneMakerEditor(Cutscene cutscene) {
        this.cutscene = cutscene;
        this.control = new CutsceneMakerEditorControl(15, 15);
        this.playback = new CutsceneEditorPlayback(editorLayers, playerSession, cutscene.getMaxTick());
        control.setPlaybackCallbacks(() -> playback.play(tick), playback::pause);
    }

    public void init() {
        buttons.add(Button.builder(Component.literal("✖"), button -> {
                    Services.PACKET.sendToServer(new C2SCutsceneControl(C2SCutsceneControl.State.QUIT));
                    playerSession.getCutsceneDataSession().reset();
                    playerSession.setEditor(null);
                })
                .bounds(5, 5, 20, 20)
                .build());

        addLayerButton = Button.builder(Component.literal("+"), b -> layerSelector.toggle())
                .bounds(0, 0, 10, 10)
                .build();
        buttons.add(addLayerButton);
        totalTick = cutscene.getMaxTick();
    }

    public void addLayer(ICutsceneLayer layer) {
        if (layer instanceof CutsceneLayer cutsceneLayer) {
            editorLayers.add(new CutsceneMakerEditorLayer(cutsceneLayer, LAYER_GAP));
            rebuildSortIndices();
        }
    }

    public void removeLayer(ICutsceneLayer layer) {
        editorLayers.removeIf(el -> el.getLayer() == layer);
    }

    private void renderLayers(GuiGraphicsExtractor graphics, DeltaTracker delta, int mouseX, int mouseY) {
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int layerStartY = getStartLayerY();
        int currentY = layerStartY - scrollOffset;
        int timelineWidth = getTimelineWidth();
        int maxTick = getTotalTick();

        graphics.fill(0, layerStartY, screenWidth, screenHeight + LAYER_HEIGHT, ARGB.color(0.8f, 0));
        graphics.fill(LAYER_GAP, layerStartY, LAYER_GAP + 1, screenHeight, 0xFFFFFFFF);

        for (int i = 0; i < editorLayers.size(); i++) {
            CutsceneMakerEditorLayer editorLayer = editorLayers.get(i);
            int layerBottom = currentY + LAYER_HEIGHT;
            if (currentY >= layerStartY && currentY < screenHeight) {
                graphics.fill(0, layerBottom - 1, screenWidth, layerBottom, 0xFFFFFFFF);
                graphics.text(
                        mc.font,
                        editorLayer.getLayer().getType().getName(),
                        CutsceneMakerEditorLayer.NAME_X,
                        currentY + (mc.font.lineHeight / 2) + 1,
                        0xFFFFFFFF);
                editorLayer.render(
                        graphics,
                        delta,
                        currentY,
                        LAYER_HEIGHT,
                        timelineWidth,
                        maxTick,
                        mouseX,
                        mouseY,
                        i == 0,
                        i == editorLayers.size() - 1);
            }
            currentY += LAYER_HEIGHT;
        }

        int totalLayerHeight = editorLayers.size() * LAYER_HEIGHT;
        int maxScroll = totalLayerHeight - LAYERS_START_Y_OFFSET;
        if (maxScroll > 0) {
            int indicatorHeight = LAYERS_START_Y_OFFSET * LAYERS_START_Y_OFFSET / totalLayerHeight;
            int indicatorTop = (int) ((float) scrollOffset / maxScroll * (LAYERS_START_Y_OFFSET - indicatorHeight));
            graphics.fill(0, layerStartY + indicatorTop, 1, layerStartY + indicatorTop + indicatorHeight, 0xFFFFFFFF);
        }
    }

    private int getStartLayerY() {
        return mc.getWindow().getGuiScaledHeight() - LAYERS_START_Y_OFFSET;
    }

    private int getTimelineWidth() {
        return mc.getWindow().getGuiScaledWidth() - LAYER_GAP;
    }

    public void mouseScrolled(double deltaX, double deltaY) {
        if (openMenu != null && openMenu.isVisible()) {
            openMenu.mouseScrolled(deltaY);
            return;
        }
        layerSelector.mouseScrolled(deltaY);
        int[] mousePos = UtilsClient.getScaledMousePos();
        if (mousePos[1] < getStartLayerY()) return;
        int maxScroll = Math.max(0, editorLayers.size() * LAYER_HEIGHT - LAYERS_START_Y_OFFSET);
        scrollOffset = (int) Math.clamp(scrollOffset - deltaY * LAYER_HEIGHT, 0, maxScroll);
    }

    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        int[] mousePos = UtilsClient.getScaledMousePos();
        int addLayerY = getStartLayerY() - ADD_NEW_LAYER_BUTTON_OFFSET;

        addLayerButton.setPosition(2, addLayerY);
        layerSelector.setPosition(2, addLayerY - 3);

        renderLayers(graphics, deltaTracker, mousePos[0], mousePos[1]);

        for (Button button : buttons) {
            button.extractRenderState(graphics, mousePos[0], mousePos[1], deltaTracker.getGameTimeDeltaTicks());
        }
        layerSelector.render(graphics, deltaTracker);

        control.setPosition(LAYER_GAP / 2 - control.getWidth() / 2, getStartLayerY() - control.getHeight() - 2);
        control.render(graphics, deltaTracker, mousePos[0], mousePos[1]);

        playback.tick(deltaTracker);
        if (playback.isPlaying()) {
            playHead.setRatio(playback.getCurrentTick() / totalTick);
        }

        playHead.setY(getStartLayerY() - 5);
        playHead.render(graphics, mousePos[0], mousePos[1], LAYER_GAP, getTimelineWidth());

        if (openMenu != null && openMenu.isVisible()) {
            openMenu.render(graphics, deltaTracker, mousePos[0], mousePos[1]);
        }
    }

    public void charTyped(CharacterEvent event) {
        if (openMenu != null && openMenu.isVisible()) {
            openMenu.charTyped(event);
        }
    }

    public void keyPressed(KeyEvent event) {
        if (openMenu != null && openMenu.isVisible()) {
            openMenu.keyPressed(event);
        }
    }

    public void mouseClicked(MouseButtonEvent mouseButtonEvent, boolean isDoubleClick) {
        int[] mousePos = UtilsClient.getScaledMousePos();

        if (openMenu != null && openMenu.isVisible()) {
            if (openMenu.mouseClicked(mouseButtonEvent, isDoubleClick)) return;
        }
        boolean onAddLayerButton = addLayerButton.isMouseOver(mousePos[0], mousePos[1]);
        for (Button button : buttons) {
            button.mouseClicked(mouseButtonEvent, isDoubleClick);
        }
        if (!onAddLayerButton) {
            layerSelector.mouseClicked(mouseButtonEvent, isDoubleClick);
        }
        control.mouseClicked(mouseButtonEvent, isDoubleClick);

        // Check keyframes and layer buttons before the playhead to avoid conflicts
        int layerStartY = getStartLayerY();
        int currentY = layerStartY - scrollOffset;
        List<CutsceneMakerEditorLayer> toRemove = new ArrayList<>();
        for (int i = 0; i < editorLayers.size(); i++) {
            CutsceneMakerEditorLayer editorLayer = editorLayers.get(i);
            if (currentY >= layerStartY && currentY < mc.getWindow().getGuiScaledHeight()) {
                if (i > 0 && editorLayer.isMoveUpButtonHovered(mousePos[0], mousePos[1], currentY)) {
                    CutsceneMakerEditorLayer tmp = editorLayers.get(i - 1);
                    editorLayers.set(i - 1, editorLayer);
                    editorLayers.set(i, tmp);
                    rebuildSortIndices();
                    return;
                }
                if (i < editorLayers.size() - 1
                        && editorLayer.isMoveDownButtonHovered(mousePos[0], mousePos[1], currentY)) {
                    CutsceneMakerEditorLayer tmp = editorLayers.get(i + 1);
                    editorLayers.set(i + 1, editorLayer);
                    editorLayers.set(i, tmp);
                    rebuildSortIndices();
                    return;
                }
                if (editorLayer.isAddButtonHovered(mousePos[0], mousePos[1], currentY, LAYER_HEIGHT)) {
                    editorLayer.addKeyframe((int) (playHead.getRatio() * getTotalTick()));
                    return;
                }
                if (editorLayer.isRemoveButtonHovered(mousePos[0], mousePos[1], currentY, LAYER_HEIGHT)) {
                    toRemove.add(editorLayer);
                }
                Keyframe hovered = editorLayer.getHoveredKeyframe(mousePos[0], mousePos[1]);
                if (hovered != null) {
                    selectKeyframe(hovered, mouseButtonEvent, isDoubleClick);
                    draggingKeyframe = hovered;
                    return;
                }
            }
            currentY += LAYER_HEIGHT;
        }
        editorLayers.removeAll(toRemove);

        // Deselect keyframe when clicking elsewhere
        if (selectedKeyframe != null) {
            selectedKeyframe.setSelected(false);
            selectedKeyframe = null;
            if (openMenu != null && openMenu.isVisible()) {
                openMenu.close();
            }
        }

        if (playHead.isHovered()) {
            playHead.setDragging(true);
        } else {
            playHead.onClick(mouseButtonEvent, LAYER_GAP, getTimelineWidth(), getStartLayerY());
            updateTick();
        }
    }

    public void mouseReleased(MouseButtonEvent mouseButtonEvent) {
        playHead.setDragging(false);
        draggingKeyframe = null;
    }

    public void mouseDragged(MouseButtonEvent mouseButtonEvent, double dragX, double dragY) {
        if (draggingKeyframe != null) {
            draggingKeyframe.drag(mouseButtonEvent.x(), LAYER_GAP, getTimelineWidth(), getTotalTick());
            return;
        }
        if (playHead.isDragging()) {
            playHead.onMouseDrag(mouseButtonEvent.x(), LAYER_GAP, getTimelineWidth());
            updateTick();
            return;
        }
        if (openMenu != null && openMenu.isVisible()) {
            openMenu.mouseDragged(mouseButtonEvent, dragX, dragY);
        }
    }

    private void rebuildSortIndices() {
        for (int i = 0; i < editorLayers.size(); i++) {
            editorLayers.get(i).getLayer().setSortIndex(i);
        }
    }

    private void updateTick() {
        tick = (int) (playHead.getRatio() * totalTick);
        playback.seekTo(tick);
        Services.PACKET.sendToServer(new BiCutscenePlayHeadPacket(tick));
    }

    private void selectKeyframe(Keyframe keyframe, MouseButtonEvent event, boolean isDoubleClick) {
        if (selectedKeyframe != null && selectedKeyframe != keyframe) {
            selectedKeyframe.setSelected(false);
        }
        selectedKeyframe = keyframe;
        keyframe.click(event, isDoubleClick);

        if (openMenu != null) openMenu.close();
        openMenu = keyframe.createMenu();
    }

    public int getTick() {
        return tick;
    }

    public int getTotalTick() {
        return totalTick;
    }

    public Cutscene getCutscene() {
        return cutscene;
    }

    public CutsceneMakerEditorLayerSelector getLayerSelector() {
        return layerSelector;
    }

    public CutsceneMakerEditorPlayHead getPlayHead() {
        return playHead;
    }

    public CutsceneMakerEditorControl getControl() {
        return control;
    }
}
