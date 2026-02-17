package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;

public class OnServerStart {

    public static void serverStart(MinecraftServer server) {
        File rootDirectory = server.getWorldPath(LevelResource.ROOT).toFile();
        NarrativeCraftMod.getInstance().getFile().getInit().init(rootDirectory);
    }

}
