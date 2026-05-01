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

package fr.loudo.narrativecraft.client.screens.story;

import fr.loudo.narrativecraft.network.story.C2SChoiceSelected;
import fr.loudo.narrativecraft.platform.Services;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChoiceScreen extends Screen {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;

    private final List<String> choices;

    public ChoiceScreen(List<String> choices) {
        super(Component.empty());
        this.choices = choices;
    }

    @Override
    protected void init() {
        int totalHeight = choices.size() * (BUTTON_HEIGHT + BUTTON_GAP) - BUTTON_GAP;
        int startY = (height - totalHeight) / 2;

        for (int i = 0; i < choices.size(); i++) {
            final int index = i;
            int buttonX = (width - BUTTON_WIDTH) / 2;
            int buttonY = startY + i * (BUTTON_HEIGHT + BUTTON_GAP);
            addRenderableWidget(Button.builder(Component.literal(choices.get(i)), b -> select(index))
                    .bounds(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void select(int index) {
        Services.PACKET.sendToServer(new C2SChoiceSelected(index));
        onClose();
    }
}
