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

package fr.loudo.narrativecraft.narrative.cutscene;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.loudo.narrativecraft.narrative.SceneEntryPayload;
import fr.loudo.narrativecraft.utils.codec.NarrativeCodecs;
import java.util.List;
import java.util.UUID;

public class CutscenePayload extends SceneEntryPayload {

    public static final MapCodec<CutscenePayload> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    nameField(),
                    sceneIdField(),
                    chapterIdField(),
                    NarrativeCodecs.field(NarrativeCodecs.UUID_CODEC.listOf(), "animationIds", List.of())
                            .forGetter(CutscenePayload::getAnimationIds),
                    NarrativeCodecs.field(NarrativeCodecs.UUID_CODEC.listOf(), "subsceneIds", List.of())
                            .forGetter(CutscenePayload::getSubsceneIds))
            .apply(instance, CutscenePayload::new));

    private final List<UUID> animationIds;
    private final List<UUID> subsceneIds;

    public CutscenePayload(String name, UUID sceneId, UUID chapterId, List<UUID> animationIds, List<UUID> subsceneIds) {
        super(name, sceneId, chapterId);
        this.animationIds = animationIds;
        this.subsceneIds = subsceneIds;
    }

    public List<UUID> getAnimationIds() {
        return animationIds;
    }

    public List<UUID> getSubsceneIds() {
        return subsceneIds;
    }
}
