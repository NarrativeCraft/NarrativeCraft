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

package fr.loudo.narrativecraft.client.editors.cutscene;

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.KeyframeMenu;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayer;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.widgets.RollSliderWidget;
import fr.loudo.narrativecraft.client.screens.InputScreen;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.cutscene.CutsceneDeserializer;
import fr.loudo.narrativecraft.narrative.cutscene.CutsceneSerializer;
import fr.loudo.narrativecraft.network.cutscene.BiCutscenePlayHeadPacket;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneControl;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneSave;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.CustomFont;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.UtilsClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;

/**
 * The main container of CutsceneEditor but for the client, it handles all the rendering and server communication.
 */
public class ClientCutsceneMakerEditorMaker implements EditorMaker {

    private static final int LAYER_HEIGHT = 20;
    private static final int LAYER_GAP = 85;
    private static final int LAYERS_START_Y_OFFSET = 80;
    private static final int RULER_HEIGHT = 10;
    private static final int TICKS_PER_SECOND = 20;
    private static final float MIN_PHYSICAL_PIXELS_PER_SECOND = 5 * TICKS_PER_SECOND;
    private static final int MIN_VISIBLE_TICKS_AT_MAX_ZOOM = 2 * TICKS_PER_SECOND;
    private static final int SCROLLBAR_HEIGHT = 4;
    private static final int ADD_NEW_LAYER_BUTTON_OFFSET = 13;
    private static final int MIN_MAJOR_LABEL_SPACING = 45;
    private static final int MIN_MINOR_TICK_SPACING = 6;
    private static final int[] NICE_STEP_SECONDS = {1, 2, 5, 10, 15, 30, 60, 120, 300, 600};

    private final Minecraft mc = Minecraft.getInstance();
    private final List<CutsceneMakerEditorLayer> editorLayers = new ArrayList<>();
    private final Cutscene cutscene;
    private final ClientPlayerSession playerSession =
            ClientNarrativeCraftMod.getInstance().getPlayerSession();
    private final List<Button> buttons = new ArrayList<>();
    private final CutsceneMakerEditorLayerSelector layerSelector =
            new CutsceneMakerEditorLayerSelector(this, 90, 120, FastColor.ARGB32.color(190, 0, 0, 0));
    private final CutsceneMakerEditorPlayHead playHead = new CutsceneMakerEditorPlayHead(11, 90, 5);
    private final CutsceneMakerEditorControl control;
    private final CutsceneEditorPlayback playback;
    private final RollSliderWidget rollWidget = new RollSliderWidget();
    private final CutsceneMakerEditorShortcuts shortcuts = new CutsceneMakerEditorShortcuts(this);
    private final NarrativeEnvironment environment;

    private final List<Keyframe> selectedKeyframes = new ArrayList<>();
    private KeyframeMenu<?> openMenu;

    private Map<Keyframe, Integer> draggingOriginalTicks;
    private float draggingStartMouseX;

    private Button addLayerButton;
    private int totalTick;
    private int scrollOffset = 0;
    private float zoomFactor = 1f;
    private float viewStartTick = 0f;
    private boolean scrollbarDragging = false;
    private float scrollbarDragStartMouseX = 0f;
    private float scrollbarDragStartViewTick = 0f;
    private boolean renderingHud = true;
    private boolean wasPlaying = false;

    public ClientCutsceneMakerEditorMaker(Cutscene cutscene, NarrativeEnvironment environment) {
        this.cutscene = cutscene;
        this.control = new CutsceneMakerEditorControl(15, 15);
        this.playback = new CutsceneEditorPlayback(editorLayers, playerSession, cutscene.getLastTick());
        control.setPlaybackCallbacks(this::startPlayback, () -> {
            playback.pause();
            setPreviewRoll(0.0f);
        });
        this.environment = environment;
    }

