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

package fr.loudo.narrativecraft.client.narrative.cameraangle;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.cameraangle.ClientCameraAngleMakerEditorMaker;
import fr.loudo.narrativecraft.client.narrative.ui.ClientNarrativeUIAction;
import fr.loudo.narrativecraft.client.screens.AbstractNarrativeEntryEditScreen;
import fr.loudo.narrativecraft.client.screens.narrative.cameraangle.CameraAngleEntryEditScreen;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngle;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.network.cameraangle.BiCameraAngleEnter;
import fr.loudo.narrativecraft.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class ClientCameraAngleNarrativeUIAction implements ClientNarrativeUIAction<CameraAngle> {

    @Override
    public Screen subListSubScreen(CameraAngle entry, Screen parent) {
        return null;
    }

    @Override
    public boolean isClickable() {
        return true;
    }

    @Override
    public void customClickAction(CameraAngle entry) {
        Services.PACKET.sendToServer(new BiCameraAngleEnter(entry, NarrativeEnvironment.DEVELOPMENT));
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        ClientCameraAngleMakerEditorMaker cameraAngleEditor = new ClientCameraAngleMakerEditorMaker(entry);
        cameraAngleEditor.init();
        session.setEditor(cameraAngleEditor);
        Minecraft.getInstance().gui.setScreen(null);
    }

    @Override
    public AbstractNarrativeEntryEditScreen<CameraAngle> showEditScreen(CameraAngle entry, Screen lastScreen) {
        return new CameraAngleEntryEditScreen(entry, lastScreen);
    }

    @Override
    public AbstractNarrativeEntryEditScreen<CameraAngle> showCreateScreen(NarrativeEntry<?> parent, Screen lastScreen) {
        if (!(parent instanceof Scene scene)) return null;
        return new CameraAngleEntryEditScreen(scene, lastScreen);
    }
}
