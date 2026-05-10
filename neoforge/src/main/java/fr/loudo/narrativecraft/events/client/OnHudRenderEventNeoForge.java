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

package fr.loudo.narrativecraft.events.client;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(NarrativeCraftMod.MOD_ID)
public class OnHudRenderEventNeoForge {

    public OnHudRenderEventNeoForge(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(OnHudRenderEventNeoForge::onHudRender);
    }

    private static void onHudRender(RenderGuiEvent.Post event) {
        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        DeltaTracker deltaTracker = event.getPartialTick();

        OnHudRender.cutsceneHudRender(graphics, deltaTracker);
        OnHudRender.cameraAngleHudRender(graphics, deltaTracker);
        OnHudRender.interactionHudRender(graphics, deltaTracker);
        OnHudRender.dialogHudRender(graphics, deltaTracker);
        OnHudRender.clientInkActionsHudRender(graphics, deltaTracker);
    }
}
