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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.*;
import com.mojang.renderpearl.api.textures.*;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import imgui.ImDrawData;
import imgui.ImFontAtlas;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.type.ImInt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.OptionalDouble;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ImGui renderer implementation using the Blaze3D GPU abstraction layer.
 * Used when Minecraft is running with the Vulkan graphics backend.
 *
 * @author Enaium
 */
public class ImGuiBlaze3DRendererClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImGuiBlaze3DRendererClient.class);
    private static final Identifier IMGUI_SHADER_ID = Identifier.fromNamespaceAndPath("narrativecraft", "core/imgui");

    /**
     * ImDrawVert layout: pos(2f) + uv(2f) + col(4ub) = 20 bytes
     */
    private static final VertexFormat IMGUI_VERTEX_FORMAT = VertexFormat.builder(0)
            .addAttribute("Position", GpuFormat.RG32_FLOAT)
            .addAttribute("UV", GpuFormat.RG32_FLOAT)
            .addAttribute("Color", GpuFormat.RGBA8_UNORM)
            .build();

    private @Nullable RenderPipeline renderPipeline;
    private @Nullable CompiledRenderPipeline compiledPipeline;
    private @Nullable GpuTexture fontTexture;
    private @Nullable GpuTextureView fontTextureView;
    private @Nullable GpuSampler fontSampler;
    private @Nullable GpuBuffer vertexBuffer;
    private @Nullable GpuBuffer indexBuffer;
    private @Nullable GpuBuffer projMatrixUniform;
    private long vertexBufferSize;
    private long indexBufferSize;

    private final ByteBuffer projMatrixBuffer = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder());
    private final ImVec4 reusableClipRect = new ImVec4();

    /**
     * Binds the texture associated with an ImGui texture id.
     *
     * @param renderPass pass to bind on
     * @param texId      ImGui texture id ({@code 1} is the font atlas)
     * @return false when the id is unknown and the draw command must be skipped
     */
    private boolean bindDrawTexture(final RenderPass renderPass, final long texId) {
        final GpuTextureView view;
        final GpuSampler sampler;
        if (texId == 1) {
            view = fontTextureView;
            sampler = fontSampler;
        } else {
            final ImGuiTexturesClient.TexEntry entry = ImGuiTexturesClient.INSTANCE.getTexture(texId);
            if (entry == null) {
                return false;
            }
            view = entry.textureView();
            sampler = entry.sampler();
        }
        if (view == null || sampler == null) {
            return false;
        }
        renderPass.setUniform("Texture", view, sampler);
        return true;
    }

    /**
     * Ensures the rendering pipeline and font texture are ready for the current frame.
     * Must be called each frame before rendering.
     */
    public void newFrame() {
        if (renderPipeline == null) {
            createPipeline();
        }
        // Shaders are only resolvable once the initial resource reload has finished;
        // keep retrying every frame instead of crashing or giving up
        if (compiledPipeline == null || compiledPipeline.isClosed()) {
            compiledPipeline = RenderSystem.getCompiledPipelineNullable(renderPipeline);
        }
        if (!isReady()) {
            return;
        }
        if (fontTexture == null) {
            createFontsTexture();
        }

        ImGuiTexturesClient.INSTANCE.clearStale();
    }

    /**
     * @return true when the compiled pipeline is ready to render
     */
    private boolean isReady() {
        return compiledPipeline != null && !compiledPipeline.isClosed() && projMatrixUniform != null;
    }

    private void createPipeline() {
        final GpuDevice device = RenderSystem.getDevice();

        renderPipeline = RenderPipeline.builder()
                .withLocation(IMGUI_SHADER_ID)
                .withVertexShader(IMGUI_SHADER_ID)
                .withFragmentShader(IMGUI_SHADER_ID)
                .withVertexBinding(0, IMGUI_VERTEX_FORMAT)
                .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                .withColorTargetState(new ColorTargetState(new BlendFunction(
                        BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA,
                        BlendFactor.ONE, BlendFactor.ONE_MINUS_SRC_ALPHA)))
                .withCull(false)
                .withPolygonMode(PolygonMode.FILL)
                .withBindGroupLayout(BindGroupLayout.builder()
                        .withUniform("ProjMtx", UniformType.UNIFORM_BUFFER)
                        .build())
                .withBindGroupLayout(BindGroupLayout.builder()
                        .withUniform("Texture", UniformType.COMBINED_IMAGE_SAMPLER)
                        .build())
                .build();

        // Compile pipeline using RenderSystem's pipeline cache
        compiledPipeline = RenderSystem.getCompiledPipelineNullable(renderPipeline);

        if (compiledPipeline == null) {
            LOGGER.error("Failed to compile ImGui pipeline! Shaders may be missing from resources.");
        }

        // Recreate projection matrix uniform buffer (64 bytes for mat4 in std140 layout)
        if (projMatrixUniform != null) {
            projMatrixUniform.close();
        }
        projMatrixUniform =
                device.createBuffer(() -> "ImGui ProjMtx", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 64);
    }

    private void createFontsTexture() {
        final GpuDevice device = RenderSystem.getDevice();
        final ImFontAtlas fontAtlas = ImGui.getIO().getFonts();

        final ImInt width = new ImInt();
        final ImInt height = new ImInt();
        final ByteBuffer pixels = fontAtlas.getTexDataAsRGBA32(width, height);

        // Clean up old texture resources
        disposeFontResources();

        fontTexture = device.createTexture(
                () -> "ImGui Font",
                GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST,
                GpuFormat.RGBA8_UNORM,
                width.get(),
                height.get(),
                1,
                1);
        fontTextureView = device.createTextureView(fontTexture);
        fontSampler = device.createSampler(
                AddressMode.CLAMP_TO_EDGE,
                AddressMode.CLAMP_TO_EDGE,
                FilterMode.LINEAR,
                FilterMode.LINEAR,
                1,
                OptionalDouble.empty());

        // Upload font atlas pixels to GPU texture
        final CommandEncoder encoder = device.createCommandEncoder();
        encoder.writeToTexture(fontTexture, pixels, 0, 0, 0, 0, width.get(), height.get());
        encoder.submit();

        // Mark font texture as ready
        fontAtlas.setTexID(1);
    }

    private void disposeFontResources() {
        if (fontSampler != null) {
            fontSampler.close();
            fontSampler = null;
        }
        if (fontTextureView != null) {
            fontTextureView.close();
            fontTextureView = null;
        }
        if (fontTexture != null) {
            fontTexture.close();
            fontTexture = null;
        }
    }

    /**
     * Uploads vertex, index, and uniform data to GPU buffers.
     * Must be called BEFORE creating a render pass (outside any render pass).
     *
     * @param drawData the ImGui draw data for the current frame
     * @param encoder  the command encoder to use for buffer uploads
     */
    public void uploadDrawData(final ImDrawData drawData, final CommandEncoder encoder) {
        final int fbWidth = (int) (drawData.getDisplaySizeX() * drawData.getFramebufferScaleX());
        final int fbHeight = (int) (drawData.getDisplaySizeY() * drawData.getFramebufferScaleY());
        if (fbWidth <= 0 || fbHeight <= 0 || drawData.getCmdListsCount() <= 0) {
            return;
        }

        final GpuDevice device = RenderSystem.getDevice();

        // Calculate total buffer sizes needed
        long totalVertexSize = 0;
        long totalIndexSize = 0;
        for (int n = 0; n < drawData.getCmdListsCount(); n++) {
            totalVertexSize += (long) drawData.getCmdListVtxBufferSize(n) * ImDrawData.sizeOfImDrawVert();
            totalIndexSize += (long) drawData.getCmdListIdxBufferSize(n) * ImDrawData.sizeOfImDrawIdx();
        }

        // Resize vertex buffer if needed
        if (vertexBuffer == null || vertexBufferSize < totalVertexSize) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
            }
            vertexBufferSize = totalVertexSize + 4096; // Extra space to reduce reallocations
            vertexBuffer = device.createBuffer(
                    () -> "ImGui VB", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, vertexBufferSize);
        }

        // Resize index buffer if needed
        if (indexBuffer == null || indexBufferSize < totalIndexSize) {
            if (indexBuffer != null) {
                indexBuffer.close();
            }
            indexBufferSize = totalIndexSize + 1024; // Extra space
            indexBuffer = device.createBuffer(
                    () -> "ImGui IB", GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST, indexBufferSize);
        }

        // Upload vertex and index data for each command list
        long vertexOffset = 0;
        long indexOffset = 0;
        for (int n = 0; n < drawData.getCmdListsCount(); n++) {
            final int vtxCount = drawData.getCmdListVtxBufferSize(n);
            final int idxCount = drawData.getCmdListIdxBufferSize(n);

            if (vtxCount > 0) {
                final ByteBuffer vtxData = drawData.getCmdListVtxBufferData(n);
                final int vtxSize = vtxCount * ImDrawData.sizeOfImDrawVert();
                encoder.writeToBuffer(vertexBuffer.slice(vertexOffset, vtxSize), vtxData);
                vertexOffset += vtxSize;
            }

            if (idxCount > 0) {
                final ByteBuffer idxData = drawData.getCmdListIdxBufferData(n);
                final int idxSize = idxCount * ImDrawData.sizeOfImDrawIdx();
                encoder.writeToBuffer(indexBuffer.slice(indexOffset, idxSize), idxData);
                indexOffset += idxSize;
            }
        }

        // Upload projection matrix uniform
        final float L = drawData.getDisplayPosX();
        final float R = drawData.getDisplayPosX() + drawData.getDisplaySizeX();
        final float T = drawData.getDisplayPosY();
        final float B = drawData.getDisplayPosY() + drawData.getDisplaySizeY();

        projMatrixBuffer.clear();
        // std140 layout: mat4 = 4 x vec4, column-major
        // Row 0 (column 0 in column-major)
        projMatrixBuffer.putFloat(2.0f / (R - L));
        projMatrixBuffer.putFloat(0.0f);
        projMatrixBuffer.putFloat(0.0f);
        projMatrixBuffer.putFloat(0.0f);
        // Row 1 (column 1 in column-major)
        projMatrixBuffer.putFloat(0.0f);
        projMatrixBuffer.putFloat(2.0f / (T - B));
        projMatrixBuffer.putFloat(0.0f);
        projMatrixBuffer.putFloat(0.0f);
        // Row 2 (column 2 in column-major)
        projMatrixBuffer.putFloat(0.0f);
        projMatrixBuffer.putFloat(0.0f);
        projMatrixBuffer.putFloat(-1.0f);
        projMatrixBuffer.putFloat(0.0f);
        // Row 3 (column 3 in column-major)
        projMatrixBuffer.putFloat((R + L) / (L - R));
        projMatrixBuffer.putFloat((T + B) / (B - T));
        projMatrixBuffer.putFloat(0.0f);
        projMatrixBuffer.putFloat(1.0f);
        projMatrixBuffer.flip();

        encoder.writeToBuffer(projMatrixUniform.slice(), projMatrixBuffer);
    }

    /**
     * Renders ImGui draw data using the given render pass.
     * Must be called INSIDE an active render pass (after beginRenderPass).
     *
     * @param drawData   the ImGui draw data for the current frame
     * @param renderPass the active render pass to render into
     */
    public void renderDrawData(final ImDrawData drawData, final RenderPass renderPass) {
        if (!isReady()) {
            // Shaders are not available until the resource reload finishes; retry next frame
            return;
        }

        final int fbWidth = (int) (drawData.getDisplaySizeX() * drawData.getFramebufferScaleX());
        final int fbHeight = (int) (drawData.getDisplaySizeY() * drawData.getFramebufferScaleY());
        if (fbWidth <= 0 || fbHeight <= 0) {
            return;
        }

        renderPass.setPipeline(compiledPipeline);
        renderPass.setUniform("ProjMtx", projMatrixUniform);

        final float clipOffX = drawData.getDisplayPosX();
        final float clipOffY = drawData.getDisplayPosY();
        final float clipScaleX = drawData.getFramebufferScaleX();
        final float clipScaleY = drawData.getFramebufferScaleY();

        final IndexType indexType = ImDrawData.sizeOfImDrawIdx() == 2 ? IndexType.SHORT : IndexType.INT;

        long vertexOffset = 0;
        long indexOffset = 0;

        for (int n = 0; n < drawData.getCmdListsCount(); n++) {
            final int vtxCount = drawData.getCmdListVtxBufferSize(n);
            final int idxCount = drawData.getCmdListIdxBufferSize(n);
            final int vtxSize = vtxCount * ImDrawData.sizeOfImDrawVert();
            final int idxSize = idxCount * ImDrawData.sizeOfImDrawIdx();

            if (vtxCount == 0 || idxCount == 0) {
                continue;
            }

            // Set vertex and index buffers for this command list
            renderPass.setVertexBuffer(0, vertexBuffer.slice(vertexOffset, vtxSize));
            renderPass.setIndexBuffer(indexBuffer, indexType);

            for (int cmdI = 0; cmdI < drawData.getCmdListCmdBufferSize(n); cmdI++) {
                drawData.getCmdListCmdBufferClipRect(reusableClipRect, n, cmdI);

                final float clipMinX = (reusableClipRect.x - clipOffX) * clipScaleX;
                final float clipMinY = (reusableClipRect.y - clipOffY) * clipScaleY;
                final float clipMaxX = (reusableClipRect.z - clipOffX) * clipScaleX;
                final float clipMaxY = (reusableClipRect.w - clipOffY) * clipScaleY;

                if (clipMaxX <= clipMinX || clipMaxY <= clipMinY) {
                    continue;
                }

                final int scissorX = (int) clipMinX;
                final int scissorY = (int) (fbHeight - clipMaxY);
                final int scissorW = (int) (clipMaxX - clipMinX);
                final int scissorH = (int) (clipMaxY - clipMinY);

                renderPass.enableScissor(scissorX, scissorY, scissorW, scissorH);

                if (!bindDrawTexture(renderPass, drawData.getCmdListCmdBufferTextureId(n, cmdI))) {
                    continue;
                }

                final int elemCount = drawData.getCmdListCmdBufferElemCount(n, cmdI);
                final int idxBufferOffset = drawData.getCmdListCmdBufferIdxOffset(n, cmdI);
                final int vtxBufferOffset = drawData.getCmdListCmdBufferVtxOffset(n, cmdI);

                // Calculate absolute index offset within the combined index buffer
                final int firstIndex = (int) (indexOffset / ImDrawData.sizeOfImDrawIdx()) + idxBufferOffset;

                renderPass.drawIndexed(elemCount, 1, firstIndex, vtxBufferOffset, 0);
            }

            vertexOffset += vtxSize;
            indexOffset += idxSize;
        }
    }

    /**
     * Releases all GPU resources held by this renderer.
     */
    public void dispose() {
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
        if (indexBuffer != null) {
            indexBuffer.close();
            indexBuffer = null;
        }
        if (projMatrixUniform != null) {
            projMatrixUniform.close();
            projMatrixUniform = null;
        }
        disposeFontResources();
        renderPipeline = null;
        compiledPipeline = null;
        vertexBufferSize = 0;
        indexBufferSize = 0;
    }
}
