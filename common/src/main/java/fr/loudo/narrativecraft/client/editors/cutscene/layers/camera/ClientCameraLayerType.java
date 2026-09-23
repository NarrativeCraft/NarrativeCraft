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
import fr.loudo.narrativecraft.api.client.editors.cutscene.ClientCutsceneLayerType;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.cutscene.ClientCutsceneMakerEditorMaker;
import fr.loudo.narrativecraft.narrative.cutscene.layers.camera.CameraKeyframe;
import fr.loudo.narrativecraft.narrative.cutscene.layers.camera.CameraLayer;
import fr.loudo.narrativecraft.narrative.cutscene.layers.camera.CameraLayerType;
import fr.loudo.narrativecraft.narrative.cutscene.layers.camera.KeyframePosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class ClientCameraLayerType implements ClientCutsceneLayerType<CameraLayer> {

    @Override
    public String getTypeId() {
        return CameraLayerType.ID;
    }

    @Override
    public Class<CameraLayer> getLayerClass() {
        return CameraLayer.class;
    }

    @Override
    public CameraKeyframe createDefaultKeyframe(CameraLayer layer, int tick) {
        LocalPlayer player = Minecraft.getInstance().player;
        Vec3 position = player.position().add(0, player.getEyeHeight(), 0);
        ClientCutsceneMakerEditorMaker editor =
                ClientNarrativeCraftMod.getInstance().getCutsceneMakerEditor();
        float roll = editor == null ? 0f : editor.getPreviewRoll();
        Vec3 rotation = new Vec3(player.getXRot(), player.getYRot(), roll);
        return new CameraKeyframe(layer, tick, new KeyframePosition(position, rotation));
    }

    @Override
    public ClientCutsceneLayerPlayer createPlayer(CameraLayer layer) {
        return new ClientCameraLayerPlayer(layer);
    }
}
