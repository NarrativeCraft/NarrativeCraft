package fr.loudo.narrativecraft.network.cutscene;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record S2CCutscenePlayHeadUpdate(float ratio) implements CustomPacketPayload {

    public static final Type<S2CCutscenePlayHeadUpdate> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "play_head_update"));

    public static final StreamCodec<ByteBuf, S2CCutscenePlayHeadUpdate> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            S2CCutscenePlayHeadUpdate::ratio,
            S2CCutscenePlayHeadUpdate::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
