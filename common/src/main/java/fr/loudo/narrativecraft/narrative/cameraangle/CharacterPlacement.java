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

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.utils.codec.NarrativeCodecs;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.phys.Vec3;

public class CharacterPlacement {

    private static final Codec<Map<EquipmentSlot, ItemStack>> ITEMS_CODEC = Codec.either(
                    Codec.unboundedMap(Codec.STRING, Codec.STRING), Codec.STRING.listOf())
            .xmap(CharacterPlacement::readItems, items -> Either.left(writeItems(items)));

    public static final Codec<CharacterPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    NarrativeCodecs.ID.forGetter(CharacterPlacement::getId),
                    NarrativeCodecs.UUID_CODEC.fieldOf("characterId").forGetter(CharacterPlacement::getCharacterId),
                    NarrativeCodecs.POSITION.forGetter(CharacterPlacement::getPosition),
                    NarrativeCodecs.ROTATION.forGetter(CharacterPlacement::getRotation),
                    NarrativeCodecs.field(NarrativeCodecs.enumByName(Pose.class), "pose", Pose.STANDING)
                            .forGetter(CharacterPlacement::getPose),
                    NarrativeCodecs.field(Codec.BOOL, "onGround", true).forGetter(CharacterPlacement::isOnGround),
                    NarrativeCodecs.field(Codec.BOOL, "isTemplate", false).forGetter(CharacterPlacement::isTemplate),
                    NarrativeCodecs.optionalField(NarrativeCodecs.UUID_CODEC, "templateReferenceId")
                            .forGetter(placement -> Optional.ofNullable(placement.getTemplateReferenceId())),
                    NarrativeCodecs.field(ITEMS_CODEC, "items", Map.of()).forGetter(CharacterPlacement::getItemsBySlot))
            .apply(
                    instance,
                    (id, characterId, position, rotation, pose, onGround, isTemplate, templateReferenceId, items) -> {
                        CharacterPlacement placement = new CharacterPlacement(
                                id,
                                characterId,
                                position,
                                rotation,
                                items,
                                onGround,
                                isTemplate,
                                templateReferenceId.orElse(null));
                        placement.setPose(pose);
                        return placement;
                    }));

    private final UUID id;
    private final UUID characterId;
    private Vec3 position;
    private Vec3 rotation;
    private final Map<EquipmentSlot, ItemStack> itemsBySlot = new EnumMap<>(EquipmentSlot.class);
    private final boolean onGround;
    private final boolean isTemplate;
    private final UUID templateReferenceId;
    private Pose pose = Pose.STANDING;

    public CharacterPlacement(
            UUID id,
            UUID characterId,
            Vec3 position,
            Vec3 rotation,
            Map<EquipmentSlot, ItemStack> itemsBySlot,
            boolean onGround,
            boolean isTemplate,
            UUID templateReferenceId) {
        this.id = id;
        this.characterId = characterId;
        this.position = position;
        this.rotation = rotation;
        this.onGround = onGround;
        this.isTemplate = isTemplate;
        this.templateReferenceId = templateReferenceId;
        if (itemsBySlot != null) {
            for (Map.Entry<EquipmentSlot, ItemStack> entry : itemsBySlot.entrySet()) {
                setItem(entry.getKey(), entry.getValue());
            }
        }
    }

    public CharacterPlacement(
            UUID characterId,
            Vec3 position,
            Vec3 rotation,
            Map<EquipmentSlot, ItemStack> itemsBySlot,
            boolean onGround) {
        this(UUID.randomUUID(), characterId, position, rotation, itemsBySlot, onGround, false, null);
    }

    private static Map<EquipmentSlot, ItemStack> readItems(Either<Map<String, String>, List<String>> items) {
        Map<EquipmentSlot, ItemStack> itemsBySlot = new EnumMap<>(EquipmentSlot.class);
        items.ifLeft(itemsBySlotName -> {
            for (Map.Entry<String, String> entry : itemsBySlotName.entrySet()) {
                EquipmentSlot slot = EquipmentSlot.CODEC.byName(entry.getKey());
                if (slot == null) continue;
                NarrativeCodecs.parseItemStack(entry.getValue())
                        .result()
                        .filter(itemStack -> !itemStack.isEmpty())
                        .ifPresent(itemStack -> itemsBySlot.put(slot, itemStack));
            }
        });
        items.ifRight(legacyItems -> {
            for (String legacyItem : legacyItems) {
                NarrativeCodecs.parseItemStack(legacyItem)
                        .result()
                        .filter(itemStack -> !itemStack.isEmpty())
                        .ifPresent(itemStack -> {
                            EquipmentSlot slot = resolveLegacySlot(itemStack, itemsBySlot);
                            if (slot != null) itemsBySlot.put(slot, itemStack);
                        });
            }
        });
        return itemsBySlot;
    }

    private static Map<String, String> writeItems(Map<EquipmentSlot, ItemStack> itemsBySlot) {
        Map<String, String> itemsBySlotName = new LinkedHashMap<>();
        for (Map.Entry<EquipmentSlot, ItemStack> entry : itemsBySlot.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            itemsBySlotName.put(entry.getKey().getSerializedName(), NarrativeCodecs.writeItemStack(entry.getValue()));
        }
        return itemsBySlotName;
    }

    private static EquipmentSlot resolveLegacySlot(ItemStack stack, Map<EquipmentSlot, ItemStack> alreadyResolved) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && !alreadyResolved.containsKey(equippable.slot())) return equippable.slot();
        if (!alreadyResolved.containsKey(EquipmentSlot.MAINHAND)) return EquipmentSlot.MAINHAND;
        if (!alreadyResolved.containsKey(EquipmentSlot.OFFHAND)) return EquipmentSlot.OFFHAND;
        return null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public ICharacterStory resolveCharacter(CharacterManager characters, Scene scene) {
        return characters.resolveCharacter(characterId, scene);
    }

    public Vec3 getPosition() {
        return position;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
    }

    public Vec3 getRotation() {
        return rotation;
    }

    public void setRotation(Vec3 rotation) {
        this.rotation = rotation;
    }

    public Map<EquipmentSlot, ItemStack> getItemsBySlot() {
        return itemsBySlot;
    }

    public ItemStack getItem(EquipmentSlot slot) {
        return itemsBySlot.getOrDefault(slot, ItemStack.EMPTY);
    }

    public void setItem(EquipmentSlot slot, ItemStack itemStack) {
        if (slot == null) return;
        if (itemStack == null || itemStack.isEmpty()) {
            itemsBySlot.remove(slot);
            return;
        }
        itemsBySlot.put(slot, itemStack);
    }

    public boolean isOnGround() {
        return onGround;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public UUID getTemplateReferenceId() {
        return templateReferenceId;
    }

    public Pose getPose() {
        return pose;
    }

    public void setPose(Pose pose) {
        this.pose = pose;
    }
}
