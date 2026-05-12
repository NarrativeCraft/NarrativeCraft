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
import fr.loudo.narrativecraft.narrative.interaction.InteractionZone;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class InteractionZoneEditScreen extends Screen {

    private static final int FIELD_WIDTH = 200;
    private static final int FIELD_HEIGHT = 20;
    private static final int GAP = 6;

    private final ClientInteractionMakerEditorMaker editor;
    private final InteractionZone zone;
    private final Screen lastScreen;

    private EditBox nameBox;
    private EditBox stitchNameBox;
    private boolean oneTime;

    public InteractionZoneEditScreen(
            ClientInteractionMakerEditorMaker editor, InteractionZone zone, Screen lastScreen) {
        super(Translation.message("screen.interaction.zones"));
        this.editor = editor;
        this.zone = zone;
        this.lastScreen = lastScreen;
        this.oneTime = zone.isOneTime();
    }

    @Override
    protected void init() {
        int x = this.width / 2 - FIELD_WIDTH / 2;
        int y = this.height / 4;

        StringWidget nameLabel = new StringWidget(Translation.message("name"), this.font);
        nameLabel.setPosition(x, y);
        addRenderableWidget(nameLabel);
        y += 12;
        nameBox = new EditBox(this.font, x, y, FIELD_WIDTH, FIELD_HEIGHT, Component.empty());
        nameBox.setValue(zone.getName());
        addRenderableWidget(nameBox);
        y += FIELD_HEIGHT + GAP;

        StringWidget stitchNameLabel =
                new StringWidget(Translation.message("screen.interaction.stitch_name"), this.font);
        stitchNameLabel.setPosition(x, y);
        addRenderableWidget(stitchNameLabel);
        y += 12;
        stitchNameBox = new EditBox(this.font, x, y, FIELD_WIDTH, FIELD_HEIGHT, Component.empty());
        stitchNameBox.setValue(zone.getStitchName());
        addRenderableWidget(stitchNameBox);
        y += FIELD_HEIGHT + GAP;

        StringWidget corner1Label =
                new StringWidget(Component.literal("C1: " + formatVec(zone.getCorner1())), this.font);
        corner1Label.setPosition(x, y);
        addRenderableWidget(corner1Label);
        y += 14;

        StringWidget corner2Label =
                new StringWidget(Component.literal("C2: " + formatVec(zone.getCorner2())), this.font);
        corner2Label.setPosition(x, y);
        addRenderableWidget(corner2Label);
        y += 14 + GAP;

        Button placeCornersButton = Button.builder(Translation.message("screen.interaction.place_corners"), b -> {
                    applyNameAndStitch();
                    editor.enterCornerPlacementMode(zone, this);
                })
                .bounds(x, y, FIELD_WIDTH, FIELD_HEIGHT)
                .build();
        addRenderableWidget(placeCornersButton);
        y += FIELD_HEIGHT + GAP;

        Button oneTimeClickButton = Button.builder(
                        Component.literal(Translation.message("screen.interaction.one_time_click")
                                        .getString() + ": "
                                + Translation.message(oneTime ? "yes" : "no").getString()),
                        b -> {
                            oneTime = !oneTime;
                            rebuild();
                        })
                .bounds(x, y, FIELD_WIDTH, FIELD_HEIGHT)
                .build();
        addRenderableWidget(oneTimeClickButton);
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

    private void rebuild() {
        applyNameAndStitch();
        clearWidgets();
        init();
    }

    private void applyNameAndStitch() {
        String name = nameBox.getValue().trim();
        if (!name.isEmpty()) zone.setName(name);
        zone.setStitchName(stitchNameBox.getValue().trim());
        zone.setOneTime(oneTime);
    }

    private void saveAndClose() {
        applyNameAndStitch();
        minecraft.setScreen(lastScreen);
    }

    private static String formatVec(Vec3 vec) {
        if (vec == null || vec.equals(Vec3.ZERO)) return "—";
        return String.format("%.1f, %.1f, %.1f", vec.x, vec.y, vec.z);
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
