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
import fr.loudo.narrativecraft.network.NarrativePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record C2SCutsceneControl(State state) implements NarrativePacket {

    public static final ResourceLocation TYPE = new ResourceLocation(NarrativeCraftMod.MOD_ID, "cutscene_control");

    public static C2SCutsceneControl read(FriendlyByteBuf buf) {
        return new C2SCutsceneControl(buf.readEnum(State.class));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(state);
    }

    @Override
    public ResourceLocation type() {
        return TYPE;
    }

    public enum State {
        PLAY,
        PAUSE,
        QUIT;
    }
}
