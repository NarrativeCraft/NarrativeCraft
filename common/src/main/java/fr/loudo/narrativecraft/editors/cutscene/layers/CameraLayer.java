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

package fr.loudo.narrativecraft.editors.cutscene.layers;

import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.CameraKeyframe;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.KeyframePosition;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class CameraLayer extends CutsceneLayer {

    public CameraLayer(CutsceneLayerType layerType) {
        super(layerType);
    }

    @Override
    public String getTypeId() {
        return CameraLayerType.ID;
    }

    @Override
    public CameraKeyframe createDefaultKeyframe(int tick) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 pos = mc.player.position();
        Vec3 rot = new Vec3(mc.player.getXRot(), mc.player.getYRot(), 0);
        float fov = (float) (int) mc.options.fov().get();
        return new CameraKeyframe(this, tick, new KeyframePosition(pos, rot, fov));
    }
}
