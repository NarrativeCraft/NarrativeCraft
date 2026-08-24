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

package fr.loudo.narrativecraft.client.rendering;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.ARGB;

public class ImageTexture {

    private static final String DEFAULT_EXTENSION = ".png";

    private static final Map<String, ImageTexture> LOADED = new HashMap<>();
    private static final Set<String> FAILED = new HashSet<>();

    private final Identifier location;
    private final int width;
    private final int height;

    private ImageTexture(Identifier location, int width, int height) {
        this.location = location;
        this.width = width;
        this.height = height;
    }

    @Nullable
    public static ImageTexture load(String path) {
        if (path == null || path.isEmpty()) return null;

        ImageTexture cached = LOADED.get(path);
        if (cached != null) return cached;
        if (FAILED.contains(path)) return null;

        ImageTexture texture = read(path);
        if (texture == null) {
            FAILED.add(path);
        } else {
            LOADED.put(path, texture);
        }
        return texture;
    }

    public static void clearCache() {
        LOADED.clear();
        FAILED.clear();
    }

    @Nullable
    private static ImageTexture read(String path) {
        Identifier location = resolve(path);
        if (location == null) return null;

        Optional<Resource> resource =
                Minecraft.getInstance().getResourceManager().getResource(location);
        if (resource.isEmpty()) {
            resource = Minecraft.getInstance()
                    .getResourceManager()
                    .getResource(Identifier.withDefaultNamespace("textures/misc/unknown_pack.png"));
        }
        if (resource.isEmpty()) {
            return null;
        }

        try (InputStream stream = resource.get().open();
                NativeImage image = NativeImage.read(stream)) {
            return new ImageTexture(location, image.getWidth(), image.getHeight());
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    private static Identifier resolve(String path) {
        if (path == null || path.isEmpty()) return null;
        Identifier identifier = Identifier.parse(path);
        String file = identifier.getPath();
        if (!file.startsWith("textures/") && identifier.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            file = "textures/nc_images/" + file;
        }
        if (file.lastIndexOf('.') <= file.lastIndexOf('/')) {
            file = file + DEFAULT_EXTENSION;
        }
        return Identifier.tryBuild(identifier.getNamespace(), file);
    }

    public void render(GuiGraphicsExtractor guiGraphics, int x, int y, float alpha) {
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED, location, x, y, 0f, 0f, width, height, width, height, ARGB.white(alpha));
    }

    public void render(
            GuiGraphicsExtractor guiGraphics, float x, float y, float renderWidth, float renderHeight, float alpha) {
        if (width <= 0 || height <= 0) return;
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        guiGraphics.pose().scale(renderWidth / width, renderHeight / height);
        render(guiGraphics, 0, 0, alpha);
        guiGraphics.pose().popMatrix();
    }

    public Identifier getLocation() {
        return location;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
