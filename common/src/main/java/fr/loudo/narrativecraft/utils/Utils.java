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

package fr.loudo.narrativecraft.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

public class Utils {

    public static final String ONLY_NUMBERS = "^[0-9]*$";
    public static final String NO_SPECIAL_CHARACTERS = "^[a-zA-Z0-9_ ]*$";

    public static void sendError(Component message, Player player) {
        if (player == null) return;
        Component errorMsg = Component.empty()
                .append("[" + NarrativeCraftMod.MOD_NAME + "] ")
                .append(message)
                .withStyle(ChatFormatting.RED);
        player.sendSystemMessage(errorMsg);
    }

    public static String getEntityTypeString(EntityType<?> entityType) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString();
    }

    public static String getEntityTypeString(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }

    public static CompoundTag nbtFromString(String nbtString) throws CommandSyntaxException {
        return TagParser.parseCompoundFully(nbtString);
    }
}
