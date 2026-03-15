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
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.managers.RecordingManager;
import fr.loudo.narrativecraft.recording.Recording;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.UtilsServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public class RecordCommand {

    private static final RecordingManager RECORDING_MANAGER =
            NarrativeCraftMod.getInstance().getRecordingManager();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("nc")
                .requires(commandSourceStack ->
                        commandSourceStack.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                .then(Commands.literal("record").then(Commands.literal("start").executes(RecordCommand::startRecord)))
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

        Recording recording = new Recording(playerSession);
        RECORDING_MANAGER.add(recording);
        recording.start();

        context.getSource().sendSuccess(() -> Translation.message("record.start"), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int stopRecord(CommandContext<CommandSourceStack> context) {

        ServerPlayer player = context.getSource().getPlayer();

        PlayerSession playerSession = UtilsServer.getPlayerSessionByPlayer(player);
        if (!playerSession.sessionSet()) {
            context.getSource().sendFailure(Translation.message("session.no_session"));
            return 0;
        }

        Recording recording = RECORDING_MANAGER.getRecording(player);

        if (recording == null) {
            context.getSource().sendFailure(Translation.message("record.not_recording"));
            return 0;
        }

        recording.stop();
        context.getSource().sendSuccess(() -> Translation.message("record.stop"), true);

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

        context.getSource().sendSuccess(() -> Translation.message("record.discarded"), true);

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

        if (recording.save(recordName)) {
            context.getSource().sendSuccess(() -> Translation.message("record.saved"), true);
        } else {
            context.getSource().sendFailure(Translation.message("error.record.save"));
        }

        RECORDING_MANAGER.remove(recording);

        return Command.SINGLE_SUCCESS;
    }
}
