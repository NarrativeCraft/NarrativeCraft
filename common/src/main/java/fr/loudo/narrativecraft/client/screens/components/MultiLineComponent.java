/*
 * NarrativeCraft - Create narrative games inside Minecraft. No coding, no game engine, only text and logic.
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

package fr.loudo.narrativecraft.client.screens.components;

import fr.loudo.narrativecraft.utils.Translation;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MultiLineComponent extends Screen {

    private static final int GAP = 5;
    private static final int BUTTON_HEIGHT = 20;

    private final Screen lastScreen;
    private final Component label;
    private final int globalWidth;
    private final int multiLineHeight;
    private final Consumer<String> onSend;

    private StringWidget labelText;
    private MultiLineEditBox multiLineEditBox;
    private Button sendButton;
    private Button closeButton;

    private String value;

    public MultiLineComponent(
            Screen lastScreen,
            Component label,
            int globalWidth,
            int multiLineHeight,
            String defaultValue,
            Consumer<String> onSend) {
        super(label);
        this.lastScreen = lastScreen;
        this.label = label;
        this.globalWidth = globalWidth;
        this.multiLineHeight = multiLineHeight;
        this.value = defaultValue == null ? "" : defaultValue;
        this.onSend = onSend;
    }

    @Override
    protected void init() {
        if (multiLineEditBox != null) {
            value = multiLineEditBox.getValue();
        }

        labelText = new StringWidget(label, this.font);

        multiLineEditBox = new MultiLineEditBox.Builder().build(this.font, globalWidth, multiLineHeight, label);
        multiLineEditBox.setValue(value);

        sendButton = Button.builder(Translation.message("send"), button -> onSend.accept(multiLineEditBox.getValue()))
                .size(globalWidth, BUTTON_HEIGHT)
                .build();

        closeButton = Button.builder(Translation.message("close"), button -> this.onClose())
                .size(globalWidth, BUTTON_HEIGHT)
                .build();

        int totalHeight = labelText.getHeight() + multiLineHeight + BUTTON_HEIGHT * 2 + GAP * 3;
        int x = this.width / 2 - globalWidth / 2;
        int y = this.height / 2 - totalHeight / 2;

        labelText.setPosition(x, y);
        y += labelText.getHeight() + GAP;

        multiLineEditBox.setPosition(x, y);
        y += multiLineHeight + GAP;

        sendButton.setPosition(x, y);
        y += BUTTON_HEIGHT + GAP;

        closeButton.setPosition(x, y);

        addRenderableWidget(labelText);
        addRenderableWidget(multiLineEditBox);
        addRenderableWidget(sendButton);
        addRenderableWidget(closeButton);
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(lastScreen);
    }

    public Screen getLastScreen() {
        return lastScreen;
    }

    public String getValue() {
        return multiLineEditBox == null ? value : multiLineEditBox.getValue();
    }
}
