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

package fr.loudo.narrativecraft.client.screens.narrative.character;

import fr.loudo.narrativecraft.client.screens.AbstractNarrativeEntryEditScreen;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

public class CharacterEntryEditScreen extends AbstractNarrativeEntryEditScreen<CharacterStory> {

    private EntityType<?> selectedEntityType;

    public CharacterEntryEditScreen(Screen lastScreen) {
        super(lastScreen);
        this.selectedEntityType = EntityType.PLAYER;
    }

    public CharacterEntryEditScreen(CharacterStory entry, Screen lastScreen) {
        super(entry, lastScreen);
        this.selectedEntityType = entry.getEntityType();
    }

    @Override
    protected void addCustomFields() {
        String entityTypeLabel =
                BuiltInRegistries.ENTITY_TYPE.getKey(selectedEntityType).toString();
        Button entityTypeButton = Button.builder(
                        Component.literal(Translation.message("screen.character.entity_type")
                                        .getString() + ": " + entityTypeLabel),
                        b -> minecraft.setScreen(
                                new EntityTypePickerScreen(this, picked -> selectedEntityType = picked)))
                .size(GLOBAL_WIDTH, 20)
                .build();
        addElementToWidgetsList(entityTypeButton);
    }

    @Override
    protected CharacterStory createInstance() {
        CharacterStory character = new CharacterStory(getName(), getDescription());
        character.setEntityType(selectedEntityType);
        return character;
    }
}
