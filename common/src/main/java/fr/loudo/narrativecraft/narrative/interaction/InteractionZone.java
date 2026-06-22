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

package fr.loudo.narrativecraft.narrative.interaction;

import fr.loudo.narrativecraft.api.narrative.interaction.IInteractionZone;
import java.util.UUID;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class InteractionZone implements IInteractionZone {

    private final UUID id;
    private String name;
    private String stitchName;
    private Vec3 corner1;
    private Vec3 corner2;
    private boolean oneTime;

    public InteractionZone(UUID id, String name, String stitchName, Vec3 corner1, Vec3 corner2, boolean oneTime) {
        this.id = id;
        this.name = name;
        this.stitchName = stitchName;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.oneTime = oneTime;
    }

    public InteractionZone(String name, String stitchName, Vec3 corner1, Vec3 corner2) {
        this(UUID.randomUUID(), name, stitchName, corner1, corner2, false);
    }

    public AABB toAABB() {
        return new AABB(corner1, corner2);
    }

    public Vec3 center() {
        return corner1.add(corner2).scale(0.5);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStitchName() {
        return stitchName;
    }

    public void setStitchName(String stitchName) {
        this.stitchName = stitchName;
    }

    public Vec3 getCorner1() {
        return corner1;
    }

    public void setCorner1(Vec3 corner1) {
        this.corner1 = corner1;
    }

    public Vec3 getCorner2() {
        return corner2;
    }

    public void setCorner2(Vec3 corner2) {
        this.corner2 = corner2;
    }

    public boolean isOneTime() {
        return oneTime;
    }

    public void setOneTime(boolean oneTime) {
        this.oneTime = oneTime;
    }
}