    public void init() {
        buttons.add(Button.builder(Component.literal(CustomFont.CROSS), button -> {
                    ConfirmScreen confirmScreen = new ConfirmScreen(
                            b -> {
                                if (b) {
                                    String layersJson = CutsceneSerializer.serializeLayers(editorLayers);
                                    Services.PACKET.sendToServer(new C2SCutsceneSave(cutscene, layersJson));
                                }
                                Services.PACKET.sendToServer(new C2SCutsceneControl(C2SCutsceneControl.State.QUIT));
                                playerSession.closeEditor();
                            },
                            Translation.message("screen.confirm.title"),
                            Translation.message("screen.confirm.save"));
                    mc.setScreen(confirmScreen);
                })
                .bounds(5, 5, 20, 20)
                .build());
        buttons.add(Button.builder(Component.literal(CustomFont.SAVE), button -> {
                    String layersJson = CutsceneSerializer.serializeLayers(editorLayers);
                    Services.PACKET.sendToServer(new C2SCutsceneSave(cutscene, layersJson));
                })
                .bounds(30, 5, 20, 20)
                .build());

        if (cutscene.getAnimations().isEmpty() && cutscene.getSubscenes().isEmpty()) {
            buttons.add(Button.builder(
                            Component.literal("⏱"),
                            button -> mc.setScreen(new InputScreen(
                                    Translation.message("screen.set_max_tick.title"),
                                    raw -> {
                                        try {
                                            int value = Integer.parseInt(raw);
                                            cutscene.setManualMaxTick(value);
                                            totalTick = value;
                                            playback.setTotalTick(value);
                                            zoomFactor = getMinZoomFactor();
                                            clampViewStart();
                                            mc.setScreen(null);
                                        } catch (NumberFormatException e) {
                                            UtilsClient.sendToast(
                                                    Translation.message("error"),
                                                    Translation.message("error.only_numbers"));
                                        }
                                    },
                                    null)))
                    .bounds(55, 5, 20, 20)
                    .build());
        }

        addLayerButton = Button.builder(Component.literal("+"), b -> layerSelector.toggle())
                .bounds(0, 0, 10, 10)
                .build();
        buttons.add(addLayerButton);
        totalTick = cutscene.getMaxTick();
        zoomFactor = getMinZoomFactor();
        viewStartTick = 0f;

        if (environment == NarrativeEnvironment.PRODUCTION) {
            control.play();
            startProductionPlayback();
        }
    }

    private void startProductionPlayback() {
        if (environment != NarrativeEnvironment.PRODUCTION) return;
        int firstCameraTick = cutscene.getFirstCameraTick();
        playback.play(firstCameraTick < totalTick ? firstCameraTick : 0);
    }

    public void applyManualMaxTick(int value) {
        if (value <= 0) return;
        cutscene.setManualMaxTick(value);
        totalTick = value;
        playback.setTotalTick(value);
        zoomFactor = getMinZoomFactor();
        clampViewStart();
    }

    @Override
    public void close() {
        playback.pause();
        playerSession.getCutsceneDataSession().reset();
        for (CutsceneMakerEditorLayer layer : editorLayers) {
            layer.getLayer().stop();
        }
        if (environment != NarrativeEnvironment.DEVELOPMENT) return;
        mc.setScreen(null);
        playerSession.stopAllClientInkActions();
    }

    public void tick() {
        boolean isPlaying = playback.isPlaying();
        if (isPlaying != wasPlaying) {
            Minecraft.getInstance().options.hideGui = isPlaying;
            wasPlaying = isPlaying;
        }
    }

    @Override
    public void teleportToEditorOrigin() {}

    public void loadLayers(String layersJson) {
        editorLayers.clear();
        CutsceneDeserializer.deserializeLayers(layersJson, cutscene);
        if (cutscene.getEditorLayers() != null) {
            editorLayers.addAll(cutscene.getEditorLayers());
        }
        totalTick = cutscene.getMaxTick();
        playback.setTotalTick(totalTick);
        startProductionPlayback();
    }

    public void addLayer(CutsceneLayer layer) {
        editorLayers.add(new CutsceneMakerEditorLayer(layer, LAYER_GAP));
        rebuildSortIndices();
    }

