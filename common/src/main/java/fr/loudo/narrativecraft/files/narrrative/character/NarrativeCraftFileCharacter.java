/*
 * NarrativeCraft - Create narrative games inside Minecraft. No coding, no game engine, only text and logic.
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

package fr.loudo.narrativecraft.files.narrrative.character;

import com.google.gson.Gson;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.DeserializationResult;
import fr.loudo.narrativecraft.files.NarrativeCraftFileDefault;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileUtil;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryDeserializer;
import fr.loudo.narrativecraft.narrative.character.CharacterStorySerializer;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class NarrativeCraftFileCharacter extends NarrativeCraftFileDefault
        implements NarrativeCraftFileEditor<CharacterStory> {

    @Override
    public int create(CharacterStory entry) {
        File characterDirectory = createDirectory(NarrativeCraftFileUtil.getCharactersFolder(), entry.toFileName());
        if (characterDirectory == null) {
            NarrativeCraftMod.LOGGER.error("Failed to create character directory {}", entry.getName());
            return OPERATION_FAILED;
        }

        return edit(entry);
    }

    @Override
    public int edit(CharacterStory entry) {
        File workingFolder = NarrativeCraftFileUtil.getCharactersFolder();
        CharacterManager characterManager = NarrativeCraftMod.getInstance().getCharacterManager();
        CharacterStory oldCharacter = characterManager.getById(entry.getId());

        try {
            if (oldCharacter != null && !oldCharacter.toFileName().equals(entry.toFileName())) {
                File oldDirectory = new File(workingFolder, oldCharacter.toFileName());
                File newDirectory = new File(workingFolder, entry.toFileName());
                Files.move(oldDirectory.toPath(), newDirectory.toPath());
            }

            File characterDirectory = new File(workingFolder, entry.toFileName());
            if (!characterDirectory.exists()) {
                NarrativeCraftMod.LOGGER.error("Failed to find character directory {}", entry.getName());
                return OPERATION_FAILED;
            }

            File dataFile = createFile(characterDirectory, DATA_FILE_NAME);

            Gson gson = gsonBuilder
                    .registerTypeAdapter(CharacterStory.class, new CharacterStorySerializer())
                    .create();
            try (Writer writer = new BufferedWriter(new FileWriter(dataFile))) {
                gson.toJson(entry, writer);
            }

        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("Failed to write character data {}", entry.formattedName(), e);
            return OPERATION_FAILED;
        }

        return OPERATION_SUCCESS;
    }

    @Override
    public int delete(CharacterStory entry) {
        File characterDirectory = new File(NarrativeCraftFileUtil.getCharactersFolder(), entry.toFileName());
        if (!characterDirectory.exists()) {
            NarrativeCraftMod.LOGGER.error(
                    "Failed to delete character {} because directory {} doesn't exist",
                    entry.getName(),
                    entry.toFileName());
            return OPERATION_FAILED;
        }

        if (!deleteDirectory(characterDirectory)) {
            NarrativeCraftMod.LOGGER.error("Failed to delete character directory {}", entry.getName());
            return OPERATION_FAILED;
        }

        return OPERATION_SUCCESS;
    }

    @Override
    public List<DeserializationResult<CharacterStory>> deserialize() {
        List<DeserializationResult<CharacterStory>> deserializationResults = new ArrayList<>();

        File charactersFolder = NarrativeCraftFileUtil.getCharactersFolder();
        File[] allContents = charactersFolder.listFiles();
        if (allContents == null) {
            return deserializationResults;
        }

        gsonBuilder.registerTypeAdapter(CharacterStory.class, new CharacterStoryDeserializer());
        Gson gson = gsonBuilder.create();

        for (File file : allContents) {
            try {
                File dataFile = new File(file, DATA_FILE_NAME);
                String content = Files.readString(dataFile.toPath());
                CharacterStory character = gson.fromJson(content, CharacterStory.class);
                if (character == null) {
                    throw new Exception(String.format("Character %s deserialization returned null", file.getName()));
                }
                deserializationResults.add(new DeserializationResult<>(character, false, file.getName()));
            } catch (Exception e) {
                NarrativeCraftMod.LOGGER.error("Failed to init character {}", file.getName(), e);
                deserializationResults.add(new DeserializationResult<>(null, true, file.getName()));
            }
        }

        return deserializationResults;
    }
}
