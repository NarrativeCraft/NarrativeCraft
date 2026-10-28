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
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Handles mapping textures to longs so they can be passed around in imgui
 *
 * @author Igrium
 */
public enum ImGuiTexturesClient {
    INSTANCE;

    private @Nullable GpuSampler defaultSampler;

    /**
     * Contains a texture view and a "canonical" sampler
     *
     * @param textureView Texture view to use
     * @param sampler     Sampler to use
     */
    public record TexEntry(
            GpuTextureView textureView, @Nullable GpuSampler sampler) {}

    /**
     * Store separately so we don't have to allocate a record each time it's used
     */
    private final Map<GpuSampler, Object2LongMap<GpuTextureView>> tex2Id = new HashMap<>();

    private final Long2ObjectMap<TexEntry> id2Tex = new Long2ObjectOpenHashMap<>();

    private long nextTextureId = 2; // ImGui reserves 1 for font texture

    private @NonNull GpuSampler getDefaultSampler() {
        if (defaultSampler == null) {
            defaultSampler = RenderSystem.getDevice()
                    .createSampler(
                            AddressMode.CLAMP_TO_EDGE,
                            AddressMode.CLAMP_TO_EDGE,
                            FilterMode.LINEAR,
                            FilterMode.LINEAR,
                            1,
                            OptionalDouble.empty());
        }
        return defaultSampler;
    }

    /**
     * Get (or create) a long ID for use in imgui using the default sampler
     *
     * @param texture Texture to use
     * @return ID
     */
    public long textureId(@NonNull GpuTextureView texture) {
        return textureId(texture, getDefaultSampler());
    }

    /**
     * Get (or create) a long ID for use in imgui
     *
     * @param texture Texture to use
     * @param sampler Sampler to use
     * @return The ID
     */
    public long textureId(@NonNull GpuTextureView texture, @NonNull GpuSampler sampler) {
        if (texture.texture() instanceof GlTexture gl) {
            return gl.glId();
        }

        RenderSystem.assertOnRenderThread();
        var map = tex2Id.computeIfAbsent(sampler, _ -> new Object2LongOpenHashMap<>());

        // Don't use computeIfAbsent so capturing lambda doesn't allocate
        long texId = map.getOrDefault(texture, -1);
        if (texId < 0) {
            texId = nextTextureId++;
            map.put(texture, texId);
            id2Tex.put(texId, new TexEntry(texture, sampler));
        }
        return texId;
    }

    /**
     * Get the texture for a given ID
     *
     * @param id ID to use
     * @return The texture that ID belongs to
     */
    public @Nullable TexEntry getTexture(long id) {
        return id2Tex.get(id);
    }

    /**
     * Dispose of all stale texture registrations
     */
    public void clearStale() {
        RenderSystem.assertOnRenderThread();
        var iter = id2Tex.long2ObjectEntrySet().iterator();
        while (iter.hasNext()) {
            var entry = iter.next();
            TexEntry texEntry = entry.getValue();
            //noinspection resource
            if (texEntry.textureView().isClosed()) {
                var map = tex2Id.get(texEntry.sampler());
                if (map != null) {
                    map.removeLong(texEntry.textureView());
                }
                iter.remove();
            }
        }

        tex2Id.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}
