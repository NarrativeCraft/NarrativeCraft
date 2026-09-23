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

package fr.loudo.narrativecraft.narrative.character;

import com.mojang.serialization.Codec;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.files.NarrativeCraftFileDefault;
import fr.loudo.narrativecraft.files.NarrativeCraftFileUtil;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.utils.Utils;
import java.io.File;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.PlayerModelType;

public class CharacterStory extends NarrativeEntry<CharacterStoryPayload> implements ICharacterStory {

    public static final String USERNAME_VARIABLE = "user";
    public static final String DEFAULT_ENTITY_TYPE_ID = "minecraft:player";

    public static final Codec<CharacterStory> CODEC =
            entryCodec(CharacterStoryPayload.CODEC, CharacterStory::fromPayload);

    private final CharacterType characterType = CharacterType.NORMAL;
    private DialogData dialogData = new DialogData();
    private EntityType<?> entityType = EntityTypes.PLAYER;
    private PlayerModelType modelType;
    private String customNbt = "";
    private MainCharacterAttribute mainCharacterAttribute = new MainCharacterAttribute();

    public CharacterStory(UUID id, String name) {
        super(id, name);
    }

    public static CharacterStory fromPayload(UUID id, CharacterStoryPayload payload) {
        CharacterStory character = new CharacterStory(id, payload.getName());
        character.dialogData = payload.getDialogData();
        character.modelType = parseModelType(payload.getModelType());
        character.entityType = Utils.resolveEntityType(payload.getEntityTypeId());
        character.customNbt = payload.getCustomNbt();
        character.mainCharacterAttribute = new MainCharacterAttribute(payload.getMainCharacterAttribute());
        return character;
    }

    public static PlayerModelType parseModelType(String modelTypeName) {
        return modelTypeName.isEmpty() ? null : Utils.parsePlayerModelType(modelTypeName);
    }

    public static String modelTypeName(PlayerModelType modelType) {
        return modelType != null ? modelType.name() : "";
    }

    public static String entityTypeId(EntityType<?> entityType) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString();
    }

    public void copyAttributesFrom(CharacterStory source) {
        dialogData = source.dialogData;
        entityType = source.entityType;
        modelType = source.modelType;
        customNbt = source.customNbt;
        mainCharacterAttribute = source.mainCharacterAttribute;
    }

    public DialogData getDialogData() {
        return dialogData;
    }

    @Override
    public Scene getScene() {
        return null;
    }

    @Override
    public File getSkinFile() {
        File charactersFolder = NarrativeCraftFileUtil.getCharactersFolder();
        File characterFolder = new File(charactersFolder, toFileName());
        if (!characterFolder.exists()) return null;

        return new File(characterFolder, NarrativeCraftFileDefault.SKIN_CHARACTER_FILE);
    }

    public void setDialogData(DialogData dialogData) {
        this.dialogData = dialogData;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType<?> entityType) {
        this.entityType = entityType;
    }

    public PlayerModelType getModelType() {
        if (modelType == null) {
            return PlayerModelType.WIDE;
        }
        return modelType;
    }

    public void setModelType(PlayerModelType modelType) {
        this.modelType = modelType;
    }

    public CharacterType getCharacterType() {
        return characterType;
    }

    public String getCustomNbt() {
        return customNbt;
    }

    public void setCustomNbt(String customNbt) {
        this.customNbt = customNbt;
    }

    public MainCharacterAttribute getMainCharacterAttribute() {
        return mainCharacterAttribute;
    }

    public boolean isMainCharacter() {
        return mainCharacterAttribute.isMainCharacter();
    }

    public void setMainCharacterAttribute(MainCharacterAttribute mainCharacterAttribute) {
        this.mainCharacterAttribute = mainCharacterAttribute;
    }

    @Override
    public CharacterStoryPayload toPayload() {
        return new CharacterStoryPayload(
                name,
                dialogData,
                modelTypeName(modelType),
                entityTypeId(entityType),
                customNbt,
                mainCharacterAttribute);
    }

    @Override
    public String formattedName() {
        return mainCharacterAttribute.isMainCharacter() ? "[M] " + name : name;
    }

    @Override
    public String toFileName() {
        return getNormalizedName();
    }
}
