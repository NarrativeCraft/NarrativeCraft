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

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.dialog.ITextEffect;
import fr.loudo.narrativecraft.client.gui.GuiGraphicsExtractorExtension;
import fr.loudo.narrativecraft.dialog.effects.WaitTextEffect;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

public class DialogScrollText {

    private static final Pattern OPEN_TAG = Pattern.compile("\\[(\\w+)([^]]*)]");
    private static final Pattern CLOSE_TAG = Pattern.compile("\\[/(\\w+)]");
    private static final Pattern PARAM = Pattern.compile("(\\w+)=(\\S+)");

    private final List<LetterEntry> letters = new ArrayList<>();
    private int visibleCount = 0;
    private float tickAccumulator = 0f;
    private int waitTicksRemaining = 0;
    private long currentTick = 0;

    public void setText(String rawText) {
        letters.clear();
        visibleCount = 0;
        tickAccumulator = 0f;
        waitTicksRemaining = 0;
        parseText(rawText);
    }

    public void tick(float scrollSpeed) {
        currentTick++;
        if (visibleCount >= letters.size()) return;

        if (waitTicksRemaining > 0) {
            waitTicksRemaining--;
            return;
        }

        tickAccumulator += scrollSpeed;
        while (tickAccumulator >= 1f && visibleCount < letters.size()) {
            LetterEntry entry = letters.get(visibleCount);
            entry.visible = true;
            visibleCount++;
            tickAccumulator -= 1f;

            // Check if this letter is a wait trigger
            if (entry.effectName != null && entry.effectName.equals("wait")) {
                waitTicksRemaining = WaitTextEffect.getWaitTicks(entry.params);
                break;
            }

            if (entry.soundCallback != null) entry.soundCallback.run();
        }
    }

    public void forceFinish() {
        for (LetterEntry entry : letters) {
            entry.visible = true;
        }
        visibleCount = letters.size();
        tickAccumulator = 0f;
    }

    public boolean isFinished() {
        return visibleCount >= letters.size();
    }

    public int getVisibleCount() {
        return visibleCount;
    }

    private List<float[]> computeLetterPositionsRaw(
            float originX, float originY, float maxWidth, Font font, DialogData data) {
        List<float[]> positions = new ArrayList<>(letters.size());
        for (int i = 0; i < letters.size(); i++) positions.add(new float[] {originX, originY});

        float x = originX;
        float y = originY;
        int i = 0;

        while (i < letters.size()) {
            String letter = letters.get(i).letter;

            if (letter.isEmpty()) {
                positions.get(i)[0] = x;
                positions.get(i)[1] = y;
                i++;
                continue;
            }

            if (letter.equals("\n")) {
                positions.get(i)[0] = x;
                positions.get(i)[1] = y;
                x = originX;
                y += font.lineHeight + data.getLineGap();
                i++;
                continue;
            }

            if (letter.equals(" ")) {
                positions.get(i)[0] = x;
                positions.get(i)[1] = y;
                x += font.width(" ") + data.getLetterSpacing();
                i++;
                continue;
            }

            // Measure the whole word starting at i
            int wordEnd = i;
            float wordWidth = 0f;
            while (wordEnd < letters.size()
                    && !letters.get(wordEnd).letter.equals(" ")
                    && !letters.get(wordEnd).letter.equals("\n")) {
                if (wordEnd > i) wordWidth += data.getLetterSpacing();
                wordWidth += font.width(letters.get(wordEnd).letter);
                wordEnd++;
            }

            // Wrap if the word doesn't fit and we're not already at the start of a line
            if (x > originX && (x - originX) + wordWidth > maxWidth) {
                x = originX;
                y += font.lineHeight + data.getLineGap();
            }

            // Place each letter of the word
            for (int k = i; k < wordEnd; k++) {
                positions.get(k)[0] = x;
                positions.get(k)[1] = y;
                x += font.width(letters.get(k).letter) + data.getLetterSpacing();
            }

            i = wordEnd;
        }

        return positions;
    }

    public List<float[]> computeLetterPositions(
            float originX, float originY, float maxWidth, Font font, DialogData data) {
        List<float[]> positions = computeLetterPositionsRaw(originX, originY, maxWidth, font, data);

        DialogData.TextAlignment alignment = data.getTextAlignment();
        if (alignment != DialogData.TextAlignment.LEFT) {
            Map<Float, Float> rowRightEdge = new LinkedHashMap<>();
            for (int index = 0; index < letters.size(); index++) {
                LetterEntry entry = letters.get(index);
                if (entry.letter.isEmpty() || entry.letter.equals("\n") || entry.letter.equals(" ")) continue;
                float[] letterPosition = positions.get(index);
                float rightEdge = (letterPosition[0] - originX) + font.width(entry.letter);
                rowRightEdge.merge(letterPosition[1], rightEdge, Math::max);
            }
            float maxLineWidth =
                    rowRightEdge.values().stream().max(Float::compareTo).orElse(0f);
            for (int index = 0; index < letters.size(); index++) {
                LetterEntry entry = letters.get(index);
                if (entry.letter.isEmpty() || entry.letter.equals("\n") || entry.letter.equals(" ")) continue;
                float[] letterPosition = positions.get(index);
                float lineWidth = rowRightEdge.getOrDefault(letterPosition[1], 0f);
                float offset =
                        switch (alignment) {
                            case CENTER -> (maxLineWidth - lineWidth) / 2f;
                            case RIGHT -> maxLineWidth - lineWidth;
                            default -> 0f;
                        };
                letterPosition[0] += offset;
            }
        }

        return positions;
    }

