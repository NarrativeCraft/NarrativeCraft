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

package fr.loudo.narrativecraft.client.screens.narrative.cameraangle;

import fr.loudo.narrativecraft.client.editors.cameraangle.ClientCameraAngleMakerEditorMaker.TemplatePick;
import fr.loudo.narrativecraft.client.screens.AbstractNarrativeEntryPickerScreen;
import fr.loudo.narrativecraft.narrative.cameraangle.TemplateSourceType;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;

public class CameraAngleTemplatePickerScreen
        extends AbstractNarrativeEntryPickerScreen<CameraAngleTemplatePickerScreen.Entry> {

    public CameraAngleTemplatePickerScreen(Scene scene, Screen lastScreen, Consumer<TemplatePick> onPick) {
        super(
                Translation.message("screen.camera_angle_editor.add_template"),
                buildEntries(scene),
                lastScreen,
                entry -> onPick.accept(new TemplatePick(entry.sourceType(), entry.refId())));
    }

    private static List<Entry> buildEntries(Scene scene) {
        List<Entry> entries = new ArrayList<>();
        scene.getAnimationManager()
                .getList()
                .forEach(animation -> entries.add(
                        new Entry(TemplateSourceType.ANIMATION, animation.getId(), "[A] " + animation.getName())));
        scene.getSubsceneManager()
                .getList()
                .forEach(subscene -> entries.add(
                        new Entry(TemplateSourceType.SUBSCENE, subscene.getId(), "[S] " + subscene.getName())));
        scene.getCutsceneManager()
                .getList()
                .forEach(cutscene -> entries.add(
                        new Entry(TemplateSourceType.CUTSCENE, cutscene.getId(), "[C] " + cutscene.getName())));
        return entries;
    }

    @Override
    protected String getItemName(Entry item) {
        return item.displayName();
    }

    public record Entry(TemplateSourceType sourceType, UUID refId, String displayName) {}
}