    public void toggleHud() {
        renderingHud = !renderingHud;
    }

    public void removeLayer(ICutsceneLayer layer) {
        editorLayers.removeIf(el -> el.getLayer() == layer);
    }

    public void clearSelection() {
        for (Keyframe keyframe : selectedKeyframes) {
            keyframe.setSelected(false);
        }
        selectedKeyframes.clear();
        if (openMenu != null && openMenu.isVisible()) {
            openMenu.close();
        }
    }

    private void renderLayers(GuiGraphics graphics, DeltaTracker delta, int mouseX, int mouseY) {
        if (environment != NarrativeEnvironment.DEVELOPMENT) return;
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int layerStartY = getStartLayerY();
        int layersAreaStartY = getLayersAreaStartY();
        int currentY = layersAreaStartY - scrollOffset;
        int timelineWidth = getTimelineWidth();
        float visibleTicks = getVisibleTicks();
        int viewportHeight = LAYERS_START_Y_OFFSET - RULER_HEIGHT;

        graphics.fill(
                0, layerStartY, screenWidth, screenHeight + LAYER_HEIGHT, FastColor.ARGB32.color((int) (0.8 * 255), 0));
        graphics.fill(LAYER_GAP, layerStartY, LAYER_GAP + 1, screenHeight, 0xFFFFFFFF);

        renderRuler(graphics, screenWidth, screenHeight, layerStartY, timelineWidth, visibleTicks);

        for (int i = 0; i < editorLayers.size(); i++) {
            CutsceneMakerEditorLayer editorLayer = editorLayers.get(i);
            int layerBottom = currentY + LAYER_HEIGHT;
            if (currentY >= layersAreaStartY && currentY < screenHeight) {
                graphics.fill(0, layerBottom - 1, screenWidth, layerBottom, 0xFFFFFFFF);
                graphics.drawString(
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
                        visibleTicks,
                        viewStartTick,
                        mouseX,
                        mouseY,
                        i == 0,
                        i == editorLayers.size() - 1);
            }
            currentY += LAYER_HEIGHT;
        }

        int totalLayerHeight = editorLayers.size() * LAYER_HEIGHT;
        int maxScroll = totalLayerHeight - viewportHeight;
        if (maxScroll > 0) {
            int indicatorHeight = viewportHeight * viewportHeight / totalLayerHeight;
            int indicatorTop = (int) ((float) scrollOffset / maxScroll * (viewportHeight - indicatorHeight));
            graphics.fill(
                    0,
                    layersAreaStartY + indicatorTop,
                    1,
                    layersAreaStartY + indicatorTop + indicatorHeight,
                    0xFFFFFFFF);
        }

        renderScrollbar(graphics, screenHeight, timelineWidth);
    }

    private void renderRuler(
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight,
            int rulerY,
            int timelineWidth,
            float visibleTicks) {
        if (environment != NarrativeEnvironment.DEVELOPMENT) return;
        if (totalTick <= 0 || timelineWidth <= 0 || visibleTicks <= 0) return;

        int rulerEndY = rulerY + RULER_HEIGHT;
        float pixelsPerTick = timelineWidth / visibleTicks;

        int currentTick = (int) playback.getCurrentTick();
        String tickDisplay = currentTick + " / " + formatTimeTicks(currentTick);
        graphics.drawString(
                mc.font,
                Component.literal(tickDisplay),
                LAYER_GAP / 2 - Minecraft.getInstance().font.width(tickDisplay) / 2,
                rulerY + 3,
                0xFFFFFFFF);

        int majorStep = getNiceMajorStep(pixelsPerTick);
        int minorStep = getNiceMinorStep(majorStep, pixelsPerTick);

        int rangeEnd = Math.min(totalTick, (int) Math.ceil(viewStartTick + visibleTicks));
        int firstMinor = Math.max(0, (int) viewStartTick / minorStep * minorStep);

        for (int tickMark = firstMinor; tickMark <= rangeEnd; tickMark += minorStep) {
            if (tickMark < 0 || tickMark > totalTick) continue;
            float x = LAYER_GAP + (tickMark - viewStartTick) * pixelsPerTick;
            if (x < LAYER_GAP || x > LAYER_GAP + timelineWidth) continue;
            int xi = (int) x;

            if (tickMark % majorStep == 0) {
                graphics.fill(xi, rulerEndY, xi + 1, screenHeight, 0xFFA0A0A0);

                String label = formatTimeTicks(tickMark);
                int labelWidth = mc.font.width(label);
                int labelX = Math.clamp(xi - labelWidth / 2, LAYER_GAP, LAYER_GAP + timelineWidth - labelWidth);
                graphics.drawString(mc.font, Component.literal(label), labelX, rulerY + 1, 0xFFFFFFFF);
            } else {
                graphics.fill(xi, rulerEndY, xi + 1, screenHeight, 0x18AAAAAA);
            }
        }
    }

