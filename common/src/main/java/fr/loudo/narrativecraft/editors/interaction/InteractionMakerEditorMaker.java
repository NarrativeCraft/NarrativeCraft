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

package fr.loudo.narrativecraft.editors.interaction;

import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.interaction.Interaction;
import fr.loudo.narrativecraft.narrative.interaction.InteractionPoint;
import fr.loudo.narrativecraft.narrative.interaction.InteractionZone;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.network.BiStopEditorMaker;
import fr.loudo.narrativecraft.network.interaction.BiInteractionEnter;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.session.PlayerSession;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InteractionMakerEditorMaker implements EditorMaker {

    private final Interaction interaction;
    private final PlayerSession playerSession;
    private final NarrativeEnvironment environment;
    private final Set<UUID> insideZoneIds = new HashSet<>();

    public InteractionMakerEditorMaker(Interaction interaction, PlayerSession playerSession) {
        this.interaction = interaction;
        this.playerSession = playerSession;
        this.environment = NarrativeEnvironment.DEVELOPMENT;
    }

    public InteractionMakerEditorMaker(
            Interaction interaction, PlayerSession playerSession, NarrativeEnvironment environment) {
        this.interaction = interaction;
        this.playerSession = playerSession;
        this.environment = environment;
    }

    @Override
    public void init() {
        Services.PACKET.sendToPlayer(playerSession.getPlayer(), new BiInteractionEnter(interaction, environment));
        if (environment == NarrativeEnvironment.DEVELOPMENT) teleportToEditorOrigin();
    }

    @Override
    public void tick() {
        if (environment != NarrativeEnvironment.PRODUCTION) return;

        StoryHandler storyHandler = playerSession.getStoryHandler();
        if (storyHandler == null) return;

        for (InteractionZone zone : interaction.getZones()) {
            boolean isInside = new AABB(zone.getCorner1(), zone.getCorner2())
                    .contains(playerSession.getPlayer().position());
            boolean wasInside = insideZoneIds.contains(zone.getId());

            if (isInside && !wasInside) {
                if (!(zone.isOneTime() && storyHandler.hasAlreadyInteracted(zone.getId())) && playerSession.isGameplayMode()) {
                    storyHandler.playStitch(zone.getStitchName());
                    if (zone.isOneTime()) storyHandler.addInteractionId(zone.getId());
                }
                insideZoneIds.add(zone.getId());
            } else if (!isInside) {
                insideZoneIds.remove(zone.getId());
            }
        }
    }

    @Override
    public void teleportToEditorOrigin() {
        ServerPlayer player = playerSession.getPlayer();
        Vec3 position = resolveOrigin();
        if (position == null) return;
        player.connection.teleport(position.x, position.y, position.z, player.getYRot(), player.getXRot());
    }

    @Override
    public NarrativeEnvironment getEnvironment() {
        return environment;
    }

    private Vec3 resolveOrigin() {
        if (!interaction.getZones().isEmpty()) {
            return interaction.getZones().get(0).center();
        }
        if (!interaction.getPoints().isEmpty()) {
            return interaction.getPoints().get(0).getPosition();
        }
        return null;
    }

    public void stop() {
        Services.PACKET.sendToPlayer(playerSession.getPlayer(), BiStopEditorMaker.INSTANCE);
    }

    public Interaction getInteraction() {
        return interaction;
    }

    public PlayerSession getPlayerSession() {
        return playerSession;
    }

    public void addZone(InteractionZone zone) {
        interaction.getZones().add(zone);
    }

    public void removeZone(java.util.UUID zoneId) {
        interaction.getZones().removeIf(zone -> zone.getId().equals(zoneId));
    }

    public void addPoint(InteractionPoint point) {
        interaction.getPoints().add(point);
    }

    public void removePoint(java.util.UUID pointId) {
        interaction.getPoints().removeIf(point -> point.getId().equals(pointId));
    }
}
