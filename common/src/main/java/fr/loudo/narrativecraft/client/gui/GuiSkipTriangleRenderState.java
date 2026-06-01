/*
 * NarrativeCraft - Create your own stories, easily, and freely in Minecraft.
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

package fr.loudo.narrativecraft.client.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;

public record GuiSkipTriangleRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        float width,
        float height,
        int color,
        ScreenRectangle scissorArea,
        ScreenRectangle bounds)
        implements GuiElementRenderState {

    public GuiSkipTriangleRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            float width,
            float height,
            int color,
            ScreenRectangle scissorArea) {
        this(
                pipeline,
                textureSetup,
                pose,
                width,
                height,
                color,
                scissorArea,
                getBounds(-width, -height, width, height, pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        consumer.addVertexWith2DPose(this.pose(), -width, -height).setColor(color);
        consumer.addVertexWith2DPose(this.pose(), -width, height).setColor(color);
        consumer.addVertexWith2DPose(this.pose(), width, 0).setColor(color);
        consumer.addVertexWith2DPose(this.pose(), -width, -height).setColor(color);
    }

    private static ScreenRectangle getBounds(
            float x0, float y0, float x1, float y1, Matrix3x2f pose, ScreenRectangle scissorArea) {
        ScreenRectangle screenrectangle =
                (new ScreenRectangle((int) x0, (int) y0, (int) (x1 - x0), (int) (y1 - y0))).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(screenrectangle) : screenrectangle;
    }
}
