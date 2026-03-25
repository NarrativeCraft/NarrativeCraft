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

package fr.loudo.narrativecraft.network.cutscene;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class C2SCutsceneState implements CustomPacketPayload {

    private final State state;
    private final UUID chapterId;
    private final UUID sceneId;
    private final UUID cutsceneId;

    public C2SCutsceneState(State state, Cutscene cutscene) {
        this.state = state;
        this.chapterId = cutscene.getScene().getChapter().getId();
        this.sceneId = cutscene.getScene().getId();
        this.cutsceneId = cutscene.getId();
    }

    public C2SCutsceneState(State state, UUID chapterId, UUID sceneId, UUID cutsceneId) {
        this.state = state;
        this.chapterId = chapterId;
        this.sceneId = sceneId;
        this.cutsceneId = cutsceneId;
    }

    public static final Type<C2SCutsceneState> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "cutscene_state"));

    public static final StreamCodec<ByteBuf, C2SCutsceneState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE.map(b -> State.values()[b], s -> (byte) s.ordinal()),
            C2SCutsceneState::getState,
            UUIDUtil.STREAM_CODEC,
            C2SCutsceneState::getChapterId,
            UUIDUtil.STREAM_CODEC,
            C2SCutsceneState::getSceneId,
            UUIDUtil.STREAM_CODEC,
            C2SCutsceneState::getCutsceneId,
            C2SCutsceneState::new);

    public State getState() {
        return state;
    }

    public UUID getChapterId() {
        return chapterId;
    }

    public UUID getSceneId() {
        return sceneId;
    }

    public UUID getCutsceneId() {
        return cutsceneId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum State {
        ENTER,
        PLAY,
        PAUSE,
        QUIT;
    }
}
