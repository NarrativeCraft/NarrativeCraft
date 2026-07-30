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

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.EasingType;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Interpolation;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.KeyframeSegment;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayerType;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.cutscene.ClientCutsceneMakerEditorMaker;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.CameraKeyframe;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.KeyframePosition;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public class CameraLayer extends CutsceneLayer {

    private int lastSentChunkX = Integer.MIN_VALUE;
    private int lastSentChunkZ = Integer.MIN_VALUE;

    public CameraLayer(ICutsceneLayerType layerType) {
        super(layerType);
    }

    @Override
    public CameraKeyframe createDefaultKeyframe(int tick) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 pos = mc.player.position();
        pos = pos.add(0, mc.player.getEyeHeight(), 0);
        ClientCutsceneMakerEditorMaker editor =
                ClientNarrativeCraftMod.getInstance().getCutsceneMakerEditor();
        float roll = editor.getPreviewRoll();
        Vec3 rot = new Vec3(mc.player.getXRot(), mc.player.getYRot(), roll);
        return new CameraKeyframe(this, tick, new KeyframePosition(pos, rot));
    }

    @Override
    public boolean execute(float tick) {
        if (!isTickCoveredBy(tick)) return false;
        KeyframePosition keyframePosition = getInterpolatedPosition(tick);
        Vec3 position = keyframePosition.getPosition();
        Vec3 rot = keyframePosition.getRotation();

        ClientNarrativeCraftMod.getInstance()
                .getPlayerSession()
                .getCutsceneDataSession()
                .setKeyframePosition(keyframePosition);

        LocalPlayer localPlayer = Minecraft.getInstance().player;
        position = position.subtract(0, localPlayer.getEyeHeight(), 0);
        localPlayer.setPos(position);
        localPlayer.setXRot((float) rot.x);
        localPlayer.setYRot((float) rot.y);
        localPlayer.setYHeadRot((float) rot.y);

        int chunkX = (int) position.x >> 4;
        int chunkZ = (int) position.z >> 4;
        if (chunkX != lastSentChunkX || chunkZ != lastSentChunkZ) {
            localPlayer.connection.send(new ServerboundMovePlayerPacket.PosRot(
                    position.x, position.y, position.z, (float) rot.y, (float) rot.x, localPlayer.onGround()));
            lastSentChunkX = chunkX;
            lastSentChunkZ = chunkZ;
        }
        return true;
    }

    public List<CameraKeyframe> getSortedCameraKeyframes() {
        return getSortedKeyframes(CameraKeyframe.class);
    }

    public KeyframePosition getInterpolatedPosition(float tick) {
        List<CameraKeyframe> sorted = getSortedKeyframes(CameraKeyframe.class);

        if (sorted.isEmpty()) return null;
        if (sorted.size() == 1) return sorted.get(0).getPosition();
        if (tick <= sorted.get(0).getTick()) return sorted.get(0).getPosition();
        if (tick >= sorted.get(sorted.size() - 1).getTick())
            return sorted.get(sorted.size() - 1).getPosition();

        KeyframeSegment<CameraKeyframe> seg = findSegment(sorted, tick);
        if (seg.to().getEasing() == EasingType.SMOOTH) {
            return interpolateCatmullRom(
                    seg.p0().getPosition(),
                    seg.from().getPosition(),
                    seg.to().getPosition(),
                    seg.p3().getPosition(),
                    seg.rawT());
        } else {
            return interpolateLinear(
                    seg.from().getPosition(),
                    seg.to().getPosition(),
                    Interpolation.applyEasing(seg.to().getEasing(), seg.rawT()));
        }
    }

    private static KeyframePosition interpolateLinear(KeyframePosition a, KeyframePosition b, double t) {
        return new KeyframePosition(
                new Vec3(
                        Interpolation.lerp(a.getPosition().x, b.getPosition().x, t),
                        Interpolation.lerp(a.getPosition().y, b.getPosition().y, t),
                        Interpolation.lerp(a.getPosition().z, b.getPosition().z, t)),
                new Vec3(
                        Interpolation.lerp(a.getRotation().x, b.getRotation().x, t),
                        Interpolation.lerp(a.getRotation().y, b.getRotation().y, t),
                        Interpolation.lerp(a.getRotation().z, b.getRotation().z, t)));
    }

    private static KeyframePosition interpolateCatmullRom(
            KeyframePosition p0, KeyframePosition p1, KeyframePosition p2, KeyframePosition p3, double t) {
        return new KeyframePosition(
                new Vec3(
                        Interpolation.catmullRom(
                                p0.getPosition().x, p1.getPosition().x, p2.getPosition().x, p3.getPosition().x, t),
                        Interpolation.catmullRom(
                                p0.getPosition().y, p1.getPosition().y, p2.getPosition().y, p3.getPosition().y, t),
                        Interpolation.catmullRom(
                                p0.getPosition().z, p1.getPosition().z, p2.getPosition().z, p3.getPosition().z, t)),
                new Vec3(
                        Interpolation.catmullRom(
                                p0.getRotation().x, p1.getRotation().x, p2.getRotation().x, p3.getRotation().x, t),
                        Interpolation.catmullRom(
                                p0.getRotation().y, p1.getRotation().y, p2.getRotation().y, p3.getRotation().y, t),
                        Interpolation.catmullRom(
                                p0.getRotation().z, p1.getRotation().z, p2.getRotation().z, p3.getRotation().z, t)));
    }
}
