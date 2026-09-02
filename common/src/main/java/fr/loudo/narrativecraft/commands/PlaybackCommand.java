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
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.editors.cutscene.CutsceneMakerEditorMaker;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.network.cutscene.BiCutsceneEnter;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.playback.Playback;
import fr.loudo.narrativecraft.playback.PlaybackManager;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.UtilsServer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public class PlaybackCommand {

    private static final String CHAPTER_INDEX_ARGUMENT = "chapter_index";
    private static final String SCENE_NAME_ARGUMENT = "scene_name";
    private static final String ANIMATION_NAME_ARGUMENT = "animation_name";
    private static final String SUBSCENE_NAME_ARGUMENT = "subscene_name";
    private static final String CUTSCENE_NAME_ARGUMENT = "cutscene_name";
    private static final String TARGETS_ARGUMENT = "targets";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .requires(stack -> stack.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                .then(Commands.literal("playback")
                        .then(Commands.literal("start")
                                .then(entry(
                                        "animation",
                                        ANIMATION_NAME_ARGUMENT,
                                        CommandSuggestions::suggestAnimationsByScene,
                                        PlaybackCommand::startAnimation))
                                .then(entry(
                                        "subscene",
                                        SUBSCENE_NAME_ARGUMENT,
                                        CommandSuggestions::suggestSubscenesByScene,
                                        PlaybackCommand::startSubscene))
                                .then(entry(
                                        "cutscene",
                                        CUTSCENE_NAME_ARGUMENT,
                                        CommandSuggestions::suggestCutscenesByScene,
                                        PlaybackCommand::startCutscene)))
                        .then(Commands.literal("stop")
                                .then(entry(
                                        "animation",
                                        ANIMATION_NAME_ARGUMENT,
                                        CommandSuggestions::suggestAnimationsByScene,
                                        PlaybackCommand::stopAnimation))
                                .then(entry(
                                        "subscene",
                                        SUBSCENE_NAME_ARGUMENT,
                                        CommandSuggestions::suggestSubscenesByScene,
                                        PlaybackCommand::stopSubscene))
                                .then(entry(
                                        "cutscene",
                                        CUTSCENE_NAME_ARGUMENT,
                                        CommandSuggestions::suggestCutscenesByScene,
                                        PlaybackCommand::stopCutscene)))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> entry(
            String literal,
            String nameArgument,
            SuggestionProvider<CommandSourceStack> nameSuggestions,
            PlaybackAction action) {
        return Commands.literal(literal)
                .then(Commands.argument(CHAPTER_INDEX_ARGUMENT, IntegerArgumentType.integer())
                        .suggests(CommandSuggestions::suggestChapters)
                        .then(Commands.argument(SCENE_NAME_ARGUMENT, StringArgumentType.string())
                                .suggests(CommandSuggestions::suggestSceneByChapter)
                                .then(Commands.argument(nameArgument, StringArgumentType.string())
                                        .suggests(nameSuggestions)
                                        .executes(context -> run(context, nameArgument, action, false))
                                        .then(Commands.argument(TARGETS_ARGUMENT, EntityArgument.players())
                                                .executes(context -> run(context, nameArgument, action, true))))));
    }

    private static int run(
            CommandContext<CommandSourceStack> context,
            String nameArgument,
            PlaybackAction action,
            boolean explicitTargets)
            throws CommandSyntaxException {

        int chapterIndex = IntegerArgumentType.getInteger(context, CHAPTER_INDEX_ARGUMENT);
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(chapterIndex);
        if (chapter == null) {
            context.getSource()
                    .sendFailure(Translation.message(
                            "error.not_exists", Translation.message("chapter").getString(), chapterIndex));
            return 0;
        }

        String sceneName = StringArgumentType.getString(context, SCENE_NAME_ARGUMENT);
        Scene scene = chapter.getSceneManager().getByName(sceneName);
        if (scene == null) {
            context.getSource()
                    .sendFailure(Translation.message(
                            "error.not_exists", Translation.message("scene").getString(), sceneName));
            return 0;
        }

        Collection<ServerPlayer> targets = explicitTargets
                ? EntityArgument.getPlayers(context, TARGETS_ARGUMENT)
                : List.of(context.getSource().getPlayerOrException());
        if (targets.isEmpty()) {
            return 0;
        }

        String name = StringArgumentType.getString(context, nameArgument);
        return action.run(context, new PlaybackRequest(scene, name, targets, explicitTargets));
    }

    private static int startAnimation(CommandContext<CommandSourceStack> context, PlaybackRequest request) {
        Animation animation = request.scene().getAnimationManager().getByName(request.name());
        if (animation == null) {
            return notExists(context, "animation", request.name());
        }
        if (!startPlayback(context, animation, request)) {
            return 0;
        }
        sendStarted(context, "animation", request);
        return Command.SINGLE_SUCCESS;
    }

    private static int startSubscene(CommandContext<CommandSourceStack> context, PlaybackRequest request) {
        Subscene subscene = request.scene().getSubsceneManager().getByName(request.name());
        if (subscene == null) {
            return notExists(context, "subscene", request.name());
        }
        for (Animation animation : subscene.getAnimations()) {
            if (!startPlayback(context, animation, request)) {
                return 0;
            }
        }
        sendStarted(context, "subscene", request);
        return Command.SINGLE_SUCCESS;
    }

    private static int startCutscene(CommandContext<CommandSourceStack> context, PlaybackRequest request) {
        Cutscene cutscene = request.scene().getCutsceneManager().getByName(request.name());
        if (cutscene == null) {
            return notExists(context, "cutscene", request.name());
        }

        for (ServerPlayer target : request.targets()) {
            PlayerSession session = UtilsServer.getPlayerSessionByPlayer(target);
            CutsceneMakerEditorMaker editor =
                    new CutsceneMakerEditorMaker(cutscene, session, NarrativeEnvironment.PRODUCTION);
            editor.setStandalone(true);
            Services.PACKET.sendToPlayer(target, new BiCutsceneEnter(cutscene, NarrativeEnvironment.PRODUCTION));
            session.openEditor(editor);
            editor.start();
        }

        sendStarted(context, "cutscene", request);
        return Command.SINGLE_SUCCESS;
    }

    private static int stopAnimation(CommandContext<CommandSourceStack> context, PlaybackRequest request) {
        Animation animation = request.scene().getAnimationManager().getByName(request.name());
        if (animation == null) {
            return notExists(context, "animation", request.name());
        }
        int stopped = stopPlaybacks(request, playback -> playback.getAnimation().equals(animation));
        return sendStopped(context, "animation", request, stopped);
    }

    private static int stopSubscene(CommandContext<CommandSourceStack> context, PlaybackRequest request) {
        Subscene subscene = request.scene().getSubsceneManager().getByName(request.name());
        if (subscene == null) {
            return notExists(context, "subscene", request.name());
        }
        int stopped =
                stopPlaybacks(request, playback -> subscene.getAnimations().contains(playback.getAnimation()));
        return sendStopped(context, "subscene", request, stopped);
    }

    private static int stopCutscene(CommandContext<CommandSourceStack> context, PlaybackRequest request) {
        Cutscene cutscene = request.scene().getCutsceneManager().getByName(request.name());
        if (cutscene == null) {
            return notExists(context, "cutscene", request.name());
        }

        int stopped = 0;
        for (ServerPlayer target : request.targets()) {
            PlayerSession session = UtilsServer.getPlayerSessionByPlayer(target);
            if (!(session.getEditor() instanceof CutsceneMakerEditorMaker editor)) continue;
            if (!editor.getCutscene().equals(cutscene)) continue;
            session.closeEditor();
            stopped++;
        }

        return sendStopped(context, "cutscene", request, stopped);
    }

    private static boolean startPlayback(
            CommandContext<CommandSourceStack> context, Animation animation, PlaybackRequest request) {
        if (!animation.initialize()) {
            context.getSource().sendFailure(Translation.message("error.animation.initialize", animation.getName()));
            return false;
        }

        Playback playback = new Playback(animation, requester(context, request));
        playback.setKillOnEnd(true);
        NarrativeCraftMod.getInstance().getPlaybackManager().add(playback);
        playback.start(request.targets());
        return true;
    }

    private static int stopPlaybacks(PlaybackRequest request, Predicate<Playback> filter) {
        PlaybackManager playbackManager = NarrativeCraftMod.getInstance().getPlaybackManager();

        List<Playback> toStop = new ArrayList<>();
        for (Playback playback : playbackManager.getList()) {
            if (!filter.test(playback)) continue;
            if (Collections.disjoint(playback.getTargetedPlayers(), request.targets())) continue;
            toStop.add(playback);
        }

        for (Playback playback : toStop) {
            playback.stopAndKill();
            playbackManager.remove(playback);
        }

        return toStop.size();
    }

    private static ServerPlayer requester(CommandContext<CommandSourceStack> context, PlaybackRequest request) {
        ServerPlayer player = context.getSource().getPlayer();
        return player == null ? request.targets().iterator().next() : player;
    }

    private static int notExists(CommandContext<CommandSourceStack> context, String typeKey, String name) {
        context.getSource()
                .sendFailure(Translation.message(
                        "error.not_exists", Translation.message(typeKey).getString(), name));
        return 0;
    }

    private static void sendStarted(
            CommandContext<CommandSourceStack> context, String typeKey, PlaybackRequest request) {
        String type = Translation.message(typeKey).getString();
        context.getSource()
                .sendSuccess(
                        () -> Translation.message(
                                request.explicitTargets() ? "playback.start_for_players" : "playback.start",
                                type,
                                request.name()),
                        false);
    }

    private static int sendStopped(
            CommandContext<CommandSourceStack> context, String typeKey, PlaybackRequest request, int stopped) {
        String type = Translation.message(typeKey).getString();
        if (stopped == 0) {
            context.getSource().sendFailure(Translation.message("playback.not_playing", type, request.name()));
            return 0;
        }
        context.getSource().sendSuccess(() -> Translation.message("playback.stop", type, request.name()), false);
        return Command.SINGLE_SUCCESS;
    }

    @FunctionalInterface
    private interface PlaybackAction {
        int run(CommandContext<CommandSourceStack> context, PlaybackRequest request);
    }

    private record PlaybackRequest(
            Scene scene, String name, Collection<ServerPlayer> targets, boolean explicitTargets) {}
}
