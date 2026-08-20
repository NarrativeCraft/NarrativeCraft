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
import fr.loudo.narrativecraft.api.narrative.animation.IAnimation;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.files.NarrativeCraftFileUtil;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.recording.Recording;
import fr.loudo.narrativecraft.recording.RecordingData;
import fr.loudo.narrativecraft.recording.RecordingReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Animation extends NarrativeEntry<AnimationPayload> implements IAnimation {

    private final Scene scene;
    private final List<RecordingData> recordingDataList = new ArrayList<>();
    private int totalTick;
    private ICharacterStory characterStory;

    public Animation(UUID id, String name, String description, Scene scene, ICharacterStory characterStory) {
        super(id, name, description);
        this.scene = scene;
        this.characterStory = characterStory;
    }

    public Animation(UUID id, String name, Scene scene, int totalTick, ICharacterStory characterStory) {
        super(id, name, "");
        this.scene = scene;
        this.totalTick = totalTick;
        this.characterStory = characterStory;
    }

    public Animation(UUID id, String name, Scene scene) {
        super(id, name, "");
        this.scene = scene;
    }

    // Why deserialization's here : to not have all animations in memory when the server start.
    // By moving this here, we take the data when we need it, then it's on cache.
    public boolean initialize() {
        if (!recordingDataList.isEmpty()) return true;

        File animationsFolder = NarrativeCraftFileUtil.getAnimationsFolder(scene);
        File animationFile = new File(animationsFolder, toFileName());

        if (!animationFile.exists()) {
            NarrativeCraftMod.LOGGER.error(
                    "Animation file {} does not exist. Chapter {} scene {}",
                    toFileName(),
                    scene.getChapter().getChapterIndex(),
                    scene.getName());
            return false;
        }

        try (DataInputStream stream = new DataInputStream(new FileInputStream(animationFile))) {
            RecordingReader reader = new RecordingReader(stream);
            RecordingReader.RecordingHeader header = reader.readHeader();
            totalTick = header.totalTick();
            reader.readLocalActionsId();
            for (int i = 0; i < header.entityCount(); i++) {
                RecordingReader.EntityHeader entityHeader = reader.readEntityHeader();
                RecordingData recordingData =
                        new RecordingData(entityHeader.entityRecordingId(), entityHeader.entityType());
                recordingData.setInitialNbt(entityHeader.initialNbt());
                recordingData.setSpawnTick(entityHeader.spawnTick());
                for (AbstractAction action : reader.readAllActions(entityHeader.actionCount())) {
                    recordingData.addAction(action);
                }
                recordingDataList.add(recordingData);
            }

        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Could not read recording data of animation {}", toFileName(), e);
            return false;
        }

        return true;
    }

    public List<Subscene> getLinkedSubscenes() {
        List<Subscene> subscenes = new ArrayList<>();
        for (Subscene subscene : scene.getSubsceneManager().getList()) {
            if (subscene.getAnimations().contains(this)) {
                subscenes.add(subscene);
            }
        }
        return subscenes;
    }

    public int getTotalTick() {
        return totalTick;
    }

    public ICharacterStory getCharacterStory() {
        return characterStory;
    }

    public void setCharacterStory(ICharacterStory characterStory) {
        this.characterStory = characterStory;
    }

    @Override
    public AnimationPayload toPayload() {
        return new AnimationPayload(
                name, description, scene.getId(), scene.getChapter().getId(), totalTick, characterStory.getId());
    }

    @Override
    public String formattedName() {
        return name;
    }

    @Override
    public String toFileName() {
        return name.replace(" ", "_").toLowerCase() + Recording.RECORDING_EXTENSION;
    }

    public Scene getScene() {
        return scene;
    }

    public List<RecordingData> getRecordingDataList() {
        return recordingDataList;
    }
}
