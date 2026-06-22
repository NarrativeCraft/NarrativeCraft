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

package fr.loudo.narrativecraft.events.client;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.cameraangle.ClientCameraAngleMakerEditorMaker;
import fr.loudo.narrativecraft.client.editors.cutscene.ClientCutsceneMakerEditorMaker;
import fr.loudo.narrativecraft.client.editors.dialog.ClientCharacterDialogEditorMaker;
import fr.loudo.narrativecraft.client.editors.dialog.ClientGlobalDialogEditorMaker;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.editors.EditorMaker;
import net.minecraft.client.input.MouseButtonEvent;

public class OnScreenMouseDragEvent {

    public static void onCutsceneTimelineDrag(MouseButtonEvent mouseButtonEvent, double dragX, double dragY) {
        ClientPlayerSession playerSession =
                ClientNarrativeCraftMod.getInstance().getPlayerSession();
        EditorMaker editorMaker = playerSession.getEditor();
        if (editorMaker instanceof ClientCutsceneMakerEditorMaker cutsceneEditor) {
            cutsceneEditor.mouseDragged(mouseButtonEvent, dragX, dragY);
        } else if (editorMaker instanceof ClientCameraAngleMakerEditorMaker cameraAngleEditor) {
            cameraAngleEditor.mouseDragged(mouseButtonEvent, dragX, dragY);
        } else if (editorMaker instanceof ClientGlobalDialogEditorMaker globalDialogEditor) {
            globalDialogEditor.mouseDragged(mouseButtonEvent, dragX, dragY);
        } else if (editorMaker instanceof ClientCharacterDialogEditorMaker characterDialogEditor) {
            characterDialogEditor.mouseDragged(mouseButtonEvent, dragX, dragY);
        }
    }
}
