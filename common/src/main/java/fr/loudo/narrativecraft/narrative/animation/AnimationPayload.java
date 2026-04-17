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

package fr.loudo.narrativecraft.narrative.animation;

import fr.loudo.narrativecraft.narrative.NarrativeEntryPayload;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class AnimationPayload extends NarrativeEntryPayload {

    private final UUID chapterId;
    private final UUID sceneId;
    private final int totalTick;
    private final String characterRef;

    public static final StreamCodec<ByteBuf, AnimationPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            AnimationPayload::getName,
            ByteBufCodecs.STRING_UTF8,
            AnimationPayload::getDescription,
            UUIDUtil.STREAM_CODEC,
            AnimationPayload::getSceneId,
            UUIDUtil.STREAM_CODEC,
            AnimationPayload::getChapterId,
            ByteBufCodecs.INT,
            AnimationPayload::getTotalTick,
            ByteBufCodecs.STRING_UTF8,
            AnimationPayload::getCharacterRef,
            AnimationPayload::new);

    public AnimationPayload(
            String name, String description, UUID sceneId, UUID chapterId, int totalTick, String characterRef) {
        super(name, description);
        this.chapterId = chapterId;
        this.sceneId = sceneId;
        this.totalTick = totalTick;
        this.characterRef = characterRef != null ? characterRef : "";
    }

    public UUID getChapterId() {
        return chapterId;
    }

    public UUID getSceneId() {
        return sceneId;
    }

    public int getTotalTick() {
        return totalTick;
    }

    public String getCharacterRef() {
        return characterRef;
    }
}
