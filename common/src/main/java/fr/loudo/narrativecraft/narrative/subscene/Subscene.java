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

package fr.loudo.narrativecraft.narrative.subscene;

import fr.loudo.narrativecraft.api.narrative.subscene.ISubscene;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Subscene extends NarrativeEntry<SubscenePayload> implements ISubscene {

    private final Scene scene;
    private List<Animation> animations;

    public Subscene(UUID id, String name, String description, Scene scene, List<Animation> animations) {
        super(id, name, description);
        this.scene = scene;
        this.animations = new ArrayList<>(animations);
    }

    public Subscene(UUID id, String name, String description, Scene scene) {
        super(id, name, description);
        this.scene = scene;
        this.animations = new ArrayList<>();
    }

    public Subscene(String name, String description, Scene scene) {
        super(name, description);
        this.scene = scene;
        this.animations = new ArrayList<>();
    }

    public Scene getScene() {
        return scene;
    }

    public List<Animation> getAnimations() {
        return animations;
    }

    public void setAnimations(List<Animation> animations) {
        this.animations = new ArrayList<>(animations);
    }

    @Override
    public String formattedName() {
        return name;
    }

    @Override
    public String toFileName() {
        return name.replace(" ", "_").toLowerCase() + ".json";
    }

    @Override
    public SubscenePayload toPayload() {
        List<UUID> animationIds = animations.stream().map(Animation::getId).toList();
        return new SubscenePayload(
                name, description, scene.getId(), scene.getChapter().getId(), animationIds);
    }
}
