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

package fr.loudo.narrativecraft.client.narrative.cutscene;

import fr.loudo.narrativecraft.client.narrative.ClientSceneEntryEditor;
import fr.loudo.narrativecraft.narrative.NarrativeManager;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.cutscene.CutscenePayload;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ClientCutsceneEditor extends ClientSceneEntryEditor<CutscenePayload, Cutscene> {

    @Override
    protected NarrativeManager<Cutscene> getManager(Scene scene) {
        return scene.getCutsceneManager();
    }

    @Override
    protected Cutscene create(UUID entryId, CutscenePayload payload, Scene scene) {
        return new Cutscene(
                entryId, payload.getName(), scene, resolveAnimations(payload, scene), resolveSubscenes(payload, scene));
    }

    @Override
    protected void update(Cutscene cutscene, CutscenePayload payload) {
        cutscene.setAnimations(resolveAnimations(payload, cutscene.getScene()));
        cutscene.setSubscenes(resolveSubscenes(payload, cutscene.getScene()));
    }

    private List<Animation> resolveAnimations(CutscenePayload payload, Scene scene) {
        return payload.getAnimationIds().stream()
                .map(animationId -> scene.getAnimationManager().getById(animationId))
                .filter(Objects::nonNull)
                .toList();
    }

    private List<Subscene> resolveSubscenes(CutscenePayload payload, Scene scene) {
        return payload.getSubsceneIds().stream()
                .map(subsceneId -> scene.getSubsceneManager().getById(subsceneId))
                .filter(Objects::nonNull)
                .toList();
    }
}
