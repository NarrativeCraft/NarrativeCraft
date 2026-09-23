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

package fr.loudo.narrativecraft.narrative;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.loudo.narrativecraft.narrative.animation.AnimationPayload;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngleData;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAnglePayload;
import fr.loudo.narrativecraft.narrative.chapter.ChapterPayload;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryPayload;
import fr.loudo.narrativecraft.narrative.cutscene.CutscenePayload;
import fr.loudo.narrativecraft.narrative.cutscene.CutsceneTimeline;
import fr.loudo.narrativecraft.narrative.interaction.InteractionData;
import fr.loudo.narrativecraft.narrative.interaction.InteractionPayload;
import fr.loudo.narrativecraft.narrative.npc.NpcPayload;
import fr.loudo.narrativecraft.narrative.scene.ScenePayload;
import fr.loudo.narrativecraft.narrative.subscene.SubscenePayload;
import fr.loudo.narrativecraft.utils.codec.NarrativeCodecs;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.network.codec.StreamCodec;

public enum NarrativeEntryType {
    CHAPTER(ChapterPayload.class, ChapterPayload.CODEC),
    SCENE(ScenePayload.class, ScenePayload.CODEC),
    ANIMATION(AnimationPayload.class, AnimationPayload.CODEC),
    SUBSCENE(SubscenePayload.class, SubscenePayload.CODEC),
    CUTSCENE(CutscenePayload.class, CutscenePayload.CODEC, CutsceneTimeline.class, CutsceneTimeline.CODEC),
    CAMERA_ANGLE(CameraAnglePayload.class, CameraAnglePayload.CODEC, CameraAngleData.class, CameraAngleData.CODEC),
    INTERACTION(InteractionPayload.class, InteractionPayload.CODEC, InteractionData.class, InteractionData.CODEC),
    CHARACTER(CharacterStoryPayload.class, CharacterStoryPayload.CODEC),
    NPC(NpcPayload.class, NpcPayload.CODEC);

    private final Class<? extends NarrativeEntryPayload> payloadClass;
    private final StreamCodec<ByteBuf, ? extends NarrativeEntryPayload> codec;
    private final Class<? extends NarrativeEntryDetail> detailClass;
    private final StreamCodec<ByteBuf, ? extends NarrativeEntryDetail> detailCodec;

    <P extends NarrativeEntryPayload> NarrativeEntryType(Class<P> payloadClass, MapCodec<P> codec) {
        this.payloadClass = payloadClass;
        this.codec = NarrativeCodecs.streamCodec(codec.codec());
        this.detailClass = null;
        this.detailCodec = null;
    }

    <P extends NarrativeEntryPayload, D extends NarrativeEntryDetail> NarrativeEntryType(
            Class<P> payloadClass, MapCodec<P> codec, Class<D> detailClass, Codec<D> detailCodec) {
        this.payloadClass = payloadClass;
        this.codec = NarrativeCodecs.streamCodec(codec.codec());
        this.detailClass = detailClass;
        this.detailCodec = NarrativeCodecs.streamCodec(detailCodec);
    }

    public static NarrativeEntryType fromPayload(NarrativeEntryPayload payload) {
        for (NarrativeEntryType type : values()) {
            if (type.payloadClass.equals(payload.getClass())) {
                return type;
            }
        }
        throw new IllegalArgumentException(
                "Unknown NarrativeEntry type: " + payload.getClass().getName());
    }

    public String getKey() {
        return name().toLowerCase(Locale.ROOT);
    }

    @SuppressWarnings("unchecked")
    public <P extends NarrativeEntryPayload> StreamCodec<ByteBuf, P> getCodec() {
        return (StreamCodec<ByteBuf, P>) codec;
    }

    public boolean hasDetail() {
        return detailCodec != null;
    }

    public boolean acceptsDetail(NarrativeEntryDetail detail) {
        return detailClass != null && detailClass.isInstance(detail);
    }

    @SuppressWarnings("unchecked")
    public <D extends NarrativeEntryDetail> StreamCodec<ByteBuf, D> getDetailCodec() {
        if (detailCodec == null) {
            throw new IllegalStateException(name() + " entries have no detail");
        }
        return (StreamCodec<ByteBuf, D>) detailCodec;
    }
}
