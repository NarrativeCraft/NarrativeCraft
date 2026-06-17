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
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class InteractionZoneListScreen extends AbstractInteractionListScreen<InteractionZone> {

    public InteractionZoneListScreen(ClientInteractionMakerEditorMaker editor, Screen lastScreen) {
        super(Translation.message("screen.interaction.zones"), editor, lastScreen);
    }

    @Override
    protected List<InteractionZone> getItems() {
        return editor.getInteraction().getZones();
    }

    @Override
    protected Component getAddHint() {
        return Translation.message("screen.interaction.add_zone");
    }

    @Override
    protected void addItem(String name) {
        if (getItems().stream().anyMatch(z -> z.getName().equalsIgnoreCase(name))) return;
        getItems().add(new InteractionZone(name, "", Vec3.ZERO, Vec3.ZERO));
    }

    @Override
    protected void addRow(int y, InteractionZone zone) {
        addItemRow(
                y,
                zone.getName(),
                () -> teleportTo(zone.center()),
                () -> minecraft.gui.setScreen(new InteractionZoneEditScreen(editor, zone, this)),
                () -> confirmDelete(
                        zone.getName(), () -> getItems().removeIf(z -> z.getId().equals(zone.getId()))));
    }
}
