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

package fr.loudo.narrativecraft.dialog;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;

public class DialogPresetManager {

    private final Map<String, DialogData> presets = new HashMap<>();

    public void registerPreset(String name, DialogData data) {
        presets.put(name.toLowerCase(), data);
    }

    public void loadFromJson(JsonObject root) {
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String name = entry.getKey().toLowerCase();
            if (!entry.getValue().isJsonObject()) continue;
            JsonObject obj = entry.getValue().getAsJsonObject();
            DialogData data = parsePreset(obj);
            presets.put(name, data);
        }
    }

    public DialogData getPreset(String name) {
        if (name == null) return null;
        return presets.get(name.toLowerCase());
    }

    public DialogData resolveForCharacter(CharacterStory character) {
        String presetName = character.getDialogPresetName();
        DialogData preset = getPreset(presetName);
        return preset != null ? new DialogData(preset) : new DialogData();
    }

    public DialogData resolve(String presetName, DialogData overrides) {
        DialogData base = getPreset(presetName);
        return DialogData.from(base, overrides);
    }

    private DialogData parsePreset(JsonObject obj) {
        DialogData data = new DialogData();

        if (obj.has("offsetX")) data.setOffsetX(obj.get("offsetX").getAsFloat());
        if (obj.has("offsetY")) data.setOffsetY(obj.get("offsetY").getAsFloat());
        if (obj.has("width")) data.setWidth(obj.get("width").getAsFloat());
        if (obj.has("paddingX")) data.setPaddingX(obj.get("paddingX").getAsFloat());
        if (obj.has("paddingY")) data.setPaddingY(obj.get("paddingY").getAsFloat());
        if (obj.has("scale")) data.setScale(obj.get("scale").getAsFloat());
        if (obj.has("letterSpacing"))
            data.setLetterSpacing(obj.get("letterSpacing").getAsFloat());
        if (obj.has("lineGap")) data.setLineGap(obj.get("lineGap").getAsFloat());
        if (obj.has("backgroundColor"))
            data.setBackgroundColor(obj.get("backgroundColor").getAsInt());
        if (obj.has("textColor")) data.setTextColor(obj.get("textColor").getAsInt());
        if (obj.has("backgroundImage")) {
            data.setBackgroundImage(Identifier.parse(obj.get("backgroundImage").getAsString()));
        }
        if (obj.has("scrollSpeed")) data.setScrollSpeed(obj.get("scrollSpeed").getAsFloat());
        if (obj.has("letterSound")) {
            data.setLetterSound(Identifier.parse(obj.get("letterSound").getAsString()));
        }
        if (obj.has("soundMuted")) data.setSoundMuted(obj.get("soundMuted").getAsBoolean());
        if (obj.has("tailVisible")) data.setTailVisible(obj.get("tailVisible").getAsBoolean());
        if (obj.has("bobbingEnabled"))
            data.setBobbingEnabled(obj.get("bobbingEnabled").getAsBoolean());
        if (obj.has("autoSkipEnabled"))
            data.setAutoSkipEnabled(obj.get("autoSkipEnabled").getAsBoolean());
        if (obj.has("autoSkipSeconds"))
            data.setAutoSkipSeconds(obj.get("autoSkipSeconds").getAsFloat());
        if (obj.has("shakeSpeed")) data.setShakeSpeed(obj.get("shakeSpeed").getAsFloat());
        if (obj.has("shakeForce")) data.setShakeForce(obj.get("shakeForce").getAsFloat());

        return data;
    }
}
