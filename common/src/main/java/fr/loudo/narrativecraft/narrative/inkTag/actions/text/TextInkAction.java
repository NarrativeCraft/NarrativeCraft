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

package fr.loudo.narrativecraft.narrative.inkTag.actions.text;

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Interpolation;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkCommand;
import fr.loudo.narrativecraft.api.inkAction.Side;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.dialog.DialogScrollText;
import fr.loudo.narrativecraft.utils.FadeState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.List;

@InkCommand(
        keyword = "text",
        description = "Creates, moves, edits, or removes a named text overlay displayed on the client HUD.",
        syntax = "text <id:string> <action:string> (param1:string) (param2:string) (param3:string) [--block]",
        side = Side.CLIENT)
public class TextInkAction extends InkAction {

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

    private String textId;
    private String action;

    private String text = "";
    private Position position = Position.MIDDLE;
    private int color = 0xFFFFFF;
    private float opacity = 1.0f;
    private float scale = 1.0f;
    private float width = 200f;
    private boolean noTyping = true;
    private boolean noRemove = false;
    private float scrollSpeed = 1.5f;

    @Nullable
    private FadeState fadeState;

    private float fadeInSeconds;
    private float staySeconds = -1;
    private float fadeOutSeconds;

    private DialogScrollText scrollText;
    private DialogData dialogData;

    @Nullable
    private TextInkAction monitoredInstance;

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
    public void render(GuiGraphicsExtractor guiGraphics, float partialTick) {
        if (!isRunning || scrollText == null || dialogData == null) return;

        float currentOpacity = computeOpacity(partialTick);
        int adjustedAlpha = (int) (ARGB.alpha(ARGB.color(255, color)) * currentOpacity);
        dialogData.setTextColor(ARGB.color(adjustedAlpha, color));

        Font font = Minecraft.getInstance().font;
        cachedTextDimensions = scrollText.computeTextDimensions(width, font, dialogData);

        float[] origin = computeOrigin(guiGraphics.guiWidth(), guiGraphics.guiHeight());

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(origin[0], origin[1]);
        guiGraphics.pose().scale(scale, scale);
        scrollText.render2D(guiGraphics, 0, 0, dialogData, partialTick);
        guiGraphics.pose().popMatrix();
    }

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        textId = cmd.getString("id").toLowerCase();
        action = cmd.getString("action").toLowerCase();

        String value1 = cmd.getString("param1");
        String value2 = cmd.getString("param2");
        String value3 = cmd.getString("param3");

        switch (action) {
            case "create" -> {
                if (value1 == null || value1.isEmpty()) {
                    return InkActionResult.error("'create' requires a text value (value1)");
                }
                text = value1;
                noTyping = true;
                if (value2 != null && !value2.isEmpty()) {
                    try {
                        color = Integer.parseInt(value2, 16);
                    } catch (NumberFormatException e) {
                        return InkActionResult.error("Invalid hex color '" + value2 + "'");
                    }
                }
            }
            case "remove" -> {}
            case "edit" -> {
                if (value1 == null || value1.isEmpty()) {
                    return InkActionResult.error("'edit' requires a text value (value1)");
                }
                text = value1;
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
            case "color" -> {
                if (value1 == null || value1.isEmpty()) {
                    return InkActionResult.error("'color' requires a hex color value (value1)");
                }
                try {
                    color = Integer.parseInt(value1, 16);
                } catch (NumberFormatException e) {
                    return InkActionResult.error("Invalid hex color '" + value1 + "'");
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
            case "width" -> {
                if (value1 == null || value1.isEmpty()) {
                    return InkActionResult.error("'width' requires an integer value (value1)");
                }
                try {
                    width = Integer.parseInt(value1);
                } catch (NumberFormatException e) {
                    return InkActionResult.error("Invalid width value '" + value1 + "'");
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
            }
            case "type" -> {
                if (value1 != null && !value1.isEmpty()) {
                    try {
                        scrollSpeed = Float.parseFloat(value1);
                    } catch (NumberFormatException e) {
                        return InkActionResult.error("Invalid type speed value '" + value1 + "'");
                    }
                }
                noTyping = false;
                if (cmd.flag("block")) blocking = true;
            }
            default -> {
                return InkActionResult.error("Unknown text action '" + action + "'");
            }
        }

        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        ClientPlayerSession session = (ClientPlayerSession) playerSession;
        TextInkAction existing = findActiveById(textId, session.getActiveClientInkActions());

        if (action.equals("create")) {
            if (existing != null) {
                isRunning = false;
                return InkActionResult.error("Text id '" + textId + "' already exists");
            }
            dialogData = new DialogData();
            dialogData.setWidth(width);
            dialogData.setTextColor(ARGB.color(255, color));
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
    private static TextInkAction findActiveById(String id, List<InkAction> actions) {
        for (InkAction action : actions) {
            if (action instanceof TextInkAction textAction
                    && textAction.textId != null
                    && textAction.textId.equalsIgnoreCase(id)
                    && textAction.action.equals("create")) {
                return textAction;
            }
        }
        return null;
    }
}
