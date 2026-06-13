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
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.playback.Playback;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.UtilsServer;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class PlaybackCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .requires(stack -> stack.hasPermission(2))
                .then(Commands.literal("playback")
                        .then(Commands.literal("start")
                                .then(Commands.argument("animation_name", StringArgumentType.string())
                                        .suggests(PlaybackCommand::suggestAnimations)
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(PlaybackCommand::startPlaybackToPlayers))
                                        .executes(context -> startPlayback(
                                                context, StringArgumentType.getString(context, "animation_name")))))));
    }

    private static Animation getAnimation(CommandContext<CommandSourceStack> context, String animationName) {
        ServerPlayer player = context.getSource().getPlayer();

        PlayerSession session = UtilsServer.getPlayerSessionByPlayer(player);
        if (!session.sessionSet()) {
            context.getSource().sendFailure(Translation.message("session.no_session"));
            return null;
        }

        Animation animation = session.getScene().getAnimationManager().getByName(animationName);
        if (animation == null) {
            context.getSource()
                    .sendFailure(Translation.message(
                            "error.not_exists", Translation.message("animation").getString(), animationName));
            return null;
        }
        if (!animation.initialize()) {
            context.getSource().sendFailure(Translation.message("error.animation.initialize", animation.getName()));
            return null;
        }
        return animation;
    }

    private static int startPlayback(CommandContext<CommandSourceStack> context, String animationName) {

        Animation animation = getAnimation(context, animationName);
        if (animation == null) {
            return 0;
        }

        ServerPlayer player = context.getSource().getPlayer();
        Playback playback = new Playback(animation, player);
        playback.setKillOnEnd(true);
        NarrativeCraftMod.getInstance().getPlaybackManager().add(playback);
        playback.start();

        context.getSource().sendSuccess(() -> Translation.message("playback.start", animationName), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int startPlaybackToPlayers(CommandContext<CommandSourceStack> context) {

        try {
            String animationName = StringArgumentType.getString(context, "animation_name");
            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");

            Animation animation = getAnimation(context, animationName);
            if (animation == null) {
                return 0;
            }

            ServerPlayer player = context.getSource().getPlayer();

            Playback playback = new Playback(animation, player);
            playback.setKillOnEnd(true);
            NarrativeCraftMod.getInstance().getPlaybackManager().add(playback);
            playback.start(players);

            context.getSource()
                    .sendSuccess(() -> Translation.message("playback.start_for_players", animationName), false);

        } catch (CommandSyntaxException e) {
            NarrativeCraftMod.LOGGER.error("Failed to start playback", e);
            return 0;
        }

        return Command.SINGLE_SUCCESS;
    }

    private static CompletableFuture<Suggestions> suggestAnimations(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession session = UtilsServer.getPlayerSessionByPlayer(player);
        if (!session.sessionSet()) return builder.buildFuture();

        for (Animation animation : session.getScene().getAnimationManager().getList()) {
            String name = animation.getName();
            if (name.contains(" ")) {
                builder.suggest("\"" + name + "\"");
            } else {
                builder.suggest(name);
            }
        }

        return builder.buildFuture();
    }
}
