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
import com.mojang.serialization.DataResult;
import fr.loudo.narrativecraft.client.editors.widgets.DialogFieldSet;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.dialog.DialogDataIO;
import java.lang.reflect.Type;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class CameraAngleSerializer implements JsonSerializer<CameraAngle> {

    public static String serializeData(CameraAngle cameraAngle) {
        JsonObject data = new JsonObject();
        data.add("cameras", serializeCameras(cameraAngle));
        data.add("characterPlacements", serializeCharacterPlacements(cameraAngle));
        if (cameraAngle.getScene() != null) {
            data.add("templateReferences", serializeTemplateReferences(cameraAngle));
        }
        return new Gson().toJson(data);
    }

    public static String serializeSingleCharacterPlacement(CharacterPlacement placement) {
        return new Gson().toJson(serializeCharacterPlacement(placement));
    }

    @Override
    public JsonElement serialize(CameraAngle src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("id", src.getId().toString());
        json.addProperty("name", src.getName());
        json.addProperty("description", src.getDescription());
        if (src.getScene() != null) {
            json.addProperty("sceneId", src.getScene().getId().toString());
            json.addProperty("chapterId", src.getScene().getChapter().getId().toString());
        }
        json.add("cameras", serializeCameras(src));
        json.add("characterPlacements", serializeCharacterPlacements(src));
        json.add("templateReferences", serializeTemplateReferences(src));
        return json;
    }

    private static JsonArray serializeCameras(CameraAngle cameraAngle) {
        JsonArray cameras = new JsonArray();
        for (CameraView cameraView : cameraAngle.getCameras()) {
            cameras.add(serializeCamera(cameraView, cameraAngle.getScene() != null));
        }
        return cameras;
    }

    private static JsonArray serializeCharacterPlacements(CameraAngle cameraAngle) {
        JsonArray placements = new JsonArray();
        for (CharacterPlacement placement : cameraAngle.getCharacterPlacements()) {
            placements.add(serializeCharacterPlacement(placement));
        }
        return placements;
    }

    private static JsonArray serializeTemplateReferences(CameraAngle cameraAngle) {
        JsonArray references = new JsonArray();
        for (TemplateReference reference : cameraAngle.getTemplateReferences()) {
            references.add(serializeTemplateReference(reference));
        }
        return references;
    }

    public static JsonObject serializeCamera(CameraView cameraView, boolean withDialogSetup) {
        JsonObject json = new JsonObject();
        json.addProperty("id", cameraView.getId().toString());
        json.addProperty("name", cameraView.getName());
        json.addProperty("x", cameraView.getPosition().x);
        json.addProperty("y", cameraView.getPosition().y);
        json.addProperty("z", cameraView.getPosition().z);
        json.addProperty("xRot", cameraView.getRotation().x);
        json.addProperty("yRot", cameraView.getRotation().y);
        json.addProperty("roll", cameraView.getRotation().z);
        json.addProperty("fov", cameraView.getFov());
        if (!withDialogSetup) return json;
        JsonArray dialogSetups = new JsonArray();
        for (CameraViewDialogSetup setup : cameraView.getDialogSetups()) {
            dialogSetups.add(serializeDialogSetup(setup));
        }
        json.add("dialogSetups", dialogSetups);
        return json;
    }

    private static JsonObject serializeDialogSetup(CameraViewDialogSetup setup) {
        JsonObject json = new JsonObject();
        json.addProperty("id", setup.getId().toString());
        json.addProperty("characterPlacementId", setup.getCharacterPlacementId().toString());
        json.add("dialogData", serializeDialogData(setup.getDialogData()));
        return json;
    }

    public static JsonObject serializeDialogData(DialogData data) {
        return DialogDataIO.serialize(data, DialogFieldSet.CAMERA_VIEW);
    }

    static JsonObject serializeCharacterPlacement(CharacterPlacement placement) {
        JsonObject json = new JsonObject();
        json.addProperty("id", placement.getId().toString());
        json.addProperty("characterId", placement.getCharacterStory().getId().toString());
        json.addProperty("x", placement.getPosition().x);
        json.addProperty("y", placement.getPosition().y);
        json.addProperty("z", placement.getPosition().z);
        json.addProperty("xRot", placement.getRotation().x);
        json.addProperty("yRot", placement.getRotation().y);
        json.addProperty("roll", placement.getRotation().z);
        json.addProperty("pose", placement.getPose().name());
        json.addProperty("onGround", placement.isOnGround());
        if (placement.isTemplate()) {
            json.addProperty("isTemplate", true);
            if (placement.getTemplateReferenceId() != null)
                json.addProperty(
                        "templateReferenceId",
                        placement.getTemplateReferenceId().toString());
        }

        JsonObject items = new JsonObject();
        for (Map.Entry<EquipmentSlot, ItemStack> entry :
                placement.getItemsBySlot().entrySet()) {
            JsonElement serialized = serializeItemStack(entry.getValue());
            if (serialized != null) items.add(entry.getKey().getSerializedName(), serialized);
        }
        json.add("items", items);
        return json;
    }

    private static JsonElement serializeItemStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        DataResult<Tag> result = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack);
        Tag tag = result.resultOrPartial(error -> {}).orElse(null);
        if (!(tag instanceof CompoundTag compound)) return null;
        return new com.google.gson.JsonPrimitive(compound.toString());
    }

    static JsonObject serializeTemplateReference(TemplateReference reference) {
        JsonObject json = new JsonObject();
        json.addProperty("id", reference.id().toString());
        json.addProperty("sourceType", reference.sourceType().name());
        json.addProperty("characterId", reference.refId().toString());
        json.addProperty("displayName", reference.displayName());
        return json;
    }
}
