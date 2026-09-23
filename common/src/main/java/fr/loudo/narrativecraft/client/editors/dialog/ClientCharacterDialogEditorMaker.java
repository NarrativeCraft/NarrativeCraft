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

package fr.loudo.narrativecraft.client.editors.dialog;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.dialog.DialogRenderer3D;
import fr.loudo.narrativecraft.client.editors.widgets.DialogPreviewEntry;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class ClientCharacterDialogEditorMaker implements EditorMaker {

    private static final String DEFAULT_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

    private final Minecraft minecraft = Minecraft.getInstance();
    private final ClientPlayerSession playerSession =
            ClientNarrativeCraftMod.getInstance().getPlayerSession();
    private final ICharacterStory character;
    private final Runnable onSave;
    private final DialogData working;

    private DialogRenderer3D renderer;
    private DialogData rendererData;
    private DialogPreviewEntry previewEntry;
    private boolean stopping = false;

    public ClientCharacterDialogEditorMaker(ICharacterStory character, Runnable onSave) {
        this.character = character;
        this.onSave = onSave;
        this.working = new DialogData(character.getDialogData());
    }

    @Override
    public void init() {}

    public void registerEntityId(int entityId) {
        if (minecraft.level == null) return;
        Entity entity = minecraft.level.getEntity(entityId);
        if (entity == null) return;
        rendererData =
                new DialogData(DialogData.from(NarrativeCraftMod.getInstance().getGlobalDialogData(), working));
        renderer = new DialogRenderer3D(rendererData, entity);
        renderer.onStopped(() -> playerSession.removeDialog3D(renderer));
        renderer.start(DEFAULT_TEXT);
        playerSession.addDialog3D(renderer);
        previewEntry = new DialogPreviewEntry(character.getName(), working, renderer);
    }

    @Override
    public void close() {
        if (stopping) return;
        stopping = true;
        if (renderer != null) {
            renderer.stop();
            renderer = null;
        }
        minecraft.gui.setScreen(null);
        playerSession.getCharactersInWorld().values().remove(character);
    }

    @Override
    public void tick() {
        if (rendererData != null) {
            rendererData.copyFrom(
                    DialogData.from(NarrativeCraftMod.getInstance().getGlobalDialogData(), working));
        }
    }

    @Override
    public NarrativeEnvironment getEnvironment() {
        return NarrativeEnvironment.DEVELOPMENT;
    }

    public void save() {
        character.setDialogData(working);
        onSave.run();
    }

    public void quit(boolean saveBeforeQuit) {
        if (saveBeforeQuit) {
            save();
        }
        playerSession.requestEditorClose();
    }

    public ICharacterStory getCharacter() {
        return character;
    }

    public DialogData getWorking() {
        return working;
    }

    public DialogPreviewEntry getPreviewEntry() {
        return previewEntry;
    }
}