    private int getNiceMajorStep(float pixelsPerTick) {
        for (int seconds : NICE_STEP_SECONDS) {
            int stepTicks = seconds * TICKS_PER_SECOND;
            if (stepTicks * pixelsPerTick >= MIN_MAJOR_LABEL_SPACING) {
                return stepTicks;
            }
        }
        return NICE_STEP_SECONDS[NICE_STEP_SECONDS.length - 1] * TICKS_PER_SECOND;
    }

    private int getNiceMinorStep(int majorStep, float pixelsPerTick) {
        int[] divisions = {4, 2};
        for (int division : divisions) {
            if (majorStep % division != 0) continue;
            int step = majorStep / division;
            if (step * pixelsPerTick >= MIN_MINOR_TICK_SPACING) {
                return step;
            }
        }
        return majorStep;
    }

    private void renderScrollbar(GuiGraphics graphics, int screenHeight, int timelineWidth) {
        if (environment != NarrativeEnvironment.DEVELOPMENT) return;
        float visibleTicks = getVisibleTicks();
        if (visibleTicks >= totalTick) return;

        int barY = screenHeight - SCROLLBAR_HEIGHT;
        // Track background
        graphics.fill(LAYER_GAP, barY, LAYER_GAP + timelineWidth, screenHeight, 0x40000000);
        // Active bar
        float barStartRatio = viewStartTick / totalTick;
        float barWidthRatio = visibleTicks / totalTick;
        int barX = LAYER_GAP + (int) (barStartRatio * timelineWidth);
        int barW = Math.max(4, (int) (barWidthRatio * timelineWidth));
        barW = Math.min(barW, LAYER_GAP + timelineWidth - barX);
        graphics.fill(barX, barY, barX + barW, screenHeight, 0xCCFFFFFF);
    }

    private String formatTimeTicks(int ticks) {
        int totalSeconds = ticks / TICKS_PER_SECOND;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + ":" + String.format("%02d", seconds);
    }

    private float getVisibleTicks() {
        return Math.max(1f, totalTick / zoomFactor);
    }

    private float getMinZoomFactor() {
        float maxVisibleTicks = getMaxVisibleTicksAtMinZoom();
        if (totalTick <= maxVisibleTicks) return 1f;
        return totalTick / maxVisibleTicks;
    }

    private float getMaxVisibleTicksAtMinZoom() {
        float physicalTimelineWidth =
                (float) (getTimelineWidth() * mc.getWindow().getGuiScale());
        float maxVisibleSeconds = physicalTimelineWidth / MIN_PHYSICAL_PIXELS_PER_SECOND;
        return Math.max(TICKS_PER_SECOND, maxVisibleSeconds * TICKS_PER_SECOND);
    }

    private float getMaxZoomFactor() {
        if (totalTick <= MIN_VISIBLE_TICKS_AT_MAX_ZOOM) return 1f;
        return (float) totalTick / MIN_VISIBLE_TICKS_AT_MAX_ZOOM;
    }

