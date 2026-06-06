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

package fr.loudo.narrativecraft.client.screens.narrative.animation;

import fr.loudo.narrativecraft.client.screens.AbstractNarrativeEntryEditScreen;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import net.minecraft.client.gui.screens.Screen;

public class AnimationEntryEditScreen extends AbstractNarrativeEntryEditScreen<Animation> {

    private final Scene scene;

    public AnimationEntryEditScreen(Animation entry, Screen lastScreen, Scene scene) {
        super(entry, lastScreen);
        this.scene = scene;
    }

    public AnimationEntryEditScreen(Animation entry, Screen lastScreen) {
        super(entry, lastScreen);
        this.scene = entry.getScene();
    }

    public AnimationEntryEditScreen(Scene scene, Screen lastScreen) {
        super(null, lastScreen);
        this.scene = scene;
    }

    @Override
    protected boolean showDescription() {
        return false;
    }

    @Override
    protected void addCustomFields() {}

    @Override
    protected Animation createInstance() {
        if (entry == null) return null;
        Animation animation = scene.getAnimationManager().getById(entry.getId());
        animation.setName(getName());
        return animation;
    }
}
