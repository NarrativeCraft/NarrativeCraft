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

package fr.loudo.narrativecraft.narrative.character;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import fr.loudo.narrativecraft.dialog.DialogDataIO;
import fr.loudo.narrativecraft.narrative.NarrativeDeserializer;
import java.lang.reflect.Type;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;

public class CharacterStoryDeserializer extends NarrativeDeserializer<CharacterStory> {

    public static void applySharedCharacterFields(JsonObject jsonObject, CharacterStory character) {
        if (jsonObject.has("dialogData") && jsonObject.get("dialogData").isJsonObject()) {
            character.setDialogData(DialogDataIO.deserialize(jsonObject.getAsJsonObject("dialogData")));
        }

        String modelTypeName = jsonObject.get("modelType").getAsString();
        if (!modelTypeName.isEmpty()) {
            character.setModelType(PlayerModelType.valueOf(modelTypeName));
        }

        String entityTypeId = jsonObject.get("entityTypeId").getAsString();
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE
                .getOptional(Identifier.parse(entityTypeId))
                .orElse(EntityType.PLAYER);
        character.setEntityType(entityType);
    }

    @Override
    public CharacterStory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        UUID id = parseId(jsonObject);
        String name = parseName(jsonObject);
        String description = parseDescription(jsonObject);

        CharacterStory character = new CharacterStory(id, name, description);

        applySharedCharacterFields(jsonObject, character);

        MainCharacterAttribute mainCharacterAttribute = new MainCharacterAttribute();
        if (jsonObject.has("mainCharacter")) {
            mainCharacterAttribute.setMainCharacter(
                    jsonObject.get("mainCharacter").getAsBoolean());
        }
        if (jsonObject.has("skinMode")) {
            try {
                mainCharacterAttribute.setSkin(MainCharacterAttribute.SkinMode.valueOf(
                        jsonObject.get("skinMode").getAsString()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        character.setMainCharacterAttribute(mainCharacterAttribute);

        return character;
    }
}
