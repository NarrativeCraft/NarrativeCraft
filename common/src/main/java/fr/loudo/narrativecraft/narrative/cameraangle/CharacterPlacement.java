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

import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class CharacterPlacement {

    private final UUID id;
    private final ICharacterStory characterStory;
    private Vec3 position;
    private Vec3 rotation;
    private final List<ItemStack> items;
    private final boolean isTemplate;
    private final UUID templateReferenceId;
    private Pose pose = Pose.STANDING;

    public CharacterPlacement(
            UUID id,
            ICharacterStory characterStory,
            Vec3 position,
            Vec3 rotation,
            List<ItemStack> items,
            boolean isTemplate,
            UUID templateReferenceId) {
        this.id = id;
        this.characterStory = characterStory;
        this.position = position;
        this.rotation = rotation;
        this.items = items;
        this.isTemplate = isTemplate;
        this.templateReferenceId = templateReferenceId;
    }

    public CharacterPlacement(
            UUID id, ICharacterStory characterStory, Vec3 position, Vec3 rotation, List<ItemStack> items) {
        this(id, characterStory, position, rotation, items, false, null);
    }

    public CharacterPlacement(ICharacterStory characterStory, Vec3 position, Vec3 rotation, List<ItemStack> items) {
        this(UUID.randomUUID(), characterStory, position, rotation, items, false, null);
    }

    public UUID getId() {
        return id;
    }

    public ICharacterStory getCharacterStory() {
        return characterStory;
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

    public List<ItemStack> getItems() {
        return items;
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
