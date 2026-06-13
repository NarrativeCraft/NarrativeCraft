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

package fr.loudo.narrativecraft.network.cameraangle;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngle;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SCameraAngleRemovePlacement(UUID chapterId, UUID sceneId, UUID cameraAngleId, UUID placementId)
        implements CustomPacketPayload {

    public C2SCameraAngleRemovePlacement(CameraAngle cameraAngle, UUID placementId) {
        this(
                cameraAngle.getScene().getChapter().getId(),
                cameraAngle.getScene().getId(),
                cameraAngle.getId(),
                placementId);
    }

    public static final Type<C2SCameraAngleRemovePlacement> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "camera_angle_remove_placement"));

    public static final StreamCodec<ByteBuf, C2SCameraAngleRemovePlacement> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            C2SCameraAngleRemovePlacement::chapterId,
            UUIDUtil.STREAM_CODEC,
            C2SCameraAngleRemovePlacement::sceneId,
            UUIDUtil.STREAM_CODEC,
            C2SCameraAngleRemovePlacement::cameraAngleId,
            UUIDUtil.STREAM_CODEC,
            C2SCameraAngleRemovePlacement::placementId,
            C2SCameraAngleRemovePlacement::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
