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
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MovementAction extends AbstractAction {

    public static final String ID = "movement";

    private static final double MAX_INTERPOLATED_MOVEMENT_SQUARED = 64.0;

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
    public ActionResult execute(IPlaybackContext context, IPlaybackSession session) {
        Entity entity = context.getEntity();
        Vec3 movement = pos.subtract(entity.position());

        if (movement.lengthSqr() > MAX_INTERPOLATED_MOVEMENT_SQUARED) {
            entity.setDeltaMovement(Vec3.ZERO);
            entity.setPos(pos);
        } else {
            entity.setDeltaMovement(movement);
            entity.move(MoverType.SELF, movement);
            entity.setPos(pos);
        }

        entity.setXRot(pitch);
        entity.setYRot(yaw);
        entity.setYHeadRot(headYaw);
        entity.setOnGround(isOnGround(entity));
        for (ServerPlayer player : session.getTargetedPlayers()) {
            player.connection.send(new ClientboundRotateHeadPacket(entity, (byte) (headYaw * 256 / 360)));
        }

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

    public Vec3 getPosition() {
        return pos;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean shouldExecuteOnRewind() {
        return true;
    }

    private boolean isOnGround(Entity entity) {
        AABB box = entity.getBoundingBox().deflate(0.001).move(0, -0.05, 0);
        return !entity.level().noCollision(entity, box);
    }
}
