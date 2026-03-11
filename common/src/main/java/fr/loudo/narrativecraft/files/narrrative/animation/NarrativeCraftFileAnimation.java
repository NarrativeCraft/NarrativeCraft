/*
 * NarrativeCraft - Create your own stories, easily, and freely in Minecraft.
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

package fr.loudo.narrativecraft.files.narrrative.animation;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.DeserializationResult;
import fr.loudo.narrativecraft.files.NarrativeCraftFileDefault;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileUtil;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.recording.Recording;
import fr.loudo.narrativecraft.recording.RecordingData;
import fr.loudo.narrativecraft.recording.RecordingReader;
import fr.loudo.narrativecraft.recording.RecordingWriter;
import fr.loudo.narrativecraft.recording.actions.AbstractAction;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class NarrativeCraftFileAnimation extends NarrativeCraftFileDefault
        implements NarrativeCraftFileEditor<Animation> {

    @Override
    public int create(Animation entry) {

        File animationsFolder = NarrativeCraftFileUtil.getAnimationsFolder(entry.getScene());

        Recording recording =
                NarrativeCraftMod.getInstance().getRecordingManager().getById(entry.getId());
        if (recording == null) {
            NarrativeCraftMod.LOGGER.error(
                    "Failed to create animation file {} because the associated record could not be found.",
                    entry.getName());
            return NarrativeCraftFileEditor.OPERATION_FAILED;
        }

        RecordingData recordingData = recording.getRecordingData();

        try (DataOutputStream stream =
                new DataOutputStream(new FileOutputStream(new File(animationsFolder, entry.toFileName())))) {
            RecordingWriter writer = new RecordingWriter(stream);
            writer.writeHeader(
                    recordingData.getRecordingId(),
                    recordingData.getPlayerUUID(),
                    entry.getName(),
                    entry.getActions().size());
            for (AbstractAction action : entry.getActions()) {
                writer.writeActionRecord(action);
            }
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to save animation {}!", entry.getName(), e);
            return NarrativeCraftFileEditor.OPERATION_FAILED;
        }

        return NarrativeCraftFileEditor.OPERATION_SUCCESS;
    }

    @Override
    public int edit(Animation entry) {

        Animation oldAnimation = entry.getScene().getAnimationManager().getById(entry.getId());

        File animationsFolder = NarrativeCraftFileUtil.getAnimationsFolder(entry.getScene());

        File oldFileRecord = new File(animationsFolder, oldAnimation.toFileName());
        File newFileRecord = new File(animationsFolder, entry.toFileName());

        try (DataInputStream inStream = new DataInputStream(new FileInputStream(oldFileRecord))) {
            RecordingReader reader = new RecordingReader(inStream);
            RecordingReader.RecordingHeader header = reader.readHeader();
            List<AbstractAction> actions = reader.readAllActions(header.actionCount());

            try (DataOutputStream outStream = new DataOutputStream(new FileOutputStream(newFileRecord))) {
                RecordingWriter writer = new RecordingWriter(outStream);
                writer.writeHeader(header.recordingId(), header.playerUUID(), entry.getName(), actions.size());
                for (AbstractAction action : actions) {
                    writer.writeActionRecord(action);
                }
            }
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to edit animation {}", oldAnimation.getName(), e);
            return NarrativeCraftFileEditor.OPERATION_FAILED;
        }

        if (!oldFileRecord.getName().equals(newFileRecord.getName())) {
            if (!oldFileRecord.delete()) {
                NarrativeCraftMod.LOGGER.warn("Failed to delete old animation file {}", oldFileRecord.getName());
            }
        }

        return NarrativeCraftFileEditor.OPERATION_SUCCESS;
    }

    @Override
    public int delete(Animation entry) {

        File animationsFolder = NarrativeCraftFileUtil.getAnimationsFolder(entry.getScene());
        File fileRecord = new File(animationsFolder, entry.toFileName());

        if (!fileRecord.exists()) {
            NarrativeCraftMod.LOGGER.error(
                    "Failed to rename animation {} because the file doesn't exists", entry.getName());
            return NarrativeCraftFileEditor.OPERATION_FAILED;
        }

        if (!fileRecord.delete()) {
            NarrativeCraftMod.LOGGER.error("Failed to rename animation {}", entry.getName());
            return NarrativeCraftFileEditor.OPERATION_FAILED;
        }

        return NarrativeCraftFileEditor.OPERATION_SUCCESS;
    }

    @Override
    public List<DeserializationResult<Animation>> deserialize() {

        List<DeserializationResult<Animation>> deserializationResults = new ArrayList<>();

        for (Chapter chapter :
                NarrativeCraftMod.getInstance().getChapterManager().getList()) {
            for (Scene scene : chapter.getSceneManager().getList()) {

                File animationsFolder = NarrativeCraftFileUtil.getAnimationsFolder(scene);
                File[] animations = animationsFolder.listFiles();
                if (animations == null) {
                    continue;
                }

                for (File file : animations) {
                    try (DataInputStream stream = new DataInputStream(new FileInputStream(file))) {
                        RecordingReader reader = new RecordingReader(stream);
                        RecordingReader.RecordingHeader header = reader.readHeader();
                        RecordingData data = new RecordingData(header.recordingId(), header.playerUUID());
                        for (AbstractAction action : reader.readAllActions(header.actionCount())) {
                            data.addAction(action);
                        }
                        Animation animation = new Animation(header.recordingId(), header.name(), scene);
                        deserializationResults.add(new DeserializationResult<>(animation, false, file.getName()));
                    } catch (IOException e) {
                        deserializationResults.add(new DeserializationResult<>(null, true, file.getName()));
                    }
                }
            }
        }

        return deserializationResults;
    }
}
