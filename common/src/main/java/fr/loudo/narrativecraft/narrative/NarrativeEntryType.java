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

import fr.loudo.narrativecraft.narrative.animation.AnimationPayload;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAnglePayload;
import fr.loudo.narrativecraft.narrative.chapter.ChapterPayload;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryPayload;
import fr.loudo.narrativecraft.narrative.cutscene.CutscenePayload;
import fr.loudo.narrativecraft.narrative.interaction.InteractionPayload;
import fr.loudo.narrativecraft.narrative.npc.NpcPayload;
import fr.loudo.narrativecraft.narrative.scene.ScenePayload;
import fr.loudo.narrativecraft.narrative.subscene.SubscenePayload;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;

public enum NarrativeEntryType {
    CHAPTER(ChapterPayload.class, ChapterPayload::read),
    SCENE(ScenePayload.class, ScenePayload::read),
    ANIMATION(AnimationPayload.class, AnimationPayload::read),
    SUBSCENE(SubscenePayload.class, SubscenePayload::read),
    CUTSCENE(CutscenePayload.class, CutscenePayload::read),
    CAMERA_ANGLE(CameraAnglePayload.class, CameraAnglePayload::read),
    INTERACTION(InteractionPayload.class, InteractionPayload::read),
    CHARACTER(CharacterStoryPayload.class, CharacterStoryPayload::read),
    NPC(NpcPayload.class, NpcPayload::read);

    private final Class<? extends NarrativeEntryPayload> clazz;
    private final Function<FriendlyByteBuf, ? extends NarrativeEntryPayload> reader;

    <T extends NarrativeEntryPayload> NarrativeEntryType(Class<T> clazz, Function<FriendlyByteBuf, T> reader) {
        this.clazz = clazz;
        this.reader = reader;
    }

    public static NarrativeEntryType fromClass(Class<? extends NarrativeEntryPayload> clazz) {
        for (NarrativeEntryType type : values()) {
            if (type.clazz.equals(clazz)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown NarrativeEntry type: " + clazz.getName());
    }

    public NarrativeEntryPayload read(FriendlyByteBuf buf) {
        return reader.apply(buf);
    }
}
