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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import fr.loudo.narrativecraft.client.editors.widgets.DialogFieldSet;
import fr.loudo.narrativecraft.dialog.DialogDataIO;
import java.lang.reflect.Type;
import net.minecraft.core.registries.BuiltInRegistries;

public class NpcSerializer implements JsonSerializer<Npc> {

    @Override
    public JsonElement serialize(Npc src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("id", src.getId().toString());
        json.addProperty("name", src.getName());
        json.add("dialogData", DialogDataIO.serialize(src.getDialogData(), DialogFieldSet.CHARACTER));
        json.addProperty(
                "modelType", src.getModelType() != null ? src.getModelType().name() : "");
        json.addProperty(
                "entityTypeId",
                BuiltInRegistries.ENTITY_TYPE.getKey(src.getEntityType()).toString());
        json.addProperty("customNbt", src.getCustomNbt() != null ? src.getCustomNbt() : "");
        json.addProperty("sceneId", src.getScene().getId().toString());
        json.addProperty("chapterId", src.getScene().getChapter().getId().toString());
        return json;
    }
}
