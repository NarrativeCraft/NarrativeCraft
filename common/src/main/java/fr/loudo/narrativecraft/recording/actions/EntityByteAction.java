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
import fr.loudo.narrativecraft.mixin.accessor.EntityAccessor;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.syncher.SynchedEntityData;

public class EntityByteAction extends AbstractAction {

    public static final String ID = "entity_byte";

    private byte entityByte;

    public EntityByteAction(int tick, byte entityByte) {
        super(tick);
        this.entityByte = entityByte;
    }

    public EntityByteAction(int tick) {
        super(tick);
    }

    @Override
    public boolean differs(AbstractAction other) {
        if (!(other instanceof EntityByteAction otherEntityByteAction)) {
            return false;
        }
        return this.entityByte != otherEntityByteAction.entityByte;
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.addByte(entityByte);
    }

    @Override
    public void read(Reader reader) throws IOException {
        entityByte = reader.readByte();
    }

    @Override
    public List<AbstractAction> createRewindSnapshot(IPlaybackContext context, IPlaybackSession session) {
        byte current = context.getEntity().getEntityData().get(EntityAccessor.getDATA_SHARED_FLAGS_ID());
        return List.of(new EntityByteAction(tick, current));
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public ActionResult execute(IPlaybackContext context, IPlaybackSession session) {
        SynchedEntityData entityData = context.getEntity().getEntityData();
        entityData.set(EntityAccessor.getDATA_SHARED_FLAGS_ID(), entityByte);

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
