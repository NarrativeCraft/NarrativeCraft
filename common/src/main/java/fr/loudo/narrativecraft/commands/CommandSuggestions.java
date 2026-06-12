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

package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.NarrativeCraftAPI;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;

public class CommandSuggestions {

    public static CompletableFuture<Suggestions> suggestChapters(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (Chapter chapter :
                NarrativeCraftMod.getInstance().getChapterManager().getList()) {
            builder.suggest(chapter.getChapterIndex());
        }
        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestAddons(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        NarrativeCraftAPI.getInstance().getAddonRegistry().getAll().forEach(addon -> builder.suggest(addon.getModId()));
        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestSceneByChapter(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        int chapterIndex = IntegerArgumentType.getInteger(context, "chapter_index");
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(chapterIndex);
        if (chapter == null) return builder.buildFuture();

        for (Scene scene : chapter.getSceneManager().getList()) {
            if (scene.getName().split(" ").length > 1) {
                builder.suggest("\"" + scene.getName() + "\"");
            } else {
                builder.suggest(scene.getName());
            }
        }
        return builder.buildFuture();
    }
}
