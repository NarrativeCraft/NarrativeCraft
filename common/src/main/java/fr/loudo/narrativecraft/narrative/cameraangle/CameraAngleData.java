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

package fr.loudo.narrativecraft.narrative.cameraangle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.loudo.narrativecraft.narrative.NarrativeEntryDetail;
import fr.loudo.narrativecraft.utils.codec.NarrativeCodecs;
import java.util.List;

public record CameraAngleData(
        List<CameraView> cameras,
        List<CharacterPlacement> characterPlacements,
        List<TemplateReference> templateReferences)
        implements NarrativeEntryDetail {

    public static final MapCodec<CameraAngleData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    NarrativeCodecs.field(NarrativeCodecs.lenientList(CameraView.CODEC), "cameras", List.of())
                            .forGetter(CameraAngleData::cameras),
                    NarrativeCodecs.field(
                                    NarrativeCodecs.lenientList(CharacterPlacement.CODEC),
                                    "characterPlacements",
                                    List.of())
                            .forGetter(CameraAngleData::characterPlacements),
                    NarrativeCodecs.field(
                                    NarrativeCodecs.lenientList(TemplateReference.CODEC),
                                    "templateReferences",
                                    List.of())
                            .forGetter(CameraAngleData::templateReferences))
            .apply(instance, CameraAngleData::new));

    public static final Codec<CameraAngleData> CODEC = MAP_CODEC.codec();

    public static final CameraAngleData EMPTY = new CameraAngleData(List.of(), List.of(), List.of());

    public CameraAngleData {
        cameras = List.copyOf(cameras);
        characterPlacements = List.copyOf(characterPlacements);
        templateReferences = List.copyOf(templateReferences);
    }
}
