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

package fr.loudo.narrativecraft.editors;

import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import net.minecraft.client.gui.GuiGraphics;

public interface EditorMaker {

    void init();

    void close();

    void tick();

    void teleportToEditorOrigin();

    NarrativeEnvironment getEnvironment();

    default void render(GuiGraphics graphics, float deltaTracker) {}

    default void charTyped(char codePoint, int modifiers) {}

    default void keyPressed(int keyCode, int scanCode, int modifiers) {}

    default void mouseClicked(double mouseX, double mouseY, int button, boolean isDoubleClick) {}

    default void mouseReleased(double mouseX, double mouseY, int button) {}

    default void mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {}

    default void mouseScrolled(double deltaX, double deltaY) {}
}
