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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.dialog.ITextEffect;
import fr.loudo.narrativecraft.client.rendering.ImageTexture;
import fr.loudo.narrativecraft.dialog.effects.WaitTextEffect;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

public class DialogScrollText {

    private static final Pattern OPEN_TAG = Pattern.compile("\\[(\\w+)([^]]*)]");
    private static final Pattern CLOSE_TAG = Pattern.compile("\\[/(\\w+)]");
    private static final Pattern PARAM = Pattern.compile("(\\w+)=(\\S+)");

    private static final String IMAGE_TAG = "img";
    private static final float INLINE_IMAGE_LINE_RATIO = 2f;
    private static final float SIDE_IMAGE_MIN_LINE_RATIO = 2f;
    private static final float SIDE_IMAGE_MAX_LINE_RATIO = 3f;
    private static final float SIDE_IMAGE_GAP = 3f;
    private static final float SIDE_IMAGE_MAX_WIDTH_RATIO = 1f;

    private final List<LetterEntry> letters = new ArrayList<>();
    private final List<LetterEntry> leadingImages = new ArrayList<>();
    private final List<LetterEntry> trailingImages = new ArrayList<>();

    private int bodyStart = 0;
    private int bodyEnd = 0;

    private int visibleCount = 0;
    private float tickAccumulator = 0f;
    private int waitTicksRemaining = 0;
    private long currentTick = 0;
    private ResourceLocation sound;
    private boolean mutedSound;

    public DialogScrollText(ResourceLocation sound, boolean mutedSound) {
        this.sound = sound;
        this.mutedSound = mutedSound;
    }

