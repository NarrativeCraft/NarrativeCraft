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

package fr.loudo.narrativecraft.narrative.interaction;

import fr.loudo.narrativecraft.narrative.NarrativeEntryPayload;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class InteractionPayload extends NarrativeEntryPayload {

    private final UUID chapterId;
    private final UUID sceneId;

    public static final StreamCodec<ByteBuf, InteractionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            InteractionPayload::getName,
            ByteBufCodecs.STRING_UTF8,
            InteractionPayload::getDescription,
            UUIDUtil.STREAM_CODEC,
            InteractionPayload::getSceneId,
            UUIDUtil.STREAM_CODEC,
            InteractionPayload::getChapterId,
            InteractionPayload::new);

    public InteractionPayload(String name, String description, UUID sceneId, UUID chapterId) {
        super(name, description);
        this.sceneId = sceneId;
        this.chapterId = chapterId;
    }

    public UUID getChapterId() {
        return chapterId;
    }

    public UUID getSceneId() {
        return sceneId;
    }
}
