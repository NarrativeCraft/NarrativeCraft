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

import com.google.gson.*;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.NarrativeDeserializer;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import java.lang.reflect.Type;
import java.util.UUID;
import net.minecraft.world.phys.Vec3;

public class InteractionDeserializer extends NarrativeDeserializer<Interaction> {

    @Override
    public Interaction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        if (!obj.has("sceneId") || !obj.has("chapterId")) return null;

        UUID id = parseId(obj);
        String name = parseName(obj);
        String description = parseDescription(obj);
        UUID sceneId = UUID.fromString(obj.get("sceneId").getAsString());
        UUID chapterId = UUID.fromString(obj.get("chapterId").getAsString());

        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(chapterId);
        if (chapter == null) return null;

        Scene scene = chapter.getSceneManager().getById(sceneId);
        if (scene == null) return null;

        Interaction interaction = new Interaction(id, name, description, scene);
        deserializeInto(obj, interaction);
        return interaction;
    }

    public static void deserializeInto(String json, Interaction interaction) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        deserializeInto(obj, interaction);
    }

    public static void deserializeInto(JsonObject obj, Interaction interaction) {
        interaction.getZones().clear();
        if (obj.has("zones")) {
            for (JsonElement element : obj.getAsJsonArray("zones")) {
                InteractionZone zone = deserializeZone(element.getAsJsonObject());
                if (zone != null) interaction.getZones().add(zone);
            }
        }

        interaction.getPoints().clear();
        if (obj.has("points")) {
            for (JsonElement element : obj.getAsJsonArray("points")) {
                InteractionPoint point = deserializePoint(element.getAsJsonObject());
                if (point != null) interaction.getPoints().add(point);
            }
        }
    }

    private static InteractionZone deserializeZone(JsonObject json) {
        if (!json.has("id") || !json.has("name") || !json.has("x1")) return null;

        UUID id = UUID.fromString(json.get("id").getAsString());
        String name = json.get("name").getAsString();
        String stitchName = json.has("stitchName") ? json.get("stitchName").getAsString() : "";
        Vec3 corner1 = new Vec3(
                json.get("x1").getAsDouble(),
                json.get("y1").getAsDouble(),
                json.get("z1").getAsDouble());
        Vec3 corner2 = new Vec3(
                json.get("x2").getAsDouble(),
                json.get("y2").getAsDouble(),
                json.get("z2").getAsDouble());
        boolean oneTime = json.has("oneTime") && json.get("oneTime").getAsBoolean();

        return new InteractionZone(id, name, stitchName, corner1, corner2, oneTime);
    }

    private static InteractionPoint deserializePoint(JsonObject json) {
        if (!json.has("id") || !json.has("name") || !json.has("x")) return null;

        UUID id = UUID.fromString(json.get("id").getAsString());
        String name = json.get("name").getAsString();
        String stitchName = json.has("stitchName") ? json.get("stitchName").getAsString() : "";
        Vec3 position = new Vec3(
                json.get("x").getAsDouble(),
                json.get("y").getAsDouble(),
                json.get("z").getAsDouble());
        double maxDistance =
                json.has("maxDistance") ? json.get("maxDistance").getAsDouble() : InteractionPoint.DEFAULT_MAX_DISTANCE;
        boolean useAimRadius =
                !json.has("useAimRadius") || json.get("useAimRadius").getAsBoolean();
        double aimRadius =
                json.has("aimRadius") ? json.get("aimRadius").getAsDouble() : InteractionPoint.DEFAULT_AIM_RADIUS;
        boolean neverShow = json.has("neverShow") && json.get("neverShow").getAsBoolean();
        boolean oneTimeClick = json.has("oneTime") && json.get("oneTime").getAsBoolean();

        return new InteractionPoint(
                id, name, stitchName, position, maxDistance, useAimRadius, aimRadius, neverShow, oneTimeClick);
    }
}
