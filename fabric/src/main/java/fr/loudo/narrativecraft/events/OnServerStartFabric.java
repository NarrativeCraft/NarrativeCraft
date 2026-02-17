package fr.loudo.narrativecraft.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class OnServerStartFabric implements IFabricEventRegister {

    public void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(OnServerStart::serverStart);
    }

}
