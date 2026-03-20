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
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.server.level.ServerPlayer;

public class DestroyBlockStageAction extends AbstractAction {

    public static final String ID = "destroy_block_stage";

    private BlockPos blockPos;
    private int progress; // 0-9

    public DestroyBlockStageAction(int tick, BlockPos blockPos, int progress) {
        super(tick);
        this.blockPos = blockPos;
        this.progress = progress;
    }

    public DestroyBlockStageAction(int tick) {
        super(tick);
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.addBlockPos(blockPos);
        writer.addByte((byte) progress);
    }

    @Override
    public void read(Reader reader) throws IOException {
        blockPos = reader.readBlockPos();
        progress = reader.readByte() & 0xFF;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean differs(AbstractAction other) {
        return other instanceof DestroyBlockStageAction that
                && (!this.blockPos.equals(that.blockPos) || this.progress != that.progress);
    }

    @Override
    public ActionResult execute(IPlaybackContext context) {
        int entityId = context.getEntity().getId();
        ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(entityId, blockPos, progress);

        if (context.forSpecificPlayers()) {
            for (ServerPlayer player : context.getTargetedPlayers()) {
                player.connection.send(packet);
            }
        } else {
            for (ServerPlayer player : context.getLevel().players()) {
                player.connection.send(packet);
            }
        }

        return ActionResult.OK;
    }
}
