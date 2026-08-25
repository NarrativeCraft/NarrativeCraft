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

package fr.loudo.narrativecraft.api.utils;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record UserPosition(double x, double y, double z, float xRot, float yRot) {

    public static UserPosition of(Entity entity) {
        return new UserPosition(entity.getX(), entity.getY(), entity.getZ(), entity.getXRot(), entity.getYRot());
    }

    public static UserPosition of(Vec3 position, float xRot, float yRot) {
        return new UserPosition(position.x, position.y, position.z, xRot, yRot);
    }

    public static UserPosition deserialize(JsonObject json) {
        double x = json.has("x") ? json.get("x").getAsDouble() : 0;
        double y = json.has("y") ? json.get("y").getAsDouble() : 0;
        double z = json.has("z") ? json.get("z").getAsDouble() : 0;
        float xRot = json.has("xRot") ? json.get("xRot").getAsFloat() : 0;
        float yRot = json.has("yRot") ? json.get("yRot").getAsFloat() : 0;
        return new UserPosition(x, y, z, xRot, yRot);
    }

    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("x", x);
        json.addProperty("y", y);
        json.addProperty("z", z);
        json.addProperty("xRot", xRot);
        json.addProperty("yRot", yRot);
        return json;
    }

    public Vec3 position() {
        return new Vec3(x, y, z);
    }
}
