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

package fr.loudo.narrativecraft.client.screens;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.narrative.ui.ClientNarrativeUIActionRegistry;
import fr.loudo.narrativecraft.client.screens.components.BreadcrumbWidget;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.CustomFont;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class NarrativeEntryListScreen<E extends NarrativeEntry<?>> extends PaginationsItemsScreen<E> {

    private final String breadCrumb;
    private Screen lastScreen;
    private Class<? extends NarrativeEntry<?>> entryClass;
    private NarrativeEntry<?> parentEntry;

    public NarrativeEntryListScreen(Component title, List<E> entries, Class<E> entryClass, String breadCrumb) {
        super(title, entries);
        this.entryClass = entryClass;
        this.breadCrumb = breadCrumb;
    }

    public NarrativeEntryListScreen(
            Component title, List<E> entries, Screen lastScreen, Class<E> entryClass, String breadCrumb) {
        this(title, entries, lastScreen, entryClass, null, breadCrumb);
    }

    public NarrativeEntryListScreen(
            Component title,
            List<E> entries,
            Screen lastScreen,
            Class<E> entryClass,
            NarrativeEntry<?> parentEntry,
            String breadCrumb) {
        this(title, entries, entryClass, breadCrumb);
        this.lastScreen = lastScreen;
        this.entryClass = entryClass;
        this.parentEntry = parentEntry;
    }

    @Override
    protected void init() {
        super.init();

        if (lastScreen != null) {
            Button backButton = Button.builder(Component.literal("<"), (b) -> {
                        onClose();
                    })
                    .bounds(this.width / 2 - buttonWidth / 2 - 20, 20, 20, 20)
                    .build();
            this.addRenderableWidget(backButton);
        }

        Screen createScreen =
                ClientNarrativeUIActionRegistry.getInstance().showCreateScreen(entryClass, this, parentEntry);
        if (createScreen != null) {
            Button addButton = Button.builder(Component.literal("+"), (b) -> {
                        minecraft.setScreen(ClientNarrativeUIActionRegistry.getInstance()
                                .showCreateScreen(entryClass, this, parentEntry));
                    })
                    .bounds(this.width / 2 + buttonWidth / 2 + 10, 20, 20, 20)
                    .build();
            this.addRenderableWidget(addButton);
        }

        if (entryClass == CharacterStory.class) {
            Button storiesButton = Button.builder(Component.literal("S"), b -> onClose())
                    .bounds(10, 10, 20, 20)
                    .build();
            this.addRenderableWidget(storiesButton);
        } else {
            Button charactersButton = Button.builder(Component.literal("C"), b -> {
                        minecraft.setScreen(new NarrativeEntryListScreen<>(
                                Translation.message("character"),
                                ClientNarrativeCraftMod.getInstance()
                                        .getCharacterManager()
                                        .getList(),
                                this,
                                CharacterStory.class,
                                ""));
                    })
                    .bounds(10, 10, 20, 20)
                    .build();
            this.addRenderableWidget(charactersButton);
        }
        Button settingsButton = Button.builder(Component.literal(CustomFont.SETTINGS), b -> {
                    minecraft.setScreen(new NarrativeSettingsScreen(this));
                })
                .bounds(10, 35, 20, 20)
                .build();
        this.addRenderableWidget(settingsButton);

        if (!breadCrumb.isEmpty()) {
            addRenderableWidget(new BreadcrumbWidget(20, this.height / 2, breadCrumb, this.font));
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
        addMainButton(x - 20, y, item);
        Button editButton = addEditButton(x + buttonWidth - 15, y, item);
        addDeleteButton(editButton.getX() + editButton.getWidth() + 5, y, item);
    }

    protected Button addEditButton(int x, int y, E item) {
        Button editButton = Button.builder(Component.literal("✎"), b -> {
                    minecraft.setScreen(
                            ClientNarrativeUIActionRegistry.getInstance().showEditScreen(item, this));
                })
                .bounds(x, y, 20, 20)
                .build();
        this.addRenderableWidget(editButton);
        return editButton;
    }

    protected Button addDeleteButton(int x, int y, E item) {
        ConfirmScreen confirmScreen = new ConfirmScreen(
                b -> {
                    if (b) {
                        Services.PACKET.sendToServer(BiSyncNarrativeEntryPacket.delete(item.getId(), item.toPayload()));
                    }
                    minecraft.setScreen(this);
                },
                Translation.message("screen.confirm.title"),
                Translation.message("screen.confirm.delete", item.getName()));

        Button deleteButton = Button.builder(Component.literal("✖"), b -> minecraft.setScreen(confirmScreen))
                .bounds(x, y, 20, 20)
                .build();
        this.addRenderableWidget(deleteButton);
        return deleteButton;
    }

    @Override
    protected String getItemName(E item) {
        return item.formattedName();
    }

    @Override
    protected void onItemClicked(E item) {
        Screen subScreen = ClientNarrativeUIActionRegistry.getInstance().showListSubScreen(item, this);
        if (subScreen != null) {
            minecraft.setScreen(subScreen);
        } else {
            ClientNarrativeUIActionRegistry.getInstance().customClickAction(item);
        }
    }

    @Override
    protected boolean isItemClickable(E item) {
        return ClientNarrativeUIActionRegistry.getInstance().isClickable(item);
    }
}
