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

package fr.loudo.narrativecraft.client.screens.narrative.character;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.dialog.ClientCharacterDialogEditorMaker;
import fr.loudo.narrativecraft.client.screens.AbstractNarrativeEntryEditScreen;
import fr.loudo.narrativecraft.client.screens.ClearScreen;
import fr.loudo.narrativecraft.narrative.character.PlayerModelType;
import fr.loudo.narrativecraft.narrative.npc.Npc;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.NarrativeEntryAction;
import fr.loudo.narrativecraft.network.dialog.C2SEnterDialogEditor;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.CustomFont;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

public class NpcEntryEditScreen extends AbstractNarrativeEntryEditScreen<Npc> {

    private final Scene scene;
    private EntityType<?> selectedEntityType;
    private PlayerModelType selectedModelType;

    public NpcEntryEditScreen(Scene scene, Screen lastScreen) {
        super(lastScreen);
        this.scene = scene;
        this.selectedEntityType = EntityType.PLAYER;
        this.selectedModelType = PlayerModelType.WIDE;
    }

    public NpcEntryEditScreen(Npc entry, Screen lastScreen) {
        super(entry, lastScreen);
        this.scene = entry.getScene();
        this.selectedEntityType = entry.getEntityType();
        this.selectedModelType = entry.getModelType() != null ? entry.getModelType() : PlayerModelType.WIDE;
    }

    @Override
    protected boolean hasValidated() {
        if (!super.hasValidated()) {
            return false;
        }

        Npc existing = scene.getNpcManager().getByName(getName());
        if (existing != null && (entry == null || !existing.getId().equals(entry.getId()))) {
            sendToastError(
                    Translation.message("error"),
                    Translation.message(
                            "error.already_exists", Translation.message("npc").getString(), existing.getName()));
            return false;
        }

        return true;
    }

    @Override
    protected boolean showDescription() {
        return false;
    }

    @Override
    protected void addCustomFields() {
        String entityTypeLabel =
                BuiltInRegistries.ENTITY_TYPE.getKey(selectedEntityType).toString();
        Button entityTypeButton = Button.builder(
                        Component.literal(Translation.message("entity_type").getString() + ": " + entityTypeLabel),
                        b -> minecraft.setScreen(
                                new EntityTypePickerScreen(this, picked -> selectedEntityType = picked)))
                .size(GLOBAL_WIDTH, 20)
                .build();
        addElementToWidgetsList(entityTypeButton);

        Button modelTypeButton = Button.builder(
                        Translation.message("screen.character.model_type", selectedModelType.name()), b -> {
                            selectedModelType = selectedModelType == PlayerModelType.WIDE
                                    ? PlayerModelType.SLIM
                                    : PlayerModelType.WIDE;
                            b.setMessage(Translation.message("screen.character.model_type", selectedModelType.name()));
                        })
                .size(GLOBAL_WIDTH, 20)
                .build();
        addElementToWidgetsList(modelTypeButton);
    }

    @Override
    protected void init() {
        super.init();
        if (entry != null) {
            Npc target = entry;
            Button dialogEditorButton = Button.builder(Component.literal(CustomFont.DIALOG), b -> {
                        ClientCharacterDialogEditorMaker editor = new ClientCharacterDialogEditorMaker(
                                target,
                                () -> Services.PACKET.sendToServer(new BiSyncNarrativeEntryPacket(
                                        target.getId(), target.toPayload(), NarrativeEntryAction.EDIT)));
                        editor.init();
                        ClientNarrativeCraftMod.getInstance().getPlayerSession().setEditor(editor);
                        minecraft.setScreen(new ClearScreen());
                        Services.PACKET.sendToServer(
                                new C2SEnterDialogEditor("npc", target.getId().toString()));
                    })
                    .bounds(sendButton.getX() + sendButton.getWidth() + 5, sendButton.getY(), 20, 20)
                    .build();
            dialogEditorButton.setTooltip(Tooltip.create(Translation.message("tooltip.dialog_editor")));
            addRenderableWidget(dialogEditorButton);
        }
    }

    @Override
    protected Npc createInstance() {
        if (entry == null) return new Npc(getName(), scene);
        Npc npc = scene.getNpcManager().getById(entry.getId());
        npc.setName(getName());
        npc.setEntityType(selectedEntityType);
        npc.setModelType(selectedModelType);
        return npc;
    }
}
