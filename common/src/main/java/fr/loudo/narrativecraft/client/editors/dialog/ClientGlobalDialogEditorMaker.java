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
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class ClientGlobalDialogEditorMaker implements EditorMaker {

    private static final String DEFAULT_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

    private final Minecraft minecraft = Minecraft.getInstance();
    private final ClientPlayerSession playerSession =
            ClientNarrativeCraftMod.getInstance().getPlayerSession();
    private final DialogData working =
            new DialogData(NarrativeCraftMod.getInstance().getGlobalDialogData());

    private DialogRenderer3D renderer;
    private DialogPreviewEntry previewEntry;
    private boolean stopping = false;

    @Override
    public void init() {}

    public void registerEntityId(int entityId) {
        if (minecraft.level == null) return;
        Entity entity = minecraft.level.getEntity(entityId);
        if (entity == null) return;
        renderer = new DialogRenderer3D(working, entity);
        renderer.onStopped(() -> playerSession.removeDialog3D(renderer));
        renderer.start(DEFAULT_TEXT);
        playerSession.addDialog3D(renderer);
        previewEntry = new DialogPreviewEntry("Global", working, renderer);
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
    }

    @Override
    public void tick() {}

    @Override
    public NarrativeEnvironment getEnvironment() {
        return NarrativeEnvironment.DEVELOPMENT;
    }

    public void save() {
        NarrativeCraftMod.getInstance().setGlobalDialogData(working);
        try {
            NarrativeCraftMod.getInstance().getFile().saveGlobalDialogData(working);
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to save global dialog data", e);
        }
    }

    public void quit(boolean saveBeforeQuit) {
        if (saveBeforeQuit) {
            save();
        }
        playerSession.requestEditorClose();
    }

    public DialogData getWorking() {
        return working;
    }

    public DialogPreviewEntry getPreviewEntry() {
        return previewEntry;
    }
}
