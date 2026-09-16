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

import com.mojang.blaze3d.opengl.FrameBufferAttachment;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.mixin.accessor.GlDeviceAccessor;
import fr.loudo.narrativecraft.mixin.accessor.GpuDeviceAccessor;
import imgui.ImDrawData;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;

public class ClientImGui {

    private static final String VULKAN_BACKEND_NAME = "Vulkan";
    private static final ClientImGui instance = new ClientImGui();

    private final ImGuiImplGlfw platform = new ImGuiImplGlfw();
    private ImGuiImplGl3 openGlRenderer;
    private ClientImGuiBlaze3DRenderer blaze3dRenderer;
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

        String backendName = RenderSystem.getDevice().getDeviceInfo().backendName();
        if (VULKAN_BACKEND_NAME.equals(backendName)) {
            blaze3dRenderer = new ClientImGuiBlaze3DRenderer();
            platform.initForVulkan(windowHandle, true);
        } else {
            openGlRenderer = new ImGuiImplGl3();
            platform.initForOpenGL(windowHandle, true);
            openGlRenderer.init();
        }
        created = true;
        NarrativeCraftMod.LOGGER.info("ImGui initialized with {} backend", backendName);
    }

    public boolean isCreated() {
        return created;
    }

    public void draw(ClientImGuiRenderable renderable) {
        if (!created) return;
        RenderTarget framebuffer = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        if (openGlRenderer != null) {
            drawOpenGl(framebuffer, renderable);
        } else if (blaze3dRenderer != null) {
            drawBlaze3d(framebuffer, renderable);
        }
    }

    private void drawOpenGl(RenderTarget framebuffer, ClientImGuiRenderable renderable) {
        GpuDeviceBackend backend = ((GpuDeviceAccessor) RenderSystem.getDevice()).getBackend();
        GlDeviceAccessor glDevice = (GlDeviceAccessor) backend;
        List<FrameBufferAttachment> colorAttachments = List.of((GlTexture) framebuffer.getColorTexture());
        int framebufferId =
                glDevice.getFrameBufferCache().getFbo(glDevice.getDirectStateAccess(), colorAttachments, null);
        GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, framebufferId);
        GL11C.glViewport(0, 0, framebuffer.width, framebuffer.height);

        openGlRenderer.newFrame();
        platform.newFrame();
        ImGui.newFrame();
        renderable.render(ImGui.getIO());
        ImGui.render();
        openGlRenderer.renderDrawData(ImGui.getDrawData());

        GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);
    }

    private void drawBlaze3d(RenderTarget framebuffer, ClientImGuiRenderable renderable) {
        blaze3dRenderer.newFrame();
        platform.newFrame();
        ImGui.newFrame();
        renderable.render(ImGui.getIO());
        ImGui.render();

        ImDrawData drawData = ImGui.getDrawData();
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        blaze3dRenderer.uploadDrawData(drawData, encoder);
        try (RenderPass renderPass = encoder.createRenderPass(
                () -> "NarrativeCraft ImGui", framebuffer.getColorTextureView(), Optional.empty())) {
            blaze3dRenderer.renderDrawData(drawData, renderPass);
        }
        encoder.submit();
    }

    public void dispose() {
        if (!created) return;
        if (openGlRenderer != null) {
            openGlRenderer.shutdown();
            openGlRenderer = null;
        }
        if (blaze3dRenderer != null) {
            blaze3dRenderer.dispose();
            blaze3dRenderer = null;
        }
        platform.shutdown();
        ImGui.destroyContext();
        created = false;
    }
}
