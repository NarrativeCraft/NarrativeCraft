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

package fr.loudo.narrativecraft.screens;

import fr.loudo.narrativecraft.client.narrative.ClientNarrativeEntryEditorManager;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class NarrativeEntryListScreen extends PaginationsItemsScreen<NarrativeEntry> {

    public NarrativeEntryListScreen(Component title, List<? extends NarrativeEntry> entries) {
        super(title, new ArrayList<>(entries));
    }

    @Override
    public void addWidgetsForItem(int x, int y, NarrativeEntry item) {
        super.addWidgetsForItem(x - 20, y, item);
        Button editButton = Button.builder(Component.literal("✎"), b -> {})
                .bounds(x + buttonWidth - 15, y, 20, 20)
                .build();
        this.addRenderableWidget(editButton);

        Button deleteButton = Button.builder(
                        Component.literal("✖"),
                        b -> ClientNarrativeEntryEditorManager.getInstance().delete(item))
                .bounds(editButton.getX() + editButton.getWidth() + 5, y, 20, 20)
                .build();
        this.addRenderableWidget(deleteButton);
    }

    @Override
    protected String getItemName(NarrativeEntry item) {
        return item.getName();
    }

    @Override
    protected void onItemClicked(NarrativeEntry item) {}
}
