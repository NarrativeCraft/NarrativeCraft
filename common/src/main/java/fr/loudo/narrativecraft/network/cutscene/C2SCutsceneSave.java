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
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class C2SCutsceneSave implements CustomPacketPayload {

    private final UUID chapterId;
    private final UUID sceneId;
    private final UUID cutsceneId;
    private final String layersJson;
    private final int manualMaxTick;

    public C2SCutsceneSave(Cutscene cutscene, String layersJson) {
        this.chapterId = cutscene.getScene().getChapter().getId();
        this.sceneId = cutscene.getScene().getId();
        this.cutsceneId = cutscene.getId();
        this.layersJson = layersJson;
        this.manualMaxTick = cutscene.getManualMaxTick();
    }

    public C2SCutsceneSave(UUID chapterId, UUID sceneId, UUID cutsceneId, String layersJson, int manualMaxTick) {
        this.chapterId = chapterId;
        this.sceneId = sceneId;
        this.cutsceneId = cutsceneId;
        this.layersJson = layersJson;
        this.manualMaxTick = manualMaxTick;
    }

    public static final Type<C2SCutsceneSave> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "cutscene_save"));

    public static final StreamCodec<ByteBuf, C2SCutsceneSave> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            C2SCutsceneSave::getChapterId,
            UUIDUtil.STREAM_CODEC,
            C2SCutsceneSave::getSceneId,
            UUIDUtil.STREAM_CODEC,
            C2SCutsceneSave::getCutsceneId,
            ByteBufCodecs.STRING_UTF8,
            C2SCutsceneSave::getLayersJson,
            ByteBufCodecs.VAR_INT,
            C2SCutsceneSave::getManualMaxTick,
            C2SCutsceneSave::new);

    public UUID getChapterId() {
        return chapterId;
    }

    public UUID getSceneId() {
        return sceneId;
    }

    public UUID getCutsceneId() {
        return cutsceneId;
    }

    public String getLayersJson() {
        return layersJson;
    }

    public int getManualMaxTick() {
        return manualMaxTick;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