    public void setText(String rawText) {
        letters.clear();
        leadingImages.clear();
        trailingImages.clear();
        visibleCount = 0;
        tickAccumulator = 0f;
        waitTicksRemaining = 0;
        parseText(rawText);
        splitSideImages();
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

            if (!mutedSound) {
                float pitch = 0.8F + new Random().nextFloat() * 0.4F;
                SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(sound);
                Minecraft.getInstance().player.playSound(soundEvent, 1.0F, pitch);
            }
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

    public boolean hasSideImages() {
        return !leadingImages.isEmpty() || !trailingImages.isEmpty();
    }

    public LayoutResult computeLayout(
            float flowOriginX, float flowOriginY, float maxWidth, float sideImageHeight, Font font, DialogData data) {
        int count = letters.size();
        float[] positionX = new float[count];
        float[] positionY = new float[count];
        int[] lineIndex = new int[count];
        Arrays.fill(lineIndex, -1);

        float flowMaxWidth = Math.max(1f, maxWidth);

        List<Float> lineHeights = new ArrayList<>();
        lineHeights.add((float) font.lineHeight);

        float x = flowOriginX;
        int line = 0;
        int index = bodyStart;

        while (index < bodyEnd) {
            LetterEntry entry = letters.get(index);

            if (entry.isLayoutSkipped()) {
                positionX[index] = x;
                lineIndex[index] = line;
                index++;
                continue;
            }

            if (entry.letter.equals("\n")) {
                positionX[index] = x;
                lineIndex[index] = line;
                x = flowOriginX;
                line++;
                lineHeights.add((float) font.lineHeight);
                index++;
                continue;
            }

            if (entry.letter.equals(" ")) {
                positionX[index] = x;
                lineIndex[index] = line;
                x += font.width(" ") + data.getLetterSpacing();
                index++;
                continue;
            }

            // Measure the whole word starting at index
            int wordEnd = index;
            float wordWidth = 0f;
            while (wordEnd < bodyEnd
                    && !letters.get(wordEnd).letter.equals(" ")
                    && !letters.get(wordEnd).letter.equals("\n")) {
                LetterEntry wordEntry = letters.get(wordEnd);
                if (!wordEntry.isLayoutSkipped()) {
                    if (wordEnd > index) wordWidth += data.getLetterSpacing();
                    wordWidth += wordEntry.width(font, data);
                }
                wordEnd++;
            }

            // Wrap if the word doesn't fit and we're not already at the start of a line
            if (x > flowOriginX && (x - flowOriginX) + wordWidth > flowMaxWidth) {
                x = flowOriginX;
                line++;
                lineHeights.add((float) font.lineHeight);
            }

            // Place each letter of the word
            for (int wordIndex = index; wordIndex < wordEnd; wordIndex++) {
                LetterEntry wordEntry = letters.get(wordIndex);
                positionX[wordIndex] = x;
                lineIndex[wordIndex] = line;
                if (wordEntry.isLayoutSkipped()) continue;
                lineHeights.set(line, Math.max(lineHeights.get(line), wordEntry.height(font, data)));
                x += wordEntry.width(font, data) + data.getLetterSpacing();
            }

            index = wordEnd;
        }

        float[] lineTops = new float[lineHeights.size()];
        float top = 0f;
        for (int currentLine = 0; currentLine < lineHeights.size(); currentLine++) {
            lineTops[currentLine] = top;
            top += lineHeights.get(currentLine) + data.getLineGap();
        }

        float textWidth = 0f;
        float textHeight = 0f;
        for (int entryIndex = bodyStart; entryIndex < bodyEnd; entryIndex++) {
            LetterEntry entry = letters.get(entryIndex);
            int entryLine = lineIndex[entryIndex];
            if (entryLine < 0) continue;
            float lineHeight = lineHeights.get(entryLine);
            positionY[entryIndex] = flowOriginY + lineTops[entryLine] + (lineHeight - entry.height(font, data)) / 2f;

            if (entry.isLayoutSkipped() || entry.letter.equals("\n") || entry.letter.equals(" ")) continue;
            float right = (positionX[entryIndex] - flowOriginX) + entry.width(font, data);
            float bottom = lineTops[entryLine] + lineHeight;
            if (right > textWidth) textWidth = right;
            if (bottom > textHeight) textHeight = bottom;
        }

        applyAlignment(positionX, lineIndex, flowOriginX, textWidth, font, data);

        float targetHeight = sideImageHeight > 0f
                ? sideImageHeight
                : Math.clamp(
                        textHeight,
                        SIDE_IMAGE_MIN_LINE_RATIO * font.lineHeight,
                        SIDE_IMAGE_MAX_LINE_RATIO * font.lineHeight);
        List<SideImage> leading = buildSideImages(leadingImages, targetHeight, data);
        List<SideImage> trailing = buildSideImages(trailingImages, targetHeight, data);

        float leftGutterWidth = gutterWidth(leading);
        float rightGutterWidth = gutterWidth(trailing);

        float sideHeight = 0f;
        for (SideImage image : leading) sideHeight = Math.max(sideHeight, image.height());
        for (SideImage image : trailing) sideHeight = Math.max(sideHeight, image.height());

        return new LayoutResult(
                positionX,
                positionY,
                textWidth,
                textHeight,
                leading,
                trailing,
                leftGutterWidth,
                rightGutterWidth,
                leftGutterWidth + textWidth + rightGutterWidth,
                Math.max(textHeight, sideHeight));
    }

    public float[] computeTextDimensions(float maxWidth, Font font, DialogData data) {
        LayoutResult result = computeLayout(0f, 0f, maxWidth, 0f, font, data);
        return new float[] {result.contentWidth(), result.contentHeight()};
    }

    private static List<SideImage> buildSideImages(List<LetterEntry> entries, float targetHeight, DialogData data) {
        List<SideImage> images = new ArrayList<>(entries.size());
        for (LetterEntry entry : entries) {
            float ratio = entry.image.getHeight() <= 0 ? 1f : (float) entry.image.getWidth() / entry.image.getHeight();
            float height = targetHeight;
            float width = height * ratio;
            float maxWidth = data.getWidth() * SIDE_IMAGE_MAX_WIDTH_RATIO;
            if (maxWidth > 0f && width > maxWidth) {
                height *= maxWidth / width;
                width = maxWidth;
            }
            images.add(new SideImage(entry.image, width, height));
        }
        return images;
    }

    private static float gutterWidth(List<SideImage> images) {
        float total = 0f;
        for (SideImage image : images) {
            total += image.width() + SIDE_IMAGE_GAP;
        }
        return total;
    }

    public void render2D(GuiGraphics graphics, float originX, float originY, DialogData data, float partialTick) {
        Font font = Minecraft.getInstance().font;
        LayoutResult result = computeContentLayout(originX, originY, data.getWidth(), font, data);

        renderInline2D(graphics, result, data, partialTick);
        renderSideImages2D(
                graphics, result, originX, originX + result.contentWidth(), originY + result.contentHeight() / 2f);
    }

    private LayoutResult computeContentLayout(
            float contentOriginX, float contentOriginY, float maxWidth, Font font, DialogData data) {
        LayoutResult result = computeLayout(contentOriginX, contentOriginY, maxWidth, 0f, font, data);
        if (result.leftGutterWidth() <= 0f) return result;
        return computeLayout(contentOriginX + result.leftGutterWidth(), contentOriginY, maxWidth, 0f, font, data);
    }

    public void renderInline2D(GuiGraphics graphics, LayoutResult layout, DialogData data, float partialTick) {
        Font font = Minecraft.getInstance().font;

        for (int index = bodyStart; index < bodyEnd; index++) {
            LetterEntry entry = letters.get(index);
            if (!entry.visible) break;
            if (entry.isLayoutSkipped() || entry.letter.equals("\n") || entry.letter.equals(" ")) continue;

            Vec2 offset = getEffectOffset(entry, index, partialTick);
            float x = layout.positionX()[index] + offset.x;
            float y = layout.positionY()[index] + offset.y;

            if (entry.isImage()) {
                entry.image.render(graphics, x, y, entry.width(font, data), entry.height(font, data), 1f);
                continue;
            }

            int color = applyOpacity(data.getTextColor(), 1f);
            font.drawInBatch(
                    entry.letter,
                    x,
                    y,
                    color,
                    data.isTextShadow(),
                    graphics.pose().last().pose(),
                    graphics.bufferSource(),
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    LightTexture.FULL_BRIGHT);
        }
    }

    public void renderSideImages2D(
            GuiGraphics graphics, LayoutResult layout, float leftX, float rightX, float centerY) {
        float x = leftX;
        for (SideImage image : layout.leadingImages()) {
            image.texture().render(graphics, x, centerY - image.height() / 2f, image.width(), image.height(), 1f);
            x += image.width() + SIDE_IMAGE_GAP;
        }

        x = rightX;
        List<SideImage> trailing = layout.trailingImages();
        for (int index = trailing.size() - 1; index >= 0; index--) {
            SideImage image = trailing.get(index);
            x -= image.width();
            image.texture().render(graphics, x, centerY - image.height() / 2f, image.width(), image.height(), 1f);
            x -= SIDE_IMAGE_GAP;
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
        LayoutResult result = computeContentLayout(originX, originY, data.getWidth(), font, data);

        renderInline3D(poseStack, bufferSource, result, data, partialTick);
        renderSideImages3D(
                poseStack,
                bufferSource,
                result,
                originX,
                originX + result.contentWidth(),
                originY + result.contentHeight() / 2f);
    }

    public void renderInline3D(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            LayoutResult layout,
            DialogData data,
            float partialTick) {
        Font font = Minecraft.getInstance().font;

        for (int index = bodyStart; index < bodyEnd; index++) {
            LetterEntry entry = letters.get(index);
            if (!entry.visible) break;
            if (entry.isLayoutSkipped() || entry.letter.equals("\n") || entry.letter.equals(" ")) continue;

            Vec2 offset = getEffectOffset(entry, index, partialTick);
            float x = layout.positionX()[index] + offset.x;
            float y = layout.positionY()[index] + offset.y;

            if (entry.isImage()) {
                submitImageQuad(
                        poseStack, bufferSource, entry.image, x, y, entry.width(font, data), entry.height(font, data));
                continue;
            }

            int color = applyOpacity(data.getTextColor(), 1f);
            font.drawInBatch(
                    entry.letter,
                    x,
                    y,
                    color,
                    data.isTextShadow(),
                    poseStack.last().pose(),
                    bufferSource,
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    LightTexture.FULL_BRIGHT);
        }
    }

    public void renderSideImages3D(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            LayoutResult layout,
            float leftX,
            float rightX,
            float centerY) {
        float x = leftX;
        for (SideImage image : layout.leadingImages()) {
            submitImageQuad(
                    poseStack,
                    bufferSource,
                    image.texture(),
                    x,
                    centerY - image.height() / 2f,
                    image.width(),
                    image.height());
            x += image.width() + SIDE_IMAGE_GAP;
        }

        x = rightX;
        List<SideImage> trailing = layout.trailingImages();
        for (int index = trailing.size() - 1; index >= 0; index--) {
            SideImage image = trailing.get(index);
            x -= image.width();
            submitImageQuad(
                    poseStack,
                    bufferSource,
                    image.texture(),
                    x,
                    centerY - image.height() / 2f,
                    image.width(),
                    image.height());
            x -= SIDE_IMAGE_GAP;
        }
    }

    private static void submitImageQuad(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            ImageTexture image,
            float x,
            float y,
            float width,
            float height) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.textSeeThrough(image.getLocation()));
        Matrix4f pose = poseStack.last().pose();
        consumer.addVertex(pose, x, y, 0).setColor(0xFFFFFFFF).setUv(0f, 0f).setLight(LightTexture.FULL_BRIGHT);
        consumer.addVertex(pose, x, y + height, 0)
                .setColor(0xFFFFFFFF)
                .setUv(0f, 1f)
                .setLight(LightTexture.FULL_BRIGHT);
        consumer.addVertex(pose, x + width, y + height, 0)
                .setColor(0xFFFFFFFF)
                .setUv(1f, 1f)
                .setLight(LightTexture.FULL_BRIGHT);
        consumer.addVertex(pose, x + width, y, 0)
                .setColor(0xFFFFFFFF)
                .setUv(1f, 0f)
                .setLight(LightTexture.FULL_BRIGHT);
    }

