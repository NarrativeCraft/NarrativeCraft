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

package fr.loudo.narrativecraft.narrative.interaction;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

public class InteractionSerializer implements JsonSerializer<Interaction> {

    public static String serializeData(Interaction interaction) {
        JsonObject data = new JsonObject();
        data.add("zones", serializeZones(interaction));
        data.add("points", serializePoints(interaction));
        return new Gson().toJson(data);
    }

    @Override
    public com.google.gson.JsonElement serialize(
            Interaction src, Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("id", src.getId().toString());
        json.addProperty("name", src.getName());
        json.addProperty("description", src.getDescription());
        json.addProperty("sceneId", src.getScene().getId().toString());
        json.addProperty("chapterId", src.getScene().getChapter().getId().toString());
        json.add("zones", serializeZones(src));
        json.add("points", serializePoints(src));
        return json;
    }

    private static JsonArray serializeZones(Interaction interaction) {
        JsonArray array = new JsonArray();
        for (InteractionZone zone : interaction.getZones()) {
            array.add(serializeZone(zone));
        }
        return array;
    }

    private static JsonArray serializePoints(Interaction interaction) {
        JsonArray array = new JsonArray();
        for (InteractionPoint point : interaction.getPoints()) {
            array.add(serializePoint(point));
        }
        return array;
    }

    static JsonObject serializeZone(InteractionZone zone) {
        JsonObject json = new JsonObject();
        json.addProperty("id", zone.getId().toString());
        json.addProperty("name", zone.getName());
        json.addProperty("path", zone.getStitchName());
        json.addProperty("x1", zone.getCorner1().x);
        json.addProperty("y1", zone.getCorner1().y);
        json.addProperty("z1", zone.getCorner1().z);
        json.addProperty("x2", zone.getCorner2().x);
        json.addProperty("y2", zone.getCorner2().y);
        json.addProperty("z2", zone.getCorner2().z);
        json.addProperty("oneTime", zone.isOneTime());
        return json;
    }

    static JsonObject serializePoint(InteractionPoint point) {
        JsonObject json = new JsonObject();
        json.addProperty("id", point.getId().toString());
        json.addProperty("name", point.getName());
        json.addProperty("path", point.getStitchName());
        json.addProperty("x", point.getPosition().x);
        json.addProperty("y", point.getPosition().y);
        json.addProperty("z", point.getPosition().z);
        json.addProperty("maxDistance", point.getMaxDistance());
        json.addProperty("useAimRadius", point.isUseAimRadius());
        json.addProperty("aimRadius", point.getAimRadius());
        json.addProperty("neverShow", point.isNeverShow());
        json.addProperty("oneTime", point.isOneTimeClick());
        return json;
    }
}
