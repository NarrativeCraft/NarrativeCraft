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

package fr.loudo.narrativecraft.network.interaction;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.interaction.Interaction;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class C2SInteractionEnter implements CustomPacketPayload {

    private final UUID chapterId;
    private final UUID sceneId;
    private final UUID interactionId;

    public C2SInteractionEnter(Interaction interaction) {
        this.chapterId = interaction.getScene().getChapter().getId();
        this.sceneId = interaction.getScene().getId();
        this.interactionId = interaction.getId();
    }

    public C2SInteractionEnter(UUID chapterId, UUID sceneId, UUID interactionId) {
        this.chapterId = chapterId;
        this.sceneId = sceneId;
        this.interactionId = interactionId;
    }

    public static final Type<C2SInteractionEnter> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "interaction_enter"));

    public static final StreamCodec<ByteBuf, C2SInteractionEnter> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            C2SInteractionEnter::getChapterId,
            UUIDUtil.STREAM_CODEC,
            C2SInteractionEnter::getSceneId,
            UUIDUtil.STREAM_CODEC,
            C2SInteractionEnter::getInteractionId,
            C2SInteractionEnter::new);

    public UUID getChapterId() {
        return chapterId;
    }

    public UUID getSceneId() {
        return sceneId;
    }

    public UUID getInteractionId() {
        return interactionId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
