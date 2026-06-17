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
import fr.loudo.narrativecraft.utils.CustomFont;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractInteractionListScreen<T> extends Screen {

    protected static final int ROW_HEIGHT = 20;
    protected static final int ROW_GAP = 4;
    protected static final int NAME_WIDTH = 160;
    protected static final int LIST_START_Y = 60;
    protected static final int MAX_PER_PAGE = 8;

    protected final ClientInteractionMakerEditorMaker editor;
    protected final Screen lastScreen;
    protected EditBox nameInput;
    protected int page = 1;

    protected AbstractInteractionListScreen(
            Component title, ClientInteractionMakerEditorMaker editor, Screen lastScreen) {
        super(title);
        this.editor = editor;
        this.lastScreen = lastScreen;
    }

    protected abstract List<T> getItems();

    protected abstract Component getAddHint();

    protected abstract void addItem(String name);

    protected abstract void addRow(int y, T item);

    @Override
    protected void init() {
        nameInput = new EditBox(this.font, this.width / 2 - 80, 30, 140, 18, Component.empty());
        nameInput.setHint(getAddHint());
        addRenderableWidget(nameInput);

        Button addButton = Button.builder(Component.literal("+"), b -> {
                    String name = nameInput.getValue().trim();
                    if (!name.isEmpty()) {
                        addItem(name);
                        nameInput.setValue("");
                        rebuild();
                    }
                })
                .bounds(this.width / 2 + 65, 30, 20, 18)
                .build();
        addRenderableWidget(addButton);

        Button backButton = Button.builder(Component.literal("<"), b -> onClose())
                .bounds(5, 5, 20, 20)
                .build();
        addRenderableWidget(backButton);

        List<T> items = getItems();
        int start = (page - 1) * MAX_PER_PAGE;
        int end = Math.min(start + MAX_PER_PAGE, items.size());
        for (int i = start; i < end; i++) {
            addRow(LIST_START_Y + (i - start) * (ROW_HEIGHT + ROW_GAP), items.get(i));
        }

        buildPagination(items.size());
    }

    protected void addItemRow(int y, String name, Runnable onName, Runnable onEdit, Runnable onDelete) {
        int rowX = this.width / 2 - (NAME_WIDTH + 20 + 20 + ROW_GAP * 2) / 2;

        addRenderableWidget(Button.builder(Component.literal(name), b -> onName.run())
                .bounds(rowX, y, NAME_WIDTH, ROW_HEIGHT)
                .build());
        addRenderableWidget(Button.builder(Component.literal("✎"), b -> onEdit.run())
                .bounds(rowX + NAME_WIDTH + ROW_GAP, y, 20, ROW_HEIGHT)
                .build());
        addRenderableWidget(Button.builder(Component.literal(CustomFont.CROSS), b -> onDelete.run())
                .bounds(rowX + NAME_WIDTH + ROW_GAP + 20 + ROW_GAP, y, 20, ROW_HEIGHT)
                .build());
    }

    protected void confirmDelete(String name, Runnable onConfirmed) {
        ConfirmScreen confirmScreen = new ConfirmScreen(
                b -> {
                    if (b) onConfirmed.run();
                    minecraft.gui.setScreen(this);
                    rebuild();
                },
                Translation.message("screen.confirm.title"),
                Translation.message("screen.confirm.delete", name));
        minecraft.gui.setScreen(confirmScreen);
    }

    protected void teleportTo(Vec3 position) {
        if (position == null || position.equals(Vec3.ZERO)) return;
        LocalPlayer player = minecraft.player;
        if (player == null) return;
        minecraft.gui.setScreen(null);
        player.setPos(position.x, position.y, position.z);
        player.connection.send(new ServerboundMovePlayerPacket.Pos(position, player.onGround(), false));
    }

    private void buildPagination(int totalItems) {
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / MAX_PER_PAGE));
        if (totalPages == 1) return;
        int paginationY = LIST_START_Y + MAX_PER_PAGE * (ROW_HEIGHT + ROW_GAP) + 4;
        int centerX = this.width / 2;

        if (page > 1) {
            addRenderableWidget(Button.builder(Component.literal("<"), b -> {
                        page--;
                        rebuild();
                    })
                    .bounds(centerX - 46, paginationY, 20, 18)
                    .build());
        }
        StringWidget pageWidget = new StringWidget(Component.literal(page + "/" + totalPages), this.font);
        pageWidget.setPosition(centerX - 12, paginationY + 4);
        addRenderableWidget(pageWidget);
        if (page < totalPages) {
            addRenderableWidget(Button.builder(Component.literal(">"), b -> {
                        page++;
                        rebuild();
                    })
                    .bounds(centerX + 26, paginationY, 20, 18)
                    .build());
        }
    }

    protected void rebuild() {
        this.clearWidgets();
        this.init();
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(lastScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
