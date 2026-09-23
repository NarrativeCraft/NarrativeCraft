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

package fr.loudo.narrativecraft.narrative;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import java.util.UUID;

public abstract class NarrativeDeserializer<T> implements JsonDeserializer<T> {

    protected UUID parseId(JsonObject json) {
        return UUID.fromString(json.get("id").getAsString());
    }

    protected String parseName(JsonObject json) {
        return json.get("name").getAsString();
    }

    protected boolean hasSceneReference(JsonObject json) {
        return json.has("chapterId") && json.has("sceneId");
    }

    protected Scene resolveScene(JsonObject json) {
        UUID chapterId = UUID.fromString(json.get("chapterId").getAsString());
        UUID sceneId = UUID.fromString(json.get("sceneId").getAsString());
        return NarrativeCraftMod.getInstance().getEntryResolver().scene(chapterId, sceneId);
    }
}