    private void applyAlignment(
            float[] positionX, int[] lineIndex, float flowOriginX, float maxLineWidth, Font font, DialogData data) {
        DialogData.TextAlignment alignment = data.getTextAlignment();
        if (alignment == DialogData.TextAlignment.LEFT) return;

        Map<Integer, Float> rowRightEdge = new LinkedHashMap<>();
        for (int index = bodyStart; index < bodyEnd; index++) {
            LetterEntry entry = letters.get(index);
            if (entry.isLayoutSkipped() || entry.letter.equals("\n") || entry.letter.equals(" ")) continue;
            float rightEdge = (positionX[index] - flowOriginX) + entry.width(font, data);
            rowRightEdge.merge(lineIndex[index], rightEdge, Math::max);
        }

        for (int index = bodyStart; index < bodyEnd; index++) {
            LetterEntry entry = letters.get(index);
            if (entry.isLayoutSkipped() || entry.letter.equals("\n") || entry.letter.equals(" ")) continue;
            float lineWidth = rowRightEdge.getOrDefault(lineIndex[index], 0f);
            float offset =
                    switch (alignment) {
                        case CENTER -> (maxLineWidth - lineWidth) / 2f;
                        case RIGHT -> maxLineWidth - lineWidth;
                        default -> 0f;
                    };
            positionX[index] += offset;
        }
    }

