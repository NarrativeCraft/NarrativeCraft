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

package fr.loudo.narrativecraft.client.editors.cutscene.layers.camera;

import fr.loudo.narrativecraft.api.client.editors.cutscene.ClientCutsceneLayerPlayer;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.cutscene.layers.camera.CameraLayer;
import fr.loudo.narrativecraft.narrative.cutscene.layers.camera.KeyframePosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class ClientCameraLayerPlayer implements ClientCutsceneLayerPlayer {

    private final CameraLayer layer;

    public ClientCameraLayerPlayer(CameraLayer layer) {
        this.layer = layer;
    }

    @Override
    public boolean execute(float tick) {
        if (!layer.isTickCoveredBy(tick)) return false;
        KeyframePosition keyframePosition = layer.getInterpolatedPosition(tick);
        Vec3 position = keyframePosition.getPosition();
        Vec3 rotation = keyframePosition.getRotation();

        ClientNarrativeCraftMod.getInstance()
                .getPlayerSession()
                .getCutsceneDataSession()
                .setKeyframePosition(keyframePosition);

        LocalPlayer localPlayer = Minecraft.getInstance().player;
        position = position.subtract(0, localPlayer.getEyeHeight(), 0);
        localPlayer.setPos(position);
        localPlayer.setXRot((float) rotation.x);
        localPlayer.setYRot((float) rotation.y);
        localPlayer.setYHeadRot((float) rotation.y);
        return true;
    }
}
