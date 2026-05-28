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

package fr.loudo.narrativecraft.client.screens.narrative.subscene;

import fr.loudo.narrativecraft.client.screens.NarrativeEntryListScreen;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SubsceneEntryListScreen extends NarrativeEntryListScreen<Subscene> {

    public SubsceneEntryListScreen(
            Component title,
            List<Subscene> entries,
            Screen lastScreen,
            NarrativeEntry<?> parentEntry,
            String breadCrumb) {
        super(title, entries, lastScreen, Subscene.class, parentEntry, breadCrumb);
    }

    @Override
    public void addWidgetsForItem(int x, int y, Subscene item) {
        addMainButton(x - 30, y, item);
        Button editButton = addEditButton(x + buttonWidth - 25, y, item);
        Button assignButton = addAssignButton(editButton.getX() + editButton.getWidth() + 5, y, item);
        addDeleteButton(assignButton.getX() + assignButton.getWidth() + 5, y, item);
    }

    protected Button addAssignButton(int x, int y, Subscene item) {
        Button assignButton = Button.builder(Component.literal("⚙"), b -> {
                    minecraft.setScreen(new SubsceneAnimationsAssignScreen(item, this));
                })
                .bounds(x, y, 20, 20)
                .build();
        addRenderableWidget(assignButton);
        return assignButton;
    }
}
