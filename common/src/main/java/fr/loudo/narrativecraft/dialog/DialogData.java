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

package fr.loudo.narrativecraft.dialog;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.resources.ResourceLocation;

public class DialogData {

    public enum TextAlignment {
        LEFT,
        CENTER,
        RIGHT
    }

    private float offsetX = 0f;
    private float offsetY = 0.5f;

    private float ancOffsetX = 0f;
    private float ancOffsetY = 0f;

    private float width = 120f;
    private float paddingX = 4f;
    private float paddingY = 4f;
    private float scale = 1f;
    private float letterSpacing = 0f;
    private float lineGap = 2f;
    private float bobbingNoiseShakeSpeed = 2.3f;
    private float bobbingNoiseShakeStrength = 2.5f;

    private int backgroundColor = 0xCC000000;
    private int textColor = 0xFFFFFFFF;
    private ResourceLocation backgroundImage = null;

    private float scrollSpeed = 0;
    private ResourceLocation letterSound =
            ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "sfx.dialog_sound");
    private boolean soundMuted = false;

    private boolean tailVisible = true;
    private boolean autoSkipEnabled = false;
    private float autoSkipSeconds = 3f;
    private boolean textShadow = false;

    private TextAlignment textAlignment = TextAlignment.LEFT;

    public DialogData() {}

    public DialogData(DialogData source) {
        this.offsetX = source.offsetX;
        this.offsetY = source.offsetY;
        this.ancOffsetX = source.ancOffsetX;
        this.ancOffsetY = source.ancOffsetY;
        this.width = source.width;
        this.paddingX = source.paddingX;
        this.paddingY = source.paddingY;
        this.scale = source.scale;
        this.letterSpacing = source.letterSpacing;
        this.lineGap = source.lineGap;
        this.backgroundColor = source.backgroundColor;
        this.textColor = source.textColor;
        this.backgroundImage = source.backgroundImage;
        this.scrollSpeed = source.scrollSpeed;
        this.letterSound = source.letterSound;
        this.soundMuted = source.soundMuted;
        this.tailVisible = source.tailVisible;
        this.autoSkipEnabled = source.autoSkipEnabled;
        this.autoSkipSeconds = source.autoSkipSeconds;
        this.textAlignment = source.textAlignment;
        this.textShadow = source.textShadow;
        this.bobbingNoiseShakeSpeed = source.bobbingNoiseShakeSpeed;
        this.bobbingNoiseShakeStrength = source.bobbingNoiseShakeStrength;
    }

    public void copyFrom(DialogData source) {
        this.offsetX = source.offsetX;
        this.offsetY = source.offsetY;
        this.ancOffsetX = source.ancOffsetX;
        this.ancOffsetY = source.ancOffsetY;
        this.width = source.width;
        this.paddingX = source.paddingX;
        this.paddingY = source.paddingY;
        this.scale = source.scale;
        this.letterSpacing = source.letterSpacing;
        this.lineGap = source.lineGap;
        this.backgroundColor = source.backgroundColor;
        this.textColor = source.textColor;
        this.backgroundImage = source.backgroundImage;
        this.scrollSpeed = source.scrollSpeed;
        this.letterSound = source.letterSound;
        this.soundMuted = source.soundMuted;
        this.tailVisible = source.tailVisible;
        this.autoSkipEnabled = source.autoSkipEnabled;
        this.autoSkipSeconds = source.autoSkipSeconds;
        this.textAlignment = source.textAlignment;
        this.textShadow = source.textShadow;
        this.bobbingNoiseShakeSpeed = source.bobbingNoiseShakeSpeed;
        this.bobbingNoiseShakeStrength = source.bobbingNoiseShakeStrength;
    }

    public static DialogData resolve(DialogData global, DialogData character, DialogData cameraView) {
        DialogData result = from(from(global, character), cameraView);
        if (character != null) {
            result.backgroundColor = character.backgroundColor;
            result.textColor = character.textColor;
            if (character.backgroundImage != null) {
                result.backgroundImage = character.backgroundImage;
            }
            result.letterSound = character.letterSound;
        }
        if (cameraView != null) {
            result.offsetX = cameraView.offsetX;
            result.offsetY = cameraView.offsetY;
            result.ancOffsetX = cameraView.ancOffsetX;
            result.ancOffsetY = cameraView.ancOffsetY;
            result.scale = cameraView.scale;
        }
        return result;
    }

    public static DialogData from(DialogData preset, DialogData overrides) {
        DialogData base = preset != null ? new DialogData(preset) : new DialogData();
        if (overrides == null) return base;

        DialogData defaults = new DialogData();
        if (overrides.offsetX != defaults.offsetX) base.offsetX = overrides.offsetX;
        if (overrides.offsetY != defaults.offsetY) base.offsetY = overrides.offsetY;
        if (overrides.ancOffsetX != defaults.ancOffsetX) base.ancOffsetX = overrides.ancOffsetX;
        if (overrides.ancOffsetY != defaults.ancOffsetY) base.ancOffsetY = overrides.ancOffsetY;
        if (overrides.width != defaults.width) base.width = overrides.width;
        if (overrides.paddingX != defaults.paddingX) base.paddingX = overrides.paddingX;
        if (overrides.paddingY != defaults.paddingY) base.paddingY = overrides.paddingY;
        if (overrides.scale != defaults.scale) base.scale = overrides.scale;
        if (overrides.letterSpacing != defaults.letterSpacing) base.letterSpacing = overrides.letterSpacing;
        if (overrides.lineGap != defaults.lineGap) base.lineGap = overrides.lineGap;
        if (overrides.backgroundColor != defaults.backgroundColor) base.backgroundColor = overrides.backgroundColor;
        if (overrides.textColor != defaults.textColor) base.textColor = overrides.textColor;
        if (overrides.backgroundImage != null) base.backgroundImage = overrides.backgroundImage;
        if (overrides.scrollSpeed != defaults.scrollSpeed) base.scrollSpeed = overrides.scrollSpeed;
        if (overrides.letterSound != null) base.letterSound = overrides.letterSound;
        if (overrides.soundMuted != defaults.soundMuted) base.soundMuted = overrides.soundMuted;
        if (overrides.tailVisible != defaults.tailVisible) base.tailVisible = overrides.tailVisible;
        if (overrides.autoSkipEnabled != defaults.autoSkipEnabled) base.autoSkipEnabled = overrides.autoSkipEnabled;
        if (overrides.autoSkipSeconds != defaults.autoSkipSeconds) base.autoSkipSeconds = overrides.autoSkipSeconds;
        if (overrides.textAlignment != defaults.textAlignment) base.textAlignment = overrides.textAlignment;
        if (overrides.textShadow != defaults.textShadow) base.textShadow = overrides.textShadow;
        if (overrides.bobbingNoiseShakeSpeed != defaults.bobbingNoiseShakeSpeed)
            base.bobbingNoiseShakeSpeed = overrides.bobbingNoiseShakeSpeed;
        if (overrides.bobbingNoiseShakeStrength != defaults.bobbingNoiseShakeStrength)
            base.bobbingNoiseShakeStrength = overrides.bobbingNoiseShakeStrength;
        return base;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public DialogData setOffsetX(float offsetX) {
        this.offsetX = offsetX;
        return this;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public DialogData setOffsetY(float offsetY) {
        this.offsetY = offsetY;
        return this;
    }

    public float getAncOffsetX() {
        return ancOffsetX;
    }

    public DialogData setAncOffsetX(float ancOffsetX) {
        this.ancOffsetX = ancOffsetX;
        return this;
    }

    public float getAncOffsetY() {
        return ancOffsetY;
    }

    public DialogData setAncOffsetY(float ancOffsetY) {
        this.ancOffsetY = ancOffsetY;
        return this;
    }

    public float getWidth() {
        return width;
    }

    public DialogData setWidth(float width) {
        this.width = width;
        return this;
    }

    public float getPaddingX() {
        return paddingX;
    }

    public DialogData setPaddingX(float paddingX) {
        this.paddingX = paddingX;
        return this;
    }

    public float getPaddingY() {
        return paddingY;
    }

    public DialogData setPaddingY(float paddingY) {
        this.paddingY = paddingY;
        return this;
    }

    public float getScale() {
        return scale;
    }

    public DialogData setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public float getLetterSpacing() {
        return letterSpacing;
    }

    public DialogData setLetterSpacing(float letterSpacing) {
        this.letterSpacing = letterSpacing;
        return this;
    }

    public float getLineGap() {
        return lineGap;
    }

    public DialogData setLineGap(float lineGap) {
        this.lineGap = lineGap;
        return this;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public DialogData setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public int getTextColor() {
        return textColor;
    }

    public DialogData setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    public ResourceLocation getBackgroundImage() {
        return backgroundImage;
    }

    public DialogData setBackgroundImage(ResourceLocation backgroundImage) {
        this.backgroundImage = backgroundImage;
        return this;
    }

    public float getScrollSpeed() {
        return scrollSpeed;
    }

    public DialogData setScrollSpeed(float scrollSpeed) {
        this.scrollSpeed = scrollSpeed;
        return this;
    }

    public ResourceLocation getLetterSound() {
        return letterSound;
    }

    public DialogData setLetterSound(ResourceLocation letterSound) {
        this.letterSound = letterSound;
        return this;
    }

    public boolean isSoundMuted() {
        return soundMuted;
    }

    public DialogData setSoundMuted(boolean soundMuted) {
        this.soundMuted = soundMuted;
        return this;
    }

    public boolean isTailVisible() {
        return tailVisible;
    }

    public DialogData setTailVisible(boolean tailVisible) {
        this.tailVisible = tailVisible;
        return this;
    }

    public boolean isAutoSkipEnabled() {
        return autoSkipEnabled;
    }

    public DialogData setAutoSkipEnabled(boolean autoSkipEnabled) {
        this.autoSkipEnabled = autoSkipEnabled;
        return this;
    }

    public float getAutoSkipSeconds() {
        return autoSkipSeconds;
    }

    public DialogData setAutoSkipSeconds(float autoSkipSeconds) {
        this.autoSkipSeconds = autoSkipSeconds;
        return this;
    }

    public TextAlignment getTextAlignment() {
        return textAlignment;
    }

    public DialogData setTextAlignment(TextAlignment textAlignment) {
        this.textAlignment = textAlignment;
        return this;
    }

    public boolean isTextShadow() {
        return textShadow;
    }

    public DialogData setTextShadow(boolean textShadow) {
        this.textShadow = textShadow;
        return this;
    }

    public float getBobbingNoiseShakeSpeed() {
        return bobbingNoiseShakeSpeed;
    }

    public DialogData setBobbingNoiseShakeSpeed(float bobbingNoiseShakeSpeed) {
        this.bobbingNoiseShakeSpeed = bobbingNoiseShakeSpeed;
        return this;
    }

    public float getBobbingNoiseShakeStrength() {
        return bobbingNoiseShakeStrength;
    }

    public DialogData setBobbingNoiseShakeStrength(float bobbingNoiseShakeStrength) {
        this.bobbingNoiseShakeStrength = bobbingNoiseShakeStrength;
        return this;
    }
}
