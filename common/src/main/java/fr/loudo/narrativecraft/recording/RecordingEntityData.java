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

import fr.loudo.narrativecraft.api.NarrativeCraftAPI;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.api.recording.action.IActionRegistry;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.Entity;

public class RecordingEntityData {

    private final IActionRegistry registry = NarrativeCraftAPI.getInstance().getRegistry();
    private final RecordingData recordingData;
    private final int recordingId;
    private final Entity entity;
    private boolean isTracked;
    private final Map<String, AbstractAction> lastActions = new HashMap<>();

    public RecordingEntityData(int recordingId, Entity entity, boolean isTracked) {
        recordingData = new RecordingData(entity);
        this.recordingId = recordingId;
        this.entity = entity;
        this.isTracked = isTracked;
    }

    public void addAction(AbstractAction action) {
        AbstractAction lastAction = lastActions.get(action.getId());
        if (lastAction == null || action.differs(lastAction)) {
            recordingData.addAction(action);
            lastActions.put(action.getId(), action);
        }
    }

    public void seedLastAction(String id, AbstractAction action) {
        lastActions.put(id, action);
    }

    public RecordingData getRecordingData() {
        return recordingData;
    }

    public int getRecordingId() {
        return recordingId;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean isTracked() {
        return isTracked;
    }

    public void setTracked(boolean tracked) {
        isTracked = tracked;
    }
}
