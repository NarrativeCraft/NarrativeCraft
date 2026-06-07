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

import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class InteractionPoint {

    public static final double DEFAULT_MAX_DISTANCE = 8.0;
    public static final double DEFAULT_AIM_RADIUS = 1.0;

    private final UUID id;
    private String name;
    private String stitchName;
    private Vec3 position;
    private double maxDistance;
    private boolean useAimRadius;
    private double aimRadius;
    private boolean neverShow;
    private boolean oneTimeClick;

    public InteractionPoint(
            UUID id,
            String name,
            String stitchName,
            Vec3 position,
            double maxDistance,
            boolean useAimRadius,
            double aimRadius,
            boolean neverShow,
            boolean oneTimeClick) {
        this.id = id;
        this.name = name;
        this.stitchName = stitchName;
        this.position = position;
        this.maxDistance = maxDistance;
        this.useAimRadius = useAimRadius;
        this.aimRadius = aimRadius;
        this.neverShow = neverShow;
        this.oneTimeClick = oneTimeClick;
    }

    public InteractionPoint(String name, String stitchName, Vec3 position) {
        this(
                UUID.randomUUID(),
                name,
                stitchName,
                position,
                DEFAULT_MAX_DISTANCE,
                true,
                DEFAULT_AIM_RADIUS,
                false,
                false);
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

    public Vec3 getPosition() {
        return position;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public boolean isUseAimRadius() {
        return useAimRadius;
    }

    public void setUseAimRadius(boolean useAimRadius) {
        this.useAimRadius = useAimRadius;
    }

    public double getAimRadius() {
        return aimRadius;
    }

    public void setAimRadius(double aimRadius) {
        this.aimRadius = aimRadius;
    }

    public boolean isNeverShow() {
        return neverShow;
    }

    public void setNeverShow(boolean neverShow) {
        this.neverShow = neverShow;
    }

    public boolean isOneTimeClick() {
        return oneTimeClick;
    }

    public void setOneTimeClick(boolean oneTimeClick) {
        this.oneTimeClick = oneTimeClick;
    }

    public boolean canSee(Player player) {
        if (player == null || position == null) return false;
        double distance = player.position().distanceTo(position);
        if (distance > maxDistance) return false;
        if (!useAimRadius) return true;
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookDir = player.getLookAngle();
        Vec3 toPoint = position.subtract(eyePos);
        double projection = toPoint.dot(lookDir);
        if (projection <= 0) return false;
        Vec3 perpendicular = toPoint.subtract(lookDir.scale(projection));
        return perpendicular.length() <= aimRadius;
    }

    public boolean canClick(Player player) {
        if (player == null || position == null) return false;
        double distance = player.position().distanceTo(position);
        return distance <= 3.0;
    }
}
