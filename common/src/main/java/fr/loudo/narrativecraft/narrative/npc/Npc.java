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

package fr.loudo.narrativecraft.narrative.npc;

import fr.loudo.narrativecraft.files.NarrativeCraftFileDefault;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.character.CharacterType;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;

public class Npc extends NarrativeEntry<NpcPayload> implements ICharacterStory {

    private final Scene scene;
    private final CharacterType characterType = CharacterType.NPC;
    private String dialogPresetName;
    private EntityType<?> entityType = EntityType.PLAYER;
    private PlayerModelType modelType;

    public Npc(UUID id, String name, Scene scene) {
        super(id, name, "");
        this.scene = scene;
    }

    public Npc(String name, Scene scene) {
        super(name, "");
        this.scene = scene;
    }

    public Scene getScene() {
        return scene;
    }

    public String getDialogPresetName() {
        return dialogPresetName;
    }

    public void setDialogPresetName(String dialogPresetName) {
        this.dialogPresetName = dialogPresetName;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType<?> entityType) {
        this.entityType = entityType;
    }

    public PlayerModelType getModelType() {
        return modelType;
    }

    public void setModelType(PlayerModelType modelType) {
        this.modelType = modelType;
    }

    @Override
    public CharacterType getCharacterType() {
        return characterType;
    }

    @Override
    public NpcPayload toPayload() {
        String modelTypeName = modelType != null ? modelType.name() : "";
        String entityTypeIdStr =
                BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString();
        String dialogPresetNameStr = dialogPresetName != null ? dialogPresetName : "";
        return new NpcPayload(
                name,
                dialogPresetNameStr,
                modelTypeName,
                entityTypeIdStr,
                scene.getId(),
                scene.getChapter().getId());
    }

    @Override
    public String formattedName() {
        return name;
    }

    @Override
    public String toFileName() {
        return name.toLowerCase().replace(" ", "_") + NarrativeCraftFileDefault.EXTENSION_DATA_FILE;
    }
}
