package fr.loudo.narrativecraft.file;

import java.io.File;

public class NarrativeCraftFileInit extends NarrativeCraftFileDefault {

    private static final String BUILD_DIRECTORY_NAME = "build";
    private static final String CHAPTERS_DIRECTORY_NAME = "chapters";
    private static final String CHARACTERS_DIRECTORY_NAME = "characters";
    private static final String SAVES_DIRECTORY_NAME = "saves";
    private static final String MAIN_INK_NAME = "main" + EXTENSION_SCRIPT_FILE;
    private static final String VARS_INK_NAME = "variables" + EXTENSION_SCRIPT_FILE;
    private static final String FUNCTIONS_INK_NAME = "functions" + EXTENSION_SCRIPT_FILE;

    private File rootDirectory;
    private File mainDirectory;
    private File buildDirectory;
    private File chaptersDirectory;
    private File charactersDirectory;
    private File savesDirectory;
    private File mainInk;
    private File variablesInk;
    private File functionsInk;

    public void init(File rootDirectory) {
        this.rootDirectory = rootDirectory;

        mainDirectory = createDirectory(rootDirectory, DIRECTORY_NAME);
        buildDirectory = createDirectory(mainDirectory, BUILD_DIRECTORY_NAME);
        chaptersDirectory = createDirectory(mainDirectory, CHAPTERS_DIRECTORY_NAME);
        charactersDirectory = createDirectory(mainDirectory, CHARACTERS_DIRECTORY_NAME);
        savesDirectory = createDirectory(mainDirectory, SAVES_DIRECTORY_NAME);
        mainInk = createFile(mainDirectory, MAIN_INK_NAME);
        variablesInk = createFile(mainDirectory, VARS_INK_NAME);
        functionsInk = createFile(mainDirectory, FUNCTIONS_INK_NAME);
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public File getMainDirectory() {
        return mainDirectory;
    }

    public File getBuildDirectory() {
        return buildDirectory;
    }

    public File getChaptersDirectory() {
        return chaptersDirectory;
    }

    public File getCharactersDirectory() {
        return charactersDirectory;
    }

    public File getSavesDirectory() {
        return savesDirectory;
    }

    public File getMainInk() {
        return mainInk;
    }

    public File getVariablesInk() {
        return variablesInk;
    }

    public File getFunctionsInk() {
        return functionsInk;
    }
}
