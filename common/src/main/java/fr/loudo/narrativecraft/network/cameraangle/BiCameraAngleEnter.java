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
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngle;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class BiCameraAngleEnter implements CustomPacketPayload {

    private final UUID chapterId;
    private final UUID sceneId;
    private final UUID cameraAngleId;
    private final NarrativeEnvironment environment;

    public BiCameraAngleEnter(CameraAngle cameraAngle, NarrativeEnvironment environment) {
        this.chapterId = cameraAngle.getScene().getChapter().getId();
        this.sceneId = cameraAngle.getScene().getId();
        this.cameraAngleId = cameraAngle.getId();
        this.environment = environment;
    }

    public BiCameraAngleEnter(UUID chapterId, UUID sceneId, UUID cameraAngleId, NarrativeEnvironment environment) {
        this.chapterId = chapterId;
        this.sceneId = sceneId;
        this.cameraAngleId = cameraAngleId;
        this.environment = environment;
    }

    public static final Type<BiCameraAngleEnter> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "camera_angle_enter"));

    public static final StreamCodec<ByteBuf, BiCameraAngleEnter> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            BiCameraAngleEnter::getChapterId,
            UUIDUtil.STREAM_CODEC,
            BiCameraAngleEnter::getSceneId,
            UUIDUtil.STREAM_CODEC,
            BiCameraAngleEnter::getCameraAngleId,
            ByteBufCodecs.idMapper(i -> NarrativeEnvironment.values()[i], NarrativeEnvironment::ordinal),
            BiCameraAngleEnter::getEnvironment,
            BiCameraAngleEnter::new);

    public UUID getChapterId() {
        return chapterId;
    }

    public UUID getSceneId() {
        return sceneId;
    }

    public UUID getCameraAngleId() {
        return cameraAngleId;
    }

    public NarrativeEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
