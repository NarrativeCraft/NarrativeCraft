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

package fr.loudo.narrativecraft.narrative.cutscene;

import com.google.gson.*;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.NarrativeDeserializer;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CutsceneDeserializer extends NarrativeDeserializer<Cutscene> {

    @Override
    public Cutscene deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        if (!obj.has("sceneId") || !obj.has("chapterId")) {
            return null;
        }

        UUID id = parseId(obj);
        String name = parseName(obj);
        String description = parseDescription(obj);
        UUID sceneId = UUID.fromString(obj.get("sceneId").getAsString());
        UUID chapterId = UUID.fromString(obj.get("chapterId").getAsString());

        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(chapterId);
        if (chapter == null) return null;

        Scene scene = chapter.getSceneManager().getById(sceneId);
        if (scene == null) return null;

        List<Animation> animations = new ArrayList<>();
        if (obj.has("animationIds")) {
            for (JsonElement element : obj.getAsJsonArray("animationIds")) {
                Animation animation = scene.getAnimationManager().getById(UUID.fromString(element.getAsString()));
                if (animation != null) animations.add(animation);
            }
        }

        List<Subscene> subscenes = new ArrayList<>();
        if (obj.has("subsceneIds")) {
            for (JsonElement element : obj.getAsJsonArray("subsceneIds")) {
                Subscene subscene = scene.getSubsceneManager().getById(UUID.fromString(element.getAsString()));
                if (subscene != null) subscenes.add(subscene);
            }
        }

        return new Cutscene(id, name, description, scene, animations, subscenes);
    }
}
