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

import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import fr.loudo.narrativecraft.utils.UtilsClient;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class PaginationsItemsScreen<T> extends Screen {

    protected List<T> list;
    protected int page = 1;
    protected int maxItemsPerPage = 5;
    protected int gap = 10;
    protected int buttonWidth = 170;
    protected int buttonHeight = 20;

    protected PaginationsItemsScreen(Component title) {
        super(title);
    }

    public PaginationsItemsScreen(Component title, List<T> list) {
        super(title);
        this.list = list;
    }

    public PaginationsItemsScreen(Component title, List<T> list, int page) {
        this(title, list);
        this.page = page;
    }

    protected abstract String getItemName(T item);

    protected abstract void onItemClicked(T item);

    protected abstract boolean isItemClickable(T item);

    @Override
    protected void init() {
        int maxPage = Math.max(1, (list.size() + maxItemsPerPage - 1) / maxItemsPerPage);
        this.page = Math.max(1, Math.min(this.page, maxPage));

        int startIndex = (page - 1) * maxItemsPerPage;
        int endIndex = Math.min(startIndex + maxItemsPerPage, list.size());

        List<T> itemsToDisplay = list.subList(startIndex, endIndex);

        int totalContentHeight = itemsToDisplay.isEmpty()
                ? 0
                : (itemsToDisplay.size() * buttonHeight) + ((itemsToDisplay.size() - 1) * gap);
        int startY = itemsToDisplay.isEmpty() ? (this.height / 2) : (this.height - totalContentHeight) / 2;

        int currentY = startY;
        int middleX = (this.width - this.buttonWidth) / 2;
        int listBottomY = startY + totalContentHeight;

        // Title
        StringWidget title = new StringWidget(this.title, this.font);
        title.setPosition(this.width / 2 - title.getWidth() / 2, 20 + this.font.lineHeight / 2 + 2);
        this.addRenderableWidget(title);

        for (T item : itemsToDisplay) {
            addWidgetsForItem(middleX, currentY, item);
            currentY += buttonHeight + gap;
        }

        // Navigation
        int navigationPageY = listBottomY + (this.height - listBottomY) / 2 - 10;
        int previousPageButtonX = this.width / 2 - this.buttonWidth / 2;
        int nextPageButtonX = this.width / 2 + this.buttonWidth / 2 - 20;

        if (page > 1) {
            Button previousPageButton = Button.builder(Component.literal("<"), b -> {
                        changePage(page - 1);
                    })
                    .bounds(previousPageButtonX, navigationPageY, 20, 20)
                    .build();
            previousPageButton.setTooltip(Tooltip.create(
                    Translation.message("screen.pagination.page_out_of", page - 1, maxPage), this.title));
            this.addRenderableWidget(previousPageButton);
        }

        if (page < maxPage) {
            Button nextPageButton = Button.builder(Component.literal(">"), b -> {
                        changePage(page + 1);
                    })
                    .bounds(nextPageButtonX, navigationPageY, 20, 20)
                    .build();
            nextPageButton.setTooltip(Tooltip.create(
                    Translation.message("screen.pagination.page_out_of", page + 1, maxPage), this.title));
            this.addRenderableWidget(nextPageButton);
        }

        StringWidget pageIndicator = new StringWidget(Translation.message("screen.pagination.page", page), this.font);
        pageIndicator.setPosition(
                this.width / 2 - pageIndicator.getWidth() / 2, navigationPageY + pageIndicator.getHeight() / 2 + 2);
        this.addRenderableWidget(pageIndicator);

        EditBox skipToPageBox = new EditBox(this.font, 20, 20, Component.empty());
        skipToPageBox.setTooltip(Tooltip.create(Translation.message("screen.pagination.skip_to_page_tootip")));
        skipToPageBox.setPosition(this.width / 2 - 25, navigationPageY + 25);
        this.addRenderableWidget(skipToPageBox);

        Button skipToPageButton = Button.builder(Component.literal("Go"), b -> {
                    String value = skipToPageBox.getValue();
                    if (!value.matches(Utils.ONLY_NUMBERS)) {
                        UtilsClient.sendToast(Translation.message("error"), Translation.message("error.only_numbers"));
                        return;
                    }
                    if (value.isEmpty()) return;
                    changePage(Integer.parseInt(skipToPageBox.getValue()));
                })
                .bounds(this.width / 2, navigationPageY + 25, 20, 20)
                .build();
        this.addRenderableWidget(skipToPageButton);

        if (itemsToDisplay.isEmpty()) {
            StringWidget emptyText = new StringWidget(Translation.message("screen.pagination.empty"), this.font);
            emptyText.setPosition(
                    this.width / 2 - emptyText.getWidth() / 2, this.height / 2 - this.font.lineHeight / 2);
            this.addRenderableWidget(emptyText);
        }
    }

    public void addWidgetsForItem(int x, int y, T item) {
        addMainButton(x, y, item);
    }

    protected void addMainButton(int x, int y, T item) {
        Button button = Button.builder(Component.literal(getItemName(item)), b -> {
                    onItemClicked(item);
                })
                .bounds(x, y, buttonWidth, buttonHeight)
                .build();
        button.active = isItemClickable(item);

        this.addRenderableWidget(button);
    }

    private void changePage(int page) {
        int maxPage = Math.max(1, (list.size() + maxItemsPerPage - 1) / maxItemsPerPage);
        this.page = Math.max(1, Math.min(page, maxPage));
        this.clearWidgets();
        this.init();
    }

    public int getPage() {
        return page;
    }

    public int getMaxItemsPerPage() {
        return maxItemsPerPage;
    }

    public void setMaxItemsPerPage(int maxItemsPerPage) {
        this.maxItemsPerPage = maxItemsPerPage;
    }

    public int getGap() {
        return gap;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }

    public int getButtonWidth() {
        return buttonWidth;
    }

    public void setButtonWidth(int buttonWidth) {
        this.buttonWidth = buttonWidth;
    }

    public int getButtonHeight() {
        return buttonHeight;
    }

    public void setButtonHeight(int buttonHeight) {
        this.buttonHeight = buttonHeight;
    }
}
