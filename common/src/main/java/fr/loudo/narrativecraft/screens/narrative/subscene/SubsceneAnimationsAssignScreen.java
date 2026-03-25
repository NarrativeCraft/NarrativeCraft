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

package fr.loudo.narrativecraft.screens.narrative.subscene;

import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.subscene.SubscenePayload;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.NarrativeEntryAction;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.screens.components.BreadcrumbWidget;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public class SubsceneAnimationsAssignScreen extends Screen {

    private static final int LIST_WIDTH = 150;
    private static final int MAX_PER_PAGE = 10;
    private static final int ITEM_HEIGHT = 20;
    private static final int ITEM_GAP = 2;
    private static final int LIST_START_Y = 55;
    private static final int CENTER_BUTTON_WIDTH = 30;

    private final Subscene subscene;
    private final Screen lastScreen;

    private final List<Animation> available = new ArrayList<>();
    private final List<Animation> assigned = new ArrayList<>();

    private Animation selectedLeft = null;
    private Animation selectedRight = null;
    private int leftPage = 1;
    private int rightPage = 1;

    public SubsceneAnimationsAssignScreen(Subscene subscene, Screen lastScreen) {
        super(Component.literal("Assign Animations"));
        this.subscene = subscene;
        this.lastScreen = lastScreen;

        Set<UUID> assignedIds = new HashSet<>();
        for (Animation anim : subscene.getAnimations()) {
            assignedIds.add(anim.getId());
            assigned.add(anim);
        }
        for (Animation anim : subscene.getScene().getAnimationManager().getList()) {
            if (!assignedIds.contains(anim.getId())) {
                available.add(anim);
            }
        }
    }

    @Override
    protected void init() {
        int leftListX = this.width / 4 - LIST_WIDTH / 2;
        int rightListX = 3 * this.width / 4 - LIST_WIDTH / 2;
        int centerX = this.width / 2;
        int totalListHeight = MAX_PER_PAGE * (ITEM_HEIGHT + ITEM_GAP);
        int paginationY = LIST_START_Y + totalListHeight + 5;
        int centerButtonsY = LIST_START_Y + totalListHeight / 2 - ITEM_HEIGHT;

        // Back button
        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> onClose())
                .bounds(5, 5, 20, 20)
                .build());

        // Breadcrumb
        String breadcrumb = subscene.getScene().getChapter().getName()
                + ";" + subscene.getScene().getName()
                + ";" + subscene.getName();
        addRenderableWidget(new BreadcrumbWidget(30, 8, breadcrumb, this.font));

        // Column headers
        StringWidget availableWidget =
                new StringWidget(Translation.message("screen.subscene_assign.available"), this.font);
        availableWidget.setPosition(leftListX, LIST_START_Y - 14);
        addRenderableWidget(availableWidget);

        StringWidget assignedWidget =
                new StringWidget(Translation.message("screen.subscene_assign.assigned"), this.font);
        assignedWidget.setPosition(rightListX, LIST_START_Y - 14);
        addRenderableWidget(assignedWidget);

        // Left list
        buildList(available, selectedLeft, leftListX, leftPage, anim -> {
            selectedLeft = selectedLeft == anim ? null : anim;
            selectedRight = null;
            rebuildWidgets();
        });

        // Right list
        buildList(assigned, selectedRight, rightListX, rightPage, anim -> {
            selectedRight = selectedRight == anim ? null : anim;
            selectedLeft = null;
            rebuildWidgets();
        });

        // Center ">" button, add selected left to right
        Button addButton = Button.builder(Component.literal(">"), b -> {
                    if (selectedLeft != null) {
                        assigned.add(selectedLeft);
                        available.remove(selectedLeft);
                        selectedLeft = null;
                        int maxLeftPage = Math.max(1, (int) Math.ceil((double) available.size() / MAX_PER_PAGE));
                        leftPage = Math.min(leftPage, maxLeftPage);
                        rebuildWidgets();
                    }
                })
                .bounds(centerX - CENTER_BUTTON_WIDTH / 2, centerButtonsY, CENTER_BUTTON_WIDTH, ITEM_HEIGHT)
                .build();
        addButton.active = selectedLeft != null;
        this.addRenderableWidget(addButton);

        // Center "<" button, remove selected right to left
        Button removeButton = Button.builder(Component.literal("<"), b -> {
                    if (selectedRight != null) {
                        available.add(selectedRight);
                        assigned.remove(selectedRight);
                        selectedRight = null;
                        int maxRightPage = Math.max(1, (int) Math.ceil((double) assigned.size() / MAX_PER_PAGE));
                        rightPage = Math.min(rightPage, maxRightPage);
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

        // Left pagination
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

        // Right pagination
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

        // Save button
        this.addRenderableWidget(Button.builder(Translation.message("send"), b -> save())
                .bounds(this.width / 2 - 75, this.height - 28, 150, 20)
                .build());
    }

    private void buildList(
            List<Animation> list, Animation selected, int x, int page, java.util.function.Consumer<Animation> onClick) {
        int start = (page - 1) * MAX_PER_PAGE;
        int end = Math.min(start + MAX_PER_PAGE, list.size());
        for (int i = start; i < end; i++) {
            final Animation anim = list.get(i);
            boolean isSelected = anim.equals(selected);
            String label = (isSelected ? "\u25B6 " : "  ") + anim.getName();
            int y = LIST_START_Y + (i - start) * (ITEM_HEIGHT + ITEM_GAP);
            this.addRenderableWidget(Button.builder(Component.literal(label), b -> onClick.accept(anim))
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

    private void save() {
        List<UUID> newAnimationIds = assigned.stream().map(Animation::getId).toList();
        Services.PACKET.sendToServer(new BiSyncNarrativeEntryPacket(
                subscene.getId(),
                new SubscenePayload(
                        subscene.getName(),
                        subscene.getDescription(),
                        subscene.getScene().getId(),
                        subscene.getScene().getChapter().getId(),
                        newAnimationIds),
                NarrativeEntryAction.EDIT));
        minecraft.setScreen(lastScreen);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int leftListX = this.width / 4 - LIST_WIDTH / 2;
        int rightListX = 3 * this.width / 4 - LIST_WIDTH / 2;
        int totalListHeight = MAX_PER_PAGE * (ITEM_HEIGHT + ITEM_GAP);

        guiGraphics.fill(
                leftListX - 3,
                LIST_START_Y - 17,
                leftListX + LIST_WIDTH + 3,
                LIST_START_Y + totalListHeight + 2,
                ARGB.color(70, 31, 30, 30));
        guiGraphics.fill(
                rightListX - 3,
                LIST_START_Y - 17,
                rightListX + LIST_WIDTH + 3,
                LIST_START_Y + totalListHeight + 2,
                ARGB.color(70, 31, 30, 30));

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }
}
