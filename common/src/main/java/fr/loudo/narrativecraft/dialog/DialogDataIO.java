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

package fr.loudo.narrativecraft.dialog;

import com.google.gson.JsonObject;
import fr.loudo.narrativecraft.client.editors.widgets.DialogFieldSet;
import net.minecraft.resources.ResourceLocation;

public final class DialogDataIO {

    private DialogDataIO() {}

    public static JsonObject serialize(DialogData data, DialogFieldSet fieldSet) {
        JsonObject json = new JsonObject();
        boolean all = fieldSet == DialogFieldSet.ALL;

        if (all || fieldSet == DialogFieldSet.CAMERA_VIEW) {
            json.addProperty("offsetX", data.getOffsetX());
            json.addProperty("offsetY", data.getOffsetY());
            json.addProperty("ancOffsetX", data.getAncOffsetX());
            json.addProperty("ancOffsetY", data.getAncOffsetY());
            json.addProperty("scale", data.getScale());
        }
        if (all || fieldSet == DialogFieldSet.CHARACTER) {
            json.addProperty("backgroundColor", data.getBackgroundColor());
            json.addProperty("textColor", data.getTextColor());
            if (data.getBackgroundImage() != null) {
                json.addProperty("backgroundImage", data.getBackgroundImage().toString());
            }
            if (data.getLetterSound() != null) {
                json.addProperty("letterSound", data.getLetterSound().toString());
            }
            json.addProperty("tailVisible", data.isTailVisible());
            json.addProperty("textShadow", data.isTextShadow());
        }
        if (all) {
            json.addProperty("width", data.getWidth());
            json.addProperty("paddingX", data.getPaddingX());
            json.addProperty("paddingY", data.getPaddingY());
            json.addProperty("letterSpacing", data.getLetterSpacing());
            json.addProperty("lineGap", data.getLineGap());
            json.addProperty("scrollSpeed", data.getScrollSpeed());
            json.addProperty("soundMuted", data.isSoundMuted());
            json.addProperty("autoSkipEnabled", data.isAutoSkipEnabled());
            json.addProperty("autoSkipSeconds", data.getAutoSkipSeconds());
            json.addProperty("textAlignment", data.getTextAlignment().name());
        }
        return json;
    }

    public static DialogData deserialize(JsonObject json, DialogFieldSet fieldSet) {
        DialogData data = new DialogData();
        boolean all = fieldSet == DialogFieldSet.ALL;

        if (all || fieldSet == DialogFieldSet.CAMERA_VIEW) {
            if (json.has("offsetX")) data.setOffsetX(json.get("offsetX").getAsFloat());
            if (json.has("offsetY")) data.setOffsetY(json.get("offsetY").getAsFloat());
            if (json.has("ancOffsetX"))
                data.setAncOffsetX(json.get("ancOffsetX").getAsFloat());
            if (json.has("ancOffsetY"))
                data.setAncOffsetY(json.get("ancOffsetY").getAsFloat());
            if (json.has("scale")) data.setScale(json.get("scale").getAsFloat());
        }
        if (all || fieldSet == DialogFieldSet.CHARACTER) {
            if (json.has("backgroundColor"))
                data.setBackgroundColor(json.get("backgroundColor").getAsInt());
            if (json.has("textColor")) data.setTextColor(json.get("textColor").getAsInt());
            if (json.has("backgroundImage")) {
                data.setBackgroundImage(
                        ResourceLocation.parse(json.get("backgroundImage").getAsString()));
            }
            if (json.has("letterSound")) {
                data.setLetterSound(
                        ResourceLocation.parse(json.get("letterSound").getAsString()));
            }
            if (json.has("tailVisible")) {
                data.setTailVisible(json.get("tailVisible").getAsBoolean());
            }
            if (json.has("textShadow")) {
                data.setTextShadow(json.get("textShadow").getAsBoolean());
            }
        }
        if (all) {
            if (json.has("width")) data.setWidth(json.get("width").getAsFloat());
            if (json.has("paddingX")) data.setPaddingX(json.get("paddingX").getAsFloat());
            if (json.has("paddingY")) data.setPaddingY(json.get("paddingY").getAsFloat());
            if (json.has("letterSpacing"))
                data.setLetterSpacing(json.get("letterSpacing").getAsFloat());
            if (json.has("lineGap")) data.setLineGap(json.get("lineGap").getAsFloat());
            if (json.has("scrollSpeed"))
                data.setScrollSpeed(json.get("scrollSpeed").getAsFloat());
            if (json.has("soundMuted"))
                data.setSoundMuted(json.get("soundMuted").getAsBoolean());
            if (json.has("autoSkipEnabled"))
                data.setAutoSkipEnabled(json.get("autoSkipEnabled").getAsBoolean());
            if (json.has("autoSkipSeconds"))
                data.setAutoSkipSeconds(json.get("autoSkipSeconds").getAsFloat());
            if (json.has("textAlignment")) {
                try {
                    data.setTextAlignment(DialogData.TextAlignment.valueOf(
                            json.get("textAlignment").getAsString()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return data;
    }
}
