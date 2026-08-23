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

package fr.loudo.narrativecraft.narrative.npc;

import fr.loudo.narrativecraft.narrative.NarrativeEntryPayload;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

public class NpcPayload extends NarrativeEntryPayload {

    private final String modelType;
    private final String entityTypeId;
    private final UUID sceneId;
    private final UUID chapterId;
    private final String dialogDataJson;
    private final String customNbt;

    public NpcPayload(
            String name,
            String modelType,
            String entityTypeId,
            UUID sceneId,
            UUID chapterId,
            String dialogDataJson,
            String customNbt) {
        super(name, "");
        this.modelType = modelType != null ? modelType : "";
        this.entityTypeId = entityTypeId;
        this.sceneId = sceneId;
        this.chapterId = chapterId;
        this.dialogDataJson = dialogDataJson != null ? dialogDataJson : "{}";
        this.customNbt = customNbt;
    }

    public static NpcPayload read(FriendlyByteBuf buf) {
        return new NpcPayload(
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUUID(),
                buf.readUUID(),
                buf.readUtf(),
                buf.readUtf());
    }

    @Override
    protected void writeData(FriendlyByteBuf buf) {
        buf.writeUtf(getName());
        buf.writeUtf(modelType);
        buf.writeUtf(entityTypeId);
        buf.writeUUID(sceneId);
        buf.writeUUID(chapterId);
        buf.writeUtf(dialogDataJson);
        buf.writeUtf(customNbt);
    }

    public String getModelType() {
        return modelType;
    }

    public String getEntityTypeId() {
        return entityTypeId;
    }

    public UUID getSceneId() {
        return sceneId;
    }

    public UUID getChapterId() {
        return chapterId;
    }

    public String getDialogDataJson() {
        return dialogDataJson;
    }

    public String getCustomNbt() {
        return customNbt;
    }
}
