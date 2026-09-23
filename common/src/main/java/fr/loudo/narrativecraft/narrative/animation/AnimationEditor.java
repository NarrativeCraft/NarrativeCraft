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

package fr.loudo.narrativecraft.narrative.animation;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFileRegistry;
import fr.loudo.narrativecraft.managers.AnimationManager;
import fr.loudo.narrativecraft.narrative.AbstractSceneEntryEditor;
import fr.loudo.narrativecraft.narrative.NarrativeManager;
import fr.loudo.narrativecraft.narrative.OperationResult;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.recording.Recording;
import fr.loudo.narrativecraft.recording.RecordingEntityData;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.UtilsServer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnimationEditor extends AbstractSceneEntryEditor<AnimationPayload, Animation> {

    @Override
    protected String getTypeKey() {
        return "animation";
    }

    @Override
    protected NarrativeManager<Animation> getManager(Scene scene) {
        return scene.getAnimationManager();
    }

    @Override
    protected Animation build(UUID entryId, AnimationPayload payload, Scene scene, Animation existing) {
        ICharacterStory characterStory =
                NarrativeCraftMod.getInstance().getCharacterManager().resolveCharacter(payload.getCharacterId(), scene);
        int totalTick = existing == null ? payload.getTotalTick() : existing.getTotalTick();
        return new Animation(entryId, payload.getName(), scene, totalTick, characterStory);
    }

    @Override
    protected OperationResult validate(Animation entry, NarrativeManager<Animation> siblings) {
        OperationResult validation = super.validate(entry, siblings);
        if (validation.isFailure()) return validation;
        if (entry.getCharacterStory() == null) {
            return OperationResult.failure("error.not_exists", Translation.message("character"), entry.getName());
        }
        return OperationResult.success();
    }

    @Override
    protected void copyAttributes(Animation target, Animation source) {
        target.setCharacterStory(source.getCharacterStory());
    }

    public OperationResult saveRecording(
            Recording recording, Scene scene, String name, Animation animationToOverwrite) {
        List<CharacterStory> characters =
                NarrativeCraftMod.getInstance().getCharacterManager().getList();
        if (characters.isEmpty()) return OperationResult.failure("error.record.no_characters");

        Animation animation = new Animation(recording.getId(), name, scene, recording.getTick(), characters.getFirst());
        AnimationManager animationManager = scene.getAnimationManager();

        if (animationToOverwrite != null
                && !animationToOverwrite.getNormalizedName().equals(animation.getNormalizedName())) {
            return alreadyExists(animationToOverwrite.getName());
        }
        if (animationToOverwrite == null) {
            OperationResult validation = validate(animation, animationManager);
            if (validation.isFailure()) return validation;
        }

        OperationResult storage = NarrativeCraftFileRegistry.getInstance().create(animation);
        if (storage.isFailure()) return storage;

        for (RecordingEntityData data : recording.getRecordingEntityDataList()) {
            if (!data.isTracked()) continue;
            animation.getRecordingDataList().add(data.getRecordingData());
        }

        List<Subscene> linkedSubscenes = List.of();
        if (animationToOverwrite != null) {
            linkedSubscenes = animationToOverwrite.getLinkedSubscenes();
            animationManager.remove(animationToOverwrite);
            UtilsServer.broadcastPacket(
                    BiSyncNarrativeEntryPacket.delete(animationToOverwrite.getId(), animationToOverwrite.toPayload()));
        }

        animationManager.add(animation);
        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.add(animation.getId(), animation.toPayload()));

        for (Subscene linkedSubscene : linkedSubscenes) {
            relinkSubscene(linkedSubscene, animation);
        }
        return OperationResult.success();
    }

    private void relinkSubscene(Subscene subscene, Animation animation) {
        List<Animation> animations = new ArrayList<>(subscene.getAnimations());
        animations.add(animation);
        Subscene updated = new Subscene(subscene.getId(), subscene.getName(), subscene.getScene(), animations);
        OperationResult storage = NarrativeCraftFileRegistry.getInstance().edit(subscene, updated);
        if (storage.isFailure()) {
            NarrativeCraftMod.LOGGER.warn(
                    "Failed to link animation {} back to subscene {}", animation.getName(), subscene.getName());
            return;
        }
        subscene.setAnimations(animations);
        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.edit(subscene.getId(), subscene.toPayload()));
    }
}
