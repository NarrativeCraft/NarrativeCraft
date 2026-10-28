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

package fr.loudo.narrativecraft.studio.assets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TreeIO {

    public static JsonObject serialize(Tree tree) {
        JsonObject object = new JsonObject();
        object.addProperty("name", tree.getName());
        object.addProperty("directory", tree.isDirectory());
        JsonArray child = new JsonArray();
        for (Tree subTree : tree.getChild().stream().sorted(Assets.ORDER).toList()) {
            child.add(serialize(subTree));
        }
        object.add("child", child);
        return object;
    }

    public static Tree deserialize(JsonObject object) {
        String name = object.get("name").getAsString();
        boolean directory = object.get("directory").getAsBoolean();
        Tree tree = new Tree(name, directory);
        for (JsonElement element : object.getAsJsonArray("child")) {
            tree.addChild(deserialize(element.getAsJsonObject()));
        }
        return tree;
    }
}
