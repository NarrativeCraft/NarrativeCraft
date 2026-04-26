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

package fr.loudo.narrativecraft.narrative;

import fr.loudo.narrativecraft.narrative.animation.AnimationPayload;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAnglePayload;
import fr.loudo.narrativecraft.narrative.chapter.ChapterPayload;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryPayload;
import fr.loudo.narrativecraft.narrative.cutscene.CutscenePayload;
import fr.loudo.narrativecraft.narrative.interaction.InteractionPayload;
import fr.loudo.narrativecraft.narrative.npc.NpcPayload;
import fr.loudo.narrativecraft.narrative.scene.ScenePayload;
import fr.loudo.narrativecraft.narrative.subscene.SubscenePayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public enum NarrativeEntryType {
    CHAPTER(ChapterPayload.class, ChapterPayload.STREAM_CODEC),
    SCENE(ScenePayload.class, ScenePayload.STREAM_CODEC),
    ANIMATION(AnimationPayload.class, AnimationPayload.STREAM_CODEC),
    SUBSCENE(SubscenePayload.class, SubscenePayload.STREAM_CODEC),
    CUTSCENE(CutscenePayload.class, CutscenePayload.STREAM_CODEC),
    CAMERA_ANGLE(CameraAnglePayload.class, CameraAnglePayload.STREAM_CODEC),
    INTERACTION(InteractionPayload.class, InteractionPayload.STREAM_CODEC),
    CHARACTER(CharacterStoryPayload.class, CharacterStoryPayload.STREAM_CODEC),
    NPC(NpcPayload.class, NpcPayload.STREAM_CODEC);

    private final Class<? extends NarrativeEntryPayload> clazz;
    private final StreamCodec<? super ByteBuf, ? extends NarrativeEntryPayload> codec;

    <T extends NarrativeEntryPayload> NarrativeEntryType(Class<T> clazz, StreamCodec<? super ByteBuf, T> codec) {
        this.clazz = clazz;
        this.codec = codec;
    }

    public static NarrativeEntryType fromClass(Class<? extends NarrativeEntryPayload> clazz) {
        for (NarrativeEntryType type : values()) {
            if (type.clazz.equals(clazz)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown NarrativeEntry type: " + clazz.getName());
    }

    @SuppressWarnings("unchecked")
    public <T extends NarrativeEntryPayload> StreamCodec<? super ByteBuf, T> getCodec() {
        return (StreamCodec<? super ByteBuf, T>) codec;
    }
}
