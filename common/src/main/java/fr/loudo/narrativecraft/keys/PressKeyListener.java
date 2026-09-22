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

package fr.loudo.narrativecraft.keys;

import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.interaction.ClientInteractionMakerEditorMaker;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.client.studio.NarrativeStudio;
import fr.loudo.narrativecraft.dialog.DialogRenderer;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.interaction.InteractionPoint;
import fr.loudo.narrativecraft.network.story.C2SPlayStitchStory;
import fr.loudo.narrativecraft.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class PressKeyListener {

    public static void onKeyPressed(Minecraft minecraft) {

        if (ModKeys.STUDIO_TOGGLE.consumeClick()) {
            NarrativeStudio.getInstance().toggle();
        }

        if (ModKeys.START_RECORDING.consumeClick()) {
            minecraft.getConnection().sendCommand("nc record start");
        }
        if (ModKeys.STOP_RECORDING.consumeClick()) {
            minecraft.getConnection().sendCommand("nc record stop");
        }

        ModKeys.handleKeyPress(
                InputConstants.MOUSE_BUTTON_LEFT,
                minecraft.mouseHandler.isLeftPressed(),
                DialogRenderer::advanceNextDialog,
                PressKeyListener::handleInteractionPoints);

        ModKeys.handleKeyPress(
                InputConstants.MOUSE_BUTTON_RIGHT,
                minecraft.mouseHandler.isRightPressed(),
                PressKeyListener::handleInteractionPoints);
    }

    private static void handleInteractionPoints() {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        for (EditorMaker interactionSession : session.getInteractionSessions()) {
            if (!(interactionSession instanceof ClientInteractionMakerEditorMaker interactionEditor)) continue;
            if (interactionEditor.getEnvironment() == NarrativeEnvironment.DEVELOPMENT) continue;
            if (interactionEditor.getEnvironment() == NarrativeEnvironment.PRODUCTION && session.inCamera()) continue;
            for (InteractionPoint point : interactionEditor.getInteraction().getPoints()) {
                if (point.isOneTimeClick() && session.hasClickedInteractionPoint(point.getId())) continue;
                if (point.canSee(player) && point.canClick(player)) {
                    if (point.isOneTimeClick()) session.addClickedInteractionPoint(point.getId());
                    Services.PACKET.sendToServer(
                            new C2SPlayStitchStory(point.getStitchName(), point.getId(), point.isOneTimeClick()));
                    return;
                }
            }
        }
    }
}
