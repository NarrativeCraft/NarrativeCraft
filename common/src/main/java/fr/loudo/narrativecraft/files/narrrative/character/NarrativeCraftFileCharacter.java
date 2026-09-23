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
import com.google.gson.GsonBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.DeserializationResult;
import fr.loudo.narrativecraft.files.FileTransaction;
import fr.loudo.narrativecraft.files.NarrativeCraftFileDefault;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileUtil;
import fr.loudo.narrativecraft.files.NarrativeCraftFileWriter;
import fr.loudo.narrativecraft.narrative.OperationResult;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryDeserializer;
import fr.loudo.narrativecraft.narrative.character.CharacterStorySerializer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class NarrativeCraftFileCharacter extends NarrativeCraftFileDefault
        implements NarrativeCraftFileEditor<CharacterStory> {

    private static final Gson SERIALIZER = new GsonBuilder()
            .registerTypeAdapter(CharacterStory.class, new CharacterStorySerializer())
            .create();
    private static final Gson DESERIALIZER = new GsonBuilder()
            .registerTypeAdapter(CharacterStory.class, new CharacterStoryDeserializer())
            .create();

    @Override
    public OperationResult create(CharacterStory entry) {
        FileTransaction transaction = new FileTransaction();
        try {
            File characterFolder = getCharacterFolder(entry);
            if (characterFolder.exists()) {
                throw new IOException("Character directory " + characterFolder + " already exists");
            }
            transaction.createDirectories(characterFolder);
            writeData(transaction, entry);
            transaction.commit();
        } catch (IOException e) {
            transaction.rollback();
            NarrativeCraftMod.LOGGER.error("Failed to create character {}", entry.getName(), e);
            return NarrativeCraftFileEditor.storageFailure(entry);
        }
        return OperationResult.success();
    }

    @Override
    public OperationResult edit(CharacterStory existing, CharacterStory updated) {
        FileTransaction transaction = new FileTransaction();
        try {
            transaction.move(getCharacterFolder(existing), getCharacterFolder(updated));
            writeData(transaction, updated);
            transaction.commit();
        } catch (IOException e) {
            transaction.rollback();
            NarrativeCraftMod.LOGGER.error("Failed to edit character {}", existing.getName(), e);
            return NarrativeCraftFileEditor.storageFailure(existing);
        }
        return OperationResult.success();
    }

    @Override
    public OperationResult delete(CharacterStory entry) {
        FileTransaction transaction = new FileTransaction();
        try {
            File characterFolder = getCharacterFolder(entry);
            if (!characterFolder.exists()) {
                throw new IOException("Character directory " + characterFolder + " does not exist");
            }
            transaction.delete(characterFolder);
            transaction.commit();
        } catch (IOException e) {
            transaction.rollback();
            NarrativeCraftMod.LOGGER.error("Failed to delete character {}", entry.getName(), e);
            return NarrativeCraftFileEditor.storageFailure(entry);
        }
        return OperationResult.success();
    }

    private File getCharacterFolder(CharacterStory character) {
        return new File(NarrativeCraftFileUtil.getCharactersFolder(), character.toFileName());
    }

    private void writeData(FileTransaction transaction, CharacterStory character) throws IOException {
        transaction.write(
                new File(getCharacterFolder(character), DATA_FILE_NAME),
                writer -> SERIALIZER.toJson(character, writer));
    }

    @Override
    public List<DeserializationResult<CharacterStory>> deserialize() {
        List<DeserializationResult<CharacterStory>> deserializationResults = new ArrayList<>();

        File[] allContents = NarrativeCraftFileUtil.getCharactersFolder().listFiles();
        if (allContents == null) {
            return deserializationResults;
        }

        for (File file : allContents) {
            if (NarrativeCraftFileWriter.isTemporary(file)) continue;
            try {
                File dataFile = new File(file, DATA_FILE_NAME);
                String content = Files.readString(dataFile.toPath());
                CharacterStory character = DESERIALIZER.fromJson(content, CharacterStory.class);
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
