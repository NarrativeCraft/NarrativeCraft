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

import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.files.NarrativeCraftFileRegistry;
import fr.loudo.narrativecraft.narrative.AbstractSceneEntryEditor;
import fr.loudo.narrativecraft.narrative.NarrativeManager;
import fr.loudo.narrativecraft.narrative.OperationResult;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CutsceneEditor extends AbstractSceneEntryEditor<CutscenePayload, Cutscene> {

    @Override
    protected String getTypeKey() {
        return "cutscene";
    }

    @Override
    protected NarrativeManager<Cutscene> getManager(Scene scene) {
        return scene.getCutsceneManager();
    }

    @Override
    protected Cutscene build(UUID entryId, CutscenePayload payload, Scene scene, Cutscene existing) {
        List<Animation> animations = payload.getAnimationIds().stream()
                .map(animationId -> scene.getAnimationManager().getById(animationId))
                .filter(Objects::nonNull)
                .toList();
        List<Subscene> subscenes = payload.getSubsceneIds().stream()
                .map(subsceneId -> scene.getSubsceneManager().getById(subsceneId))
                .filter(Objects::nonNull)
                .toList();
        Cutscene cutscene = new Cutscene(entryId, payload.getName(), scene, animations, subscenes);
        if (existing != null) {
            cutscene.setLayers(existing.getLayers());
            cutscene.setManualMaxTick(existing.getManualMaxTick());
        }
        return cutscene;
    }

    @Override
    protected void copyAttributes(Cutscene target, Cutscene source) {
        target.setAnimations(source.getAnimations());
        target.setSubscenes(source.getSubscenes());
    }

    public OperationResult saveLayers(Cutscene cutscene, String layersJson, int manualMaxTick) {
        List<CutsceneLayer> layers;
        try {
            layers = CutsceneDeserializer.parseLayers(layersJson);
        } catch (RuntimeException e) {
            return OperationResult.failure("error.invalid_data", cutscene.getName());
        }

        Cutscene updated = new Cutscene(
                cutscene.getId(),
                cutscene.getName(),
                cutscene.getScene(),
                cutscene.getAnimations(),
                cutscene.getSubscenes());
        updated.setLayers(layers);
        updated.setManualMaxTick(manualMaxTick);

        OperationResult storage = NarrativeCraftFileRegistry.getInstance().edit(cutscene, updated);
        if (storage.isFailure()) return storage;

        cutscene.setLayers(layers);
        cutscene.setManualMaxTick(manualMaxTick);
        return OperationResult.success();
    }
}
