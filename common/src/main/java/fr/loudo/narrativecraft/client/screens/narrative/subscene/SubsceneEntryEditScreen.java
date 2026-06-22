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

package fr.loudo.narrativecraft.client.screens.narrative.subscene;

import fr.loudo.narrativecraft.client.screens.AbstractNarrativeEntryEditScreen;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.screens.Screen;

public class SubsceneEntryEditScreen extends AbstractNarrativeEntryEditScreen<Subscene> {

    private final Scene scene;

    public SubsceneEntryEditScreen(Subscene entry, Screen lastScreen) {
        super(entry, lastScreen);
        this.scene = entry.getScene();
    }

    public SubsceneEntryEditScreen(Scene scene, Screen lastScreen) {
        super(null, lastScreen);
        this.scene = scene;
    }

    @Override
    protected boolean hasValidated() {
        if (!super.hasValidated()) {
            return false;
        }

        Subscene subscene = scene.getSubsceneManager().getByName(getName());
        if (subscene != null) {
            sendToastError(
                    Translation.message("error"),
                    Translation.message(
                            "error.already_exists",
                            Translation.message("subscene").getString(),
                            subscene.getName()));
            return false;
        }

        return true;
    }

    @Override
    protected void addCustomFields() {}

    @Override
    protected Subscene createInstance() {
        if (entry == null) return new Subscene(getName(), getDescription(), scene);
        Subscene subscene = scene.getSubsceneManager().getById(entry.getId());
        subscene.setName(getName());
        subscene.setDescription(getDescription());
        return subscene;
    }
}
