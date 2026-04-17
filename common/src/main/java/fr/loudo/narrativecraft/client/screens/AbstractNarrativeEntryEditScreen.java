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

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.NarrativeEntryAction;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class AbstractNarrativeEntryEditScreen<T extends NarrativeEntry<?>> extends Screen {

    protected static final int GLOBAL_WIDTH = 160;
    protected static final int GAP = 5;

    protected T entry;
    protected final Screen lastScreen;
    protected final List<AbstractWidget> widgets = new ArrayList<>();

    protected StringWidget nameText;
    protected EditBox nameBox;
    protected StringWidget descriptionText;
    protected MultiLineEditBox descriptionBox;
    protected Button sendButton;
    protected Button closeButton;

    public AbstractNarrativeEntryEditScreen(Screen lastScreen) {
        super(Component.literal("Narrative Entry Editor"));
        this.lastScreen = lastScreen;
    }

    public AbstractNarrativeEntryEditScreen(T entry, Screen lastScreen) {
        this(lastScreen);
        this.entry = entry;
    }

    @Override
    protected void init() {
        widgets.clear();

        nameText = new StringWidget(Translation.message("name"), this.font);
        nameBox = new EditBox(this.font, GLOBAL_WIDTH, 20, Translation.message("name"));
        nameBox.setValue(entry != null ? entry.getName() : "");

        descriptionText = new StringWidget(Translation.message("description"), this.font);
        descriptionBox =
                new MultiLineEditBox.Builder().build(this.font, GLOBAL_WIDTH, 90, Translation.message("description"));
        if (entry != null) descriptionBox.setValue(entry.getDescription());

        sendButton = Button.builder(Translation.message("send"), (b) -> {
                    boolean validated = hasValidated();
                    if (!validated) return;

                    T instance = createInstance();

                    if (entry == null) {
                        Services.PACKET.sendToServer(new BiSyncNarrativeEntryPacket(
                                instance.getId(), instance.toPayload(), NarrativeEntryAction.ADD));
                    } else {
                        Services.PACKET.sendToServer(new BiSyncNarrativeEntryPacket(
                                entry.getId(), instance.toPayload(), NarrativeEntryAction.EDIT));
                    }
                    minecraft.setScreen(lastScreen);
                })
                .size(GLOBAL_WIDTH, 20)
                .build();

        closeButton = Button.builder(Translation.message("close"), (b) -> {
                    this.onClose();
                })
                .size(GLOBAL_WIDTH, 20)
                .build();

        addElementToWidgetsList(nameText);
        addElementToWidgetsList(nameBox);
        if (showDescription()) {
            addElementToWidgetsList(descriptionText);
            addElementToWidgetsList(descriptionBox);
        }

        addCustomFields();

        addElementToWidgetsList(sendButton);
        addElementToWidgetsList(closeButton);
        layoutAndAddRenderableWidgets();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }

    public Screen getLastScreen() {
        return lastScreen;
    }

    protected void addElementToWidgetsList(AbstractWidget widget) {
        widgets.add(widget);
    }

    protected boolean hasValidated() {
        String name = getName();
        if (name.isEmpty()) {
            sendToastError(Translation.message("error"), Translation.message("error.must_have_name"));
            return false;
        }
        if (!name.matches(Utils.NO_SPECIAL_CHARACTERS)) {
            sendToastError(Translation.message("error"), Translation.message("error.no_special_characters"));
            return false;
        }
        return true;
    }

    protected void sendToastError(Component title, Component message) {
        minecraft.getToastManager().addToast(new SystemToast(new SystemToast.SystemToastId(), title, message));
    }

    protected boolean showDescription() {
        return true;
    }

    protected abstract void addCustomFields();

    protected void layoutAndAddRenderableWidgets() {
        int x = getXPos();
        int y = getStartYPos();

        for (AbstractWidget widget : widgets) {
            renderWidget(widget, x, y);
            y += widget.getHeight() + GAP;
        }
    }

    protected int getXPos() {
        return this.width / 2 - GLOBAL_WIDTH / 2;
    }

    protected void renderWidget(AbstractWidget widget, int x, int y) {
        widget.setPosition(x, y);
        addRenderableWidget(widget);
    }

    protected int getStartYPos() {
        int totalHeight = 0;
        int gapCount = widgets.isEmpty() ? 0 : widgets.size() - 1;

        for (AbstractWidget widget : widgets) {
            totalHeight += widget.getHeight();
        }
        totalHeight += gapCount * GAP;

        return (this.height / 2) - (totalHeight / 2);
    }

    protected String getName() {
        return nameBox.getValue();
    }

    protected String getDescription() {
        return descriptionBox.getValue();
    }

    protected abstract T createInstance();
}