    public float[] computeTextDimensions(float maxWidth, Font font, DialogData data) {
        List<float[]> positions = computeLetterPositionsRaw(0f, 0f, maxWidth, font, data);
        float maxX = 0f;
        float maxY = 0f;
        for (int i = 0; i < letters.size(); i++) {
            LetterEntry entry = letters.get(i);
            if (entry.letter.isEmpty() || entry.letter.equals("\n")) continue;
            float[] pos = positions.get(i);
            float right = pos[0] + font.width(entry.letter);
            float bottom = pos[1] + font.lineHeight;
            if (right > maxX) maxX = right;
            if (bottom > maxY) maxY = bottom;
        }
        return new float[] {maxX, maxY};
    }

    public void render2D(
            GuiGraphicsExtractor graphics, float originX, float originY, DialogData data, float partialTick) {
        Font font = Minecraft.getInstance().font;
        List<float[]> positions = computeLetterPositions(originX, originY, data.getWidth(), font, data);

        for (int i = 0; i < letters.size(); i++) {
            LetterEntry entry = letters.get(i);
            if (!entry.visible) break;
            if (entry.letter.isEmpty() || entry.letter.equals("\n") || entry.letter.equals(" ")) continue;

            float[] pos = positions.get(i);
            Vec2 offset = getEffectOffset(entry, i, partialTick);
            int color = applyOpacity(data.getTextColor(), 1f);

            GuiGraphicsExtractorExtension graphicsExtension = new GuiGraphicsExtractorExtension(graphics);
            graphicsExtension.text(font, entry.letter, pos[0] + offset.x, pos[1] + offset.y, color, false);
        }
    }

    public void render3D(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float originX,
            float originY,
            DialogData data,
            float partialTick) {
        Font font = Minecraft.getInstance().font;
        List<float[]> positions = computeLetterPositions(originX, originY, data.getWidth(), font, data);
        Matrix4f matrix = poseStack.last().pose();

        for (int i = 0; i < letters.size(); i++) {
            LetterEntry entry = letters.get(i);
            if (!entry.visible) break;
            if (entry.letter.isEmpty() || entry.letter.equals("\n") || entry.letter.equals(" ")) continue;

            float[] pos = positions.get(i);
            Vec2 offset = getEffectOffset(entry, i, partialTick);
            int color = applyOpacity(data.getTextColor(), 1f);

            font.drawInBatch(
                    entry.letter,
                    pos[0] + offset.x,
                    pos[1] + offset.y,
                    color,
                    false,
                    matrix,
                    bufferSource,
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    0xF000F0);
        }
    }

    private void parseText(String rawText) {
        String currentEffect = null;
        Map<String, String> currentParams = new HashMap<>();
        int i = 0;

        while (i < rawText.length()) {
            Matcher openMatcher = OPEN_TAG.matcher(rawText.substring(i));
            Matcher closeMatcher = CLOSE_TAG.matcher(rawText.substring(i));

            if (openMatcher.lookingAt()) {
                String effectName = openMatcher.group(1).toLowerCase();
                Map<String, String> params = parseParams(openMatcher.group(2));

                if (effectName.equals("wait")) {
                    // Insert an invisible pause marker at this exact position.
                    // The letters that follow are NOT tagged with "wait" — they scroll normally.
                    letters.add(new LetterEntry("", "wait", params));
                } else {
                    currentEffect = effectName;
                    currentParams = params;
                }

                i += openMatcher.end();
                continue;
            }

            if (closeMatcher.lookingAt()) {
                currentEffect = null;
                currentParams = new HashMap<>();
                i += closeMatcher.end();
                continue;
            }

            char c = rawText.charAt(i);
            String letter = (c == '\n') ? "\n" : String.valueOf(c);
            letters.add(new LetterEntry(letter, currentEffect, new HashMap<>(currentParams)));
            i++;
        }
    }

    private Map<String, String> parseParams(String paramString) {
        Map<String, String> params = new HashMap<>();
        if (paramString == null || paramString.isBlank()) return params;
        Matcher matcher = PARAM.matcher(paramString);
        while (matcher.find()) {
            params.put(matcher.group(1).toLowerCase(), matcher.group(2));
        }
        return params;
    }

    private Vec2 getEffectOffset(LetterEntry entry, int letterIndex, float partialTick) {
        if (entry.effectName == null) return Vec2.ZERO;
        ITextEffect effect =
                NarrativeCraftMod.getInstance().getTextEffectRegistry().get(entry.effectName);
        if (effect == null) return Vec2.ZERO;
        return effect.apply(letterIndex, currentTick, partialTick, entry.params);
    }

    private int applyOpacity(int color, float opacity) {
        int alpha = (int) (ARGB.alpha(color) * opacity);
        return ARGB.color(alpha, ARGB.red(color), ARGB.green(color), ARGB.blue(color));
    }

    private static class LetterEntry {
        final String letter;
        final String effectName;
        final Map<String, String> params;
        boolean visible = false;
        Runnable soundCallback;

        LetterEntry(String letter, String effectName, Map<String, String> params) {
            this.letter = letter;
            this.effectName = effectName;
            this.params = params;
        }
    }
}