    private void startPlayback() {
        if (playback.getCurrentTick() >= totalTick) {
            playHead.setRatio(0f);
            viewStartTick = 0f;
            updateTick();
        }
        playback.play(playback.getCurrentTick());
    }

    private void followPlayHead() {
        float visibleTicks = getVisibleTicks();
        if (visibleTicks >= totalTick) return;
        float currentTick = playback.getCurrentTick();
        if (currentTick >= viewStartTick && currentTick <= viewStartTick + visibleTicks) return;
        viewStartTick = currentTick;
        clampViewStart();
    }

    private void clampViewStart() {
        float maxStart = totalTick - getVisibleTicks();
        viewStartTick = (float) Math.clamp(viewStartTick, 0f, Math.max(0f, maxStart));
    }

    private void applyZoom(double scrollDelta, int mouseX) {
        float minZoom = getMinZoomFactor();
        float maxZoom = getMaxZoomFactor();
        float newZoom = (float) Math.clamp(zoomFactor * Math.pow(1.25, scrollDelta), minZoom, maxZoom);
        if (newZoom == zoomFactor) return;

        float timelineWidth = getTimelineWidth();
        float mouseRatio = (float) Math.clamp((mouseX - LAYER_GAP) / timelineWidth, 0.0, 1.0);
        float tickAtMouse = viewStartTick + mouseRatio * getVisibleTicks();

        zoomFactor = newZoom;
        viewStartTick = tickAtMouse - mouseRatio * getVisibleTicks();
        clampViewStart();
    }

    public int getPlayHeadTick() {
        return (int) Math.clamp(viewStartTick + playHead.getRatio() * getVisibleTicks(), 0, totalTick);
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        if (getVisibleTicks() >= totalTick) return false;
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        return mouseX >= LAYER_GAP
                && mouseX <= LAYER_GAP + getTimelineWidth()
                && mouseY >= screenHeight - SCROLLBAR_HEIGHT
                && mouseY <= screenHeight;
    }

    private int getStartLayerY() {
        return mc.getWindow().getGuiScaledHeight() - LAYERS_START_Y_OFFSET;
    }

    private int getLayersAreaStartY() {
        return getStartLayerY() + RULER_HEIGHT;
    }

    private int getTimelineWidth() {
        return mc.getWindow().getGuiScaledWidth() - LAYER_GAP;
    }

