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

import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkCommand;
import fr.loudo.narrativecraft.api.inkAction.Side;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.dialog.DialogData;

@InkCommand(
        keyword = "text",
        description = "Creates, moves, edits, or removes a named text overlay displayed on the client HUD.",
        syntax =
                "text <id:string> <action:string> (param1:string) (param2:string) (param3:string) (param4:string) [--block]",
        side = Side.CLIENT)
public class TextInkAction extends OverlayInkAction {

    protected String text = "";
    protected int color = 0xFFFFFF;
    protected float width = 200f;
    protected boolean noTyping = true;
    protected float scrollSpeed = 1.5f;
    protected boolean shadow, mute;
    protected DialogData.TextAlignment textAlignment = DialogData.TextAlignment.CENTER;

    @Override
    protected InkActionResult validateOwnProperty(
            ParsedCommand cmd, IScene scene, String value1, String value2, String value3) {
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
            case "edit" -> {
                if (value1 == null || value1.isEmpty()) {
                    return InkActionResult.error("'edit' requires a text value (value1)");
                }
                text = value1;
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
            case "text_alignment" -> {
                if (value1 == null || value1.isEmpty()) {
                    return InkActionResult.error("'text_alignment' requires an value (value1) [LEFT, CENTER, RIGHT]");
                }
                try {
                    textAlignment = DialogData.TextAlignment.valueOf(value1.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return InkActionResult.error("Invalid text_alignment value '" + value1 + "' [LEFT, CENTER, RIGHT]");
                }
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
            case "shadow" -> {
                if (value1 != null && !value1.isEmpty()) {
                    shadow = Boolean.parseBoolean(value1);
                }
            }
            case "mute" -> {
                if (value1 != null && !value1.isEmpty()) {
                    mute = Boolean.parseBoolean(value1);
                }
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
