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

package fr.loudo.narrativecraft.client.narrative.cutscene;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.cutscene.ClientCutsceneMakerEditor;
import fr.loudo.narrativecraft.client.narrative.ui.ClientNarrativeUIAction;
import fr.loudo.narrativecraft.client.screens.AbstractNarrativeEntryEditScreen;
import fr.loudo.narrativecraft.client.screens.narrative.cutscene.CutsceneEntryEditScreen;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneEnter;
import fr.loudo.narrativecraft.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class ClientCutsceneNarrativeUIAction implements ClientNarrativeUIAction<Cutscene> {

    @Override
    public Screen subListSubScreen(Cutscene entry, Screen parent) {
        return null;
    }

    @Override
    public boolean isClickable() {
        return true;
    }

    @Override
    public void customClickAction(Cutscene entry) {
        Services.PACKET.sendToServer(new C2SCutsceneEnter(entry));
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        ClientCutsceneMakerEditor cutsceneEditor = new ClientCutsceneMakerEditor(entry);
        cutsceneEditor.init();
        session.setEditor(cutsceneEditor);
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public AbstractNarrativeEntryEditScreen<Cutscene> showEditScreen(Cutscene entry, Screen lastScreen) {
        return new CutsceneEntryEditScreen(entry, lastScreen);
    }

    @Override
    public AbstractNarrativeEntryEditScreen<Cutscene> showCreateScreen(NarrativeEntry<?> parent, Screen lastScreen) {
        if (!(parent instanceof Scene scene)) return null;
        return new CutsceneEntryEditScreen(scene, lastScreen);
    }
}
