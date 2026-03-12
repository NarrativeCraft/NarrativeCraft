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

package fr.loudo.narrativecraft.recording;

import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileRegistry;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.recording.actions.MovementAction;
import fr.loudo.narrativecraft.session.PlayerSession;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public class Recording {

    public static final String RECORDING_EXTENSION = ".ncr";

    private final UUID id = UUID.randomUUID();
    private final PlayerSession playerSession;
    private final RecordingData recordingData;
    private boolean isRecording = false;
    private int tick = 0;

    public Recording(PlayerSession playerSession) {
        this.playerSession = playerSession;
        recordingData = new RecordingData(this);
    }

    public void tick() {
        if (!isRecording) return;
        ServerPlayer player = playerSession.getPlayer();
        recordingData.addAction(
                new MovementAction(tick, player.position(), player.getXRot(), player.getYRot(), player.getYHeadRot()));

        tick++;
    }

    public void start() {
        isRecording = true;
    }

    public void stop() {
        isRecording = false;
    }

    public boolean save(String name) {

        Animation animation = new Animation(recordingData.getRecordingId(), name, playerSession.getScene());

        if (NarrativeCraftFileRegistry.getInstance().create(animation) == NarrativeCraftFileEditor.OPERATION_FAILED) {
            return false;
        }

        playerSession.getScene().getAnimationManager().add(animation);
        Services.PACKET.sendToPlayer(
                getPlayer(), BiSyncNarrativeEntryPacket.add(animation.getId(), animation.toPayload()));

        return true;
    }

    public UUID getId() {
        return id;
    }

    public ServerPlayer getPlayer() {
        return playerSession.getPlayer();
    }

    public PlayerSession getPlayerSession() {
        return playerSession;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    public int getTick() {
        return tick;
    }

    public RecordingData getRecordingData() {
        return recordingData;
    }
}
