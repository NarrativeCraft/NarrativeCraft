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

package fr.loudo.narrativecraft.narrative.inkTag.actions;

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.EasingType;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Interpolation;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.utils.FadeState;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public abstract class OverlayInkAction extends InkAction {

    public enum Position {
        TOP_LEFT,
        TOP,
        TOP_RIGHT,
        MIDDLE_LEFT,
        MIDDLE,
        MIDDLE_RIGHT,
        BOTTOM_LEFT,
        BOTTOM,
        BOTTOM_RIGHT
    }

    public record AnimationPoint(@Nullable Position position, Vec2 offset) {}

    protected String overlayId;
    protected String action;

    protected Position position = Position.MIDDLE;
    protected float scale = 1.0f;
    protected float opacity = 1.0f;
    protected Vec2 space = new Vec2(0, 0);
    protected boolean noRemove = false;

    @Nullable
    protected FadeState fadeState;

    protected float fadeInSeconds;
    protected float staySeconds = -1;
    protected float fadeOutSeconds;

    @Nullable
    protected AnimationPoint animationFrom;

    @Nullable
    protected AnimationPoint animationTo;

    protected EasingType animationEasing = EasingType.LINEAR;
    protected double animationSeconds;
    protected int animationTick;
    protected int animationTotalTick;

    @Nullable
    protected OverlayInkAction monitoredAnimation;

    @Nullable
    protected OverlayInkAction monitoredFade;

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        overlayId = cmd.getString("id").toLowerCase();
        action = cmd.getString("action").toLowerCase();

        String value1 = cmd.getString("param1");
        String value2 = cmd.getString("param2");
        String value3 = cmd.getString("param3");
        String value4 = cmd.getString("param4");

        InkActionResult sharedResult = validateSharedProperty(cmd, value1, value2, value3, value4);
        if (sharedResult != null) return sharedResult;

        return validateOwnProperty(cmd, scene, value1, value2, value3);
    }

    protected abstract InkActionResult validateOwnProperty(
            ParsedCommand cmd, IScene scene, String value1, String value2, String value3);

    @Nullable
    protected InkActionResult validateSharedProperty(
            ParsedCommand cmd, String value1, String value2, String value3, String value4) {
        switch (action) {
            case "remove" -> {
                return InkActionResult.singleOk();
            }
            case "position", "pos" -> {
                if (value1 == null || value1.isEmpty()) {
                    return InkActionResult.error("'position' requires a position value (value1)");
                }
                try {
                    position = Position.valueOf(value1.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return InkActionResult.error("Invalid position '" + value1 + "'");
                }
            }
            case "space" -> {
                if (value1 == null || value1.isEmpty()) {
                    return InkActionResult.error("'space' requires a 'x' value (value1)");
                }
                if (value2 == null || value2.isEmpty()) {
                    return InkActionResult.error("'space' requires a 'y' value (value2)");
                }
                try {
                    space = new Vec2(Float.parseFloat(value1), Float.parseFloat(value2));
                } catch (IllegalArgumentException e) {
                    return InkActionResult.error(String.format("Invalid x or y position x:%s y:%s", value1, value2));
                }
            }
            case "opacity" -> {
                if (value1 == null || value1.isEmpty()) {
                    return InkActionResult.error("'opacity' requires a float value (value1)");
                }
                try {
                    opacity = Float.parseFloat(value1);
                } catch (NumberFormatException e) {
                    return InkActionResult.error("Invalid opacity value '" + value1 + "'");
                }
            }
            case "scale" -> {
                if (value1 == null || value1.isEmpty()) {
                    return InkActionResult.error("'scale' requires a float value (value1)");
                }
                try {
                    scale = Float.parseFloat(value1);
                } catch (NumberFormatException e) {
                    return InkActionResult.error("Invalid scale value '" + value1 + "'");
                }
            }
            case "fade" -> {
                if (value1 == null || value2 == null || value3 == null) {
                    return InkActionResult.error("'fade' requires fadeIn, stay, and fadeOut values");
                }
                try {
                    fadeInSeconds = Float.parseFloat(value1);
                    staySeconds = Float.parseFloat(value2);
                    fadeOutSeconds = Float.parseFloat(value3);
                } catch (NumberFormatException e) {
                    return InkActionResult.error("Invalid fade values");
                }
                fadeState = FadeState.FADE_IN;
                totalTick = (int) (fadeInSeconds * 20.0);
                if (cmd.flag("block")) blocking = true;
            }
            case "fadein" -> {
                if (value1 == null || value1.isEmpty()) {
                    return InkActionResult.error("'fadein' requires a duration value (value1)");
                }
                try {
                    fadeInSeconds = Float.parseFloat(value1);
                } catch (NumberFormatException e) {
                    return InkActionResult.error("Invalid fadein value '" + value1 + "'");
                }
                staySeconds = -1;
                fadeState = FadeState.FADE_IN;
                totalTick = (int) (fadeInSeconds * 20.0);
                if (cmd.flag("block")) blocking = true;
            }
            case "fadeout" -> {
                if (value1 == null || value1.isEmpty()) {
                    return InkActionResult.error("'fadeout' requires a duration value (value1)");
                }
                try {
                    fadeOutSeconds = Float.parseFloat(value1);
                } catch (NumberFormatException e) {
                    return InkActionResult.error("Invalid fadeout value '" + value1 + "'");
                }
                fadeState = FadeState.FADE_OUT;
                totalTick = (int) (fadeOutSeconds * 20.0);
                if (cmd.flag("block")) blocking = true;
            }
            case "animate" -> {
                if (value1 == null || value1.isEmpty()) {
                    return InkActionResult.error("'animate' requires a 'from' value (value1)");
                }
                if (value2 == null || value2.isEmpty()) {
                    return InkActionResult.error("'animate' requires a 'to' value (value2)");
                }
                if (value3 == null || value3.isEmpty()) {
                    return InkActionResult.error("'animate' requires a duration in seconds (value3)");
                }
                animationFrom = parseAnimationPoint(value1);
                if (animationFrom == null) {
                    return InkActionResult.error(animationPointError("from", value1));
                }
                animationTo = parseAnimationPoint(value2);
                if (animationTo == null) {
                    return InkActionResult.error(animationPointError("to", value2));
                }
                try {
                    animationSeconds = Double.parseDouble(value3);
                } catch (NumberFormatException e) {
                    return InkActionResult.error("Invalid animate seconds value '" + value3 + "'");
                }
                if (animationSeconds < 0) {
                    return InkActionResult.error("'animate' seconds must be positive");
                }
                if (value4 != null && !value4.isEmpty()) {
                    try {
                        animationEasing = EasingType.valueOf(value4.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return InkActionResult.error("Invalid easing '" + value4 + "'");
                    }
                } else {
                    animationEasing = EasingType.LINEAR;
                }
                animationTick = 0;
                animationTotalTick = (int) (animationSeconds * 20.0);
                if (cmd.flag("block")) blocking = true;
            }
            default -> {
                return null;
            }
        }
        return InkActionResult.ok();
    }

    @Nullable
    protected static AnimationPoint parseAnimationPoint(String rawValue) {
        String value = rawValue.trim();

        int separatorIndex = value.indexOf(',');
        if (separatorIndex >= 0) {
            String rawX = value.substring(0, separatorIndex).trim();
            String rawY = value.substring(separatorIndex + 1).trim();
            if (rawY.indexOf(',') >= 0) return null;
            try {
                return new AnimationPoint(null, new Vec2(-Float.parseFloat(rawX), -Float.parseFloat(rawY)));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        try {
            return new AnimationPoint(Position.valueOf(value.toUpperCase()), Vec2.ZERO);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String animationPointError(String name, String rawValue) {
        return "Invalid animate '" + name + "' value '" + rawValue + "' (expected \"x,y\" or a position name)";
    }

    protected boolean applySharedProperty(OverlayInkAction existing) {
        switch (action) {
            case "remove" -> existing.stop();
            case "position", "pos" -> existing.position = position;
            case "space" -> existing.space = space;
            case "opacity" -> existing.opacity = opacity;
            case "scale" -> existing.scale = scale;
            case "fade" -> {
                existing.fadeState = fadeState;
                existing.fadeInSeconds = fadeInSeconds;
                existing.staySeconds = staySeconds;
                existing.fadeOutSeconds = fadeOutSeconds;
                existing.totalTick = totalTick;
                existing.tick = 0;
                if (blocking) monitoredFade = existing;
            }
            case "fadein" -> {
                existing.fadeState = FadeState.FADE_IN;
                existing.fadeInSeconds = fadeInSeconds;
                existing.staySeconds = -1;
                existing.totalTick = (int) (fadeInSeconds * 20.0);
                existing.tick = 0;
                if (blocking) monitoredFade = existing;
            }
            case "fadeout" -> {
                existing.fadeState = FadeState.FADE_OUT;
                existing.fadeOutSeconds = fadeOutSeconds;
                existing.totalTick = (int) (fadeOutSeconds * 20.0);
                existing.tick = 0;
                if (blocking) monitoredFade = existing;
            }
            case "animate" -> {
                existing.animationFrom = animationFrom;
                existing.animationTo = animationTo;
                existing.animationEasing = animationEasing;
                existing.animationSeconds = animationSeconds;
                existing.animationTotalTick = animationTotalTick;
                existing.animationTick = 0;
                if (blocking) monitoredAnimation = existing;
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public void tick() {
        if (!isRunning()) return;
        if (tickMonitoredOverlay()) return;
        tickFade();
        tickAnimation();
    }

    protected boolean tickMonitoredOverlay() {
        if (monitoredAnimation != null) {
            if (!monitoredAnimation.isAnimating()) stop();
            return true;
        }
        if (monitoredFade != null) {
            if (!monitoredFade.isFading()) stop();
            return true;
        }
        return false;
    }

    public boolean isFading() {
        return isRunning() && fadeState != null;
    }

    protected void tickAnimation() {
        if (animationFrom == null || animationTick >= animationTotalTick) return;
        animationTick++;
    }

    public boolean isAnimating() {
        return animationFrom != null && animationTick < animationTotalTick;
    }

    protected void tickFade() {
        tick++;
        if (tick < totalTick || fadeState == null) return;

        tick = 0;
        switch (fadeState) {
            case FADE_IN -> {
                if (staySeconds >= 0) {
                    fadeState = FadeState.STAY;
                    totalTick = (int) (staySeconds * 20.0);
                } else {
                    fadeState = null;
                }
            }
            case STAY -> {
                fadeState = FadeState.FADE_OUT;
                totalTick = (int) (fadeOutSeconds * 20.0);
            }
            case FADE_OUT -> {
                fadeState = null;
                if (!noRemove) {
                    stop();
                }
            }
        }
    }

    protected float computeOpacity(float partialTick) {
        if (fadeState == null) return opacity;
        double progress = Mth.clamp((tick + partialTick) / (float) totalTick, 0.0, 1.0);
        return switch (fadeState) {
            case FADE_IN -> (float) Interpolation.lerp(0.0, opacity, progress);
            case STAY -> opacity;
            case FADE_OUT -> (float) Interpolation.lerp(opacity, 0.0, progress);
        };
    }

    protected float[] computeOrigin(int guiWidth, int guiHeight, float contentWidth, float contentHeight) {
        return computeOrigin(position, guiWidth, guiHeight, contentWidth, contentHeight);
    }

    protected float[] computeOrigin(
            Position anchor, int guiWidth, int guiHeight, float contentWidth, float contentHeight) {
        float scaledWidth = contentWidth * scale;
        float scaledHeight = contentHeight * scale;

        float originX =
                switch (anchor) {
                    case TOP_LEFT, MIDDLE_LEFT, BOTTOM_LEFT -> 0;
                    case TOP, MIDDLE, BOTTOM -> (guiWidth - scaledWidth) / 2f;
                    case TOP_RIGHT, MIDDLE_RIGHT, BOTTOM_RIGHT -> guiWidth - scaledWidth;
                };

        float originY =
                switch (anchor) {
                    case TOP_LEFT, TOP, TOP_RIGHT -> 0;
                    case MIDDLE_LEFT, MIDDLE, MIDDLE_RIGHT -> (guiHeight - scaledHeight) / 2f;
                    case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> guiHeight - scaledHeight;
                };

        return new float[] {originX, originY};
    }

    protected float[] computeAnimationOffset(
            int guiWidth,
            int guiHeight,
            float contentWidth,
            float contentHeight,
            float offsetScale,
            float partialTick) {
        if (animationFrom == null || animationTo == null) return new float[] {0, 0};

        float[] fromDelta =
                computeAnimationDelta(animationFrom, guiWidth, guiHeight, contentWidth, contentHeight, offsetScale);
        float[] toDelta =
                computeAnimationDelta(animationTo, guiWidth, guiHeight, contentWidth, contentHeight, offsetScale);

        double progress = animationTotalTick <= 0
                ? 1.0
                : Mth.clamp((animationTick + partialTick) / (float) animationTotalTick, 0.0, 1.0);
        double easedProgress = Interpolation.applyEasing(animationEasing, progress);

        return new float[] {
            (float) Interpolation.lerp(fromDelta[0], toDelta[0], easedProgress),
            (float) Interpolation.lerp(fromDelta[1], toDelta[1], easedProgress)
        };
    }

    private float[] computeAnimationDelta(
            AnimationPoint point,
            int guiWidth,
            int guiHeight,
            float contentWidth,
            float contentHeight,
            float offsetScale) {
        float deltaX = point.offset().x * offsetScale;
        float deltaY = point.offset().y * offsetScale;

        if (point.position() != null) {
            float[] base = computeOrigin(position, guiWidth, guiHeight, contentWidth, contentHeight);
            float[] target = computeOrigin(point.position(), guiWidth, guiHeight, contentWidth, contentHeight);
            deltaX += target[0] - base[0];
            deltaY += target[1] - base[1];
        }

        return new float[] {deltaX, deltaY};
    }

    @Nullable
    protected static <T extends OverlayInkAction> T findActiveById(
            Class<T> type, String id, List<?> actions, String originalAction) {
        for (Object action : actions) {
            if (!type.isInstance(action)) continue;
            T overlayAction = type.cast(action);
            if (overlayAction.overlayId != null
                    && overlayAction.overlayId.equalsIgnoreCase(id)
                    && overlayAction.action.equals(originalAction)) {
                return overlayAction;
            }
        }
        return null;
    }

    public String getOverlayId() {
        return overlayId;
    }

    public String getAction() {
        return action;
    }
}
