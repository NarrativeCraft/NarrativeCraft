/*
 * NarrativeCraft - Create narrative games inside Minecraft. No coding, no game engine, only text and logic.
 * Copyright (c) 2026 LOUDO and contributors
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
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.TextInputManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.backend.api.GpuDeviceBackend;
import com.mojang.renderpearl.backend.opengl.*;
import fr.loudo.narrativecraft.client.util.Icons;
import fr.loudo.narrativecraft.mixin.imgui.FrontendGpuDeviceImGuiAccessor;
import fr.loudo.narrativecraft.mixin.imgui.GlDeviceImGuiAccessor;
import imgui.ImDrawData;
import imgui.ImFontAtlas;
import imgui.ImFontConfig;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.assertion.ImAssertCallback;
import imgui.extension.implot.ImPlot;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.sdl3.ImGuiImplSdl3;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.sdl.SDLVideo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ImGuiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger("NarrativeCraft ImGui");
    private static final String INI_FILE = "config/narrativecraft-imgui.ini";
    private static final String ICON_FONT = "/assets/narrativecraft/imgui/fonts/font_awesome_icons.otf";
    private static final float FONT_SIZE = 13.0F;
    private static final ImGuiClient INSTANCE = new ImGuiClient();
    private static final Vector4fc CLEAR_COLOR = new Vector4f(0.07F, 0.07F, 0.08F, 1.0F);

    private final ImGuiImplSdl3 platform = new ImGuiImplSdl3();
    private final List<ImGuiRenderableClient> overlays = new CopyOnWriteArrayList<>();
    private @Nullable ImGuiImplGl3 glRenderer;
    private @Nullable ImGuiBlaze3DRendererClient blaze3DRenderer;
    private @Nullable TextureTarget outputTarget;
    private @Nullable RenderTarget frameOutput;
    private @Nullable OutputUnderlay outputUnderlay;
    private boolean created;

    private ImGuiClient() {}

    public static ImGuiClient getInstance() {
        return INSTANCE;
    }

    public boolean isCreated() {
        return created;
    }

    public void addOverlay(ImGuiRenderableClient renderable) {
        if (!overlays.contains(renderable)) {
            overlays.add(renderable);
        }
    }

    public void removeOverlay(ImGuiRenderableClient renderable) {
        overlays.remove(renderable);
    }

    public void create(long handle) {
        if (created) {
            return;
        }
        ImGui.createContext();
        ImPlot.createContext();

        ImGuiIO io = ImGui.getIO();
        loadFonts(io.getFonts());
        io.getFonts().build();
        io.setIniFilename(INI_FILE);
        io.setConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.setConfigErrorRecovery(true);
        io.setConfigErrorRecoveryEnableAssert(false);
        ImGui.setAssertCallback(new ImAssertCallback() {
            @Override
            public void imAssertCallback(String assertion, int line, String file) {
                LOGGER.error("ImGui assertion failed: {} at {}:{}", assertion, file, line);
            }
        });

        GpuDevice device = RenderSystem.getDevice();
        String backendName = device.getDeviceInfo().backendName();
        if ("Vulkan".equals(backendName)) {
            blaze3DRenderer = new ImGuiBlaze3DRendererClient();
            platform.initForVulkan(handle);
        } else {
            glRenderer = new ImGuiImplGl3();
            glRenderer.init();
            platform.initForOpenGL(handle, 0);
        }
        created = true;
        LOGGER.info("ImGui initialized with the {} backend", backendName);
    }

    private static void loadFonts(ImFontAtlas fonts) {
        fonts.addFontDefault();
        try (InputStream in = ImGuiClient.class.getResourceAsStream(ICON_FONT)) {
            if (in == null) {
                LOGGER.error("Icon font not found: {}", ICON_FONT);
                return;
            }
            ImFontConfig config = new ImFontConfig();
            config.setMergeMode(true);
            config.setPixelSnapH(true);
            config.setGlyphMinAdvanceX(FONT_SIZE);
            // Private use area only: the font also has latin glyphs that would replace the default font ones
            short[] ranges = {(short) Icons.MIN, (short) Icons.MAX, 0};
            fonts.addFontFromMemoryTTF(in.readAllBytes(), FONT_SIZE, config, ranges);
            config.destroy();
        } catch (IOException e) {
            LOGGER.error("Failed to load the icon font", e);
        }
    }

    public void processEvent(long eventAddress) {
        if (created) {
            platform.processEvent(eventAddress);
        }
    }

    /**
     * Draws into the full window output before ImGui, with plain copies rather than ImGui's alpha blended images.
     */
    @FunctionalInterface
    public interface OutputUnderlay {
        void draw(RenderTarget output);
    }

    /**
     * When set, ImGui is drawn into a dedicated target sized to the whole window and that target is presented
     * instead of the main render target, so the game can be rendered smaller than the window. Each frame the target
     * is cleared, the underlay is drawn, then ImGui on top.
     *
     * @param underlay what goes under the ImGui windows, or null to draw ImGui over the main render target again
     */
    public void setFullWindowOutput(@Nullable OutputUnderlay underlay) {
        outputUnderlay = underlay;
    }

    /**
     * @return the target presented to the window this frame, or null to present the main render target
     */
    public @Nullable RenderTarget getFrameOutput() {
        return frameOutput;
    }

    public void renderFrame() {
        frameOutput = null;
        if (!created) {
            return;
        }
        ImGuiRenderableClient screen =
                Minecraft.getInstance().gui.screen() instanceof ImGuiRenderableClient renderable ? renderable : null;
        RenderTarget output = prepareOutputTarget();
        if (screen == null && overlays.isEmpty()) {
            updateTextInput(false);
            ImGui.getIO().clearEventsQueue();
            return;
        }
        ImGuiRenderableClient frame = io -> {
            for (ImGuiRenderableClient overlay : overlays) {
                overlay.render(io);
            }
            if (screen != null) {
                screen.render(io);
            }
        };
        if (output != null && outputUnderlay != null) {
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(output.getColorTexture(), CLEAR_COLOR);
            outputUnderlay.draw(output);
            draw(output, frame);
            frameOutput = output;
        } else {
            draw(Minecraft.getInstance().gameRenderer.mainRenderTarget(), frame);
        }
    }

    public void copyTexture(
            GpuTexture source,
            GpuTexture destination,
            int sourceX,
            int sourceY,
            int destX,
            int destY,
            int width,
            int height) {
        if (glRenderer == null) {
            RenderSystem.getDevice()
                    .createCommandEncoder()
                    .copyTextureToTexture(source, destination, 0, destX, destY, sourceX, sourceY, width, height);
            return;
        }
        GpuDeviceBackend backend = ((FrontendGpuDeviceImGuiAccessor) RenderSystem.getDevice()).getBackend();
        DirectStateAccess directStateAccess = ((GlDeviceImGuiAccessor) backend).getDirectStateAccess();
        FrameBufferCache frameBufferCache = ((GlDeviceImGuiAccessor) backend).getFrameBufferCache();
        List<FrameBufferAttachment> sourceTextures = Collections.singletonList((GlTexture) source);
        List<FrameBufferAttachment> destinationTextures = Collections.singletonList((GlTexture) destination);
        GlStateManager._disableScissorTest();
        directStateAccess.blitFrameBuffers(
                frameBufferCache.getFbo(directStateAccess, sourceTextures, null),
                frameBufferCache.getFbo(directStateAccess, destinationTextures, null),
                sourceX,
                sourceY,
                sourceX + width,
                sourceY + height,
                destX,
                destY,
                destX + width,
                destY + height,
                GL11C.GL_COLOR_BUFFER_BIT,
                GL11C.GL_NEAREST);
    }

    private @Nullable RenderTarget prepareOutputTarget() {
        if (outputUnderlay == null) {
            releaseOutputTarget();
            return null;
        }
        Window.FramebufferSize size = Minecraft.getInstance().getWindow().queryFramebufferSize();
        if (size.width() <= 0 || size.height() <= 0) {
            return null;
        }
        if (outputTarget == null) {
            outputTarget = new TextureTarget(
                    "NarrativeCraft ImGui Output", size.width(), size.height(), GpuFormat.RGBA8_UNORM, null);
        } else if (outputTarget.width != size.width() || outputTarget.height != size.height()) {
            outputTarget.resize(size.width(), size.height());
        }
        return outputTarget;
    }

    private void releaseOutputTarget() {
        if (outputTarget != null) {
            outputTarget.destroyBuffers();
            outputTarget = null;
        }
    }

    public void draw(ImGuiRenderableClient renderable) {
        draw(Minecraft.getInstance().gameRenderer.mainRenderTarget(), renderable);
    }

    private void draw(RenderTarget framebuffer, ImGuiRenderableClient renderable) {
        if (!created) {
            return;
        }
        updateTextInput(ImGui.getIO().getWantTextInput());
        if (glRenderer != null) {
            GpuDeviceBackend backend = ((FrontendGpuDeviceImGuiAccessor) RenderSystem.getDevice()).getBackend();
            DirectStateAccess directStateAccess = ((GlDeviceImGuiAccessor) backend).getDirectStateAccess();
            FrameBufferCache frameBufferCache = ((GlDeviceImGuiAccessor) backend).getFrameBufferCache();
            List<FrameBufferAttachment> colorTextures =
                    Collections.singletonList((GlTexture) framebuffer.getColorTexture());
            GlStateManager._glBindFramebuffer(
                    GL30C.GL_FRAMEBUFFER, frameBufferCache.getFbo(directStateAccess, colorTextures, null));
            GlStateManager._viewport(0, 0, framebuffer.width, framebuffer.height);

            glRenderer.newFrame();
            platform.newFrame();
            ImGui.newFrame();
            renderable.render(ImGui.getIO());
            ImGui.render();
            glRenderer.renderDrawData(ImGui.getDrawData());

            GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);

            if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
                long backupWindow = SDLVideo.SDL_GL_GetCurrentWindow();
                long backupContext = SDLVideo.SDL_GL_GetCurrentContext();
                ImGui.updatePlatformWindows();
                ImGui.renderPlatformWindowsDefault();
                SDLVideo.SDL_GL_MakeCurrent(backupWindow, backupContext);
            }
        } else if (blaze3DRenderer != null) {
            blaze3DRenderer.newFrame();
            platform.newFrame();
            ImGui.newFrame();
            renderable.render(ImGui.getIO());
            ImGui.render();

            ImDrawData drawData = ImGui.getDrawData();
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            blaze3DRenderer.uploadDrawData(drawData, encoder);
            try (RenderPass renderPass = encoder.createRenderPass(
                    () -> "NarrativeCraft ImGui", framebuffer.getColorTextureView(), Optional.empty())) {
                blaze3DRenderer.renderDrawData(drawData, renderPass);
            }
            encoder.submit();
        }
    }

    private void updateTextInput(boolean wanted) {
        TextInputManager textInputManager = Minecraft.getInstance().textInputManager();
        if (wanted) {
            textInputManager.startTextInput(this);
        } else {
            textInputManager.stopTextInput(this);
        }
    }

    public void dispose() {
        if (!created) {
            return;
        }
        if (glRenderer != null) {
            glRenderer.shutdown();
            glRenderer = null;
        }
        if (blaze3DRenderer != null) {
            blaze3DRenderer.dispose();
            blaze3DRenderer = null;
        }
        releaseOutputTarget();
        platform.shutdown();
        ImPlot.destroyContext();
        ImGui.destroyContext();
        created = false;
    }
}
