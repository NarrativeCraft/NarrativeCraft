package fr.loudo.narrativecraft.network.story;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record S2CNotifyClientPlayStory() implements CustomPacketPayload {

    public static final Type<S2CNotifyClientPlayStory> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "notify_client_play_story"));

    public static final StreamCodec<ByteBuf, S2CNotifyClientPlayStory> STREAM_CODEC = StreamCodec.unit(new S2CNotifyClientPlayStory());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
