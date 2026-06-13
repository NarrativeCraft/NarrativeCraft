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

package fr.loudo.narrativecraft.client.inkTag.actions.text;

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Interpolation;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.dialog.DialogScrollText;
import fr.loudo.narrativecraft.narrative.inkTag.actions.TextInkAction;
import fr.loudo.narrativecraft.utils.FadeState;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class ClientTextInkAction extends TextInkAction {

    @Nullable
    private ClientTextInkAction monitoredInstance;

    private DialogScrollText scrollText;
    private DialogData dialogData;
    private float[] cachedTextDimensions = new float[] {0, 0};

    @Override
    public void tick() {
        if (!isRunning) return;

        if (monitoredInstance != null) {
            if (monitoredInstance.scrollText != null && monitoredInstance.scrollText.isFinished()) {
                isRunning = false;
            }
            return;
        }

        if (scrollText != null) {
            scrollText.tick(noTyping ? scrollText.isFinished() ? 0 : Float.MAX_VALUE : scrollSpeed);
        }

        tick++;
        if (tick >= totalTick && fadeState != null) {
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
                        isRunning = false;
                    }
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, float partialTick) {
        if (!isRunning || scrollText == null || dialogData == null) return;

        float currentOpacity = computeOpacity(partialTick);
        int adjustedAlpha = (int) (FastColor.ARGB32.alpha(FastColor.ARGB32.color(255, color)) * currentOpacity);
        dialogData.setTextColor(FastColor.ARGB32.color(adjustedAlpha, color));

        Font font = Minecraft.getInstance().font;
        cachedTextDimensions = scrollText.computeTextDimensions(width, font, dialogData);

        Minecraft mc = Minecraft.getInstance();
        float[] origin =
                computeOrigin(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(origin[0], origin[1], 0);
        guiGraphics.pose().scale(scale, scale, 1f);
        scrollText.render2D(guiGraphics, 0, 0, dialogData, partialTick);
        guiGraphics.pose().popPose();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        ClientPlayerSession session = (ClientPlayerSession) playerSession;
        ClientTextInkAction existing = findActiveById(textId, session.getActiveClientInkActions());

        if (action.equals("create")) {
            if (existing != null) {
                isRunning = false;
                return InkActionResult.error("Text id '" + textId + "' already exists");
            }
            dialogData = new DialogData();
            dialogData.setWidth(width);
            dialogData.setTextColor(FastColor.ARGB32.color(255, color));
            scrollText = new DialogScrollText(dialogData.getLetterSound(), dialogData.isSoundMuted());
            scrollText.setText(text);
            if (noTyping) scrollText.forceFinish();
            return InkActionResult.ok();
        }

        if (existing == null) {
            if (action.equals("remove")) {
                isRunning = false;
                return InkActionResult.ok();
            }
            isRunning = false;
            return InkActionResult.error("Text id '" + textId + "' not found");
        }

        switch (action) {
            case "remove" -> {
                existing.isRunning = false;
                isRunning = false;
            }
            case "edit" -> {
                existing.text = text;
                existing.scrollText.setText(text);
                isRunning = false;
            }
            case "position", "pos" -> {
                existing.position = position;
                isRunning = false;
            }
            case "color" -> {
                existing.color = color;
                isRunning = false;
            }
            case "opacity" -> {
                existing.opacity = opacity;
                isRunning = false;
            }
            case "scale" -> {
                existing.scale = scale;
                isRunning = false;
            }
            case "width" -> {
                existing.width = width;
                existing.dialogData.setWidth(width);
                isRunning = false;
            }
            case "fade" -> {
                existing.fadeState = fadeState;
                existing.fadeInSeconds = fadeInSeconds;
                existing.staySeconds = staySeconds;
                existing.fadeOutSeconds = fadeOutSeconds;
                existing.totalTick = totalTick;
                existing.tick = 0;
                isRunning = false;
            }
            case "fadein" -> {
                existing.fadeState = FadeState.FADE_IN;
                existing.fadeInSeconds = fadeInSeconds;
                existing.staySeconds = -1;
                existing.totalTick = (int) (fadeInSeconds * 20.0);
                existing.tick = 0;
                isRunning = false;
            }
            case "fadeout" -> {
                existing.fadeState = FadeState.FADE_OUT;
                existing.fadeOutSeconds = fadeOutSeconds;
                existing.totalTick = (int) (fadeOutSeconds * 20.0);
                existing.tick = 0;
                isRunning = false;
            }
            case "type" -> {
                existing.scrollSpeed = scrollSpeed;
                existing.noTyping = false;
                existing.scrollText.setText(existing.text);
                if (!blocking) {
                    isRunning = false;
                } else {
                    monitoredInstance = existing;
                }
            }
        }

        return blocking ? InkActionResult.block() : InkActionResult.ok();
    }

    private float computeOpacity(float partialTick) {
        if (fadeState == null) return opacity;
        double t = Mth.clamp((tick + partialTick) / (float) totalTick, 0.0, 1.0);
        return switch (fadeState) {
            case FADE_IN -> (float) Interpolation.lerp(0.0, opacity, t);
            case STAY -> opacity;
            case FADE_OUT -> (float) Interpolation.lerp(opacity, 0.0, t);
        };
    }

    private float[] computeOrigin(int guiWidth, int guiHeight) {
        float textWidth = cachedTextDimensions[0] * scale;
        float textHeight = cachedTextDimensions[1] * scale;

        float originX =
                switch (position) {
                    case TOP_LEFT, MIDDLE_LEFT, BOTTOM_LEFT -> 0;
                    case TOP, MIDDLE, BOTTOM -> (guiWidth - textWidth) / 2f;
                    case TOP_RIGHT, MIDDLE_RIGHT, BOTTOM_RIGHT -> guiWidth - textWidth;
                };

        float originY =
                switch (position) {
                    case TOP_LEFT, TOP, TOP_RIGHT -> 0;
                    case MIDDLE_LEFT, MIDDLE, MIDDLE_RIGHT -> (guiHeight - textHeight) / 2f;
                    case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> guiHeight - textHeight;
                };

        return new float[] {originX, originY};
    }

    @Nullable
    private static ClientTextInkAction findActiveById(String id, List<?> actions) {
        for (Object action : actions) {
            if (action instanceof ClientTextInkAction textAction
                    && textAction.textId != null
                    && textAction.textId.equalsIgnoreCase(id)
                    && textAction.action.equals("create")) {
                return textAction;
            }
        }
        return null;
    }
}
