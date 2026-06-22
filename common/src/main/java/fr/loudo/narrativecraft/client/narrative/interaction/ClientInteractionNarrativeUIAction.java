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

package fr.loudo.narrativecraft.client.narrative.interaction;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.interaction.ClientInteractionMakerEditorMaker;
import fr.loudo.narrativecraft.client.narrative.ui.ClientNarrativeUIAction;
import fr.loudo.narrativecraft.client.screens.AbstractNarrativeEntryEditScreen;
import fr.loudo.narrativecraft.client.screens.narrative.interaction.InteractionEntryEditScreen;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.interaction.Interaction;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.network.interaction.BiInteractionEnter;
import fr.loudo.narrativecraft.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class ClientInteractionNarrativeUIAction implements ClientNarrativeUIAction<Interaction> {

    @Override
    public Screen subListSubScreen(Interaction entry, Screen parent) {
        return null;
    }

    @Override
    public boolean isClickable() {
        return true;
    }

    @Override
    public void customClickAction(Interaction entry) {
        Services.PACKET.sendToServer(new BiInteractionEnter(entry, NarrativeEnvironment.DEVELOPMENT));
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        ClientInteractionMakerEditorMaker interactionEditor = new ClientInteractionMakerEditorMaker(entry);
        interactionEditor.init();
        session.setEditor(interactionEditor);
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public AbstractNarrativeEntryEditScreen<Interaction> showEditScreen(Interaction entry, Screen lastScreen) {
        return new InteractionEntryEditScreen(entry, lastScreen);
    }

    @Override
    public AbstractNarrativeEntryEditScreen<Interaction> showCreateScreen(NarrativeEntry<?> parent, Screen lastScreen) {
        if (!(parent instanceof Scene scene)) return null;
        return new InteractionEntryEditScreen(scene, lastScreen);
    }
}
