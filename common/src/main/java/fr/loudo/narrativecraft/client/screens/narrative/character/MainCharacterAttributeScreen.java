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

package fr.loudo.narrativecraft.client.screens.narrative.character;

import fr.loudo.narrativecraft.narrative.character.MainCharacterAttribute;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MainCharacterAttributeScreen extends Screen {

    private static final int BUTTON_WIDTH = 160;
    private static final int BUTTON_HEIGHT = 20;
    private static final int GAP = 5;

    private final Screen lastScreen;
    private final MainCharacterAttribute attribute;

    public MainCharacterAttributeScreen(Screen lastScreen, MainCharacterAttribute attribute) {
        super(Component.empty());
        this.lastScreen = lastScreen;
        this.attribute = attribute;
    }

    private Button skinModeButton;

    @Override
    protected void init() {
        skinModeButton = Button.builder(buildSkinModeLabel(), b -> {
                    MainCharacterAttribute.SkinMode[] values = MainCharacterAttribute.SkinMode.values();
                    int next = (attribute.getSkin().ordinal() + 1) % values.length;
                    attribute.setSkin(values[next]);
                    b.setMessage(buildSkinModeLabel());
                })
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        skinModeButton.active = attribute.isMainCharacter();

        Button mainCharacterButton = Button.builder(buildMainCharacterLabel(), b -> {
                    attribute.setMainCharacter(!attribute.isMainCharacter());
                    b.setMessage(buildMainCharacterLabel());
                    skinModeButton.active = attribute.isMainCharacter();
                })
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

        Button closeButton = Button.builder(Translation.message("close"), b -> onClose())
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

        List<AbstractWidget> buttons = List.of(mainCharacterButton, skinModeButton, closeButton);
        int totalHeight = buttons.size() * BUTTON_HEIGHT + (buttons.size() - 1) * GAP;
        int startY = (this.height / 2) - (totalHeight / 2);
        int x = this.width / 2 - BUTTON_WIDTH / 2;

        for (int i = 0; i < buttons.size(); i++) {
            AbstractWidget button = buttons.get(i);
            button.setPosition(x, startY + i * (BUTTON_HEIGHT + GAP));
            addRenderableWidget(button);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }

    private Component buildMainCharacterLabel() {
        return Translation.message(
                "screen.character.is_main_character",
                attribute.isMainCharacter()
                        ? Translation.message("yes").getString()
                        : Translation.message("no").getString());
    }

    private Component buildSkinModeLabel() {
        return Translation.message(
                "screen.character.skin", attribute.getSkin().name().toLowerCase());
    }
}
