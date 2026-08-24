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

package fr.loudo.narrativecraft.narrative.npc;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.widgets.DialogFieldSet;
import fr.loudo.narrativecraft.dialog.DialogDataIO;
import fr.loudo.narrativecraft.narrative.NarrativeDeserializer;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.utils.Utils;
import java.lang.reflect.Type;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class NpcDeserializer extends NarrativeDeserializer<Npc> {

    @Override
    public Npc deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        UUID id = parseId(jsonObject);
        String name = parseName(jsonObject);

        UUID chapterId = UUID.fromString(jsonObject.get("chapterId").getAsString());
        UUID sceneId = UUID.fromString(jsonObject.get("sceneId").getAsString());

        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(chapterId);
        if (chapter == null) {
            throw new JsonParseException("Chapter " + chapterId + " not found for NPC " + name);
        }

        Scene scene = chapter.getSceneManager().getById(sceneId);
        if (scene == null) {
            throw new JsonParseException("Scene " + sceneId + " not found for NPC " + name);
        }

        Npc npc = new Npc(id, name, scene);

        if (jsonObject.has("dialogData") && jsonObject.get("dialogData").isJsonObject()) {
            npc.setDialogData(
                    DialogDataIO.deserialize(jsonObject.getAsJsonObject("dialogData"), DialogFieldSet.CHARACTER));
        }

        String modelTypeName = jsonObject.get("modelType").getAsString();
        if (!modelTypeName.isEmpty()) {
            npc.setModelType(Utils.parsePlayerModelType(modelTypeName));
        }

        npc.setCustomNbt("");
        if (jsonObject.has("customNbt")) {
            npc.setCustomNbt(jsonObject.get("customNbt").getAsString());
        }

        String entityTypeId = jsonObject.get("entityTypeId").getAsString();
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE
                .getOptional(Identifier.parse(entityTypeId))
                .orElse(EntityTypes.PLAYER);
        npc.setEntityType(entityType);

        return npc;
    }
}
