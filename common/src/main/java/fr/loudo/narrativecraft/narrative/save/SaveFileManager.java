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

package fr.loudo.narrativecraft.narrative.save;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFileDefault;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.StoryHandlerDeserializer;
import fr.loudo.narrativecraft.narrative.story.StoryHandlerSerializer;
import fr.loudo.narrativecraft.session.PlayerSession;
import java.io.*;
import java.nio.file.Files;
import net.minecraft.server.level.ServerPlayer;

public class SaveFileManager {

    public void writeSave(PlayerSession session, boolean includeLastPosition) {
        StoryHandler storyHandler = session.getStoryHandler();
        if (storyHandler == null) return;

        File saveFile = getSaveFile(session);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(StoryHandler.class, new StoryHandlerSerializer(includeLastPosition))
                .create();
        try (Writer writer = new BufferedWriter(new FileWriter(saveFile))) {
            gson.toJson(storyHandler, writer);
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error(
                    "Failed to save story state of player {}",
                    session.getPlayer().getName());
            throw new RuntimeException(e);
        }
    }

    public StoryHandler loadSave(PlayerSession session) {
        File saveFile = getSaveFile(session);
        if (!saveFile.exists()) return null;

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(StoryHandler.class, new StoryHandlerDeserializer(session))
                .create();
        try {
            String saveData = Files.readString(saveFile.toPath());
            return gson.fromJson(saveData, StoryHandler.class);
        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error(
                    "Failed to load story state of player {}",
                    session.getPlayer().getName());
            return null;
        }
    }

    public File getSaveFile(ServerPlayer player) {
        String name = player.getUUID().toString();
        boolean isCrackedServer = !player.level().getServer().usesAuthentication();
        if (isCrackedServer) {
            name = player.getName().getString().toLowerCase().replace(" ", "_");
        }

        return new File(savesDirectory(), name + NarrativeCraftFileDefault.EXTENSION_DATA_FILE);
    }

    public void removeSaveFile(ServerPlayer player) {
        File saveFile = getSaveFile(player);
        if (!saveFile.exists()) return;

        saveFile.delete();
    }

    public File getSaveFile(PlayerSession session) {
        return getSaveFile(session.getPlayer());
    }

    public File savesDirectory() {
        return NarrativeCraftMod.getInstance().getFile().getInit().getSavesDirectory();
    }
}
