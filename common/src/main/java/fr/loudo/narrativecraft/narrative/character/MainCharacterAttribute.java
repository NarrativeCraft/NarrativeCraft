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

package fr.loudo.narrativecraft.narrative.character;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class MainCharacterAttribute {

    public enum SkinMode {
        SKIN_FROM_FILE,
        SKIN_OF_PLAYER,
        CLIENT_HAS_CHARACTER_SKIN
    }

    public static final StreamCodec<ByteBuf, MainCharacterAttribute> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            MainCharacterAttribute::isMainCharacter,
            ByteBufCodecs.idMapper(i -> SkinMode.values()[i], SkinMode::ordinal),
            MainCharacterAttribute::getSkin,
            MainCharacterAttribute::new);

    private boolean mainCharacter;
    private SkinMode skin;

    public MainCharacterAttribute() {
        this.mainCharacter = false;
        this.skin = SkinMode.SKIN_FROM_FILE;
    }

    public MainCharacterAttribute(boolean mainCharacter, SkinMode skin) {
        this.mainCharacter = mainCharacter;
        this.skin = skin;
    }

    public MainCharacterAttribute(MainCharacterAttribute other) {
        this.mainCharacter = other.mainCharacter;
        this.skin = other.skin;
    }

    public boolean isMainCharacter() {
        return mainCharacter;
    }

    public void setMainCharacter(boolean mainCharacter) {
        this.mainCharacter = mainCharacter;
    }

    public SkinMode getSkin() {
        return skin;
    }

    public void setSkin(SkinMode skin) {
        this.skin = skin;
    }
}
