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

package fr.loudo.narrativecraft.client.inkTag.actions;

import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.client.rendering.ImageTexture;
import fr.loudo.narrativecraft.narrative.inkTag.actions.ImageInkAction;
import fr.loudo.narrativecraft.utils.UtilsClient;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ClientImageInkAction extends ImageInkAction {

    private static final float REFERENCE_GUI_SCALE = 1f;

    @Nullable
    private ImageTexture texture;

    private boolean textureLoaded;

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, float partialTick) {
        if (!isRunning()) return;

        if (!textureLoaded) {
            texture = ImageTexture.load(imagePath);
            textureLoaded = true;
        }
        if (texture == null) return;

        float uiScale = UtilsClient.computeUiScale(REFERENCE_GUI_SCALE);
        float[] origin = computeOrigin(
                guiGraphics.guiWidth(),
                guiGraphics.guiHeight(),
                texture.getWidth() * uiScale,
                texture.getHeight() * uiScale);
        float[] animationOffset = computeAnimationOffset(
                guiGraphics.guiWidth(),
                guiGraphics.guiHeight(),
                texture.getWidth() * uiScale,
                texture.getHeight() * uiScale,
                uiScale,
                partialTick);

        guiGraphics.pose().pushMatrix();
        guiGraphics
                .pose()
                .translate(
                        origin[0] + space.x * uiScale + animationOffset[0],
                        origin[1] + space.y * uiScale + animationOffset[1]);
        guiGraphics.pose().scale(scale * uiScale, scale * uiScale);
        texture.render(guiGraphics, 0, 0, computeOpacity(partialTick));
        guiGraphics.pose().popMatrix();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        if (action.equals("render")) return InkActionResult.ok();

        ClientImageInkAction existing = findActiveById(
                ClientImageInkAction.class, overlayId, playerSession.getActiveClientInkActions(), "render");
        if (existing == null) {
            stop();
            return InkActionResult.singleOk();
        }

        applySharedProperty(existing);
        return blocking ? InkActionResult.block() : InkActionResult.ok();
    }
}
