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
import fr.loudo.narrativecraft.files.narrrative.AbstractNarrativeCraftFileSceneJsonEntry;
import fr.loudo.narrativecraft.narrative.npc.Npc;
import fr.loudo.narrativecraft.narrative.npc.NpcDeserializer;
import fr.loudo.narrativecraft.narrative.npc.NpcSerializer;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class NarrativeCraftFileNpc extends AbstractNarrativeCraftFileSceneJsonEntry<Npc> {

    @Override
    protected String getSubFolderName() {
        return NPC_FOLDER_NAME;
    }

    @Override
    protected boolean entryHasOwnFolder() {
        return true;
    }

    @Override
    protected Scene getScene(Npc entry) {
        return entry.getScene();
    }

    @Override
    protected Npc getOldEntry(Npc entry) {
        return entry.getScene().getNpcManager().getById(entry.getId());
    }

    @Override
    protected void registerDeserializer(GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeAdapter(Npc.class, new NpcDeserializer());
    }

    @Override
    protected Npc deserializeEntry(Gson gson, String content) throws Exception {
        return gson.fromJson(content, Npc.class);
    }

    @Override
    protected int writeJson(Npc entry, File file) {
        Gson gson =
                gsonBuilder.registerTypeAdapter(Npc.class, new NpcSerializer()).create();
        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            gson.toJson(entry, writer);
            return OPERATION_SUCCESS;
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to write npc data {}", entry.getName(), e);
            return OPERATION_FAILED;
        }
    }
}
