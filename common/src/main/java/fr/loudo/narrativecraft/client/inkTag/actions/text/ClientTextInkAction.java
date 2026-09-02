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

package fr.loudo.narrativecraft.client.inkTag.actions.text;

import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.dialog.DialogScrollText;
import fr.loudo.narrativecraft.narrative.inkTag.actions.TextInkAction;
import fr.loudo.narrativecraft.utils.UtilsClient;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;

public class ClientTextInkAction extends TextInkAction {

    private static final float REFERENCE_GUI_SCALE = 3f;

    @Nullable
    private ClientTextInkAction monitoredInstance;

    private DialogScrollText scrollText;
    private DialogData dialogData;
    private float[] cachedTextDimensions = new float[] {0, 0};

    @Override
    public void tick() {
        if (!isRunning()) return;

        if (tickMonitoredOverlay()) return;

        if (monitoredInstance != null) {
            if (monitoredInstance.scrollText != null && monitoredInstance.scrollText.isFinished()) {
                stop();
            }
            return;
        }

        if (scrollText != null) {
            scrollText.tick(noTyping ? scrollText.isFinished() ? 0 : Float.MAX_VALUE : scrollSpeed);
        }

        tickFade();
        tickAnimation();
    }

    @Override
    public void render(GuiGraphics guiGraphics, float partialTick) {
        if (!isRunning() || scrollText == null || dialogData == null) return;

        float currentOpacity = computeOpacity(partialTick);
        int adjustedAlpha = (int) (FastColor.ARGB32.alpha(FastColor.ARGB32.color(255, color)) * currentOpacity);
        dialogData.setTextColor(FastColor.ARGB32.color(adjustedAlpha, color));

        Font font = Minecraft.getInstance().font;
        cachedTextDimensions = scrollText.computeTextDimensions(width, font, dialogData);

        float uiScale = UtilsClient.computeUiScale(REFERENCE_GUI_SCALE);
        float renderedWidth = cachedTextDimensions[0] * uiScale;
        float renderedHeight = cachedTextDimensions[1] * uiScale;

        float[] origin = computeOrigin(guiGraphics.guiWidth(), guiGraphics.guiHeight(), renderedWidth, renderedHeight);
        float[] animationOffset = computeAnimationOffset(
                guiGraphics.guiWidth(), guiGraphics.guiHeight(), renderedWidth, renderedHeight, uiScale, partialTick);

        guiGraphics.pose().pushPose();
        guiGraphics
                .pose()
                .translate(
                        origin[0] + space.x * uiScale + animationOffset[0],
                        origin[1] + space.y * uiScale + animationOffset[1],
                        0f);
        guiGraphics.pose().scale(scale * uiScale, scale * uiScale, 1f);
        scrollText.render2D(guiGraphics, 0, 0, dialogData, partialTick);
        guiGraphics.pose().popPose();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        ClientPlayerSession session = (ClientPlayerSession) playerSession;
        ClientTextInkAction existing =
                findActiveById(ClientTextInkAction.class, overlayId, session.getActiveClientInkActions(), "create");

        if (action.equals("create")) {
            if (existing != null) {
                stop();
                return InkActionResult.error("Text id '" + overlayId + "' already exists");
            }
            dialogData = new DialogData();
            dialogData.setWidth(width);
            dialogData.setTextColor(FastColor.ARGB32.color(255, color));
            dialogData.setTextAlignment(textAlignment);
            scrollText = new DialogScrollText(dialogData.getLetterSound(), dialogData.isSoundMuted());
            scrollText.setText(text);
            if (noTyping) scrollText.forceFinish();
            return InkActionResult.ok();
        }

        if (existing == null) {
            if (action.equals("remove")) {
                return InkActionResult.singleOk();
            }
            stop();
            return InkActionResult.error("Text id '" + overlayId + "' not found");
        }

        if (!applySharedProperty(existing)) {
            switch (action) {
                case "edit" -> {
                    existing.text = text;
                    existing.scrollText.setText(text);
                    existing.scrollText.forceFinish();
                }
                case "color" -> existing.color = color;
                case "width" -> {
                    existing.width = width;
                    existing.dialogData.setWidth(width);
                }
                case "text_alignment" -> {
                    existing.textAlignment = textAlignment;
                    existing.dialogData.setTextAlignment(textAlignment);
                }
                case "type" -> {
                    existing.scrollSpeed = scrollSpeed;
                    existing.noTyping = false;
                    existing.scrollText.setText(existing.text);
                    if (blocking) {
                        monitoredInstance = existing;
                    }
                }
                case "shadow" -> {
                    existing.shadow = shadow;
                    existing.dialogData.setTextShadow(shadow);
                }
                case "mute" -> {
                    existing.mute = mute;
                    existing.dialogData.setSoundMuted(mute);
                    existing.scrollText.setMutedSound(mute);
                }
            }
        }

        return blocking ? InkActionResult.block() : InkActionResult.singleOk();
    }
}
