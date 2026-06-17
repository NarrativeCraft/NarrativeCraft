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

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.dialog.ClientCharacterDialogEditorMaker;
import fr.loudo.narrativecraft.client.screens.ClearScreen;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.MainCharacterAttribute;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.NarrativeEntryAction;
import fr.loudo.narrativecraft.network.dialog.C2SEnterDialogEditor;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CharacterEntryEditScreen extends AbstractCharacterEntryEditScreen<CharacterStory> {

    private MainCharacterAttribute mainCharacterAttribute;

    public CharacterEntryEditScreen(Screen lastScreen) {
        super(lastScreen);
        this.mainCharacterAttribute = new MainCharacterAttribute();
    }

    public CharacterEntryEditScreen(CharacterStory entry, Screen lastScreen) {
        super(entry, lastScreen);
        this.mainCharacterAttribute = new MainCharacterAttribute(entry.getMainCharacterAttribute());
    }

    @Override
    protected boolean hasValidated() {
        if (!super.hasValidated()) {
            return false;
        }

        CharacterStory existing =
                ClientNarrativeCraftMod.getInstance().getCharacterManager().getByName(getName());
        if (existing != null && (entry == null || !existing.getId().equals(entry.getId()))) {
            sendToastError(
                    Translation.message("error"),
                    Translation.message(
                            "error.already_exists",
                            Translation.message("character").getString(),
                            existing.getName()));
            return false;
        }

        return true;
    }

    @Override
    protected void addCustomFields() {
        super.addCustomFields();
        Button mainCharacterSettingsButton = Button.builder(
                        Translation.message("screen.character.main_character_settings"),
                        b -> minecraft.gui.setScreen(new MainCharacterAttributeScreen(this, mainCharacterAttribute)))
                .size(GLOBAL_WIDTH, 20)
                .build();
        addElementToWidgetsList(mainCharacterSettingsButton);
    }

    @Override
    protected void init() {
        super.init();
        if (entry != null) {
            CharacterStory target = entry;
            Button dialogEditorButton = Button.builder(Component.literal("D"), b -> {
                        ClientCharacterDialogEditorMaker editor = new ClientCharacterDialogEditorMaker(
                                target,
                                () -> Services.PACKET.sendToServer(new BiSyncNarrativeEntryPacket(
                                        target.getId(), target.toPayload(), NarrativeEntryAction.EDIT)));
                        editor.init();
                        ClientNarrativeCraftMod.getInstance().getPlayerSession().setEditor(editor);
                        minecraft.gui.setScreen(new ClearScreen());
                        Services.PACKET.sendToServer(new C2SEnterDialogEditor(
                                "character", target.getId().toString()));
                    })
                    .bounds(sendButton.getX() + sendButton.getWidth() + 5, sendButton.getY(), 20, 20)
                    .build();
            addRenderableWidget(dialogEditorButton);
        }
    }

    @Override
    protected CharacterStory createInstance() {
        CharacterStory character = new CharacterStory(getName(), getDescription());
        character.setEntityType(selectedEntityType);
        character.setModelType(selectedModelType);
        character.setMainCharacterAttribute(mainCharacterAttribute);
        return character;
    }
}
