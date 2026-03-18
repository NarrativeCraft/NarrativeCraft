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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import fr.loudo.narrativecraft.api.playback.IPlaybackContext;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.api.recording.action.ActionResult;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.entity.Pose;

public class PoseAction extends AbstractAction {

    public static final String ID = "pose";

    private static final BiMap<Integer, Pose> poseMap;
    private static final BiMap<Pose, Integer> poseIdMap;
    static
    {
        EnumMap<Pose, Integer> enumMap = new EnumMap<>(Pose.class);
        enumMap.put(Pose.STANDING, 1);
        enumMap.put(Pose.FALL_FLYING, 2);
        enumMap.put(Pose.SLEEPING, 3);
        enumMap.put(Pose.SWIMMING, 4);
        enumMap.put(Pose.SPIN_ATTACK, 5);
        enumMap.put(Pose.CROUCHING, 6);
        enumMap.put(Pose.DYING, 7);
        enumMap.put(Pose.LONG_JUMPING, 8);
        enumMap.put(Pose.CROAKING, 9);
        enumMap.put(Pose.USING_TONGUE, 10);
        enumMap.put(Pose.SITTING, 11);
        enumMap.put(Pose.ROARING, 12);
        enumMap.put(Pose.SNIFFING, 13);
        enumMap.put(Pose.EMERGING, 14);
        enumMap.put(Pose.DIGGING, 15);
        enumMap.put(Pose.SLIDING, 16);
        enumMap.put(Pose.SHOOTING, 17);
        enumMap.put(Pose.INHALING, 18);

        poseIdMap = HashBiMap.create(enumMap);
        poseMap = poseIdMap.inverse();
    }


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
        writer.addInt(poseIdMap.get(pose));
    }

    @Override
    public void read(Reader reader) throws IOException {
        pose = poseMap.get(reader.readInt());
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