    private float gutterWidth(List<LetterEntry> images, Font font, DialogData data) {
        float total = 0f;
        for (LetterEntry entry : images) {
            total += entry.width(font, data) + SIDE_IMAGE_GAP;
        }
        return total;
    }

    private void splitSideImages() {
        bodyStart = 0;
        bodyEnd = letters.size();

        int scan = 0;
        while (scan < letters.size()) {
            LetterEntry entry = letters.get(scan);
            if (entry.isImage()) {
                leadingImages.add(entry);
                scan++;
                continue;
            }
            if (entry.isLayoutSkipped() || entry.letter.equals(" ")) {
                scan++;
                continue;
            }
            break;
        }
        if (!leadingImages.isEmpty()) bodyStart = scan;

        scan = letters.size();
        while (scan > bodyStart) {
            LetterEntry entry = letters.get(scan - 1);
            if (entry.isImage()) {
                trailingImages.addFirst(entry);
                scan--;
                continue;
            }
            if (entry.isLayoutSkipped() || entry.letter.equals(" ")) {
                scan--;
                continue;
            }
            break;
        }
        if (!trailingImages.isEmpty()) bodyEnd = scan;

        for (LetterEntry entry : leadingImages) entry.visible = true;
        for (LetterEntry entry : trailingImages) entry.visible = true;
    }

