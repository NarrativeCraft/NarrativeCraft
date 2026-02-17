package fr.loudo.narrativecraft.file;

import fr.loudo.narrativecraft.NarrativeCraftMod;

import java.io.File;
import java.io.IOException;

public class NarrativeCraftFileDefault {

    protected static final String EXTENSION_SCRIPT_FILE = ".ink";
    public static final String EXTENSION_DATA_FILE = ".json";

    protected final String DIRECTORY_NAME = NarrativeCraftMod.MOD_ID;

    protected File createDirectory(File parent, String name) {
        File directory = new File(parent, name);
        if (!directory.exists()) {
            if (!directory.mkdir()) NarrativeCraftMod.LOGGER.error("Couldn't create directory {}!", name);
        }
        return directory;
    }

    protected File createFile(File parent, String name) {
        File file = new File(parent, name);
        if (!file.exists()) {
            try {
                if (!file.createNewFile())
                    NarrativeCraftMod.LOGGER.error("Couldn't create file {}!", file.getAbsolutePath());
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.error("Couldn't create file {}! Cause: {}", file.getAbsolutePath(), e);
            }
        }
        return file;
    }

    protected void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }

}
