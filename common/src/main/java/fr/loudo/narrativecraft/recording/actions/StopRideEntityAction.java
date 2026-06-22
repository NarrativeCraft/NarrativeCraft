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

package fr.loudo.narrativecraft.recording.actions;

import fr.loudo.narrativecraft.api.playback.IPlaybackContext;
import fr.loudo.narrativecraft.api.playback.IPlaybackSession;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.api.recording.action.ActionResult;
import java.io.IOException;
import java.util.List;

public class StopRideEntityAction extends AbstractAction {

    public static final String ID = "stop_ride_entity";

    private int entityRecordingId;

    public StopRideEntityAction(int tick, int entityRecordingId) {
        super(tick);
        this.entityRecordingId = entityRecordingId;
    }

    public StopRideEntityAction(int tick) {
        super(tick);
    }

    @Override
    public List<AbstractAction> createRewindSnapshot(IPlaybackContext context, IPlaybackSession session) {
        int recordingIdPlayback = context.getRecordingId();
        if (recordingIdPlayback == entityRecordingId) return List.of();
        return List.of(new RideEntityAction(tick, entityRecordingId));
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.addInt(entityRecordingId);
    }

    @Override
    public void read(Reader reader) throws IOException {
        entityRecordingId = reader.readInt();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ActionResult execute(IPlaybackContext context, IPlaybackSession session) {
        context.getEntity().stopRiding();
        return ActionResult.OK;
    }

    @Override
    public boolean shouldExecuteOnRewind() {
        return false;
    }
}
