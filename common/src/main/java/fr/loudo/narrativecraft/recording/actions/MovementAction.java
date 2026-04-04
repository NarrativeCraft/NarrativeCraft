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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class MovementAction extends AbstractAction {

    public static final String ID = "movement";

    private Vec3 pos;
    private float pitch, yaw, headYaw;

    public MovementAction(int tick, Vec3 vec3, float pitch, float yaw, float headYaw) {
        super(tick);
        this.pos = vec3;
        this.pitch = pitch;
        this.yaw = yaw;
        this.headYaw = headYaw;
    }

    public MovementAction(int tick) {
        super(tick);
    }

    @Override
    public ActionResult execute(IPlaybackContext context) {
        Entity entity = context.getEntity();
        entity.setPos(pos);
        entity.setXRot(pitch);
        entity.setYRot(yaw);
        entity.setYHeadRot(headYaw);

        return ActionResult.OK;
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.addVec3(pos);
        writer.addFloat(pitch);
        writer.addFloat(yaw);
        writer.addFloat(headYaw);
    }

    @Override
    public void read(Reader reader) throws IOException {
        pos = reader.readVec3();
        pitch = reader.readFloat();
        yaw = reader.readFloat();
        headYaw = reader.readFloat();
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
