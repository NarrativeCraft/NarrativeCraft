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

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.interaction.ClientInteractionMakerEditorMaker;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.interaction.InteractionPoint;
import fr.loudo.narrativecraft.network.story.C2SPlayStitchStory;
import fr.loudo.narrativecraft.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class OnPressButtonEvent {

    public static void pressButton(int button) {
        if (button != 0 && button != 1) return;
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        if (!(session.getEditor() instanceof ClientInteractionMakerEditorMaker interactionEditor)) return;
        if (interactionEditor.getEnvironment() == NarrativeEnvironment.DEVELOPMENT) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        for (InteractionPoint point : interactionEditor.getInteraction().getPoints()) {
            if (point.isConditionMet(player)) {
                Services.PACKET.sendToServer(new C2SPlayStitchStory(point.getStitchName()));
                return;
            }
        }
    }
}
