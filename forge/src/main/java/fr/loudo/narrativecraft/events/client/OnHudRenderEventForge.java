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

package fr.loudo.narrativecraft.events.client;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NarrativeCraftMod.MOD_ID, value = Dist.CLIENT)
public class OnHudRenderEventForge {

    @SubscribeEvent
    public static void onHudRender(RenderGuiEvent.Post event) {
        //        GuiGraphics graphics = event.getGuiGraphics();
        //        float partialTick = event.getPartialTick();
        //
        //        graphics.pose().pushPose();
        //        graphics.pose().translate(0, 0, 5000f);
        //
        //        OnHudRender.editorHudRender(graphics, partialTick);
        //        OnHudRender.clientInkActionsHudRender(graphics, partialTick);
        //        OnHudRender.saveIconHudRender(graphics, partialTick);
        //        OnHudRender.dialogHudRender(graphics, partialTick);
        //
        //        graphics.pose().popPose();
    }
}
