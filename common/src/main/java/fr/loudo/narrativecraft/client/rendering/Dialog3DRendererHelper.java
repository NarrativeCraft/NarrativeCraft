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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

public final class Dialog3DRendererHelper {

    public static final int LAYER_BACKGROUND = 0;
    public static final int LAYER_TEXT = 1;
    public static final int LAYER_FOREGROUND = 2;

    /** Plain white 1x1 texture, used to draw flat-colored geometry through the text-see-through render type. */
    public static final Identifier WHITE_TEXTURE =
            Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "textures/misc/white.png");

    private Dialog3DRendererHelper() {}

    public static VertexConsumer fillUnusedVertexAttributes(VertexConsumer consumer) {
        return consumer.setUv1(0, 0).setUv3(0f, 0f).setNormal(0f, 0f, 1f).setLineWidth(1f);
    }

    public static void geometry(
            SubmitNodeCollector collector,
            int order,
            PoseStack poseStack,
            RenderType renderType,
            SubmitNodeCollector.CustomGeometryRenderer renderer) {
        ((SubmitNodeStorage) collector).order(order).submitCustomGeometry(poseStack, renderType, renderer);
    }

    public static void textBackground(
            SubmitNodeCollector collector,
            int order,
            PoseStack poseStack,
            float x0,
            float y0,
            float x1,
            float y1,
            int color,
            Font.DisplayMode displayMode,
            int lightCoords) {
        ((SubmitNodeStorage) collector)
                .order(order)
                .submitTextBackground(poseStack, x0, y0, x1, y1, color, displayMode, lightCoords);
    }

    public static void text(
            SubmitNodeCollector collector,
            int order,
            PoseStack poseStack,
            float x,
            float y,
            FormattedCharSequence text,
            boolean dropShadow,
            Font.DisplayMode displayMode,
            int lightCoords,
            int color,
            int backgroundColor,
            int outlineColor) {
        ((SubmitNodeStorage) collector)
                .order(order)
                .submitText(
                        poseStack,
                        x,
                        y,
                        text,
                        dropShadow,
                        displayMode,
                        lightCoords,
                        color,
                        backgroundColor,
                        outlineColor);
    }
}
