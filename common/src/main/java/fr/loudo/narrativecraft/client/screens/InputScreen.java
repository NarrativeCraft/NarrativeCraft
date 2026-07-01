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

package fr.loudo.narrativecraft.client.screens;

import fr.loudo.narrativecraft.utils.Translation;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class InputScreen extends Screen {

    private static final int FIELD_WIDTH = 160;
    private static final int FIELD_HEIGHT = 20;
    private static final int GAP = 5;

    private final Component prompt;
    private final Consumer<String> onSend;
    private final Screen lastScreen;

    private EditBox inputBox;

    public InputScreen(Component prompt, Consumer<String> onSend, Screen lastScreen) {
        super(prompt);
        this.prompt = prompt;
        this.onSend = onSend;
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int centerY = height / 2;

        StringWidget promptWidget = new StringWidget(prompt, font);
        promptWidget.setPosition(centerX - promptWidget.getWidth() / 2, centerY - FIELD_HEIGHT - GAP - 20);
        addRenderableWidget(promptWidget);

        inputBox = new EditBox(font, 0, 0, FIELD_WIDTH, FIELD_HEIGHT, Component.empty());
        inputBox.setPosition(centerX - FIELD_WIDTH / 2, centerY - FIELD_HEIGHT / 2);
        addRenderableWidget(inputBox);

        addRenderableWidget(Button.builder(Translation.message("send"), b -> confirm())
                .bounds(centerX - FIELD_WIDTH / 2, centerY + FIELD_HEIGHT / 2 + GAP, FIELD_WIDTH, FIELD_HEIGHT)
                .build());

        addRenderableWidget(Button.builder(Translation.message("close"), b -> onClose())
                .bounds(
                        centerX - FIELD_WIDTH / 2,
                        centerY + FIELD_HEIGHT / 2 + GAP + FIELD_HEIGHT + GAP,
                        FIELD_WIDTH,
                        FIELD_HEIGHT)
                .build());

        setInitialFocus(inputBox);
    }

    private void confirm() {
        String raw = inputBox.getValue().trim();
        if (raw.isEmpty()) return;
        onSend.accept(raw);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }
}
