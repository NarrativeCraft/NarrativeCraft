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

package fr.loudo.narrativecraft.recording.actions;

import fr.loudo.narrativecraft.api.playback.IPlaybackContext;
import fr.loudo.narrativecraft.api.playback.IPlaybackSession;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.api.recording.action.ActionResult;
import java.io.IOException;
import java.util.List;
import net.minecraft.world.entity.Entity;

public class RideEntityAction extends AbstractAction {

    public static final String ID = "ride_entity";

    private int nearbyEntityLocalId;

    public RideEntityAction(int tick, int nearbyEntityLocalId) {
        super(tick);
        this.nearbyEntityLocalId = nearbyEntityLocalId;
    }

    public RideEntityAction(int tick) {
        super(tick);
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.addInt(nearbyEntityLocalId);
    }

    @Override
    public void read(Reader reader) throws IOException {
        nearbyEntityLocalId = reader.readInt();
    }

    @Override
    public List<AbstractAction> createRewindSnapshot(IPlaybackContext context, IPlaybackSession session) {
        return List.of(new StopRideEntityAction(tick));
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ActionResult execute(IPlaybackContext context, IPlaybackSession session) {
        Entity vehicle = session.getEntityByRecordingId(nearbyEntityLocalId);
        if (vehicle == null) return ActionResult.IGNORED;
        // Protection to prevent stack overflow if it tries to ride the entity itself
        if (vehicle.getUUID().equals(context.getEntity().getUUID())) return ActionResult.IGNORED;

        context.getEntity().startRiding(vehicle, true, true);
        return ActionResult.OK;
    }
}
