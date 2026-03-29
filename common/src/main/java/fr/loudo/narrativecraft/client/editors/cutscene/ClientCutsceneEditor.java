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

import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayer;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.editors.Editor;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneState;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.UtilsClient;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public class ClientCutsceneEditor implements Editor {

    private static final int LAYER_HEIGHT = 20;
    private static final int LAYER_GAP = 85;
    private static final int LAYERS_START_Y_OFFSET = 80;
    private static final int ADD_NEW_LAYER_BUTTON_OFFSET = 13;

    private final Minecraft mc = Minecraft.getInstance();
    private final List<ICutsceneLayer> layersAdded = new ArrayList<>();
    private final Cutscene cutscene;
    private final ClientPlayerSession playerSession =
            ClientNarrativeCraftMod.getInstance().getPlayerSession();
    private final List<Button> buttons = new ArrayList<>();
    private final CutsceneEditorLayerSelector layerSelector =
            new CutsceneEditorLayerSelector(this, 90, 120, ARGB.color(190, 0, 0, 0));

    private Button addLayerButton;
    private int scrollOffset = 0;

    public ClientCutsceneEditor(Cutscene cutscene) {
        this.cutscene = cutscene;
    }

    public void init() {
        buttons.add(Button.builder(Component.literal("✖"), button -> {
                    Services.PACKET.sendToServer(new C2SCutsceneState(C2SCutsceneState.State.QUIT, cutscene));
                    playerSession.setEditor(null);
                })
                .bounds(5, 5, 20, 20)
                .build());

        addLayerButton = Button.builder(Component.literal("+"), b -> layerSelector.toggle())
                .bounds(0, 0, 10, 10)
                .build();
        buttons.add(addLayerButton);
    }

    public void addLayer(ICutsceneLayer layer) {
        layersAdded.add(layer);
    }

    public void removeLayer(ICutsceneLayer layer) {
        layersAdded.remove(layer);
    }

    private void renderLayers(GuiGraphicsExtractor graphics) {
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int layerStartY = getStartLayerY();
        int currentY = layerStartY - scrollOffset;

        // Layers background
        graphics.fill(0, layerStartY, screenWidth, screenHeight + LAYER_HEIGHT, ARGB.color(0.8f, 0));

        // Separator
        graphics.fill(LAYER_GAP, layerStartY, LAYER_GAP + 1, screenHeight, 0xFFFFFFFF);

        for (ICutsceneLayer layer : layersAdded) {
            int totalHeight = (currentY + LAYER_HEIGHT);
            if (currentY >= layerStartY && currentY < screenHeight) {
                // Bottom line of the layer
                graphics.fill(0, totalHeight - 1, screenWidth, totalHeight, 0xFFFFFFFF);

                // Layer name
                graphics.text(mc.font, layer.getName(), 5, currentY + (mc.font.lineHeight / 2) + 1, 0xFFFFFFFF);

                // TODO: draw layers button to add n stuff
            }
            currentY += LAYER_HEIGHT;
        }

        // Scroll behavior if layers overflow screen
        int totalLayerHeight = layersAdded.size() * LAYER_HEIGHT;
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

    public void mouseScrolled(double deltaX, double deltaY) {
        layerSelector.mouseScrolled(deltaY);
        int[] mousePos = UtilsClient.getScaledMousePos();
        if (mousePos[1] < getStartLayerY()) return;
        int maxScroll = Math.max(0, layersAdded.size() * LAYER_HEIGHT - LAYERS_START_Y_OFFSET);
        scrollOffset = (int) Math.clamp(scrollOffset - deltaY * LAYER_HEIGHT, 0, maxScroll);
    }

    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        int[] mousePos = UtilsClient.getScaledMousePos();
        int addLayerX = LAYER_GAP - addLayerButton.getWidth() / 2;
        int addLayerY = getStartLayerY() - ADD_NEW_LAYER_BUTTON_OFFSET;

        addLayerButton.setPosition(addLayerX, addLayerY);
        layerSelector.setPosition(addLayerX + 2, addLayerY);

        renderLayers(graphics);

        for (Button button : buttons) {
            button.extractRenderState(graphics, mousePos[0], mousePos[1], deltaTracker.getGameTimeDeltaTicks());
        }
        layerSelector.render(graphics, deltaTracker);
    }

    public void mouseClicked(MouseButtonEvent mouseButtonEvent, boolean isDoubleClick) {
        int[] mousePos = UtilsClient.getScaledMousePos();
        boolean onAddLayerButton = addLayerButton.isMouseOver(mousePos[0], mousePos[1]);
        for (Button button : buttons) {
            button.mouseClicked(mouseButtonEvent, isDoubleClick);
        }
        if (!onAddLayerButton) {
            layerSelector.mouseClicked(mouseButtonEvent, isDoubleClick);
        }
    }

    public Cutscene getCutscene() {
        return cutscene;
    }
}
