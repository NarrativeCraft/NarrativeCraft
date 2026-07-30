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

package fr.loudo.narrativecraft.api.recording;

import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.Entity;

public interface IRecording {
    void addAction(AbstractAction action, Entity entity);

    void addAction(AbstractAction action, UUID entityId);

    /**
     * Marks the given entity as tracked (interacted with during recording).
     * Captures its current NBT state so it can be exactly recreated at playback.
     * The entity must already be in the scan list; if not found, returns -1.
     *
     * @return the recording ID of the entity, used in RideEntityAction.
     */
    int markEntityAsTracked(Entity entity);

    void start();

    void stop();

    IRecordingEntityData getRecordingEntityData(Entity entity);

    IRecordingEntityData getRecordingEntityData(UUID entityId);

    List<IRecordingEntityData> getRecordingEntities();

    int getTick();

    boolean isRecording();
}
