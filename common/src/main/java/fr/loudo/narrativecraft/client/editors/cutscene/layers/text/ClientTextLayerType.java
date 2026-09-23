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

package fr.loudo.narrativecraft.client.editors.cutscene.layers.text;

import fr.loudo.narrativecraft.api.client.editors.cutscene.ClientCutsceneLayerPlayer;
import fr.loudo.narrativecraft.api.client.editors.cutscene.ClientCutsceneLayerType;
import fr.loudo.narrativecraft.narrative.cutscene.layers.text.TextKeyframe;
import fr.loudo.narrativecraft.narrative.cutscene.layers.text.TextLayer;
import fr.loudo.narrativecraft.narrative.cutscene.layers.text.TextLayerType;

public class ClientTextLayerType implements ClientCutsceneLayerType<TextLayer> {

    @Override
    public String getTypeId() {
        return TextLayerType.ID;
    }

    @Override
    public Class<TextLayer> getLayerClass() {
        return TextLayer.class;
    }

    @Override
    public TextKeyframe createDefaultKeyframe(TextLayer layer, int tick) {
        return new TextKeyframe(layer, tick);
    }

    @Override
    public ClientCutsceneLayerPlayer createPlayer(TextLayer layer) {
        return new ClientTextLayerPlayer(layer);
    }
}
