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

import fr.loudo.narrativecraft.client.screens.components.BreadcrumbWidget;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;

public abstract class AbstractAssignScreen<T> extends Screen {

    private static final int LIST_WIDTH = 150;
    private static final int MAX_PER_PAGE = 10;
    private static final int ITEM_HEIGHT = 20;
    private static final int ITEM_GAP = 2;
    private static final int LIST_START_Y = 55;
    private static final int CENTER_BUTTON_WIDTH = 30;

    protected final Screen lastScreen;

    private final List<T> available = new ArrayList<>();
    private final List<T> assigned = new ArrayList<>();
    private T selectedLeft = null;
    private T selectedRight = null;
    private int leftPage = 1;
    private int rightPage = 1;
    private boolean listsInitialized = false;

    protected AbstractAssignScreen(Component title, Screen lastScreen) {
        super(title);
        this.lastScreen = lastScreen;
    }

    protected abstract List<T> buildAvailableList();

    protected abstract List<T> buildAssignedList();

    protected abstract String getItemName(T item);

    protected abstract String getBreadcrumb();

    protected abstract Component getAvailableColumnLabel();

    protected abstract Component getAssignedColumnLabel();

    protected abstract void onSave(List<T> assigned);

    @Override
    protected void init() {
        if (!listsInitialized) {
            available.addAll(buildAvailableList());
            assigned.addAll(buildAssignedList());
            listsInitialized = true;
        }

        int leftListX = this.width / 4 - LIST_WIDTH / 2;
        int rightListX = 3 * this.width / 4 - LIST_WIDTH / 2;
        int centerX = this.width / 2;
        int totalListHeight = MAX_PER_PAGE * (ITEM_HEIGHT + ITEM_GAP);
        int paginationY = LIST_START_Y + totalListHeight + 5;
        int centerButtonsY = LIST_START_Y + totalListHeight / 2 - ITEM_HEIGHT;

        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> onClose())
                .bounds(5, 5, 20, 20)
                .build());

        addRenderableWidget(new BreadcrumbWidget(30, 8, getBreadcrumb(), this.font));

        StringWidget availableWidget = new StringWidget(getAvailableColumnLabel(), this.font);
        availableWidget.setPosition(leftListX, LIST_START_Y - 14);
        addRenderableWidget(availableWidget);

        StringWidget assignedWidget = new StringWidget(getAssignedColumnLabel(), this.font);
        assignedWidget.setPosition(rightListX, LIST_START_Y - 14);
        addRenderableWidget(assignedWidget);

        buildList(available, selectedLeft, leftListX, leftPage, item -> {
            selectedLeft = selectedLeft == item ? null : item;
            selectedRight = null;
            rebuildWidgets();
        });

        buildList(assigned, selectedRight, rightListX, rightPage, item -> {
            selectedRight = selectedRight == item ? null : item;
            selectedLeft = null;
            rebuildWidgets();
        });

        Button addButton = Button.builder(Component.literal(">"), b -> {
                    if (selectedLeft != null) {
                        assigned.add(selectedLeft);
                        available.remove(selectedLeft);
                        selectedLeft = null;
                        leftPage = Math.clamp((int) Math.ceil((double) available.size() / MAX_PER_PAGE), 1, leftPage);
                        rebuildWidgets();
                    }
                })
                .bounds(centerX - CENTER_BUTTON_WIDTH / 2, centerButtonsY, CENTER_BUTTON_WIDTH, ITEM_HEIGHT)
                .build();
        addButton.active = selectedLeft != null;
        this.addRenderableWidget(addButton);

        Button removeButton = Button.builder(Component.literal("<"), b -> {
                    if (selectedRight != null) {
                        available.add(selectedRight);
                        assigned.remove(selectedRight);
                        selectedRight = null;
                        rightPage = Math.clamp((int) Math.ceil((double) assigned.size() / MAX_PER_PAGE), 1, rightPage);
                        rebuildWidgets();
                    }
                })
                .bounds(
                        centerX - CENTER_BUTTON_WIDTH / 2,
                        centerButtonsY + ITEM_HEIGHT + 4,
                        CENTER_BUTTON_WIDTH,
                        ITEM_HEIGHT)
                .build();
        removeButton.active = selectedRight != null;
        this.addRenderableWidget(removeButton);

        buildPagination(
                leftListX,
                paginationY,
                available.size(),
                leftPage,
                () -> {
                    leftPage--;
                    rebuildWidgets();
                },
                () -> {
                    leftPage++;
                    rebuildWidgets();
                });

        buildPagination(
                rightListX,
                paginationY,
                assigned.size(),
                rightPage,
                () -> {
                    rightPage--;
                    rebuildWidgets();
                },
                () -> {
                    rightPage++;
                    rebuildWidgets();
                });

        this.addRenderableWidget(Button.builder(Translation.message("send"), b -> onSave(assigned))
                .bounds(this.width / 2 - 75, this.height - 28, 150, 20)
                .build());
    }

    private void buildList(List<T> list, T selected, int x, int page, Consumer<T> onClick) {
        int start = (page - 1) * MAX_PER_PAGE;
        int end = Math.min(start + MAX_PER_PAGE, list.size());
        for (int i = start; i < end; i++) {
            final T item = list.get(i);
            boolean isSelected = item.equals(selected);
            String label = (isSelected ? "\u25B6 " : "  ") + getItemName(item);
            int y = LIST_START_Y + (i - start) * (ITEM_HEIGHT + ITEM_GAP);
            this.addRenderableWidget(Button.builder(Component.literal(label), b -> onClick.accept(item))
                    .bounds(x, y, LIST_WIDTH, ITEM_HEIGHT)
                    .build());
        }
    }

    private void buildPagination(
            int x, int y, int totalItems, int currentPage, Runnable prevAction, Runnable nextAction) {
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / MAX_PER_PAGE));
        if (totalPages == 1) return;

        if (currentPage > 1) {
            this.addRenderableWidget(Button.builder(Component.literal("<"), b -> prevAction.run())
                    .bounds(x, y, 20, 18)
                    .build());
        }
        StringWidget widget = new StringWidget(Component.literal(currentPage + "/" + totalPages), this.font);
        widget.setPosition(x + 24, y + 4);
        addRenderableWidget(widget);
        if (currentPage < totalPages) {
            this.addRenderableWidget(Button.builder(Component.literal(">"), b -> nextAction.run())
                    .bounds(x + 52, y, 20, 18)
                    .build());
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        int leftListX = this.width / 4 - LIST_WIDTH / 2;
        int rightListX = 3 * this.width / 4 - LIST_WIDTH / 2;
        int totalListHeight = MAX_PER_PAGE * (ITEM_HEIGHT + ITEM_GAP);

        graphics.fill(
                leftListX - 3,
                LIST_START_Y - 17,
                leftListX + LIST_WIDTH + 3,
                LIST_START_Y + totalListHeight + 2,
                FastColor.ARGB32.color(70, 31, 30, 30));
        graphics.fill(
                rightListX - 3,
                LIST_START_Y - 17,
                rightListX + LIST_WIDTH + 3,
                LIST_START_Y + totalListHeight + 2,
                FastColor.ARGB32.color(70, 31, 30, 30));

        super.renderBackground(graphics, mouseX, mouseY, a);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }
}
