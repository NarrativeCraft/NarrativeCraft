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

package fr.loudo.narrativecraft.client.studio;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.loudo.narrativecraft.utils.UtilsClient;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.world.level.GameType;

public class NarrativeStudio {

    private static final Minecraft minecraft = Minecraft.getInstance();
    private static final NarrativeStudio instance = new NarrativeStudio();

    private boolean open = false;

    private final ImBoolean showDemoWindow = new ImBoolean(false);
    private final ImFloat exampleValue = new ImFloat(0.5f);
    private final ImString exampleText = new ImString("Hello NarrativeCraft", 256);
    private int clickCount;
    private GameType lastGameType;

    private NarrativeStudio() {}

    public static NarrativeStudio getInstance() {
        return instance;
    }

    public boolean isOpen() {
        return open;
    }

    public void open() {
        open = true;
        lastGameType = minecraft.gameMode.getPlayerMode();
        minecraft.mouseHandler.releaseMouse();
        minecraft.player.connection.send(new ServerboundChangeGameModePacket(GameType.SPECTATOR));
        UtilsClient.setHudHidden(true);
    }

    public void close() {
        open = false;
        minecraft.mouseHandler.grabMouse();
        minecraft.player.connection.send(new ServerboundChangeGameModePacket(lastGameType));
        UtilsClient.setHudHidden(false);
    }

    public void toggle() {
        if (open) {
            close();
        } else {
            open();
        }
    }

    public void render(ImGuiIO io) {
        ImGui.setNextWindowPos(20, 20, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(360, 220, ImGuiCond.FirstUseEver);
        if (ImGui.begin("NarrativeCraft Studio")) {
            ImGui.text("Backend: " + RenderSystem.getDevice().getDeviceInfo().backendName());
            ImGui.text(String.format("%.1f FPS (%.2f ms)", io.getFramerate(), 1000.0f / io.getFramerate()));
            ImGui.separator();

            ImGui.inputText("Text", exampleText);
            ImGui.sliderFloat("Value", exampleValue.getData(), 0.0f, 1.0f);
            if (ImGui.button("Click me")) {
                clickCount++;
            }
            ImGui.sameLine();
            ImGui.text("Clicked " + clickCount + " times");

            ImGui.checkbox("Show ImGui demo", showDemoWindow);
        }
        ImGui.end();

        if (showDemoWindow.get()) {
            ImGui.showDemoWindow(showDemoWindow);
        }
    }
}
