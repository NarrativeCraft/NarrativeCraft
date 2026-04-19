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

package fr.loudo.narrativecraft.events.client;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.cameraangle.ClientCameraAngleMakerEditor;
import fr.loudo.narrativecraft.client.editors.cutscene.ClientCutsceneMakerEditor;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.dialog.DialogRenderer2D;
import fr.loudo.narrativecraft.editors.Editor;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class OnHudRender {

    public static void cutsceneHudRender(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Editor editor = ClientNarrativeCraftMod.getInstance().getPlayerSession().getEditor();
        if (editor instanceof ClientCutsceneMakerEditor cutsceneEditor) {
            cutsceneEditor.render(graphics, deltaTracker);
        }
    }

    public static void cameraAngleHudRender(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Editor editor = ClientNarrativeCraftMod.getInstance().getPlayerSession().getEditor();
        if (editor instanceof ClientCameraAngleMakerEditor cameraAngleEditor) {
            cameraAngleEditor.render(graphics, deltaTracker);
        }
    }

    public static void dialogHudRender(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        List<DialogRenderer2D> dialogs = session.getActiveDialog2DRenderers();
        for (DialogRenderer2D dialog : dialogs) {
            dialog.render(graphics, deltaTracker);
        }
    }
}
