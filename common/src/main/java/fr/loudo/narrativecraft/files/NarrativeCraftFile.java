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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.widgets.DialogFieldSet;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.dialog.DialogDataIO;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngle;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngleDeserializer;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngleSerializer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class NarrativeCraftFile {

    private final NarrativeCraftFileInit init = new NarrativeCraftFileInit();

    public NarrativeCraftFileInit getInit() {
        return init;
    }

    public CameraAngle getMainScreenData() {
        File dataFolder = init.getDataDirectory();
        File mainScreenDataFile = new File(dataFolder, NarrativeCraftFileInit.MAIN_SCREEN_DATA_NAME);
        if (mainScreenDataFile.exists()) {
            try {
                String data = Files.readString(mainScreenDataFile.toPath());
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(CameraAngle.class, new CameraAngleDeserializer())
                        .create();
                return gson.fromJson(data, CameraAngle.class);
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.error("Failed to init main screen data!", e);
            }
        }
        return new CameraAngle("Main Screen", "Some super secret data...", null);
    }

    public void saveMainScreenData(CameraAngle mainScreenData) throws IOException {
        File dataFolder = init.getDataDirectory();
        File mainScreenDataFile = new File(dataFolder, NarrativeCraftFileInit.MAIN_SCREEN_DATA_NAME);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CameraAngle.class, new CameraAngleSerializer())
                .create();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(mainScreenDataFile))) {
            gson.toJson(mainScreenData, writer);
        }
    }

    public DialogData getGlobalDialogData() {
        File dataFolder = init.getDataDirectory();
        File globalDialogFile = new File(dataFolder, NarrativeCraftFileInit.GLOBAL_DIALOG_DATA_NAME);
        if (globalDialogFile.exists()) {
            try {
                String content = Files.readString(globalDialogFile.toPath());
                JsonObject json = JsonParser.parseString(content).getAsJsonObject();
                return DialogDataIO.deserialize(json, DialogFieldSet.ALL);
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.error("Failed to load global dialog data!", e);
            }
        }
        return new DialogData();
    }

    public void saveGlobalDialogData(DialogData data) throws IOException {
        File dataFolder = init.getDataDirectory();
        File globalDialogFile = new File(dataFolder, NarrativeCraftFileInit.GLOBAL_DIALOG_DATA_NAME);
        JsonObject json = DialogDataIO.serialize(data, DialogFieldSet.ALL);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(globalDialogFile))) {
            new Gson().toJson(json, writer);
        }
    }
}
