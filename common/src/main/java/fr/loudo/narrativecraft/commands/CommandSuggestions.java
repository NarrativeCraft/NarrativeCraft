package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import net.minecraft.commands.CommandSourceStack;

import java.util.concurrent.CompletableFuture;

public class CommandSuggestions {

    public static CompletableFuture<Suggestions> suggestChapters(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (Chapter chapter : NarrativeCraftMod.getInstance().getChapterManager().getList()) {
            builder.suggest(chapter.getChapterIndex());
        }
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
