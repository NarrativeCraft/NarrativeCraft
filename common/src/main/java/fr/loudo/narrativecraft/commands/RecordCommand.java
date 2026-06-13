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
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.managers.RecordingManager;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.playback.Playback;
import fr.loudo.narrativecraft.recording.Recording;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.UtilsServer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class RecordCommand {

    private static final RecordingManager RECORDING_MANAGER =
            NarrativeCraftMod.getInstance().getRecordingManager();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("nc")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                .then(Commands.literal("record")
                        .then(Commands.literal("start")
                                .executes(RecordCommand::startRecord)
                                .then(Commands.literal("with")
                                        .then(Commands.argument("subscene_names", StringArgumentType.greedyString())
                                                .suggests(RecordCommand::suggestSubscenes)
                                                .executes(RecordCommand::startRecordWithSubscenes)))))
                .then(Commands.literal("record").then(Commands.literal("stop").executes(RecordCommand::stopRecord)))
                .then(Commands.literal("record")
                        .then(Commands.literal("discard").executes(RecordCommand::discardRecord)))
                .then(Commands.literal("record")
                        .then(Commands.literal("save")
                                .then(Commands.argument("record_name", StringArgumentType.string())
                                        .executes(RecordCommand::saveRecord)))));
    }

    private static int startRecord(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (RECORDING_MANAGER.isRecording(player)) {
            context.getSource().sendFailure(Translation.message("record.already_recording"));
            return 0;
        }

        PlayerSession playerSession = UtilsServer.getPlayerSessionByPlayer(player);
        if (!playerSession.sessionSet()) {
            context.getSource().sendFailure(Translation.message("session.no_session"));
            return 0;
        }

        return doStartRecord(context, playerSession, List.of());
    }

    private static int startRecordWithSubscenes(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (RECORDING_MANAGER.isRecording(player)) {
            context.getSource().sendFailure(Translation.message("record.already_recording"));
            return 0;
        }

        PlayerSession playerSession = UtilsServer.getPlayerSessionByPlayer(player);
        if (!playerSession.sessionSet()) {
            context.getSource().sendFailure(Translation.message("session.no_session"));
            return 0;
        }

        String subsceneNamesRaw = StringArgumentType.getString(context, "subscene_names");
        List<Subscene> subscenes = new ArrayList<>();
        for (String name : parseSubscenesName(subsceneNamesRaw)) {
            if (name.isEmpty()) continue;
            Subscene subscene = playerSession.getScene().getSubsceneManager().getByName(name);
            if (subscene == null) {
                context.getSource()
                        .sendFailure(Translation.message(
                                "error.not_exists",
                                Translation.message("subscene").getString(),
                                name));
                return 0;
            }
            subscenes.add(subscene);
        }

        return doStartRecord(context, playerSession, subscenes);
    }

    private static int doStartRecord(
            CommandContext<CommandSourceStack> context, PlayerSession playerSession, List<Subscene> subscenes) {
        ServerPlayer player = context.getSource().getPlayer();

        List<Animation> animationsToPlay = new ArrayList<>();
        for (Subscene subscene : subscenes) {
            for (Animation animation : subscene.getAnimations()) {
                if (!animation.initialize()) {
                    context.getSource()
                            .sendFailure(Translation.message("error.animation.initialize", animation.getName()));
                    return 0;
                }
                animationsToPlay.add(animation);
            }
        }

        Recording recording = RECORDING_MANAGER.getRecording(player);
        if (recording != null) {
            RECORDING_MANAGER.remove(recording);
        }
        recording = new Recording(playerSession);
        RECORDING_MANAGER.add(recording);

        for (Animation animation : animationsToPlay) {
            Playback playback = new Playback(animation, player);
            playback.setKillOnEnd(true);
            NarrativeCraftMod.getInstance().getPlaybackManager().add(playback);
            playback.start();
            recording.addSubscenePlayback(playback);
        }

        recording.start();
        context.getSource().sendSuccess(() -> Translation.message("record.start"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static CompletableFuture<Suggestions> suggestSubscenes(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession session = UtilsServer.getPlayerSessionByPlayer(player);
        if (!session.sessionSet()) return builder.buildFuture();

        String remaining = builder.getRemaining();
        int lastSpace = remaining.lastIndexOf(' ');
        String currentWord = lastSpace >= 0 ? remaining.substring(lastSpace + 1) : remaining;

        Set<String> alreadyTyped = new HashSet<>();
        if (lastSpace > 0) {
            alreadyTyped.addAll(Arrays.asList(remaining.substring(0, lastSpace).split("\\s+")));
        }

        SuggestionsBuilder offsetBuilder = builder.createOffset(builder.getStart() + lastSpace + 1);

        for (Subscene subscene : session.getScene().getSubsceneManager().getList()) {
            String name = subscene.getName();
            if (!alreadyTyped.contains(name) && name.toLowerCase().startsWith(currentWord.toLowerCase())) {
                if (name.contains(" ")) {
                    offsetBuilder.suggest("\"" + name + "\"");
                } else {
                    offsetBuilder.suggest(name);
                }
            }
        }

        return offsetBuilder.buildFuture();
    }

    private static int stopRecord(CommandContext<CommandSourceStack> context) {

        ServerPlayer player = context.getSource().getPlayer();

        PlayerSession playerSession = UtilsServer.getPlayerSessionByPlayer(player);
        if (!playerSession.sessionSet()) {
            context.getSource().sendFailure(Translation.message("session.no_session"));
            return 0;
        }

        Recording recording = RECORDING_MANAGER.getRecording(player);

        if (recording == null || !recording.isRecording()) {
            context.getSource().sendFailure(Translation.message("record.not_recording"));
            return 0;
        }

        recording.stop();
        context.getSource().sendSuccess(() -> Translation.message("record.stop"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int discardRecord(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();

        Recording recording = RECORDING_MANAGER.getRecording(player);
        if (recording == null) {
            context.getSource().sendFailure(Translation.message("record.not_recording"));
            return 0;
        }

        recording.stop();
        RECORDING_MANAGER.remove(recording);

        context.getSource().sendSuccess(() -> Translation.message("record.discarded"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int saveRecord(CommandContext<CommandSourceStack> context) {

        String recordName = StringArgumentType.getString(context, "record_name");

        ServerPlayer player = context.getSource().getPlayer();

        PlayerSession playerSession = UtilsServer.getPlayerSessionByPlayer(player);
        if (!playerSession.sessionSet()) {
            context.getSource().sendFailure(Translation.message("session.no_session"));
            return 0;
        }

        Recording recording = RECORDING_MANAGER.getRecording(player);

        if (recording == null) {
            context.getSource().sendFailure(Translation.message("record.recorded_nothing"));
            return 0;
        }

        if (recording.isRecording()) {
            context.getSource().sendFailure(Translation.message("record.recording"));
        }

        CharacterManager characterManager = NarrativeCraftMod.getInstance().getCharacterManager();
        if (characterManager.getList().isEmpty()) {
            context.getSource().sendFailure(Translation.message("error.record.no_characters"));
            return 0;
        }

        Animation existingAnimation =
                playerSession.getScene().getAnimationManager().getByName(recordName);
        if (existingAnimation != null) {
            if (!recordName.equals(recording.getPendingOverwriteName())) {
                recording.setPendingOverwriteName(recordName);
                context.getSource().sendFailure(Translation.message("record.overwrite_confirm", recordName));
                return 0;
            }
            recording.setPendingOverwriteName(null);
            context.getSource().sendSuccess(() -> Translation.message("record.saving"), false);
            if (recording.save(recordName, existingAnimation)) {
                context.getSource().sendSuccess(() -> Translation.message("record.saved"), false);
            } else {
                context.getSource().sendFailure(Translation.message("error.record.save"));
            }
            RECORDING_MANAGER.remove(recording);
            return Command.SINGLE_SUCCESS;
        }

        recording.setPendingOverwriteName(null);
        context.getSource().sendSuccess(() -> Translation.message("record.saving"), false);
        if (recording.save(recordName)) {
            context.getSource().sendSuccess(() -> Translation.message("record.saved"), false);
        } else {
            context.getSource().sendFailure(Translation.message("error.record.save"));
        }

        RECORDING_MANAGER.remove(recording);

        return Command.SINGLE_SUCCESS;
    }

    private static List<String> parseSubscenesName(String raw) {
        List<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < raw.length()) {
            while (i < raw.length() && raw.charAt(i) == ' ') i++;
            if (i >= raw.length()) break;
            if (raw.charAt(i) == '"') {
                int start = ++i;
                while (i < raw.length() && raw.charAt(i) != '"') i++;
                tokens.add(raw.substring(start, i));
                if (i < raw.length()) i++;
            } else {
                int start = i;
                while (i < raw.length() && raw.charAt(i) != ' ') i++;
                tokens.add(raw.substring(start, i));
            }
        }
        return tokens;
    }
}
