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

package fr.loudo.narrativecraft.client.imgui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import imgui.ImDrawData;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.sdl3.ImGuiImplSdl3;
import java.util.Optional;
import net.minecraft.client.Minecraft;

public class ClientImGui {

    private static final ClientImGui instance = new ClientImGui();

    private final ImGuiImplSdl3 platform = new ImGuiImplSdl3();
    private ClientImGuiBlaze3DRenderer renderer;
    private boolean created;

    private ClientImGui() {}

    public static ClientImGui getInstance() {
        return instance;
    }

    public void create(long windowHandle) {
        if (created) return;
        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.getFonts().addFontDefault();
        io.getFonts().build();
        io.setIniFilename(NarrativeCraftMod.MOD_ID + ".ini");
        io.setConfigFlags(ImGuiConfigFlags.DockingEnable);

        renderer = new ClientImGuiBlaze3DRenderer();
        platform.initForVulkan(windowHandle);
        created = true;
        NarrativeCraftMod.LOGGER.info(
                "ImGui initialized with {} backend",
                RenderSystem.getDevice().getDeviceInfo().backendName());
    }

    public boolean isCreated() {
        return created;
    }

    /**
     * Forwards a raw SDL event to ImGui so it can track mouse, keyboard and text input.
     *
     * @param eventAddress native address of the {@code SDL_Event}
     */
    public void processEvent(long eventAddress) {
        if (!created) return;
        platform.processEvent(eventAddress);
    }

    public void draw(ClientImGuiRenderable renderable) {
        if (!created) return;
        RenderTarget framebuffer = Minecraft.getInstance().gameRenderer.mainRenderTarget();

        renderer.newFrame();
        platform.newFrame();
        ImGui.newFrame();
        renderable.render(ImGui.getIO());
        ImGui.render();

        ImDrawData drawData = ImGui.getDrawData();
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        // Buffer uploads must happen outside the render pass
        renderer.uploadDrawData(drawData, encoder);
        try (RenderPass renderPass = encoder.createRenderPass(
                () -> "NarrativeCraft ImGui", framebuffer.getColorTextureView(), Optional.empty())) {
            renderer.renderDrawData(drawData, renderPass);
        }
        encoder.submit();
    }

    public void dispose() {
        if (!created) return;
        if (renderer != null) {
            renderer.dispose();
            renderer = null;
        }
        platform.shutdown();
        ImGui.destroyContext();
        created = false;
    }
}
