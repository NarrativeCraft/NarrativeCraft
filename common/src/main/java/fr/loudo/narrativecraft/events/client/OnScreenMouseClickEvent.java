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
import fr.loudo.narrativecraft.client.editors.cameraangle.ClientCameraAngleMakerEditorMaker;
import fr.loudo.narrativecraft.client.editors.cutscene.ClientCutsceneMakerEditorMaker;
import fr.loudo.narrativecraft.editors.EditorMaker;
import net.minecraft.client.input.MouseButtonEvent;

public class OnScreenMouseClickEvent {

    public static void cutsceneHudClick(MouseButtonEvent mouseButtonEvent, boolean isDoubleClick) {
        EditorMaker editorMaker =
                ClientNarrativeCraftMod.getInstance().getPlayerSession().getEditor();
        if (editorMaker instanceof ClientCutsceneMakerEditorMaker cutsceneEditor) {
            cutsceneEditor.mouseClicked(mouseButtonEvent, isDoubleClick);
        } else if (editorMaker instanceof ClientCameraAngleMakerEditorMaker cameraAngleEditor) {
            cameraAngleEditor.mouseClicked(mouseButtonEvent, isDoubleClick);
        }
    }

    public static void cutsceneHudRelease(MouseButtonEvent mouseButtonEvent) {
        EditorMaker editorMaker =
                ClientNarrativeCraftMod.getInstance().getPlayerSession().getEditor();
        if (editorMaker instanceof ClientCutsceneMakerEditorMaker cutsceneEditor) {
            cutsceneEditor.mouseReleased(mouseButtonEvent);
        } else if (editorMaker instanceof ClientCameraAngleMakerEditorMaker cameraAngleEditor) {
            cameraAngleEditor.mouseReleased(mouseButtonEvent);
        }
    }
}
