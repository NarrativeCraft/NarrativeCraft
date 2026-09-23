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

package fr.loudo.narrativecraft.narrative.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.narrative.SceneEntryPayload;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.utils.codec.NarrativeCodecs;
import java.util.UUID;

public class NpcPayload extends SceneEntryPayload {

    public static final MapCodec<NpcPayload> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    nameField(),
                    sceneIdField(),
                    chapterIdField(),
                    NarrativeCodecs.fieldOrElseGet(DialogData.CHARACTER_CODEC, "dialogData", DialogData::new)
                            .forGetter(NpcPayload::getDialogData),
                    NarrativeCodecs.field(Codec.STRING, "modelType", "").forGetter(NpcPayload::getModelType),
                    NarrativeCodecs.field(Codec.STRING, "entityTypeId", CharacterStory.DEFAULT_ENTITY_TYPE_ID)
                            .forGetter(NpcPayload::getEntityTypeId),
                    NarrativeCodecs.field(Codec.STRING, "customNbt", "").forGetter(NpcPayload::getCustomNbt))
            .apply(instance, NpcPayload::new));

    private final DialogData dialogData;
    private final String modelType;
    private final String entityTypeId;
    private final String customNbt;

    public NpcPayload(
            String name,
            UUID sceneId,
            UUID chapterId,
            DialogData dialogData,
            String modelType,
            String entityTypeId,
            String customNbt) {
        super(name, sceneId, chapterId);
        this.dialogData = dialogData != null ? dialogData : new DialogData();
        this.modelType = modelType != null ? modelType : "";
        this.entityTypeId = entityTypeId;
        this.customNbt = customNbt != null ? customNbt : "";
    }

    public DialogData getDialogData() {
        return dialogData;
    }

    public String getModelType() {
        return modelType;
    }

    public String getEntityTypeId() {
        return entityTypeId;
    }

    public String getCustomNbt() {
        return customNbt;
    }
}
