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

package fr.loudo.narrativecraft.network.cutscene;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class BiCutsceneEnter implements CustomPacketPayload {

    private final UUID chapterId;
    private final UUID sceneId;
    private final UUID cutsceneId;
    private final NarrativeEnvironment environment;

    public BiCutsceneEnter(Cutscene cutscene, NarrativeEnvironment environment) {
        this.chapterId = cutscene.getScene().getChapter().getId();
        this.sceneId = cutscene.getScene().getId();
        this.cutsceneId = cutscene.getId();
        this.environment = environment;
    }

    public BiCutsceneEnter(UUID chapterId, UUID sceneId, UUID cutsceneId, NarrativeEnvironment environment) {
        this.chapterId = chapterId;
        this.sceneId = sceneId;
        this.cutsceneId = cutsceneId;
        this.environment = environment;
    }

    public static final Type<BiCutsceneEnter> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "cutscene_state"));

    public static final StreamCodec<ByteBuf, BiCutsceneEnter> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            BiCutsceneEnter::getChapterId,
            UUIDUtil.STREAM_CODEC,
            BiCutsceneEnter::getSceneId,
            UUIDUtil.STREAM_CODEC,
            BiCutsceneEnter::getCutsceneId,
            ByteBufCodecs.idMapper(i -> NarrativeEnvironment.values()[i], NarrativeEnvironment::ordinal),
            BiCutsceneEnter::getEnvironment,
            BiCutsceneEnter::new);

    public UUID getChapterId() {
        return chapterId;
    }

    public UUID getSceneId() {
        return sceneId;
    }

    public UUID getCutsceneId() {
        return cutsceneId;
    }

    public NarrativeEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