    public void mouseScrolled(double deltaX, double deltaY) {
        if (!renderingHud) return;
        int[] mousePos = UtilsClient.getScaledMousePos();

        if (Screen.hasAltDown() && mousePos[1] >= getStartLayerY()) {
            applyZoom(deltaY, mousePos[0]);
            return;
        }

        if (openMenu != null && openMenu.isVisible()) {
            openMenu.mouseScrolled(deltaY);
            return;
        }
        layerSelector.mouseScrolled(deltaY);
        if (mousePos[1] < getLayersAreaStartY()) return;
        int maxScroll = Math.max(0, editorLayers.size() * LAYER_HEIGHT - (LAYERS_START_Y_OFFSET - RULER_HEIGHT));
        scrollOffset = (int) Math.clamp(scrollOffset - deltaY * LAYER_HEIGHT, 0, maxScroll);
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        playback.tick(deltaTracker);

        if (environment != NarrativeEnvironment.DEVELOPMENT) return;
        if (control.isPlaying() && !playback.isPlaying()) {
            control.pause();
        }
        if (playback.isPlaying()) {
            followPlayHead();
        }

        if (!renderingHud) return;

        int[] mousePos = UtilsClient.getScaledMousePos();
        int addLayerY = getStartLayerY() - ADD_NEW_LAYER_BUTTON_OFFSET;

        addLayerButton.setPosition(2, addLayerY);
        layerSelector.setPosition(2, addLayerY - 3);

        renderLayers(graphics, deltaTracker, mousePos[0], mousePos[1]);

        for (Button button : buttons) {
            button.render(graphics, mousePos[0], mousePos[1], deltaTracker.getGameTimeDeltaTicks());
        }
        layerSelector.render(graphics, deltaTracker);

        control.setPosition(LAYER_GAP / 2 - control.getWidth() / 2, getStartLayerY() - control.getHeight() - 2);
        control.render(graphics, deltaTracker, mousePos[0], mousePos[1]);

        float visibleTicks = getVisibleTicks();
        float playHeadRatio = (playback.getCurrentTick() - viewStartTick) / visibleTicks;
        playHead.setRatio(playHeadRatio);

        playHead.setY(getStartLayerY() - 5);
        playHead.render(graphics, mousePos[0], mousePos[1], LAYER_GAP, getTimelineWidth());

        if (openMenu != null && openMenu.isVisible()) {
            openMenu.render(graphics, deltaTracker, mousePos[0], mousePos[1]);
        }

        rollWidget.render(
                graphics,
                mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight(),
                mousePos[0],
                mousePos[1]);
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        if (!renderingHud || environment != NarrativeEnvironment.DEVELOPMENT) return;
        if (openMenu != null && openMenu.isVisible()) {
            openMenu.charTyped(codePoint, modifiers);
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!renderingHud || environment != NarrativeEnvironment.DEVELOPMENT) return;
        if (openMenu != null && openMenu.isVisible() && openMenu.keyPressed(keyCode, scanCode, modifiers)) return;
        shortcuts.handleKeyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button, boolean isDoubleClick) {
        if (!renderingHud || environment != NarrativeEnvironment.DEVELOPMENT) return;
        int[] mousePos = UtilsClient.getScaledMousePos();

        if (rollWidget.mouseClicked(mouseX, mouseY, button)) return;

        if (isOverScrollbar((mouseX), (mouseY))) {
            scrollbarDragging = true;
            scrollbarDragStartMouseX = (float) (mouseX);
            scrollbarDragStartViewTick = viewStartTick;
            return;
        }

        if (openMenu != null && openMenu.isVisible()) {
            if (openMenu.mouseClicked(mouseX, mouseY, button, isDoubleClick)) return;
        }
        boolean onAddLayerButton = addLayerButton.isMouseOver(mousePos[0], mousePos[1]);
        for (Button button1 : buttons) {
            button1.mouseClicked(mouseX, mouseY, button);
        }
        if (!onAddLayerButton) {
            layerSelector.mouseClicked(mouseX, mouseY, button, isDoubleClick);
        }
        control.mouseClicked(mouseX, mouseY, button, isDoubleClick);

        // Check keyframes and layer buttons before the playhead to avoid conflicts
        int layersAreaStartY = getLayersAreaStartY();
        int currentY = layersAreaStartY - scrollOffset;
        List<CutsceneMakerEditorLayer> toRemove = new ArrayList<>();
        for (int i = 0; i < editorLayers.size(); i++) {
            CutsceneMakerEditorLayer editorLayer = editorLayers.get(i);
            if (currentY >= layersAreaStartY && currentY < mc.getWindow().getGuiScaledHeight()) {
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
                    editorLayer.addKeyframe(getPlayHeadTick());
                    return;
                }
                if (editorLayer.isRemoveButtonHovered(mousePos[0], mousePos[1], currentY, LAYER_HEIGHT)) {
                    toRemove.add(editorLayer);
                }
                Keyframe hovered = editorLayer.getHoveredKeyframe(mousePos[0], mousePos[1]);
                if (hovered != null) {
                    handleKeyframeClick(hovered, mouseX, mouseY, button, isDoubleClick);
                    return;
                }
            }
            currentY += LAYER_HEIGHT;
        }
        editorLayers.removeAll(toRemove);

        if (!selectedKeyframes.isEmpty()) {
            clearSelection();
        }

