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

package fr.loudo.narrativecraft.network.cameraangle;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngle;
import fr.loudo.narrativecraft.narrative.cameraangle.TemplateReference;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record C2SCameraAngleAddTemplateReference(
        UUID chapterId,
        UUID sceneId,
        UUID cameraAngleId,
        String sourceType,
        UUID refId,
        UUID templateReferenceId,
        String displayName)
        implements CustomPacketPayload {

    public C2SCameraAngleAddTemplateReference(CameraAngle cameraAngle, TemplateReference reference) {
        this(
                cameraAngle.getScene().getChapter().getId(),
                cameraAngle.getScene().getId(),
                cameraAngle.getId(),
                reference.sourceType().name(),
                reference.refId(),
                reference.id(),
                reference.displayName());
    }

    public static final Type<C2SCameraAngleAddTemplateReference> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "camera_angle_add_template_reference"));

    public static final StreamCodec<ByteBuf, C2SCameraAngleAddTemplateReference> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            C2SCameraAngleAddTemplateReference::chapterId,
            UUIDUtil.STREAM_CODEC,
            C2SCameraAngleAddTemplateReference::sceneId,
            UUIDUtil.STREAM_CODEC,
            C2SCameraAngleAddTemplateReference::cameraAngleId,
            ByteBufCodecs.STRING_UTF8,
            C2SCameraAngleAddTemplateReference::sourceType,
            UUIDUtil.STREAM_CODEC,
            C2SCameraAngleAddTemplateReference::refId,
            UUIDUtil.STREAM_CODEC,
            C2SCameraAngleAddTemplateReference::templateReferenceId,
            ByteBufCodecs.STRING_UTF8,
            C2SCameraAngleAddTemplateReference::displayName,
            C2SCameraAngleAddTemplateReference::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
