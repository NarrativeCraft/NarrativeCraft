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

package fr.loudo.narrativecraft.narrative.animation;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.recording.Recording;
import fr.loudo.narrativecraft.recording.actions.AbstractAction;

import java.util.*;

public class Animation extends NarrativeEntry<AnimationPayload> {

    private final Scene scene;
    private Map<Integer, List<AbstractAction>> actions = new HashMap<>();

    public Animation(UUID id, String name, String description, Scene scene) {
        super(id, name, description);
        this.scene = scene;
    }

    public Animation(String name, String description, Scene scene) {
        super(name, description);
        this.scene = scene;
    }

    public Animation(String name, Scene scene) {
        super(name, "");
        this.scene = scene;
    }

    public Animation(UUID id, String name, Scene scene) {
        super(id, name, "");
        this.scene = scene;
    }

    @Override
    public AnimationPayload toPayload() {
        return new AnimationPayload(
                name, description, scene.getId(), scene.getChapter().getId());
    }

    @Override
    public String formattedName() {
        return name;
    }

    @Override
    public String toFileName() {
        return name.replace(" ", "_").toLowerCase() + Recording.RECORDING_EXTENSION;
    }

    public Scene getScene() {
        return scene;
    }

    public Map<Integer, List<AbstractAction>> getActions() {
        return actions;
    }

    public void setActions(Map<Integer, List<AbstractAction>> actions) {
        this.actions = actions;
    }
}
