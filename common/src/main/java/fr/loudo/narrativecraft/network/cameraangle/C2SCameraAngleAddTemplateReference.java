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
import fr.loudo.narrativecraft.network.NarrativePacket;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record C2SCameraAngleAddTemplateReference(
        UUID chapterId,
        UUID sceneId,
        UUID cameraAngleId,
        String sourceType,
        UUID refId,
        UUID templateReferenceId,
        String displayName)
        implements NarrativePacket {

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

    public static final ResourceLocation TYPE =
            new ResourceLocation(NarrativeCraftMod.MOD_ID, "camera_angle_add_template_reference");

    public static C2SCameraAngleAddTemplateReference read(FriendlyByteBuf buf) {
        return new C2SCameraAngleAddTemplateReference(
                buf.readUUID(),
                buf.readUUID(),
                buf.readUUID(),
                buf.readUtf(),
                buf.readUUID(),
                buf.readUUID(),
                buf.readUtf());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(chapterId);
        buf.writeUUID(sceneId);
        buf.writeUUID(cameraAngleId);
        buf.writeUtf(sourceType);
        buf.writeUUID(refId);
        buf.writeUUID(templateReferenceId);
        buf.writeUtf(displayName);
    }

    @Override
    public ResourceLocation type() {
        return TYPE;
    }
}
