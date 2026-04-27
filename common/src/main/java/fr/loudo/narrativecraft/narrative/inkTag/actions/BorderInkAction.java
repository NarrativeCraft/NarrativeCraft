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
import net.minecraft.client.gui.GuiGraphicsExtractor;

@InkCommand(
        keyword = "border",
        description = "Draws solid rectangles along screen edges to create cinematic letterboxing or vignettes.",
        syntax = "border <verb:string> [up:int=0] [right:int=0] [down:int=0] [left:int=0] "
                + "[color:string=000000] [opacity:float=1.0] [duration:float=0] [easing:string=smooth]",
        side = Side.CLIENT)
public class BorderInkAction extends InkAction {

    @Override
    public void tick() {
        // TODO: animate border edges when Easing and ICustomGuiRender are available
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, float partialTick) {
        // TODO: draw border rectangles via ICustomGuiRender when available
    }

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        // TODO: parse verb (set/clear/in/out) and initialise border fields
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        // TODO: apply border state when Easing and ICustomGuiRender are available
        isRunning = false;
        return InkActionResult.ignored();
    }
}
