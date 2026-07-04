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

package fr.loudo.narrativecraft.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import fr.loudo.narrativecraft.mixin.accessor.PlayerAccessor;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.UUID;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stat;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

// FakePlayer class from Forge
public class FakePlayer extends ServerPlayer {
    public static final String CHARACTER_ID_PROPERTY = "nc_character_id";
    private static final ClientInformation DEFAULT_CLIENT_INFO = ClientInformation.createDefault();
    private final boolean isInvulnerable;

    public static GameProfile createCharacterProfile(ICharacterStory characterStory, String name) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        profile.getProperties()
                .put(
                        CHARACTER_ID_PROPERTY,
                        new Property(
                                CHARACTER_ID_PROPERTY, characterStory.getId().toString()));
        return profile;
    }

    public FakePlayer(ServerLevel level, GameProfile profile, boolean isInvulnerable) {
        super(level.getServer(), level, profile, DEFAULT_CLIENT_INFO);
        this.connection = new FakePlayerNetHandler(level.getServer(), this, profile);
        this.isInvulnerable = isInvulnerable;
        getEntityData().set(PlayerAccessor.getDATA_PLAYER_MODE_CUSTOMISATION(), (byte) 0b01111111);

        this.invulnerableTime = 0;
    }

    @Override
    public void awardStat(@NotNull Stat stat, int amount) {}

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        return (damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || !isInvulnerable)
                && super.hurt(damageSource, amount);
    }

    private static class FakePlayerNetHandler extends ServerGamePacketListenerImpl {
        private static final net.minecraft.network.Connection DUMMY_CONNECTION =
                new DummyConnection(PacketFlow.CLIENTBOUND);

        public FakePlayerNetHandler(MinecraftServer server, ServerPlayer player, GameProfile profile) {
            super(server, DUMMY_CONNECTION, player, new CommonListenerCookie(profile, 0, DEFAULT_CLIENT_INFO, false));
        }

        @Override
        public void tick() {}

        @Override
        public void resetPosition() {}

        @Override
        public void disconnect(Component message) {}

        @Override
        public void handlePlayerInput(ServerboundPlayerInputPacket packet) {}

        @Override
        public void handleMoveVehicle(ServerboundMoveVehiclePacket packet) {}

        @Override
        public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket packet) {}

        @Override
        public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket packet) {}

        @Override
        public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket packet) {}

        @Override
        public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket packet) {}

        @Override
        public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket packet) {}

        @Override
        public void handleSetCommandBlock(ServerboundSetCommandBlockPacket packet) {}

        @Override
        public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket packet) {}

        @Override
        public void handleRenameItem(ServerboundRenameItemPacket packet) {}

        @Override
        public void handleSetBeaconPacket(ServerboundSetBeaconPacket packet) {}

        @Override
        public void handleSetStructureBlock(ServerboundSetStructureBlockPacket packet) {}

        @Override
        public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket packet) {}

        @Override
        public void handleJigsawGenerate(ServerboundJigsawGeneratePacket packet) {}

        @Override
        public void handleSelectTrade(ServerboundSelectTradePacket packet) {}

        @Override
        public void handleEditBook(ServerboundEditBookPacket packet) {}

        @Override
        public void handleEntityTagQuery(ServerboundEntityTagQueryPacket packet) {}

        @Override
        public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQueryPacket packet) {}

        @Override
        public void handleMovePlayer(ServerboundMovePlayerPacket packet) {}

        @Override
        public void teleport(double x, double y, double z, float yaw, float pitch) {}

        @Override
        public void handlePlayerAction(ServerboundPlayerActionPacket packet) {}

        @Override
        public void handleUseItemOn(ServerboundUseItemOnPacket packet) {}

        @Override
        public void handleUseItem(ServerboundUseItemPacket packet) {}

        @Override
        public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket packet) {}

        @Override
        public void handlePaddleBoat(ServerboundPaddleBoatPacket packet) {}

        @Override
        public void send(Packet<?> packet) {}

        @Override
        public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packet) {}

        @Override
        public void handleChat(ServerboundChatPacket packet) {}

        @Override
        public void handleAnimate(ServerboundSwingPacket packet) {}

        @Override
        public void handlePlayerCommand(ServerboundPlayerCommandPacket packet) {}

        @Override
        public void handleInteract(ServerboundInteractPacket packet) {}

        @Override
        public void handleClientCommand(ServerboundClientCommandPacket packet) {}

        @Override
        public void handleContainerClose(ServerboundContainerClosePacket packet) {}

        @Override
        public void handleContainerClick(ServerboundContainerClickPacket packet) {}

        @Override
        public void handlePlaceRecipe(ServerboundPlaceRecipePacket packet) {}

        @Override
        public void handleContainerButtonClick(ServerboundContainerButtonClickPacket packet) {}

        @Override
        public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket packet) {}

        @Override
        public void handleSignUpdate(ServerboundSignUpdatePacket packet) {}

        @Override
        public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket packet) {}

        @Override
        public void handleChangeDifficulty(ServerboundChangeDifficultyPacket packet) {}

        @Override
        public void handleLockDifficulty(ServerboundLockDifficultyPacket packet) {}

        @Override
        public void ackBlockChangesUpTo(int sequence) {}

        @Override
        public void handleChatCommand(ServerboundChatCommandPacket packet) {}

        @Override
        public void handleChatAck(ServerboundChatAckPacket packet) {}

        @Override
        public void sendPlayerChatMessage(PlayerChatMessage message, ChatType.Bound boundChatType) {}

        @Override
        public void sendDisguisedChatMessage(Component content, ChatType.Bound boundChatType) {}

        @Override
        public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket packet) {}
    }

    private static class DummyConnection extends net.minecraft.network.Connection {
        public DummyConnection(PacketFlow packetFlow) {
            super(packetFlow);
            new EmbeddedChannel(this);
        }
    }
}
