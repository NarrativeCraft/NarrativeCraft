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

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.dialog.DialogRenderer2D;
import fr.loudo.narrativecraft.dialog.DialogRenderer3D;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.keys.PressKeyListener;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraView;
import fr.loudo.narrativecraft.network.inkAction.C2SInkActionFinished;
import fr.loudo.narrativecraft.platform.Services;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class OnClientTickEvent {

    public static void tick(Minecraft minecraft) {
        PressKeyListener.onKeyPressed(minecraft);

        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        new ArrayList<>(session.getActiveDialog2DRenderers()).forEach(DialogRenderer2D::tick);
        new ArrayList<>(session.getActiveDialog3DRenderers()).forEach(DialogRenderer3D::tick);

        EditorMaker editorMaker = session.getEditor();
        if (editorMaker != null) {
            editorMaker.tick();
        }

        tickNarrativeCamera(minecraft, session);
        tickClientInkActions(session);
    }

    private static void tickNarrativeCamera(Minecraft minecraft, ClientPlayerSession session) {
        CameraView narrativeCameraView = session.getNarrativeCameraView();
        if (narrativeCameraView == null || minecraft.player == null) return;
        LocalPlayer player = minecraft.player;
        player.setPos(narrativeCameraView.getPosition().subtract(0, player.getEyeHeight(), 0));
        player.setXRot((float) narrativeCameraView.getRotation().x);
        player.setYRot((float) narrativeCameraView.getRotation().y);
        player.setYHeadRot((float) narrativeCameraView.getRotation().y);
        player.connection.send(new ServerboundMovePlayerPacket.PosRot(
                narrativeCameraView.getPosition(),
                (float) narrativeCameraView.getRotation().x,
                (float) narrativeCameraView.getRotation().y,
                player.onGround(),
                false));
    }

    private static void tickClientInkActions(ClientPlayerSession session) {
        Iterator<InkAction> iterator = session.getActiveClientInkActions().iterator();
        while (iterator.hasNext()) {
            InkAction action = iterator.next();
            action.tick();
            if (!action.isRunning()) {
                iterator.remove();
                if (action.isBlocking()) {
                    Services.PACKET.sendToServer(new C2SInkActionFinished(action.getInstanceId()));
                }
            }
        }
    }
}
