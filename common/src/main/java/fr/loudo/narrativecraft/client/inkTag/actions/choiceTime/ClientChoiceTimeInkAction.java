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

package fr.loudo.narrativecraft.client.inkTag.actions.choiceTime;

import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.client.screens.story.ChoiceScreen;
import fr.loudo.narrativecraft.narrative.inkTag.actions.ChoiceTimeInkAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class ClientChoiceTimeInkAction extends ChoiceTimeInkAction {

    private ChoiceTimeLineBar timeLineBar;

    @Override
    public void tick() {
        timeLineBar.tick();
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, float partialTick) {
        timeLineBar.render(guiGraphics, partialTick);
        if (timeLineBar.isFadeOutFinished()) {
            isRunning = false;
        }
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        timeLineBar = new ChoiceTimeLineBar(
                lineColor,
                outlineColor,
                outlinePadding,
                lineWidth,
                lineHeight,
                lineOffsetY,
                easingType,
                showText,
                (int) (fadeDuration * 20),
                (int) (seconds * 20),
                this::onTimeout);
        return InkActionResult.ok();
    }

    public void forceFinish() {
        timeLineBar.forceFinish();
    }

    private void onTimeout() {
        if (!(Minecraft.getInstance().gui.screen() instanceof ChoiceScreen choiceScreen)) return;

        if (defaultAnswerIndex == -1) {
            defaultAnswerIndex = Mth.randomBetweenInclusive(
                    RandomSource.create(), 1, choiceScreen.getButtons().size());
        }
        choiceScreen.forceChooseChoice(defaultAnswerIndex - 1);
    }
}
