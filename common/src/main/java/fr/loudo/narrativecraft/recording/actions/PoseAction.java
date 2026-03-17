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
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.api.recording.action.ActionResult;
import java.io.IOException;
import net.minecraft.world.entity.Pose;

public class PoseAction extends AbstractAction {

    public static final String ID = "pose";

    private Pose pose;

    public PoseAction(int tick) {
        super(tick);
    }

    public PoseAction(int tick, Pose pose) {
        super(tick);
        this.pose = pose;
    }

    @Override
    public boolean differs(AbstractAction other) {
        if (!(other instanceof PoseAction otherPose)) {
            return false;
        }
        return this.pose != otherPose.pose;
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.addInt(pose.id());
    }

    @Override
    public void read(Reader reader) throws IOException {
        pose = Pose.BY_ID.apply(reader.readInt());
    }

    @Override
    public ActionResult execute(IPlaybackContext context) {
        if (pose == null) {
            return ActionResult.ERROR;
        }

        context.getEntity().setPose(pose);

        return ActionResult.OK;
    }

    @Override
    public String getId() {
        return ID;
    }
}
