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

package fr.loudo.narrativecraft.client.screens.narrative.interaction;

import fr.loudo.narrativecraft.client.editors.interaction.ClientInteractionMakerEditorMaker;
import fr.loudo.narrativecraft.narrative.interaction.InteractionPoint;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class InteractionPointEditScreen extends Screen {

    private static final int FIELD_WIDTH = 200;
    private static final int FIELD_HEIGHT = 20;
    private static final int GAP = 6;

    private final ClientInteractionMakerEditorMaker editor;
    private final InteractionPoint point;
    private final Screen lastScreen;

    private EditBox nameBox;
    private EditBox stitchNameBox;
    private EditBox maxDistanceBox;
    private EditBox aimRadiusBox;
    private boolean useAimRadius;

    public InteractionPointEditScreen(
            ClientInteractionMakerEditorMaker editor, InteractionPoint point, Screen lastScreen) {
        super(Translation.message("screen.interaction.points"));
        this.editor = editor;
        this.point = point;
        this.lastScreen = lastScreen;
        this.useAimRadius = point.isUseAimRadius();
    }

    @Override
    protected void init() {
        int x = this.width / 2 - FIELD_WIDTH / 2;
        int totalHeight = 12
                + (FIELD_HEIGHT + GAP)
                + 12
                + (FIELD_HEIGHT + GAP)
                + 12
                + (FIELD_HEIGHT + GAP)
                + (FIELD_HEIGHT + GAP)
                + (useAimRadius ? 12 + (FIELD_HEIGHT + GAP) : 0)
                + (FIELD_HEIGHT + GAP)
                + (FIELD_HEIGHT + GAP)
                + FIELD_HEIGHT;
        int y = this.height / 2 - totalHeight / 2;

        StringWidget nameLabel = new StringWidget(Translation.message("name"), this.font);
        nameLabel.setPosition(x, y);
        addRenderableWidget(nameLabel);
        y += 12;
        nameBox = new EditBox(this.font, x, y, FIELD_WIDTH, FIELD_HEIGHT, Component.empty());
        nameBox.setValue(point.getName());
        addRenderableWidget(nameBox);
        y += FIELD_HEIGHT + GAP;

        StringWidget stitchNameLabel =
                new StringWidget(Translation.message("screen.interaction.stitch_name"), this.font);
        stitchNameLabel.setPosition(x, y);
        addRenderableWidget(stitchNameLabel);
        y += 12;
        stitchNameBox = new EditBox(this.font, x, y, FIELD_WIDTH, FIELD_HEIGHT, Component.empty());
        stitchNameBox.setValue(point.getStitchName());
        addRenderableWidget(stitchNameBox);
        y += FIELD_HEIGHT + GAP;

        StringWidget maxDistanceLabel =
                new StringWidget(Translation.message("screen.interaction.max_distance"), this.font);
        maxDistanceLabel.setPosition(x, y);
        addRenderableWidget(maxDistanceLabel);
        y += 12;
        maxDistanceBox = new EditBox(this.font, x, y, FIELD_WIDTH / 2 - 2, FIELD_HEIGHT, Component.empty());
        maxDistanceBox.setValue(String.format("%.1f", point.getMaxDistance()));
        addRenderableWidget(maxDistanceBox);
        y += FIELD_HEIGHT + GAP;

        Component aimToggleLabel = Component.literal(
                Translation.message("screen.interaction.use_aim_radius").getString()
                        + ": "
                        + (useAimRadius ? "ON" : "OFF"));
        Button aimToggleButton = Button.builder(aimToggleLabel, b -> {
                    useAimRadius = !useAimRadius;
                    rebuild();
                })
                .bounds(x, y, FIELD_WIDTH, FIELD_HEIGHT)
                .build();
        addRenderableWidget(aimToggleButton);
        y += FIELD_HEIGHT + GAP;

        if (useAimRadius) {
            StringWidget aimRadiusLabel =
                    new StringWidget(Translation.message("screen.interaction.aim_radius"), this.font);
            aimRadiusLabel.setPosition(x, y);
            addRenderableWidget(aimRadiusLabel);
            y += 12;
            aimRadiusBox = new EditBox(this.font, x, y, FIELD_WIDTH / 2 - 2, FIELD_HEIGHT, Component.empty());
            aimRadiusBox.setValue(String.format("%.1f", point.getAimRadius()));
            addRenderableWidget(aimRadiusBox);
            y += FIELD_HEIGHT + GAP;
        }

        Button resetPositionButton = Button.builder(
                        Translation.message("screen.interaction.reset_position"), b -> resetPosition())
                .bounds(x, y, FIELD_WIDTH, FIELD_HEIGHT)
                .build();
        addRenderableWidget(resetPositionButton);
        y += FIELD_HEIGHT + GAP;

        Button previewButton = Button.builder(Translation.message("screen.interaction.preview"), b -> {
                    // TODO: execute stitch preview
                })
                .bounds(x, y, FIELD_WIDTH, FIELD_HEIGHT)
                .build();
        addRenderableWidget(previewButton);
        y += FIELD_HEIGHT + GAP;

        Button saveButton = Button.builder(Translation.message("send"), b -> saveAndClose())
                .bounds(x, y, FIELD_WIDTH / 2 - 2, FIELD_HEIGHT)
                .build();
        addRenderableWidget(saveButton);

        Button closeButton = Button.builder(Translation.message("close"), b -> onClose())
                .bounds(x + FIELD_WIDTH / 2 + 2, y, FIELD_WIDTH / 2 - 2, FIELD_HEIGHT)
                .build();
        addRenderableWidget(closeButton);
    }

    private void resetPosition() {
        LocalPlayer player = minecraft.player;
        if (player == null) return;
        point.setPosition(player.position());
        minecraft.setScreen(lastScreen);
    }

    private void saveAndClose() {
        String name = nameBox.getValue().trim();
        if (!name.isEmpty()) point.setName(name);
        point.setStitchName(stitchNameBox.getValue().trim());
        point.setUseAimRadius(useAimRadius);

        try {
            double maxDistance = Double.parseDouble(maxDistanceBox.getValue().trim());
            point.setMaxDistance(Math.max(1.0, Math.min(32.0, maxDistance)));
        } catch (NumberFormatException ignored) {
        }

        if (useAimRadius && aimRadiusBox != null) {
            try {
                double aimRadius = Double.parseDouble(aimRadiusBox.getValue().trim());
                point.setAimRadius(Math.max(0.1, Math.min(5.0, aimRadius)));
            } catch (NumberFormatException ignored) {
            }
        }

        minecraft.setScreen(lastScreen);
    }

    private void rebuild() {
        this.clearWidgets();
        this.init();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
