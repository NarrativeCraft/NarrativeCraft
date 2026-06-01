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

package fr.loudo.narrativecraft.narrative.interaction;

import fr.loudo.narrativecraft.api.narrative.interaction.IInteraction;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Interaction extends NarrativeEntry<InteractionPayload> implements IInteraction {

    private final Scene scene;
    private final List<InteractionZone> zones = new ArrayList<>();
    private final List<InteractionPoint> points = new ArrayList<>();

    public Interaction(UUID id, String name, String description, Scene scene) {
        super(id, name, description);
        this.scene = scene;
    }

    public Interaction(String name, String description, Scene scene) {
        super(name, description);
        this.scene = scene;
    }

    public Scene getScene() {
        return scene;
    }

    public List<InteractionZone> getZones() {
        return zones;
    }

    public List<InteractionPoint> getPoints() {
        return points;
    }

    @Override
    public InteractionPayload toPayload() {
        return new InteractionPayload(
                name, description, scene.getId(), scene.getChapter().getId());
    }

    @Override
    public String formattedName() {
        return name;
    }

    @Override
    public String toFileName() {
        return name.replace(" ", "_").toLowerCase() + ".json";
    }
}
