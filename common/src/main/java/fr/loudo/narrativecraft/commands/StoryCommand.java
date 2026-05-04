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

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.managers.PlayerSessionManager;
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
import net.minecraft.server.permissions.Permissions;

public class StoryCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("story")
                        .then(Commands.literal("reload")
                                .requires(commandSourceStack ->
                                        commandSourceStack.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                                .executes(StoryCommand::reload))
                        .then(Commands.literal("play")
                                .requires(commandSourceStack ->
                                        commandSourceStack.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    return playFor(ctx, player);
                                })
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                            return playFor(ctx, target);
                                        })))
                        .then(Commands.literal("stop")
                                .requires(commandSourceStack ->
                                        commandSourceStack.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
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

        String compiledJson;
        try {
            compiledJson = StoryCompilerHandler.compileToJson();
        } catch (Exception e) {
            context.getSource().sendSystemMessage(Component.empty());
            NarrativeCraftMod.LOGGER.error("Failed to compile story", e);
            context.getSource().sendFailure(Component.literal(e.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }

        List<StoryCompilerHandler.TagError> tagErrors = StoryCompilerHandler.validateTags();
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

        NarrativeCraftMod.getInstance().setCompiledStoryJson(compiledJson);
        context.getSource()
                .sendSuccess(() -> Translation.message("story.compiled").withStyle(ChatFormatting.GREEN), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int playFor(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        String compiledJson = NarrativeCraftMod.getInstance().getCompiledStoryJson();
        if (compiledJson == null) {
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
            StoryHandler storyHandler = new StoryHandler(session, compiledJson);
            session.setStoryHandler(storyHandler);
            storyHandler.start();
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
