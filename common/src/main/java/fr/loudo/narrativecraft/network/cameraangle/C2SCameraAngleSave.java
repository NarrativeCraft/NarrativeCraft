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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class C2SCameraAngleSave implements CustomPacketPayload {

    private final UUID chapterId;
    private final UUID sceneId;
    private final UUID cameraAngleId;
    private final String dataJson;

    public C2SCameraAngleSave(CameraAngle cameraAngle, String dataJson) {
        this.chapterId = cameraAngle.getScene().getChapter().getId();
        this.sceneId = cameraAngle.getScene().getId();
        this.cameraAngleId = cameraAngle.getId();
        this.dataJson = dataJson;
    }

    public C2SCameraAngleSave(UUID chapterId, UUID sceneId, UUID cameraAngleId, String dataJson) {
        this.chapterId = chapterId;
        this.sceneId = sceneId;
        this.cameraAngleId = cameraAngleId;
        this.dataJson = dataJson;
    }

    public static final Type<C2SCameraAngleSave> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "camera_angle_save"));

    public static final StreamCodec<ByteBuf, C2SCameraAngleSave> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            C2SCameraAngleSave::getChapterId,
            UUIDUtil.STREAM_CODEC,
            C2SCameraAngleSave::getSceneId,
            UUIDUtil.STREAM_CODEC,
            C2SCameraAngleSave::getCameraAngleId,
            ByteBufCodecs.STRING_UTF8,
            C2SCameraAngleSave::getDataJson,
            C2SCameraAngleSave::new);

    public UUID getChapterId() {
        return chapterId;
    }

    public UUID getSceneId() {
        return sceneId;
    }

    public UUID getCameraAngleId() {
        return cameraAngleId;
    }

    public String getDataJson() {
        return dataJson;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
