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

package fr.loudo.narrativecraft.events.server;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.recording.RecordingEntityData;
import fr.loudo.narrativecraft.recording.actions.DeathAction;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.signals.SignalCharacterDeath;
import fr.loudo.narrativecraft.signals.SignalPlayerDeath;
import fr.loudo.narrativecraft.signals.SignalPlayerKillEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class OnDeathEvent {

    public static void onDeath(LivingEntity entity, DamageSource damageSource) {
        handleRecording(entity);
        handleKillSignal(entity, damageSource);
        handleDeathSignal(entity, damageSource);
        handleCharacterDeathSignal(entity);
    }

    private static void handleCharacterDeathSignal(LivingEntity entity) {
        List<PlayerSession> playerSessions = new ArrayList<>(
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getList());

        for (PlayerSession playerSession : playerSessions) {
            StoryHandler storyHandler = playerSession.getStoryHandler();
            if (storyHandler == null) continue;

            for (Map.Entry<String, Entity> characterEntity :
                    storyHandler.getCharacterEntities().entrySet()) {
                if (characterEntity.getValue() != entity) continue;

                NarrativeCraftMod.getInstance()
                        .getSignalEmitter()
                        .emit(new SignalCharacterDeath(characterEntity.getKey()), playerSession.getPlayer());
                return;
            }
        }
    }

    private static void handleKillSignal(LivingEntity entity, DamageSource damageSource) {
        if (!(damageSource.getEntity() instanceof ServerPlayer player)) return;

        NarrativeCraftMod.getInstance().getSignalEmitter().emit(new SignalPlayerKillEntity(entity), player);
    }

    private static void handleDeathSignal(LivingEntity entity, DamageSource damageSource) {
        if (!(entity instanceof ServerPlayer player)) return;

        NarrativeCraftMod.getInstance().getSignalEmitter().emit(new SignalPlayerDeath(player, damageSource), player);
    }

    private static void handleRecording(LivingEntity entity) {
        RecordingEntityData data =
                NarrativeCraftMod.getInstance().getRecordingManager().getRecordingEntityData(entity);
        if (data == null) return;

        data.markAsTracked();
        data.addAction(new DeathAction(data.getRecordingTick()));
    }
}
