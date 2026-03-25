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

package fr.loudo.narrativecraft.screens.narrative.cutscene;

import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.screens.AbstractNarrativeEntryEditScreen;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.screens.Screen;

public class CutsceneEntryEditScreen extends AbstractNarrativeEntryEditScreen<Cutscene> {

    private final Scene scene;

    public CutsceneEntryEditScreen(Cutscene entry, Screen lastScreen) {
        super(entry, lastScreen);
        this.scene = entry.getScene();
    }

    public CutsceneEntryEditScreen(Scene scene, Screen lastScreen) {
        super(null, lastScreen);
        this.scene = scene;
    }

    @Override
    protected boolean hasValidated() {
        if (!super.hasValidated()) {
            return false;
        }

        Cutscene cutscene = scene.getCutsceneManager().getByName(getName());
        if (cutscene != null) {
            sendToastError(
                    Translation.message("error"),
                    Translation.message(
                            "error.already_exists",
                            Translation.message("cutscene").getString(),
                            cutscene.getName()));
            return false;
        }

        return true;
    }

    @Override
    protected void addCustomFields() {}

    @Override
    protected Cutscene createInstance() {
        return new Cutscene(getName(), getDescription(), scene);
    }
}
