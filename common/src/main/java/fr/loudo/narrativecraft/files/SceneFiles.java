/*
 * NarrativeCraft - Create narrative games inside Minecraft. No coding, no game engine, only text and logic.
 * Copyright (c) 2026 LOUDO and contributors
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

import com.google.gson.*;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.files.FileData;
import fr.loudo.narrativecraft.api.files.FileDataIO;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.scene.SceneIO;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;

public class SceneFiles implements FileData<Scene> {

    public static final String FORMAT = "nc_scene";

    private final File narrativeFolder;

    public SceneFiles(File narrativeFolder) {
        this.narrativeFolder = narrativeFolder;
    }

    public boolean save(Scene scene, @Nullable Path path) {
        Gson gson = new Gson();
        File file = sceneFile(scene, path);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            gson.toJson(io().write(scene), writer);
        } catch (JsonIOException | IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to save scene {}", path, e);
            return false;
        }
        return true;
    }

    public Scene restore(Path path) {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            return io().read(json);
        } catch (IOException | JsonParseException | IllegalStateException e) {
            NarrativeCraftMod.LOGGER.error("Failed to read scene {}", path, e);
            return null;
        }
    }

    private File sceneFile(String sceneName, @Nullable Path path) {
        String fileName = String.format("%s.%s.%s", sceneName, FORMAT, NarrativeFiles.DEFAULT_FORMAT);
        String relative = path == null ? fileName : path.resolve(fileName).toString();
        return FileHelper.createFile(narrativeFolder, relative);
    }

    private File sceneFile(Scene scene, @Nullable Path path) {
        return sceneFile(scene.getName(), path);
    }

    @Override
    public FileDataIO<Scene> io() {
        return new SceneIO();
    }
}
