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

package fr.loudo.narrativecraft.client.screens.narrative.cameraangle;

import fr.loudo.narrativecraft.narrative.cameraangle.CameraView;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CameraAngleCameraNameScreen extends Screen {

    private static final int FIELD_WIDTH = 160;
    private static final int FIELD_HEIGHT = 20;
    private static final int GAP = 5;

    private final Component prompt;
    private final String initialName;
    private final CameraView existingCameraView;
    private final Consumer<String> onConfirm;
    private final Screen lastScreen;

    private EditBox nameBox;

    public CameraAngleCameraNameScreen(
            Component prompt,
            String initialName,
            CameraView existingCameraView,
            Consumer<String> onConfirm,
            Screen lastScreen) {
        super(prompt);
        this.prompt = prompt;
        this.initialName = initialName;
        this.existingCameraView = existingCameraView;
        this.onConfirm = onConfirm;
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        StringWidget promptWidget = new StringWidget(prompt, this.font);
        promptWidget.setPosition(centerX - promptWidget.getWidth() / 2, centerY - FIELD_HEIGHT - GAP - 20);
        addRenderableWidget(promptWidget);

        nameBox = new EditBox(this.font, FIELD_WIDTH, FIELD_HEIGHT, Translation.message("name"));
        nameBox.setValue(initialName);
        nameBox.setPosition(centerX - FIELD_WIDTH / 2, centerY - FIELD_HEIGHT / 2);
        addRenderableWidget(nameBox);

        Button confirmButton = Button.builder(Translation.message("send"), b -> confirm())
                .bounds(centerX - FIELD_WIDTH / 2, centerY + FIELD_HEIGHT / 2 + GAP, FIELD_WIDTH, FIELD_HEIGHT)
                .build();
        addRenderableWidget(confirmButton);

        Button closeButton = Button.builder(Translation.message("close"), b -> onClose())
                .bounds(
                        centerX - FIELD_WIDTH / 2,
                        centerY + FIELD_HEIGHT / 2 + GAP + FIELD_HEIGHT + GAP,
                        FIELD_WIDTH,
                        FIELD_HEIGHT)
                .build();
        addRenderableWidget(closeButton);

        setInitialFocus(nameBox);
    }

    private void confirm() {
        String value = nameBox.getValue().trim();
        if (value.isEmpty()) return;
        onConfirm.accept(value);
        onClose();
    }

    public CameraView getExistingCamera() {
        return existingCameraView;
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(lastScreen);
    }
}