    private void parseText(String rawText) {
        String currentEffect = null;
        Map<String, String> currentParams = new HashMap<>();
        int i = 0;
        int length = rawText.length();
        // Reuse the matchers and slide a region instead of allocating a substring per character.
        Matcher openMatcher = OPEN_TAG.matcher(rawText);
        Matcher closeMatcher = CLOSE_TAG.matcher(rawText);

        while (i < length) {
            openMatcher.region(i, length);
            closeMatcher.region(i, length);

            if (openMatcher.lookingAt()) {
                String effectName = openMatcher.group(1).toLowerCase();

                if (effectName.equals(IMAGE_TAG)) {
                    ImageTexture texture =
                            ImageTexture.load(openMatcher.group(2).trim());
                    if (texture != null) letters.add(new LetterEntry(texture));
                    i = openMatcher.end();
                    continue;
                }

                Map<String, String> params = parseParams(openMatcher.group(2));

                if (effectName.equals("wait")) {
                    // Insert an invisible pause marker at this exact position.
                    // The letters that follow are NOT tagged with "wait", they scroll normally.
                    letters.add(new LetterEntry("", "wait", params));
                } else {
                    currentEffect = effectName;
                    currentParams = params;
                }

                i = openMatcher.end();
                continue;
            }

            if (closeMatcher.lookingAt()) {
                currentEffect = null;
                currentParams = new HashMap<>();
                i = closeMatcher.end();
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
        int alpha = (int) (FastColor.ARGB32.alpha(color) * opacity);
        return FastColor.ARGB32.color(
                alpha, FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color));
    }

    public void setMutedSound(boolean mutedSound) {
        this.mutedSound = mutedSound;
    }

    public void setSound(ResourceLocation sound) {
        this.sound = sound;
    }

    public record LayoutResult(
            float[] positionX,
            float[] positionY,
            float textWidth,
            float textHeight,
            List<SideImage> leadingImages,
            List<SideImage> trailingImages,
            float leftGutterWidth,
            float rightGutterWidth,
            float contentWidth,
            float contentHeight) {}

    public record SideImage(ImageTexture texture, float width, float height) {}

    private static class LetterEntry {
        final String letter;
        final String effectName;
        final Map<String, String> params;
        final ImageTexture image;
        boolean visible = false;

        LetterEntry(String letter, String effectName, Map<String, String> params) {
            this.letter = letter;
            this.effectName = effectName;
            this.params = params;
            this.image = null;
        }

        LetterEntry(ImageTexture image) {
            this.letter = "";
            this.effectName = null;
            this.params = Map.of();
            this.image = image;
        }

        boolean isImage() {
            return image != null;
        }

        boolean isLayoutSkipped() {
            return image == null && letter.isEmpty();
        }

        float width(Font font, DialogData data) {
            if (image == null) return font.width(letter);
            return imageSize(font, data)[0];
        }

        float height(Font font, DialogData data) {
            if (image == null) return font.lineHeight;
            return imageSize(font, data)[1];
        }

        private float[] imageSize(Font font, DialogData data) {
            float ratio = image.getHeight() <= 0 ? 1f : (float) image.getWidth() / image.getHeight();
            float height = INLINE_IMAGE_LINE_RATIO * font.lineHeight;
            float width = height * ratio;
            float maxWidth = data.getWidth();
            if (maxWidth > 0f && width > maxWidth) {
                height *= maxWidth / width;
                width = maxWidth;
            }
            return new float[] {width, height};
        }
    }
}
