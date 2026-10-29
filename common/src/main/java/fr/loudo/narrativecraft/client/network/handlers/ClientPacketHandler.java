/*
 * NarrativeCraft - Create narrative games inside Minecraft. No coding, no game engine, only text and logic.
 * Copyright (c) 2026 LOUDO and contributors
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

package fr.loudo.narrativecraft.client.network.handlers;

import com.google.common.hash.Hashing;
import com.google.gson.JsonParser;
import fr.loudo.narrativecraft.client.studio.NarrativeStudioClient;
import fr.loudo.narrativecraft.files.NarrativeFiles;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.network.S2CInitTree;
import fr.loudo.narrativecraft.network.S2COpenScene;
import fr.loudo.narrativecraft.studio.assets.TreeIO;
import java.nio.charset.StandardCharsets;

public class ClientPacketHandler {

    public static void initTree(S2CInitTree packet) {
        String assetsHash = Hashing.sha256()
                .hashString(packet.treeJson(), StandardCharsets.UTF_8)
                .toString();
        NarrativeStudioClient.getInstance().getAssets().setHash(assetsHash);
        NarrativeStudioClient.getInstance()
                .setAssetsRoot(TreeIO.deserialize(
                        JsonParser.parseString(packet.treeJson()).getAsJsonObject()));
    }

    public static void openScene(S2COpenScene packet) {
        Scene scene = NarrativeFiles.getInstance()
                .getSceneFiles()
                .io()
                .read(JsonParser.parseString(packet.data()).getAsJsonObject());
        if (scene == null) return;

        NarrativeStudioClient.getInstance().getTabsManager().open(scene);
    }
}
