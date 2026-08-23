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

package fr.loudo.narrativecraft.client.screens.narrative.character;

import fr.loudo.narrativecraft.client.screens.components.MultiLineComponent;
import fr.loudo.narrativecraft.narrative.character.MainCharacterAttribute;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CharacterSettingsScreen extends Screen {

    private static final int GLOBAL_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int GAP = 5;
    private static final int TOP_PADDING = 30;
    private static final int BOTTOM_PADDING = 10;
    private static final int CUSTOM_NBT_BOX_HEIGHT = 80;

    private final Screen lastScreen;
    private final MainCharacterAttribute mainCharacterAttribute;
    private final Consumer<String> onCustomNbtChanged;

    private String customNbt;

    public CharacterSettingsScreen(
            Screen lastScreen,
            MainCharacterAttribute mainCharacterAttribute,
            String customNbt,
            Consumer<String> onCustomNbtChanged) {
        super(Component.empty());
        this.lastScreen = lastScreen;
        this.mainCharacterAttribute = mainCharacterAttribute;
        this.customNbt = customNbt == null ? "" : customNbt;
        this.onCustomNbtChanged = onCustomNbtChanged;
    }

    @Override
    protected void init() {
        Button mainCharacterSettingsButton = Button.builder(
                        Translation.message("screen.character.main_character_settings"),
                        button ->
                                minecraft.gui.setScreen(new MainCharacterAttributeScreen(this, mainCharacterAttribute)))
                .size(GLOBAL_WIDTH, BUTTON_HEIGHT)
                .build();

        Button customNbtButton = Button.builder(
                        Translation.message("screen.character.custom_nbt"),
                        button -> minecraft.gui.setScreen(new MultiLineComponent(
                                this,
                                Translation.message("screen.character.custom_nbt"),
                                GLOBAL_WIDTH,
                                CUSTOM_NBT_BOX_HEIGHT,
                                customNbt,
                                value -> {
                                    customNbt = value;
                                    onCustomNbtChanged.accept(value);
                                    minecraft.gui.setScreen(this);
                                })))
                .size(GLOBAL_WIDTH, BUTTON_HEIGHT)
                .build();

        Button closeButton = Button.builder(Translation.message("close"), button -> this.onClose())
                .size(GLOBAL_WIDTH, BUTTON_HEIGHT)
                .build();

        List<AbstractWidget> buttons = List.of(mainCharacterSettingsButton, customNbtButton);
        int x = this.width / 2 - GLOBAL_WIDTH / 2;

        for (int i = 0; i < buttons.size(); i++) {
            AbstractWidget button = buttons.get(i);
            button.setPosition(x, TOP_PADDING + i * (BUTTON_HEIGHT + GAP));
            addRenderableWidget(button);
        }

        closeButton.setPosition(x, this.height - BUTTON_HEIGHT - BOTTOM_PADDING);
        addRenderableWidget(closeButton);
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(lastScreen);
    }
}
