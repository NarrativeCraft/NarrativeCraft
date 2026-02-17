package fr.loudo.narrativecraft.platform.services;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public interface IPacketSender {

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
}