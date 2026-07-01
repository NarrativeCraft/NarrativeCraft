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

import com.google.common.collect.ImmutableBiMap;
import fr.loudo.narrativecraft.api.playback.IPlaybackContext;
import fr.loudo.narrativecraft.api.playback.IPlaybackSession;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.api.recording.action.ActionResult;
import java.io.IOException;
import java.util.List;
import net.minecraft.world.entity.Pose;

public class PoseAction extends AbstractAction {

    public static final String ID = "pose";

    private static final ImmutableBiMap<Pose, Integer> POSE_IDS = ImmutableBiMap.<Pose, Integer>builder()
            .put(Pose.STANDING, 1)
            .put(Pose.FALL_FLYING, 2)
            .put(Pose.SLEEPING, 3)
            .put(Pose.SWIMMING, 4)
            .put(Pose.SPIN_ATTACK, 5)
            .put(Pose.CROUCHING, 6)
            .put(Pose.DYING, 7)
            .put(Pose.LONG_JUMPING, 8)
            .put(Pose.CROAKING, 9)
            .put(Pose.USING_TONGUE, 10)
            .put(Pose.SITTING, 11)
            .put(Pose.ROARING, 12)
            .put(Pose.SNIFFING, 13)
            .put(Pose.EMERGING, 14)
            .put(Pose.DIGGING, 15)
            .build();

    private Pose pose;

    public PoseAction(int tick) {
        super(tick);
    }

    public PoseAction(int tick, Pose pose) {
        super(tick);
        this.pose = pose;
    }

    @Override
    public List<AbstractAction> createRewindSnapshot(IPlaybackContext context, IPlaybackSession session) {
        return List.of(new PoseAction(tick, context.getEntity().getPose()));
    }

    @Override
    public boolean differs(AbstractAction other) {
        return other instanceof PoseAction that && this.pose != that.pose;
    }

    @Override
    public void write(Writer writer) throws IOException {
        Integer id = POSE_IDS.get(pose);
        if (id == null) {
            throw new IOException("Unknown pose: " + pose);
        }
        writer.addInt(id);
    }

    @Override
    public void read(Reader reader) throws IOException {
        int id = reader.readInt();
        this.pose = POSE_IDS.inverse().get(id);
        if (this.pose == null) {
            throw new IOException("Unknown pose ID: " + id);
        }
    }

    @Override
    public ActionResult execute(IPlaybackContext context, IPlaybackSession session) {
        context.getEntity().setPose(pose);
        return ActionResult.OK;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean shouldExecuteOnRewind() {
        return true;
    }
}
