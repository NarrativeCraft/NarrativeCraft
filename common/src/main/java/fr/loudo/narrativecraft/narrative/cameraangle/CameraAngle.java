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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.loudo.narrativecraft.api.narrative.cameraangle.ICameraAngle;
import fr.loudo.narrativecraft.files.NarrativeCraftFileDefault;
import fr.loudo.narrativecraft.narrative.DetailedNarrativeEntry;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.utils.codec.NarrativeCodecs;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CameraAngle extends NarrativeEntry<CameraAnglePayload>
        implements ICameraAngle, DetailedNarrativeEntry<CameraAngleData> {

    public static final Codec<CameraAngle> MAIN_SCREEN_CODEC =
            NarrativeCodecs.versioned(RecordCodecBuilder.mapCodec(instance -> instance.group(
                            NarrativeCodecs.ID.forGetter(CameraAngle::getId),
                            Codec.STRING.fieldOf("name").forGetter(CameraAngle::getName),
                            CameraAngleData.MAP_CODEC.forGetter(CameraAngle::getDetail))
                    .apply(instance, (id, name, data) -> {
                        CameraAngle cameraAngle = new CameraAngle(id, name, null);
                        cameraAngle.setDetail(data);
                        return cameraAngle;
                    })));

    private final Scene scene;
    private final List<CameraView> cameraViews = new ArrayList<>();
    private final List<CharacterPlacement> characterPlacements = new ArrayList<>();
    private final List<TemplateReference> templateReferences = new ArrayList<>();

    public CameraAngle(UUID id, String name, Scene scene) {
        super(id, name);
        this.scene = scene;
    }

    public CameraAngle(String name, Scene scene) {
        super(name);
        this.scene = scene;
    }

    public static Codec<CameraAngle> codec(Scene scene) {
        return entryCodec(
                CameraAnglePayload.CODEC, CameraAngleData.MAP_CODEC, (id, payload) -> fromPayload(id, payload, scene));
    }

    public static CameraAngle fromPayload(UUID id, CameraAnglePayload payload, Scene scene) {
        return new CameraAngle(id, payload.getName(), scene);
    }

    @Override
    public CameraAngleData getDetail() {
        return new CameraAngleData(cameraViews, characterPlacements, templateReferences);
    }

    @Override
    public void setDetail(CameraAngleData data) {
        cameraViews.clear();
        cameraViews.addAll(data.cameras());
        characterPlacements.clear();
        characterPlacements.addAll(data.characterPlacements());
        templateReferences.clear();
        templateReferences.addAll(data.templateReferences());
    }

    public Scene getScene() {
        return scene;
    }

    public List<CameraView> getCameras() {
        return cameraViews;
    }

    public List<CharacterPlacement> getCharacterPlacements() {
        return characterPlacements;
    }

    public List<TemplateReference> getTemplateReferences() {
        return templateReferences;
    }

    public CameraView getCameraByName(String name) {
        for (CameraView cameraView : cameraViews) {
            if (cameraView.getName().equalsIgnoreCase(name)) return cameraView;
        }
        return null;
    }

    @Override
    public CameraAnglePayload toPayload() {
        return new CameraAnglePayload(name, scene.getId(), scene.getChapter().getId());
    }

    @Override
    public String formattedName() {
        return name;
    }

    @Override
    public String toFileName() {
        return getNormalizedName() + NarrativeCraftFileDefault.EXTENSION_DATA_FILE;
    }
}
