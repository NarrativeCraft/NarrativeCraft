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

package fr.loudo.narrativecraft.narrative.inkTag.actions;

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkCommand;
import fr.loudo.narrativecraft.api.inkAction.Side;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.utils.FadeState;
import net.minecraft.world.phys.Vec2;

import javax.annotation.Nullable;

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

    protected String textId;
    protected String action;

    protected String text = "";
    protected Position position = Position.MIDDLE;
    protected int color = 0xFFFFFF;
    protected float opacity = 1.0f;
    protected float scale = 1.0f;
    protected float width = 200f;
    protected boolean noTyping = true;
    protected boolean noRemove = false;
    protected float scrollSpeed = 1.5f;
    protected Vec2 space = new Vec2(0, 0);

    @Nullable
    protected FadeState fadeState;

    protected float fadeInSeconds;
    protected float staySeconds = -1;
    protected float fadeOutSeconds;

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
        return InkActionResult.ignored();
    }
}
