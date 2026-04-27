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

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkCommand;
import fr.loudo.narrativecraft.api.inkAction.Side;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@InkCommand(
        keyword = "text",
        description = "Creates, moves, edits, or removes a named text overlay displayed on the client HUD.",
        syntax = "text <id:string> <action:string> [value1:string] [value2:string] [value3:string]",
        side = Side.CLIENT)
public class TextInkAction extends InkAction {

    @Override
    public void tick() {
        // TODO: implement full text display/animation logic
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, float partialTick) {
        // TODO: render text overlay
    }

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        // TODO: parse all text subcommands (create, remove, edit, position, color, scale, …)
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        // TODO: apply text action when TextInkAction infrastructure is complete
        isRunning = false;
        return InkActionResult.ignored();
    }
}
