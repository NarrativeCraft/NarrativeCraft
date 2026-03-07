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

import fr.loudo.narrativecraft.client.narrative.ui.ClientNarrativeUIActionRegistry;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class NarrativeEntryListScreen<E extends NarrativeEntry<?>> extends PaginationsItemsScreen<E> {

    private Screen lastScreen;
    private Class<? extends NarrativeEntry<?>> entryClass;

    public NarrativeEntryListScreen(Component title, List<E> entries, Class<E> entryClass) {
        super(title, entries);
        this.entryClass = entryClass;
    }

    public NarrativeEntryListScreen(Component title, List<E> entries, Screen lastScreen, Class<E> entryClass) {
        this(title, entries, entryClass);
        this.lastScreen = lastScreen;
        this.entryClass = entryClass;
    }

    @Override
    protected void init() {
        super.init();

        if (lastScreen != null) {
            Button backButton = Button.builder(Component.literal("<"), (b) -> {
                        onClose();
                    })
                    .bounds(this.width / 2 - buttonWidth / 2, 20, 20, 20)
                    .build();
            this.addRenderableWidget(backButton);
        }

        Screen createScreen = ClientNarrativeUIActionRegistry.getInstance().showCreateScreen(entryClass, this);
        if (createScreen != null) {
            Button addButton = Button.builder(Component.literal("+"), (b) -> {
                        minecraft.setScreen(
                                ClientNarrativeUIActionRegistry.getInstance().showCreateScreen(entryClass, this));
                    })
                    .bounds(this.width / 2 + buttonWidth / 2, 20, 20, 20)
                    .build();
            this.addRenderableWidget(addButton);
        }
    }

    public void reload() {
        this.clearWidgets();
        init();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }

    @Override
    public void addWidgetsForItem(int x, int y, E item) {
        super.addWidgetsForItem(x - 20, y, item);
        Button editButton = Button.builder(Component.literal("✎"), b -> {
                    minecraft.setScreen(
                            ClientNarrativeUIActionRegistry.getInstance().showEditScreen(item, this));
                })
                .bounds(x + buttonWidth - 15, y, 20, 20)
                .build();
        this.addRenderableWidget(editButton);

        ConfirmScreen confirmScreen = new ConfirmScreen(
                b -> {
                    if (b) {
                        Services.PACKET.sendToServer(BiSyncNarrativeEntryPacket.delete(item.getId(), item.toPayload()));
                    }
                    minecraft.setScreen(this);
                },
                Translation.message("screen.confirm.title"),
                Translation.message("screen.confirm.message", item.getName()));

        Button deleteButton = Button.builder(Component.literal("✖"), b -> minecraft.setScreen(confirmScreen))
                .bounds(editButton.getX() + editButton.getWidth() + 5, y, 20, 20)
                .build();
        this.addRenderableWidget(deleteButton);
    }

    @Override
    protected String getItemName(E item) {
        return item.formattedName();
    }

    @Override
    protected void onItemClicked(E item) {
        minecraft.setScreen(ClientNarrativeUIActionRegistry.getInstance().showListSubScreen(item, this));
    }

    @Override
    protected boolean isItemClickable(E item) {
        return ClientNarrativeUIActionRegistry.getInstance().hasSubScreen(item);
    }
}
