package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod(NarrativeCraftMod.MOD_ID)
public class OnServerStartNeoForge {

    public OnServerStartNeoForge(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(OnServerStartNeoForge::onServerStart);
    }

    private static void onServerStart(ServerStartedEvent event) {
        OnServerStart.serverStart(event.getServer());
    }

}
