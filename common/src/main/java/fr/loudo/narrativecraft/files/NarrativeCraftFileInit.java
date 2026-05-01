/*
 * NarrativeCraft - Create your own stories, easily, and freely in Minecraft.
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

package fr.loudo.narrativecraft.files;

import java.io.File;

public class NarrativeCraftFileInit extends NarrativeCraftFileDefault {

    public static final String BUILD_DIRECTORY_NAME = "build";
    public static final String CHAPTERS_DIRECTORY_NAME = "chapters";
    public static final String CHARACTERS_DIRECTORY_NAME = "characters";
    public static final String SAVES_DIRECTORY_NAME = "saves";
    public static final String MAIN_INK_NAME = "main" + EXTENSION_SCRIPT_FILE;
    public static final String VARS_INK_NAME = "variables" + EXTENSION_SCRIPT_FILE;
    public static final String FUNCTIONS_INK_NAME = "functions" + EXTENSION_SCRIPT_FILE;

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
