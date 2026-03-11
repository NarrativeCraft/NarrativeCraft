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

package fr.loudo.narrativecraft.recording;

import fr.loudo.narrativecraft.recording.actions.AbstractAction;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RecordingData {

    private final Map<Integer, List<AbstractAction>> actions = new HashMap<>();
    private final UUID recordingId;
    private final UUID playerUUID;

    public RecordingData(Recording recording) {
        this.recordingId = recording.getId();
        this.playerUUID = recording.getPlayer().getUUID();
    }

    RecordingData(UUID recordingId, UUID playerUUID) {
        this.recordingId = recordingId;
        this.playerUUID = playerUUID;
    }

    public void addAction(AbstractAction action) {
        actions.computeIfAbsent(action.getTick(), k -> new ArrayList<>()).add(action);
    }

    public void save(DataOutputStream stream) throws IOException {
        List<AbstractAction> sortedActions = actions.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .flatMap(e -> e.getValue().stream())
                .toList();

        RecordingWriter writer = new RecordingWriter(stream);
        writer.writeHeader(recordingId, playerUUID, sortedActions.size());
        for (AbstractAction action : sortedActions) {
            writer.writeActionRecord(action);
        }
    }

    public static RecordingData load(DataInputStream stream) throws IOException {
        RecordingReader reader = new RecordingReader(stream);
        RecordingReader.RecordingHeader header = reader.readHeader();
        RecordingData data = new RecordingData(header.recordingId(), header.playerUUID());
        for (AbstractAction action : reader.readAllActions(header.actionCount())) {
            data.addAction(action);
        }
        return data;
    }

    public Map<Integer, List<AbstractAction>> getActions() {
        return actions;
    }

    public UUID getRecordingId() {
        return recordingId;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }
}
