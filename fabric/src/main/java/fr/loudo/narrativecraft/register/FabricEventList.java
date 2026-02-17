package fr.loudo.narrativecraft.register;

import fr.loudo.narrativecraft.events.IFabricEventRegister;
import fr.loudo.narrativecraft.events.OnServerStartFabric;

import java.util.ArrayList;
import java.util.List;

public class FabricEventList {

    private final List<IFabricEventRegister> events = new ArrayList<>();

    public FabricEventList() {
        events.add(new OnServerStartFabric());
    }

    public void register() {
        for (IFabricEventRegister event : events) {
            event.register();
        }
    }

}
