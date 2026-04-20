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

package fr.loudo.narrativecraft.narrative.cameraangle;

import com.google.gson.*;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.narrative.NarrativeDeserializer;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.utils.Utils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class CameraAngleDeserializer extends NarrativeDeserializer<CameraAngle> {

    @Override
    public CameraAngle deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
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

        CameraAngle cameraAngle = new CameraAngle(id, name, description, scene);
        deserializeInto(obj, cameraAngle);
        return cameraAngle;
    }

    public static void deserializeInto(String json, CameraAngle cameraAngle) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        deserializeInto(obj, cameraAngle);
    }

    public static void deserializeInto(JsonObject obj, CameraAngle cameraAngle) {
        cameraAngle.getCameras().clear();
        if (obj.has("cameras")) {
            for (JsonElement element : obj.getAsJsonArray("cameras")) {
                CameraView cameraView = deserializeCamera(element.getAsJsonObject());
                if (cameraView != null) cameraAngle.getCameras().add(cameraView);
            }
        }

        cameraAngle.getCharacterPlacements().clear();
        if (obj.has("characterPlacements")) {
            for (JsonElement element : obj.getAsJsonArray("characterPlacements")) {
                CharacterPlacement placement =
                        deserializeCharacterPlacement(element.getAsJsonObject(), cameraAngle.getScene());
                if (placement != null) cameraAngle.getCharacterPlacements().add(placement);
            }
        }

        cameraAngle.getTemplateReferences().clear();
        if (obj.has("templateReferences")) {
            for (JsonElement element : obj.getAsJsonArray("templateReferences")) {
                TemplateReference reference = deserializeTemplateReference(element.getAsJsonObject());
                if (reference != null) cameraAngle.getTemplateReferences().add(reference);
            }
        }
    }

    private static CameraView deserializeCamera(JsonObject json) {
        if (!json.has("name") || !json.has("x")) return null;

        String name = json.get("name").getAsString();
        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();
        double xRot = json.get("xRot").getAsDouble();
        double yRot = json.get("yRot").getAsDouble();
        double roll = json.has("roll") ? json.get("roll").getAsDouble() : 0.0;
        float fov = json.has("fov") ? json.get("fov").getAsFloat() : 70f;

        CameraView cameraView = new CameraView(name, new Vec3(x, y, z), new Vec3(xRot, yRot, roll), fov);
        if (json.has("dialogSetups")) {
            for (JsonElement element : json.getAsJsonArray("dialogSetups")) {
                CameraViewDialogSetup setup = deserializeDialogSetup(element.getAsJsonObject());
                if (setup != null) cameraView.getDialogSetups().add(setup);
            }
        }
        return cameraView;
    }

    private static CameraViewDialogSetup deserializeDialogSetup(JsonObject json) {
        if (!json.has("id") || !json.has("characterPlacementId")) return null;
        UUID id = UUID.fromString(json.get("id").getAsString());
        UUID characterPlacementId =
                UUID.fromString(json.get("characterPlacementId").getAsString());
        DialogData dialogData =
                json.has("dialogData") ? deserializeDialogData(json.getAsJsonObject("dialogData")) : new DialogData();
        return new CameraViewDialogSetup(id, characterPlacementId, dialogData);
    }

    static DialogData deserializeDialogData(JsonObject json) {
        DialogData data = new DialogData();
        if (json.has("offsetX")) data.setOffsetX(json.get("offsetX").getAsFloat());
        if (json.has("offsetY")) data.setOffsetY(json.get("offsetY").getAsFloat());
        if (json.has("width")) data.setWidth(json.get("width").getAsFloat());
        if (json.has("paddingX")) data.setPaddingX(json.get("paddingX").getAsFloat());
        if (json.has("paddingY")) data.setPaddingY(json.get("paddingY").getAsFloat());
        if (json.has("scale")) data.setScale(json.get("scale").getAsFloat());
        if (json.has("letterSpacing"))
            data.setLetterSpacing(json.get("letterSpacing").getAsFloat());
        if (json.has("lineGap")) data.setLineGap(json.get("lineGap").getAsFloat());
        if (json.has("backgroundColor"))
            data.setBackgroundColor(json.get("backgroundColor").getAsInt());
        if (json.has("textColor")) data.setTextColor(json.get("textColor").getAsInt());
        if (json.has("backgroundImage")) {
            data.setBackgroundImage(Identifier.parse(json.get("backgroundImage").getAsString()));
        }
        if (json.has("scrollSpeed")) data.setScrollSpeed(json.get("scrollSpeed").getAsFloat());
        if (json.has("letterSound")) {
            data.setLetterSound(Identifier.parse(json.get("letterSound").getAsString()));
        }
        if (json.has("soundMuted")) data.setSoundMuted(json.get("soundMuted").getAsBoolean());
        if (json.has("tailVisible")) data.setTailVisible(json.get("tailVisible").getAsBoolean());
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
        return data;
    }

    public static CharacterPlacement deserializeCharacterPlacementFromJson(String json, Scene scene) {
        return deserializeCharacterPlacement(JsonParser.parseString(json).getAsJsonObject(), scene);
    }

    static CharacterPlacement deserializeCharacterPlacement(JsonObject json, Scene scene) {
        if (!json.has("id") || !json.has("characterId")) return null;

        UUID id = UUID.fromString(json.get("id").getAsString());
        UUID characterId = UUID.fromString(json.get("characterId").getAsString());
        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();
        double xRot = json.get("xRot").getAsDouble();
        double yRot = json.get("yRot").getAsDouble();
        double roll = json.has("roll") ? json.get("roll").getAsDouble() : 0.0;

        List<ItemStack> items = new ArrayList<>();
        if (json.has("items")) {
            for (JsonElement element : json.getAsJsonArray("items")) {
                ItemStack stack = deserializeItemStack(element.getAsString());
                if (stack != null && !stack.isEmpty()) items.add(stack);
            }
        }

        ICharacterStory characterStory =
                NarrativeCraftMod.getInstance().getCharacterManager().resolveCharacter(characterId, scene);
        if (characterStory == null) {
            return null;
        }

        return new CharacterPlacement(id, characterStory, new Vec3(x, y, z), new Vec3(xRot, yRot, roll), items);
    }

    private static ItemStack deserializeItemStack(String nbtString) {
        try {
            CompoundTag tag = Utils.nbtFromString(nbtString);
            return ItemStack.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
        } catch (Exception e) {
            return null;
        }
    }

    private static TemplateReference deserializeTemplateReference(JsonObject json) {
        if (!json.has("id") || !json.has("sourceType") || !json.has("characterId")) return null;

        UUID id = UUID.fromString(json.get("id").getAsString());
        TemplateSourceType sourceType =
                TemplateSourceType.valueOf(json.get("sourceType").getAsString());
        UUID refId = UUID.fromString(json.get("characterId").getAsString());

        return new TemplateReference(id, sourceType, refId);
    }
}
