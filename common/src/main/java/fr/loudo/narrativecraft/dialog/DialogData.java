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

package fr.loudo.narrativecraft.dialog;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.resources.Identifier;

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

    private float width = 80f;
    private float paddingX = 4f;
    private float paddingY = 4f;
    private float scale = 1f;
    private float letterSpacing = 0f;
    private float lineGap = 2f;

    private int backgroundColor = 0xCC000000;
    private int textColor = 0xFFFFFFFF;
    private Identifier backgroundImage = null;

    private float scrollSpeed = 0;
    private Identifier letterSound = Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "sfx.dialog_sound");
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
        return base;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    public float getAncOffsetX() {
        return ancOffsetX;
    }

    public void setAncOffsetX(float ancOffsetX) {
        this.ancOffsetX = ancOffsetX;
    }

    public float getAncOffsetY() {
        return ancOffsetY;
    }

    public void setAncOffsetY(float ancOffsetY) {
        this.ancOffsetY = ancOffsetY;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getPaddingX() {
        return paddingX;
    }

    public void setPaddingX(float paddingX) {
        this.paddingX = paddingX;
    }

    public float getPaddingY() {
        return paddingY;
    }

    public void setPaddingY(float paddingY) {
        this.paddingY = paddingY;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getLetterSpacing() {
        return letterSpacing;
    }

    public void setLetterSpacing(float letterSpacing) {
        this.letterSpacing = letterSpacing;
    }

    public float getLineGap() {
        return lineGap;
    }

    public void setLineGap(float lineGap) {
        this.lineGap = lineGap;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public Identifier getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(Identifier backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public float getScrollSpeed() {
        return scrollSpeed;
    }

    public void setScrollSpeed(float scrollSpeed) {
        this.scrollSpeed = scrollSpeed;
    }

    public Identifier getLetterSound() {
        return letterSound;
    }

    public void setLetterSound(Identifier letterSound) {
        this.letterSound = letterSound;
    }

    public boolean isSoundMuted() {
        return soundMuted;
    }

    public void setSoundMuted(boolean soundMuted) {
        this.soundMuted = soundMuted;
    }

    public boolean isTailVisible() {
        return tailVisible;
    }

    public void setTailVisible(boolean tailVisible) {
        this.tailVisible = tailVisible;
    }

    public boolean isAutoSkipEnabled() {
        return autoSkipEnabled;
    }

    public void setAutoSkipEnabled(boolean autoSkipEnabled) {
        this.autoSkipEnabled = autoSkipEnabled;
    }

    public float getAutoSkipSeconds() {
        return autoSkipSeconds;
    }

    public void setAutoSkipSeconds(float autoSkipSeconds) {
        this.autoSkipSeconds = autoSkipSeconds;
    }

    public TextAlignment getTextAlignment() {
        return textAlignment;
    }

    public void setTextAlignment(TextAlignment textAlignment) {
        this.textAlignment = textAlignment;
    }

    public boolean isTextShadow() {
        return textShadow;
    }

    public void setTextShadow(boolean textShadow) {
        this.textShadow = textShadow;
    }
}
