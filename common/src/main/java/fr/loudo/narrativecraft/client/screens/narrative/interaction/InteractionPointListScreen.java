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

package fr.loudo.narrativecraft.client.screens.narrative.interaction;

import fr.loudo.narrativecraft.client.editors.interaction.ClientInteractionMakerEditorMaker;
import fr.loudo.narrativecraft.narrative.interaction.InteractionPoint;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class InteractionPointListScreen extends AbstractInteractionListScreen<InteractionPoint> {

    public InteractionPointListScreen(ClientInteractionMakerEditorMaker editor, Screen lastScreen) {
        super(Translation.message("screen.interaction.points"), editor, lastScreen);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected List<InteractionPoint> getItems() {
        return editor.getInteraction().getPoints();
    }

    @Override
    protected Component getAddHint() {
        return Translation.message("screen.interaction.add_point");
    }

    @Override
    protected void addItem(String name) {
        if (getItems().stream().anyMatch(p -> p.getName().equalsIgnoreCase(name))) return;
        LocalPlayer player = minecraft.player;
        Vec3 position = player != null ? player.position() : Vec3.ZERO;
        getItems().add(new InteractionPoint(name, "", position));
    }

    @Override
    protected void addRow(int y, InteractionPoint point) {
        addItemRow(
                y,
                point.getName(),
                () -> teleportTo(point.getPosition()),
                () -> minecraft.setScreen(new InteractionPointEditScreen(editor, point, this)),
                () -> confirmDelete(point.getName(), () -> getItems()
                        .removeIf(p -> p.getId().equals(point.getId()))));
    }
}
