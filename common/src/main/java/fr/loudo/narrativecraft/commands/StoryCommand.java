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

package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.managers.PlayerSessionManager;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryCompilerHandler;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class StoryCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("story")
                        .then(Commands.literal("reload")
                                .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                                .executes(StoryCommand::reload))
                        .then(Commands.literal("play")
                                .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    return playFor(ctx, player, null);
                                })
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                            return playFor(ctx, target, null);
                                        })
                                        .then(Commands.argument("chapter_index", IntegerArgumentType.integer())
                                                .suggests(CommandSuggestions::suggestChapters)
                                                .then(Commands.argument("scene_name", StringArgumentType.string())
                                                        .suggests(CommandSuggestions::suggestSceneByChapter)
                                                        .executes(ctx -> {
                                                            ServerPlayer target =
                                                                    EntityArgument.getPlayer(ctx, "target");
                                                            return playFor(
                                                                    ctx,
                                                                    target,
                                                                    IntegerArgumentType.getInteger(
                                                                            ctx, "chapter_index"),
                                                                    StringArgumentType.getString(ctx, "scene_name"));
                                                        })))))
                        .then(Commands.literal("stop")
                                .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    return stopFor(ctx, player);
                                })
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                            return stopFor(ctx, target);
                                        })))));
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        context.getSource()
                .sendSystemMessage(Translation.message("story.compiling").withStyle(ChatFormatting.YELLOW));

        StoryCompilerHandler.LibraryResult result = StoryCompilerHandler.compileLibrary();

        if (result.getDefaultError() != null) {
            context.getSource().sendSystemMessage(Component.empty());
            NarrativeCraftMod.LOGGER.error("Failed to compile story: {}", result.getDefaultError());
            context.getSource()
                    .sendFailure(Component.literal(result.getDefaultError()).withStyle(ChatFormatting.RED));
            return 0;
        }

        List<StoryCompilerHandler.TagError> tagErrors = result.getDefaultTagErrors();
        if (!tagErrors.isEmpty()) {
            for (StoryCompilerHandler.TagError tagError : tagErrors) {
                context.getSource().sendFailure(tagError.toMessage());
            }
            context.getSource()
                    .sendFailure(Translation.message(
                                    "story.error_compilation",
                                    Component.literal(String.valueOf(tagErrors.size()))
                                            .withStyle(ChatFormatting.GOLD))
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        NarrativeCraftMod.getInstance().setStoryLibrary(result.getLibrary());
        reportLocaleIssues(context, result);
        LocaleCommand.broadcastLocales();

        context.getSource()
                .sendSuccess(() -> Translation.message("story.compiled").withStyle(ChatFormatting.GREEN), false);

        return Command.SINGLE_SUCCESS;
    }

    private static void reportLocaleIssues(
            CommandContext<CommandSourceStack> context, StoryCompilerHandler.LibraryResult result) {
        result.getLocaleErrors().forEach((locale, error) -> context.getSource()
                .sendFailure(Translation.message(
                                "locale.compile_failed", Component.literal(locale), Component.literal(error))
                        .withStyle(ChatFormatting.RED)));

        result.getLocaleTagErrors().forEach((locale, tagErrors) -> {
            for (StoryCompilerHandler.TagError tagError : tagErrors) {
                context.getSource().sendFailure(tagError.toMessage());
            }
            context.getSource()
                    .sendFailure(Translation.message(
                                    "locale.tag_errors",
                                    Component.literal(locale),
                                    Component.literal(String.valueOf(tagErrors.size())))
                            .withStyle(ChatFormatting.RED));
        });

        for (String locale : result.getStructureMismatches()) {
            context.getSource()
                    .sendSystemMessage(Translation.message("locale.structure_mismatch", Component.literal(locale))
                            .withStyle(ChatFormatting.GOLD));
        }
    }

    private static int playFor(CommandContext<CommandSourceStack> context, ServerPlayer target, String knotPath) {
        if (!NarrativeCraftMod.getInstance().hasCompiledStory()) {
            context.getSource()
                    .sendFailure(Translation.message("story.not_compiled").withStyle(ChatFormatting.RED));
            return 0;
        }

        PlayerSessionManager sessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession session = sessionManager.getByPlayer(target);

        StoryHandler existing = session.getStoryHandler();
        if (existing != null) {
            existing.stop();
        }

        try {
            StoryHandler storyHandler = new StoryHandler(session);
            session.setStoryHandler(storyHandler);
            if (knotPath != null) {
                storyHandler.start(knotPath);
            } else {
                storyHandler.start();
            }
        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error(
                    "Failed to start story for player {}", target.getName().getString(), e);
            context.getSource().sendFailure(Component.literal(e.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }

        context.getSource()
                .sendSuccess(
                        () -> Translation.message("story.started", target.getName())
                                .withStyle(ChatFormatting.GREEN),
                        false);
        return Command.SINGLE_SUCCESS;
    }

    private static int playFor(
            CommandContext<CommandSourceStack> context, ServerPlayer target, int chapterIndex, String sceneName) {
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(chapterIndex);
        if (chapter == null) {
            context.getSource()
                    .sendFailure(Translation.message(
                            "error.not_exists", Translation.message("chapter").getString(), chapterIndex));
            return 0;
        }

        Scene scene = chapter.getSceneManager().getByName(sceneName);
        if (scene == null) {
            context.getSource()
                    .sendFailure(Translation.message(
                            "error.not_exists", Translation.message("scene").getString(), sceneName));
            return 0;
        }

        return playFor(context, target, scene.knotName());
    }

    private static int stopFor(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        PlayerSessionManager sessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession session = sessionManager.getByPlayer(target);

        StoryHandler storyHandler = session.getStoryHandler();
        if (storyHandler == null) {
            context.getSource()
                    .sendFailure(Translation.message("story.not_running", target.getName())
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        storyHandler.stop();
        session.setStoryHandler(null);

        context.getSource()
                .sendSuccess(
                        () -> Translation.message("story.stopped", target.getName())
                                .withStyle(ChatFormatting.GREEN),
                        false);
        return Command.SINGLE_SUCCESS;
    }
}
