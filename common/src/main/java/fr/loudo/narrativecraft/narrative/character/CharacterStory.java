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

import com.google.gson.Gson;
import fr.loudo.narrativecraft.client.editors.widgets.DialogFieldSet;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.dialog.DialogDataIO;
import fr.loudo.narrativecraft.files.NarrativeCraftFileDefault;
import fr.loudo.narrativecraft.files.NarrativeCraftFileUtil;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import java.io.File;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;

public class CharacterStory extends NarrativeEntry<CharacterStoryPayload> implements ICharacterStory {

    private final CharacterType characterType = CharacterType.NORMAL;
    private DialogData dialogData = new DialogData();
    private EntityType<?> entityType = EntityType.PLAYER;
    private PlayerModelType modelType;
    private MainCharacterAttribute mainCharacterAttribute = new MainCharacterAttribute();

    public CharacterStory(String name, String description) {
        super(name, description);
    }

    public CharacterStory(UUID id, String name, String description) {
        super(id, name, description);
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

    public MainCharacterAttribute getMainCharacterAttribute() {
        return mainCharacterAttribute;
    }

    public void setMainCharacterAttribute(MainCharacterAttribute mainCharacterAttribute) {
        this.mainCharacterAttribute = mainCharacterAttribute;
    }

    @Override
    public CharacterStoryPayload toPayload() {
        String modelTypeName = modelType != null ? modelType.name() : "";
        String entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString();
        String dialogDataJson = new Gson().toJson(DialogDataIO.serialize(dialogData, DialogFieldSet.CHARACTER));
        return new CharacterStoryPayload(
                name, description, modelTypeName, entityTypeId, mainCharacterAttribute, dialogDataJson);
    }

    @Override
    public String formattedName() {
        return mainCharacterAttribute.isMainCharacter() ? "[M] " + name : name;
    }

    @Override
    public String toFileName() {
        return name.toLowerCase().replace(" ", "_");
    }
}