        if (playHead.isHovered()) {
            playHead.setDragging(true);
        } else if (playHead.onClick(mouseX, mouseY, button, LAYER_GAP, getTimelineWidth(), getStartLayerY())) {
            updateTick();
        }
    }

    private void handleKeyframeClick(
            Keyframe keyframe, double mouseX, double mouseY, int button, boolean isDoubleClick) {
        if (Screen.hasShiftDown()) {
            if (selectedKeyframes.contains(keyframe)) {
                keyframe.setSelected(false);
                selectedKeyframes.remove(keyframe);
            } else {
                keyframe.setSelected(true);
                selectedKeyframes.add(keyframe);
            }
            if (openMenu != null) openMenu.close();
            openMenu = selectedKeyframes.size() == 1 ? selectedKeyframes.get(0).createMenu() : null;
        } else {
            if (!selectedKeyframes.contains(keyframe)) {
                clearSelection();
                keyframe.setSelected(true);
                selectedKeyframes.add(keyframe);
            }
            if (selectedKeyframes.size() == 1) {
                if (openMenu != null) openMenu.close();
                openMenu = keyframe.createMenu();
            }
        }

        draggingStartMouseX = (float) (mouseX);
        draggingOriginalTicks = new HashMap<>();
        for (Keyframe selected : selectedKeyframes) {
            draggingOriginalTicks.put(selected, selected.getTick());
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (!renderingHud || environment != NarrativeEnvironment.DEVELOPMENT) return;
        rollWidget.mouseReleased();
        playHead.setDragging(false);
        if (draggingOriginalTicks != null) {
            shortcuts.recordMoveAction(draggingOriginalTicks);
            draggingOriginalTicks = null;
        }
        scrollbarDragging = false;
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!renderingHud || environment != NarrativeEnvironment.DEVELOPMENT) return;
        if (rollWidget.mouseDragged((mouseY))) return;
        if (scrollbarDragging) {
            float dragDeltaPixels = (float) (mouseX) - scrollbarDragStartMouseX;
            float tickDelta = dragDeltaPixels / getTimelineWidth() * totalTick;
            viewStartTick = scrollbarDragStartViewTick + tickDelta;
            clampViewStart();
            return;
        }
        if (draggingOriginalTicks != null && !draggingOriginalTicks.isEmpty()) {
            float deltaTick = (float) (((mouseX) - draggingStartMouseX) / getTimelineWidth() * getVisibleTicks());
            for (Map.Entry<Keyframe, Integer> entry : draggingOriginalTicks.entrySet()) {
                int newTick = (int) Math.clamp(entry.getValue() + deltaTick, 0, totalTick);
                entry.getKey().setTick(newTick);
            }
            return;
        }
        if (playHead.isDragging()) {
            playHead.onMouseDrag((mouseX), LAYER_GAP, getTimelineWidth());
            updateTick();
            return;
        }
        if (openMenu != null && openMenu.isVisible()) {
            openMenu.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    private void rebuildSortIndices() {
        for (int i = 0; i < editorLayers.size(); i++) {
            editorLayers.get(i).getLayer().setSortIndex(i);
        }
    }

    private void updateTick() {
        int tick = getPlayHeadTick();
        playback.seekTo(tick);
        Services.PACKET.sendToServer(new BiCutscenePlayHeadPacket(tick));
    }

    public float getTick() {
        return playback.getCurrentTick();
    }

    public int getTotalTick() {
        return totalTick;
    }

    public Cutscene getCutscene() {
        return cutscene;
    }

    public CutsceneEditorPlayback getPlayback() {
        return playback;
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

    public boolean isRenderingHud() {
        return renderingHud;
    }

    public void setRenderingHud(boolean renderingHud) {
        this.renderingHud = renderingHud;
    }

    public float getPreviewRoll() {
        return rollWidget.getValue();
    }

    public void setPreviewRoll(float roll) {
        rollWidget.setValue(roll);
    }

    public RollSliderWidget getRollWidget() {
        return rollWidget;
    }

    public List<CutsceneMakerEditorLayer> getEditorLayers() {
        return editorLayers;
    }

    public List<Keyframe> getSelectedKeyframes() {
        return selectedKeyframes;
    }

    public NarrativeEnvironment getEnvironment() {
        return environment;
    }
}
