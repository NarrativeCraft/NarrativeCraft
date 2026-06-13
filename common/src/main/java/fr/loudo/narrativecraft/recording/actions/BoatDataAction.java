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
import fr.loudo.narrativecraft.mixin.accessor.BoatAccessor;
import java.io.IOException;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.vehicle.Boat;

public class BoatDataAction extends AbstractAction {

    public static final String ID = "boat_data";

    private boolean movingLeftPaddle;
    private boolean movingRightPaddle;
    private int bubbleTime;

    public BoatDataAction(int tick, Boat boat) {
        super(tick);
        this.movingLeftPaddle = boat.getEntityData().get(BoatAccessor.getDATA_ID_PADDLE_LEFT());
        this.movingRightPaddle = boat.getEntityData().get(BoatAccessor.getDATA_ID_PADDLE_RIGHT());
        this.bubbleTime = boat.getEntityData().get(BoatAccessor.getDATA_ID_BUBBLE_TIME());
    }

    public BoatDataAction(int tick) {
        super(tick);
    }

    @Override
    public boolean differs(AbstractAction other) {
        if (!(other instanceof BoatDataAction that)) return false;
        return this.movingLeftPaddle != that.movingLeftPaddle
                || this.movingRightPaddle != that.movingRightPaddle
                || this.bubbleTime != that.bubbleTime;
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.addBoolean(movingLeftPaddle);
        writer.addBoolean(movingRightPaddle);
        writer.addInt(bubbleTime);
    }

    @Override
    public void read(Reader reader) throws IOException {
        movingLeftPaddle = reader.readBoolean();
        movingRightPaddle = reader.readBoolean();
        bubbleTime = reader.readInt();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ActionResult execute(IPlaybackContext context, IPlaybackSession session) {
        if (!(context.getEntity() instanceof Boat boat)) return ActionResult.IGNORED;

        SynchedEntityData entityData = boat.getEntityData();
        entityData.set(BoatAccessor.getDATA_ID_PADDLE_LEFT(), movingLeftPaddle);
        entityData.set(BoatAccessor.getDATA_ID_PADDLE_RIGHT(), movingRightPaddle);
        entityData.set(BoatAccessor.getDATA_ID_BUBBLE_TIME(), bubbleTime);

        return ActionResult.OK;
    }

    @Override
    public boolean shouldExecuteOnRewind() {
        return true;
    }
}
