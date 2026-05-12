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

package fr.loudo.narrativecraft.client.editors.interaction;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.EasingType;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Interpolation;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.interaction.Interaction;
import fr.loudo.narrativecraft.narrative.interaction.InteractionPoint;
import fr.loudo.narrativecraft.narrative.interaction.InteractionZone;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InteractionMakerEditorRenderer {

    private static final Identifier POINT_TEXTURE =
            Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "textures/interaction/interaction_point.png");
    private static final float POINT_SPRITE_SCALE = 0.1f;
    private static final float POINT_ANIMATION_SPEED = 6.5f;
    private static final Map<UUID, Float> pointAnimT = new HashMap<>();

    public static void render(PoseStack poseStack, DeltaTracker deltaTracker) {
        EditorMaker editorMaker =
                ClientNarrativeCraftMod.getInstance().getPlayerSession().getEditor();
        if (!(editorMaker instanceof ClientInteractionMakerEditorMaker interactionEditor)) return;

        Minecraft minecraft = Minecraft.getInstance();
        Vec3 cameraPosition = minecraft.gameRenderer.getMainCamera().position();
        Interaction interaction = interactionEditor.getInteraction();
        boolean isDev = interactionEditor.getEnvironment() == NarrativeEnvironment.DEVELOPMENT;
        float deltaSeconds = deltaTracker.getGameTimeDeltaTicks() / 20.0f;

        VertexConsumer lineConsumer = minecraft.renderBuffers().bufferSource().getBuffer(RenderTypes.lines());
        Matrix4f lineMatrix = poseStack.last().pose();
        for (InteractionZone zone : interaction.getZones()) {
            Vec3[] corners = resolveZoneCorners(zone, interactionEditor);
            if (corners == null || !isDev) continue;
            drawAABB(
                    lineConsumer, lineMatrix, new AABB(corners[0], corners[1]), cameraPosition, 0.2f, 0.5f, 1.0f, 1.0f);
        }
        minecraft.renderBuffers().bufferSource().endBatch(RenderTypes.lines());

        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        ClientPlayerSession clientSession =
                ClientNarrativeCraftMod.getInstance().getPlayerSession();

        for (InteractionPoint point : interaction.getPoints()) {
            Vec3 pointPosition = resolvePointPosition(point, interactionEditor);
            if (pointPosition == null) continue;
            float renderScale;
            if (isDev) {
                renderScale = POINT_SPRITE_SCALE;
            } else {
                if (point.isNeverShow()
                        || (point.isOneTimeClick() && clientSession.hasClickedInteractionPoint(point.getId()))) {
                    pointAnimT.remove(point.getId());
                    continue;
                }
                boolean conditionMet = isPointConditionMet(point, minecraft.player);
                float currentT = pointAnimT.getOrDefault(point.getId(), 0f);
                float step = POINT_ANIMATION_SPEED * deltaSeconds;
                currentT = conditionMet ? Math.min(currentT + step, 1f) : Math.max(currentT - step, 0f);
                pointAnimT.put(point.getId(), currentT);
                float easedT = (float) Interpolation.applyEasing(EasingType.EASE_OUT, currentT);
                renderScale = (float) Interpolation.lerp(0, POINT_SPRITE_SCALE, easedT);
            }
            if (renderScale > 0.001f) {
                drawPointSprite(poseStack, bufferSource, cameraPosition, pointPosition, minecraft, renderScale);
            }
        }

        for (InteractionZone zone : interaction.getZones()) {
            Vec3[] corners = resolveZoneCorners(zone, interactionEditor);
            if (corners == null || !isDev) continue;
            Vec3 center = corners[0].add(corners[1]).scale(0.5);
            drawNameTag(
                    poseStack, bufferSource, cameraPosition, center, 0.3, 0.1f, zone.getName(), 0xFF88CCFF, minecraft);
        }

        if (isDev) {
            for (InteractionPoint point : interaction.getPoints()) {
                Vec3 pointPosition = resolvePointPosition(point, interactionEditor);
                if (pointPosition == null) continue;
                drawNameTag(
                        poseStack,
                        bufferSource,
                        cameraPosition,
                        pointPosition,
                        0.35,
                        0.025f,
                        point.getName(),
                        0xFFFFCC00,
                        minecraft);
            }
        }

        bufferSource.endBatch();
    }

    private static boolean isPointConditionMet(InteractionPoint point, LocalPlayer player) {
        return point.isConditionMet(player);
    }

    private static Vec3 resolvePointPosition(InteractionPoint point, ClientInteractionMakerEditorMaker editor) {
        if (editor.isInPointPlacementMode() && point == editor.getPointBeingPlaced()) {
            return editor.getTempPointPosition();
        }
        return point.getPosition();
    }

    private static Vec3[] resolveZoneCorners(InteractionZone zone, ClientInteractionMakerEditorMaker editor) {
        Vec3 corner1;
        Vec3 corner2;
        if (editor.isInCornerPlacementMode() && zone == editor.getZoneBeingEdited()) {
            corner1 = editor.getTempCorner1();
            corner2 = editor.getTempCorner2();
        } else {
            corner1 = zone.getCorner1();
            corner2 = zone.getCorner2();
        }
        if (corner1 == null || corner2 == null) return null;
        return new Vec3[] {corner1, corner2};
    }

    private static void drawPointSprite(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            Vec3 cameraPosition,
            Vec3 worldPosition,
            Minecraft minecraft,
            float scale) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderTypes.beaconBeam(POINT_TEXTURE, true));
        poseStack.pushPose();
        poseStack.translate(
                worldPosition.x - cameraPosition.x,
                worldPosition.y - cameraPosition.y,
                worldPosition.z - cameraPosition.z);
        poseStack.mulPose(minecraft.gameRenderer.getMainCamera().rotation());
        poseStack.scale(scale, scale, 1.0f);
        Matrix4f matrix = poseStack.last().pose();
        int light = LightCoordsUtil.FULL_BRIGHT;
        buffer.addVertex(matrix, -0.5f, 0.5f, 0f)
                .setColor(1f, 1f, 1f, 1f)
                .setUv(0f, 0f)
                .setUv1(0, 0)
                .setLight(light)
                .setNormal(0, 0, 1);
        buffer.addVertex(matrix, -0.5f, -0.5f, 0f)
                .setColor(1f, 1f, 1f, 1f)
                .setUv(0f, 1f)
                .setUv1(0, 0)
                .setLight(light)
                .setNormal(0, 0, 1);
        buffer.addVertex(matrix, 0.5f, -0.5f, 0f)
                .setColor(1f, 1f, 1f, 1f)
                .setUv(1f, 1f)
                .setUv1(0, 0)
                .setLight(light)
                .setNormal(0, 0, 1);
        buffer.addVertex(matrix, 0.5f, 0.5f, 0f)
                .setColor(1f, 1f, 1f, 1f)
                .setUv(1f, 0f)
                .setUv1(0, 0)
                .setLight(light)
                .setNormal(0, 0, 1);
        poseStack.popPose();
    }

    private static void drawNameTag(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            Vec3 cameraPosition,
            Vec3 worldPosition,
            double yOffset,
            float scale,
            String name,
            int color,
            Minecraft minecraft) {
        poseStack.pushPose();
        poseStack.translate(
                worldPosition.x - cameraPosition.x,
                worldPosition.y - cameraPosition.y + yOffset,
                worldPosition.z - cameraPosition.z);
        poseStack.mulPose(minecraft.gameRenderer.getMainCamera().rotation());
        poseStack.scale(scale, -scale, scale);
        Matrix4f matrix = poseStack.last().pose();
        Font font = minecraft.font;
        font.drawInBatch(
                name,
                -font.width(name) / 2f,
                0,
                color,
                false,
                matrix,
                bufferSource,
                Font.DisplayMode.SEE_THROUGH,
                0x40000000,
                0xF000F0);
        poseStack.popPose();
    }

    private static void drawAABB(
            VertexConsumer vertexConsumer,
            Matrix4f matrix,
            AABB aabb,
            Vec3 cameraPosition,
            float red,
            float green,
            float blue,
            float alpha) {
        float minX = (float) (aabb.minX - cameraPosition.x);
        float minY = (float) (aabb.minY - cameraPosition.y);
        float minZ = (float) (aabb.minZ - cameraPosition.z);
        float maxX = (float) (aabb.maxX - cameraPosition.x);
        float maxY = (float) (aabb.maxY - cameraPosition.y);
        float maxZ = (float) (aabb.maxZ - cameraPosition.z);

        Vector3f v0 = new Vector3f(minX, minY, minZ);
        Vector3f v1 = new Vector3f(maxX, minY, minZ);
        Vector3f v2 = new Vector3f(maxX, minY, maxZ);
        Vector3f v3 = new Vector3f(minX, minY, maxZ);
        Vector3f v4 = new Vector3f(minX, maxY, minZ);
        Vector3f v5 = new Vector3f(maxX, maxY, minZ);
        Vector3f v6 = new Vector3f(maxX, maxY, maxZ);
        Vector3f v7 = new Vector3f(minX, maxY, maxZ);

        drawLine(vertexConsumer, matrix, v0, v1, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, v1, v2, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, v2, v3, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, v3, v0, red, green, blue, alpha);

        drawLine(vertexConsumer, matrix, v4, v5, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, v5, v6, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, v6, v7, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, v7, v4, red, green, blue, alpha);

        drawLine(vertexConsumer, matrix, v0, v4, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, v1, v5, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, v2, v6, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, v3, v7, red, green, blue, alpha);
    }

    private static void drawLine(
            VertexConsumer vertexConsumer,
            Matrix4f matrix,
            Vector3f from,
            Vector3f to,
            float red,
            float green,
            float blue,
            float alpha) {
        float dx = to.x - from.x;
        float dy = to.y - from.y;
        float dz = to.z - from.z;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length > 0) {
            dx /= length;
            dy /= length;
            dz /= length;
        }
        vertexConsumer
                .addVertex(matrix, from.x, from.y, from.z)
                .setLineWidth(4.0f)
                .setColor(red, green, blue, alpha)
                .setNormal(dx, dy, dz);
        vertexConsumer
                .addVertex(matrix, to.x, to.y, to.z)
                .setLineWidth(4.0f)
                .setColor(red, green, blue, alpha)
                .setNormal(dx, dy, dz);
    }
}
