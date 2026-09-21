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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.BlendFactor;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.api.pipeline.PolygonMode;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import imgui.ImDrawData;
import imgui.ImFontAtlas;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.type.ImInt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.OptionalDouble;
import net.minecraft.resources.Identifier;

public class ClientImGuiBlaze3DRenderer {

    private static final long FONT_TEXTURE_ID = 1;
    private static final Identifier SHADER_ID = Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "core/imgui");
    private static final VertexFormat VERTEX_FORMAT = VertexFormat.builder(0)
            .addAttribute("Position", GpuFormat.RG32_FLOAT)
            .addAttribute("UV", GpuFormat.RG32_FLOAT)
            .addAttribute("Color", GpuFormat.RGBA8_UNORM)
            .build();

    private RenderPipeline renderPipeline;
    private CompiledRenderPipeline compiledPipeline;
    private GpuTexture fontTexture;
    private GpuTextureView fontTextureView;
    private GpuSampler fontSampler;
    private GpuBuffer vertexBuffer;
    private GpuBuffer indexBuffer;
    private GpuBuffer projectionMatrixUniform;
    private long vertexBufferSize;
    private long indexBufferSize;

    private final ByteBuffer projectionMatrixBuffer =
            ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder());
    private final ImVec4 clipRect = new ImVec4();

    public void newFrame() {
        if (renderPipeline == null) {
            createPipeline();
        }
        // Shaders are only resolvable once the initial resource reload has finished; retry every frame until then
        if (compiledPipeline == null || compiledPipeline.isClosed()) {
            compiledPipeline = RenderSystem.getCompiledPipelineNullable(renderPipeline);
        }
        if (!isReady()) {
            return;
        }
        if (fontTexture == null) {
            createFontsTexture();
            NarrativeCraftMod.LOGGER.info("ImGui Blaze3D pipeline compiled, renderer ready");
        }
    }

    private boolean isReady() {
        return compiledPipeline != null && !compiledPipeline.isClosed() && projectionMatrixUniform != null;
    }

    private void createPipeline() {
        GpuDevice device = RenderSystem.getDevice();
        renderPipeline = RenderPipeline.builder()
                .withLocation(SHADER_ID)
                .withVertexShader(SHADER_ID)
                .withFragmentShader(SHADER_ID)
                .withVertexBinding(0, VERTEX_FORMAT)
                .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                .withColorTargetState(new ColorTargetState(new BlendFunction(
                        BlendFactor.SRC_ALPHA,
                        BlendFactor.ONE_MINUS_SRC_ALPHA,
                        BlendFactor.ONE,
                        BlendFactor.ONE_MINUS_SRC_ALPHA)))
                .withCull(false)
                .withPolygonMode(PolygonMode.FILL)
                .withBindGroupLayout(BindGroupLayout.builder()
                        .withUniform("ProjMtx", UniformType.UNIFORM_BUFFER)
                        .build())
                .withBindGroupLayout(BindGroupLayout.builder()
                        .withUniform("Texture", UniformType.COMBINED_IMAGE_SAMPLER)
                        .build())
                .build();

        if (projectionMatrixUniform != null) {
            projectionMatrixUniform.close();
        }
        projectionMatrixUniform = device.createBuffer(
                () -> "NarrativeCraft ImGui ProjMtx", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 64);
    }

    private void createFontsTexture() {
        GpuDevice device = RenderSystem.getDevice();
        ImFontAtlas fontAtlas = ImGui.getIO().getFonts();
        ImInt width = new ImInt();
        ImInt height = new ImInt();
        ByteBuffer pixels = fontAtlas.getTexDataAsRGBA32(width, height);

        disposeFontResources();

        fontTexture = device.createTexture(
                () -> "NarrativeCraft ImGui Font",
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

        CommandEncoder encoder = device.createCommandEncoder();
        encoder.writeToTexture(fontTexture, pixels, 0, 0, 0, 0, width.get(), height.get());
        encoder.submit();

        fontAtlas.setTexID(FONT_TEXTURE_ID);
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

    public void uploadDrawData(ImDrawData drawData, CommandEncoder encoder) {
        int framebufferWidth = (int) (drawData.getDisplaySizeX() * drawData.getFramebufferScaleX());
        int framebufferHeight = (int) (drawData.getDisplaySizeY() * drawData.getFramebufferScaleY());
        if (framebufferWidth <= 0 || framebufferHeight <= 0 || drawData.getCmdListsCount() <= 0) {
            return;
        }

        GpuDevice device = RenderSystem.getDevice();

        long totalVertexSize = 0;
        long totalIndexSize = 0;
        for (int commandListIndex = 0; commandListIndex < drawData.getCmdListsCount(); commandListIndex++) {
            totalVertexSize +=
                    (long) drawData.getCmdListVtxBufferSize(commandListIndex) * ImDrawData.sizeOfImDrawVert();
            totalIndexSize += (long) drawData.getCmdListIdxBufferSize(commandListIndex) * ImDrawData.sizeOfImDrawIdx();
        }

        if (vertexBuffer == null || vertexBufferSize < totalVertexSize) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
            }
            vertexBufferSize = totalVertexSize + 4096;
            vertexBuffer = device.createBuffer(
                    () -> "NarrativeCraft ImGui VB",
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                    vertexBufferSize);
        }

        if (indexBuffer == null || indexBufferSize < totalIndexSize) {
            if (indexBuffer != null) {
                indexBuffer.close();
            }
            indexBufferSize = totalIndexSize + 1024;
            indexBuffer = device.createBuffer(
                    () -> "NarrativeCraft ImGui IB", GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST, indexBufferSize);
        }

        long vertexOffset = 0;
        long indexOffset = 0;
        for (int commandListIndex = 0; commandListIndex < drawData.getCmdListsCount(); commandListIndex++) {
            int vertexCount = drawData.getCmdListVtxBufferSize(commandListIndex);
            int indexCount = drawData.getCmdListIdxBufferSize(commandListIndex);

            if (vertexCount > 0) {
                ByteBuffer vertexData = drawData.getCmdListVtxBufferData(commandListIndex);
                int vertexSize = vertexCount * ImDrawData.sizeOfImDrawVert();
                encoder.writeToBuffer(vertexBuffer.slice(vertexOffset, vertexSize), vertexData);
                vertexOffset += vertexSize;
            }

            if (indexCount > 0) {
                ByteBuffer indexData = drawData.getCmdListIdxBufferData(commandListIndex);
                int indexSize = indexCount * ImDrawData.sizeOfImDrawIdx();
                encoder.writeToBuffer(indexBuffer.slice(indexOffset, indexSize), indexData);
                indexOffset += indexSize;
            }
        }

        float left = drawData.getDisplayPosX();
        float right = drawData.getDisplayPosX() + drawData.getDisplaySizeX();
        float top = drawData.getDisplayPosY();
        float bottom = drawData.getDisplayPosY() + drawData.getDisplaySizeY();

        projectionMatrixBuffer.clear();
        projectionMatrixBuffer.putFloat(2.0f / (right - left));
        projectionMatrixBuffer.putFloat(0.0f);
        projectionMatrixBuffer.putFloat(0.0f);
        projectionMatrixBuffer.putFloat(0.0f);
        projectionMatrixBuffer.putFloat(0.0f);
        projectionMatrixBuffer.putFloat(2.0f / (top - bottom));
        projectionMatrixBuffer.putFloat(0.0f);
        projectionMatrixBuffer.putFloat(0.0f);
        projectionMatrixBuffer.putFloat(0.0f);
        projectionMatrixBuffer.putFloat(0.0f);
        projectionMatrixBuffer.putFloat(-1.0f);
        projectionMatrixBuffer.putFloat(0.0f);
        projectionMatrixBuffer.putFloat((right + left) / (left - right));
        projectionMatrixBuffer.putFloat((top + bottom) / (bottom - top));
        projectionMatrixBuffer.putFloat(0.0f);
        projectionMatrixBuffer.putFloat(1.0f);
        projectionMatrixBuffer.flip();

        encoder.writeToBuffer(projectionMatrixUniform.slice(), projectionMatrixBuffer);
    }

    public void renderDrawData(ImDrawData drawData, RenderPass renderPass) {
        if (!isReady() || fontTexture == null || vertexBuffer == null || indexBuffer == null) {
            return;
        }

        int framebufferWidth = (int) (drawData.getDisplaySizeX() * drawData.getFramebufferScaleX());
        int framebufferHeight = (int) (drawData.getDisplaySizeY() * drawData.getFramebufferScaleY());
        if (framebufferWidth <= 0 || framebufferHeight <= 0) {
            return;
        }

        renderPass.setPipeline(compiledPipeline);
        renderPass.setUniform("ProjMtx", projectionMatrixUniform);

        float clipOffsetX = drawData.getDisplayPosX();
        float clipOffsetY = drawData.getDisplayPosY();
        float clipScaleX = drawData.getFramebufferScaleX();
        float clipScaleY = drawData.getFramebufferScaleY();

        IndexType indexType = ImDrawData.sizeOfImDrawIdx() == 2 ? IndexType.SHORT : IndexType.INT;

        long vertexOffset = 0;
        long indexOffset = 0;

        for (int commandListIndex = 0; commandListIndex < drawData.getCmdListsCount(); commandListIndex++) {
            int vertexCount = drawData.getCmdListVtxBufferSize(commandListIndex);
            int indexCount = drawData.getCmdListIdxBufferSize(commandListIndex);
            int vertexSize = vertexCount * ImDrawData.sizeOfImDrawVert();
            int indexSize = indexCount * ImDrawData.sizeOfImDrawIdx();

            if (vertexCount == 0 || indexCount == 0) {
                continue;
            }

            renderPass.setVertexBuffer(0, vertexBuffer.slice(vertexOffset, vertexSize));
            renderPass.setIndexBuffer(indexBuffer, indexType);

            for (int commandIndex = 0;
                    commandIndex < drawData.getCmdListCmdBufferSize(commandListIndex);
                    commandIndex++) {
                drawData.getCmdListCmdBufferClipRect(clipRect, commandListIndex, commandIndex);

                float clipMinX = (clipRect.x - clipOffsetX) * clipScaleX;
                float clipMinY = (clipRect.y - clipOffsetY) * clipScaleY;
                float clipMaxX = (clipRect.z - clipOffsetX) * clipScaleX;
                float clipMaxY = (clipRect.w - clipOffsetY) * clipScaleY;

                if (clipMaxX <= clipMinX || clipMaxY <= clipMinY) {
                    continue;
                }

                renderPass.enableScissor(
                        (int) clipMinX, (int) (framebufferHeight - clipMaxY), (int) (clipMaxX - clipMinX), (int)
                                (clipMaxY - clipMinY));

                long textureId = drawData.getCmdListCmdBufferTextureId(commandListIndex, commandIndex);
                if (textureId != FONT_TEXTURE_ID) {
                    continue;
                }
                renderPass.setUniform("Texture", fontTextureView, fontSampler);

                int elementCount = drawData.getCmdListCmdBufferElemCount(commandListIndex, commandIndex);
                int indexBufferOffset = drawData.getCmdListCmdBufferIdxOffset(commandListIndex, commandIndex);
                int vertexBufferOffset = drawData.getCmdListCmdBufferVtxOffset(commandListIndex, commandIndex);
                int firstIndex = (int) (indexOffset / ImDrawData.sizeOfImDrawIdx()) + indexBufferOffset;

                renderPass.drawIndexed(elementCount, 1, firstIndex, vertexBufferOffset, 0);
            }

            vertexOffset += vertexSize;
            indexOffset += indexSize;
        }
    }

    public void dispose() {
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
        if (indexBuffer != null) {
            indexBuffer.close();
            indexBuffer = null;
        }
        if (projectionMatrixUniform != null) {
            projectionMatrixUniform.close();
            projectionMatrixUniform = null;
        }
        disposeFontResources();
        renderPipeline = null;
        compiledPipeline = null;
        vertexBufferSize = 0;
        indexBufferSize = 0;
    }
}
