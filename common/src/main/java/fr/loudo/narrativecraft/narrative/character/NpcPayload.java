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

package fr.loudo.narrativecraft.narrative.character;

import fr.loudo.narrativecraft.narrative.NarrativeEntryPayload;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class NpcPayload extends NarrativeEntryPayload {

    public static final StreamCodec<ByteBuf, NpcPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            NpcPayload::getName,
            ByteBufCodecs.STRING_UTF8,
            NpcPayload::getDialogPresetName,
            ByteBufCodecs.STRING_UTF8,
            NpcPayload::getModelType,
            ByteBufCodecs.STRING_UTF8,
            NpcPayload::getEntityTypeId,
            UUIDUtil.STREAM_CODEC,
            NpcPayload::getSceneId,
            UUIDUtil.STREAM_CODEC,
            NpcPayload::getChapterId,
            NpcPayload::new);

    private final String dialogPresetName;
    private final String modelType;
    private final String entityTypeId;
    private final UUID sceneId;
    private final UUID chapterId;

    public NpcPayload(
            String name, String dialogPresetName, String modelType, String entityTypeId, UUID sceneId, UUID chapterId) {
        super(name, "");
        this.dialogPresetName = dialogPresetName != null ? dialogPresetName : "";
        this.modelType = modelType != null ? modelType : "";
        this.entityTypeId = entityTypeId;
        this.sceneId = sceneId;
        this.chapterId = chapterId;
    }

    public String getDialogPresetName() {
        return dialogPresetName;
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
}
