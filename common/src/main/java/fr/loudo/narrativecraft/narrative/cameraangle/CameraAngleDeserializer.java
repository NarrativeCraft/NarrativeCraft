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

package fr.loudo.narrativecraft.narrative.cameraangle;

import com.google.gson.*;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.widgets.DialogFieldSet;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.dialog.DialogDataIO;
import fr.loudo.narrativecraft.narrative.NarrativeDeserializer;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.utils.Utils;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.phys.Vec3;

public class CameraAngleDeserializer extends NarrativeDeserializer<CameraAngle> {

    @Override
    public CameraAngle deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        UUID id = parseId(obj);
        String name = parseName(obj);
        String description = parseDescription(obj);
        if (!obj.has("sceneId") || !obj.has("chapterId")) {
            CameraAngle cameraAngle = new CameraAngle(id, name, description, null);
            deserializeInto(obj, cameraAngle);
            return cameraAngle;
        }

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

    public static CameraView deserializeCamera(JsonObject json) {
        if (!json.has("name") || !json.has("x")) return null;

        UUID id = json.has("id") ? UUID.fromString(json.get("id").getAsString()) : UUID.randomUUID();
        String name = json.get("name").getAsString();
        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();
        double xRot = json.get("xRot").getAsDouble();
        double yRot = json.get("yRot").getAsDouble();
        double roll = json.has("roll") ? json.get("roll").getAsDouble() : 0.0;
        float fov = json.has("fov") ? json.get("fov").getAsFloat() : 70f;

        CameraView cameraView = new CameraView(id, name, new Vec3(x, y, z), new Vec3(xRot, yRot, roll), fov);
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

    public static DialogData deserializeDialogData(JsonObject json) {
        return DialogDataIO.deserialize(json, DialogFieldSet.CAMERA_VIEW);
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

        Map<EquipmentSlot, ItemStack> itemsBySlot = deserializeItems(json);

        ICharacterStory characterStory =
                NarrativeCraftMod.getInstance().getCharacterManager().resolveCharacter(characterId, scene);
        if (characterStory == null) {
            return null;
        }

        boolean isTemplate = json.has("isTemplate") && json.get("isTemplate").getAsBoolean();
        boolean onGround = !json.has("onGround") || json.get("onGround").getAsBoolean();
        UUID templateReferenceId = json.has("templateReferenceId")
                ? UUID.fromString(json.get("templateReferenceId").getAsString())
                : null;

        CharacterPlacement placement = new CharacterPlacement(
                id,
                characterStory,
                new Vec3(x, y, z),
                new Vec3(xRot, yRot, roll),
                itemsBySlot,
                onGround,
                isTemplate,
                templateReferenceId);

        if (json.has("pose")) {
            try {
                placement.setPose(Pose.valueOf(json.get("pose").getAsString()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        return placement;
    }

    private static Map<EquipmentSlot, ItemStack> deserializeItems(JsonObject json) {
        Map<EquipmentSlot, ItemStack> itemsBySlot = new EnumMap<>(EquipmentSlot.class);
        if (!json.has("items")) return itemsBySlot;

        JsonElement itemsElement = json.get("items");
        if (itemsElement.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry :
                    itemsElement.getAsJsonObject().entrySet()) {
                EquipmentSlot slot = EquipmentSlot.CODEC.byName(entry.getKey());
                if (slot == null) continue;
                ItemStack stack = deserializeItemStack(entry.getValue().getAsString());
                if (stack != null && !stack.isEmpty()) itemsBySlot.put(slot, stack);
            }
            return itemsBySlot;
        }

        if (itemsElement.isJsonArray()) {
            for (JsonElement element : itemsElement.getAsJsonArray()) {
                ItemStack stack = deserializeItemStack(element.getAsString());
                if (stack == null || stack.isEmpty()) continue;
                EquipmentSlot slot = resolveLegacySlot(stack, itemsBySlot);
                if (slot != null) itemsBySlot.put(slot, stack);
            }
        }
        return itemsBySlot;
    }

    private static EquipmentSlot resolveLegacySlot(ItemStack stack, Map<EquipmentSlot, ItemStack> alreadyResolved) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && !alreadyResolved.containsKey(equippable.slot())) return equippable.slot();
        if (!alreadyResolved.containsKey(EquipmentSlot.MAINHAND)) return EquipmentSlot.MAINHAND;
        if (!alreadyResolved.containsKey(EquipmentSlot.OFFHAND)) return EquipmentSlot.OFFHAND;
        return null;
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
        String displayName = json.has("displayName") ? json.get("displayName").getAsString() : "";

        return new TemplateReference(id, sourceType, refId, displayName);
    }
}
