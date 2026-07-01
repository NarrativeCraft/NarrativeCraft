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

package fr.loudo.narrativecraft.network.cutscene;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.network.NarrativePacket;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class C2SCutsceneSave implements NarrativePacket {

    private final UUID chapterId;
    private final UUID sceneId;
    private final UUID cutsceneId;
    private final String layersJson;
    private final int manualMaxTick;

    public C2SCutsceneSave(Cutscene cutscene, String layersJson) {
        this.chapterId = cutscene.getScene().getChapter().getId();
        this.sceneId = cutscene.getScene().getId();
        this.cutsceneId = cutscene.getId();
        this.layersJson = layersJson;
        this.manualMaxTick = cutscene.getManualMaxTick();
    }

    public C2SCutsceneSave(UUID chapterId, UUID sceneId, UUID cutsceneId, String layersJson, int manualMaxTick) {
        this.chapterId = chapterId;
        this.sceneId = sceneId;
        this.cutsceneId = cutsceneId;
        this.layersJson = layersJson;
        this.manualMaxTick = manualMaxTick;
    }

    public static final ResourceLocation TYPE = new ResourceLocation(NarrativeCraftMod.MOD_ID, "cutscene_save");

    public static C2SCutsceneSave read(FriendlyByteBuf buf) {
        return new C2SCutsceneSave(buf.readUUID(), buf.readUUID(), buf.readUUID(), buf.readUtf(), buf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(chapterId);
        buf.writeUUID(sceneId);
        buf.writeUUID(cutsceneId);
        buf.writeUtf(layersJson);
        buf.writeVarInt(manualMaxTick);
    }

    public UUID getChapterId() {
        return chapterId;
    }

    public UUID getSceneId() {
        return sceneId;
    }

    public UUID getCutsceneId() {
        return cutsceneId;
    }

    public String getLayersJson() {
        return layersJson;
    }

    public int getManualMaxTick() {
        return manualMaxTick;
    }

    @Override
    public ResourceLocation type() {
        return TYPE;
    }
}
