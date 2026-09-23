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

package fr.loudo.narrativecraft.narrative.interaction;

import fr.loudo.narrativecraft.files.NarrativeCraftFileRegistry;
import fr.loudo.narrativecraft.narrative.AbstractSceneEntryEditor;
import fr.loudo.narrativecraft.narrative.NarrativeManager;
import fr.loudo.narrativecraft.narrative.OperationResult;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import java.util.UUID;

public class InteractionEditor extends AbstractSceneEntryEditor<InteractionPayload, Interaction> {

    @Override
    protected String getTypeKey() {
        return "interaction";
    }

    @Override
    protected NarrativeManager<Interaction> getManager(Scene scene) {
        return scene.getInteractionManager();
    }

    @Override
    protected Interaction build(UUID entryId, InteractionPayload payload, Scene scene, Interaction existing) {
        Interaction interaction = new Interaction(entryId, payload.getName(), scene);
        if (existing != null) {
            interaction.copyDataFrom(existing);
        }
        return interaction;
    }

    @Override
    protected void copyAttributes(Interaction target, Interaction source) {}

    public OperationResult saveData(Interaction interaction, String dataJson) {
        Interaction updated = new Interaction(interaction.getId(), interaction.getName(), interaction.getScene());
        try {
            InteractionDeserializer.deserializeInto(dataJson, updated);
        } catch (RuntimeException e) {
            return OperationResult.failure("error.invalid_data", interaction.getName());
        }

        OperationResult storage = NarrativeCraftFileRegistry.getInstance().edit(interaction, updated);
        if (storage.isFailure()) return storage;

        interaction.copyDataFrom(updated);
        return OperationResult.success();
    }
}
