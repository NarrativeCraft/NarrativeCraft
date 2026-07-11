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
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.story.locale.StoryLocaleManager;
import fr.loudo.narrativecraft.network.story.S2CStoryLocales;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public class LocaleCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("locale")
                        .requires(commandSourceStack ->
                                commandSourceStack.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                        .then(Commands.literal("list").executes(LocaleCommand::list))
                        .then(Commands.literal("sync").executes(LocaleCommand::sync))
                        .then(Commands.literal("add")
                                .then(Commands.argument("locale", StringArgumentType.word())
                                        .executes(context ->
                                                add(context, StringArgumentType.getString(context, "locale")))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("locale", StringArgumentType.word())
                                        .suggests(CommandSuggestions::suggestLocales)
                                        .executes(context ->
                                                remove(context, StringArgumentType.getString(context, "locale")))))
                        .then(Commands.literal("default")
                                .then(Commands.argument("locale", StringArgumentType.word())
                                        .suggests(CommandSuggestions::suggestLocales)
                                        .executes(context -> setDefault(
                                                context, StringArgumentType.getString(context, "locale")))))));
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        String defaultLocale = StoryLocaleManager.getDefaultLocale();
        List<String> locales = StoryLocaleManager.listLocales();

        context.getSource()
                .sendSuccess(
                        () -> Translation.message(
                                        "locale.list",
                                        Component.literal(defaultLocale).withStyle(ChatFormatting.GOLD),
                                        Component.literal(locales.isEmpty() ? "-" : String.join(", ", locales))
                                                .withStyle(ChatFormatting.WHITE))
                                .withStyle(ChatFormatting.GREEN),
                        false);
        return Command.SINGLE_SUCCESS;
    }

    private static int add(CommandContext<CommandSourceStack> context, String locale) {
        String normalized = locale.toLowerCase();

        if (!StoryLocaleManager.isValidLocale(normalized)) {
            context.getSource()
                    .sendFailure(Translation.message("locale.invalid_code", Component.literal(locale))
                            .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (StoryLocaleManager.exists(normalized)) {
            context.getSource()
                    .sendFailure(Translation.message("locale.already_exists", Component.literal(normalized))
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        try {
            StoryLocaleManager.create(normalized);
        } catch (IOException exception) {
            return fail(context, "Failed to create locale '" + normalized + "'", exception);
        }

        broadcastLocales();
        context.getSource()
                .sendSuccess(
                        () -> Translation.message("locale.added", Component.literal(normalized))
                                .withStyle(ChatFormatting.GREEN),
                        false);
        return Command.SINGLE_SUCCESS;
    }

    private static int remove(CommandContext<CommandSourceStack> context, String locale) {
        String normalized = locale.toLowerCase();

        if (!StoryLocaleManager.listLocales().contains(normalized)) {
            context.getSource()
                    .sendFailure(Translation.message("locale.not_exists", Component.literal(normalized))
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        try {
            StoryLocaleManager.remove(normalized);
        } catch (IOException exception) {
            return fail(context, "Failed to remove locale '" + normalized + "'", exception);
        }

        broadcastLocales();
        context.getSource()
                .sendSuccess(
                        () -> Translation.message("locale.removed", Component.literal(normalized))
                                .withStyle(ChatFormatting.GREEN),
                        false);
        return Command.SINGLE_SUCCESS;
    }

    private static int sync(CommandContext<CommandSourceStack> context) {
        Map<String, StoryLocaleManager.SyncReport> reports;
        try {
            reports = StoryLocaleManager.syncAll();
        } catch (IOException exception) {
            return fail(context, "Failed to sync locales", exception);
        }

        for (Map.Entry<String, StoryLocaleManager.SyncReport> entry : reports.entrySet()) {
            StoryLocaleManager.SyncReport report = entry.getValue();
            context.getSource()
                    .sendSuccess(
                            () -> Translation.message(
                                            "locale.synced",
                                            Component.literal(entry.getKey()).withStyle(ChatFormatting.GOLD),
                                            Component.literal(String.valueOf(
                                                    report.getCreated().size())),
                                            Component.literal(String.valueOf(
                                                    report.getRenamed().size())),
                                            Component.literal(String.valueOf(
                                                    report.getDeleted().size())),
                                            Component.literal(String.valueOf(
                                                    report.getRepaired().size())))
                                    .withStyle(ChatFormatting.GREEN),
                            false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int setDefault(CommandContext<CommandSourceStack> context, String locale) {
        String normalized = locale.toLowerCase();

        if (StoryLocaleManager.isDefaultLocale(normalized)) {
            context.getSource()
                    .sendFailure(Translation.message("locale.default_already", Component.literal(normalized))
                            .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (!StoryLocaleManager.listLocales().contains(normalized)) {
            context.getSource()
                    .sendFailure(Translation.message("locale.not_exists", Component.literal(normalized))
                            .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (isAnyStoryRunning()) {
            context.getSource()
                    .sendFailure(Translation.message("locale.story_running").withStyle(ChatFormatting.RED));
            return 0;
        }

        String previousDefault = StoryLocaleManager.getDefaultLocale();
        try {
            StoryLocaleManager.setDefault(normalized);
        } catch (IOException exception) {
            return fail(context, "Failed to set '" + normalized + "' as the default locale", exception);
        }

        broadcastLocales();
        context.getSource()
                .sendSuccess(
                        () -> Translation.message(
                                        "locale.default_changed",
                                        Component.literal(normalized).withStyle(ChatFormatting.GOLD),
                                        Component.literal(previousDefault).withStyle(ChatFormatting.GOLD))
                                .withStyle(ChatFormatting.GREEN),
                        false);
        context.getSource()
                .sendSystemMessage(Translation.message("locale.reload_needed").withStyle(ChatFormatting.YELLOW));
        return Command.SINGLE_SUCCESS;
    }

    public static void broadcastLocales() {
        MinecraftServer server = NarrativeCraftMod.getInstance().getServer();
        if (server == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendLocales(player);
        }
    }

    public static void sendLocales(ServerPlayer player) {
        Services.PACKET.sendToPlayer(
                player,
                new S2CStoryLocales(StoryLocaleManager.listAvailableLocales(), StoryLocaleManager.getDefaultLocale()));
    }

    private static boolean isAnyStoryRunning() {
        MinecraftServer server = NarrativeCraftMod.getInstance().getServer();
        if (server == null) return false;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerSession session =
                    NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
            if (session != null && session.getStoryHandler() != null) return true;
        }
        return false;
    }

    private static int fail(CommandContext<CommandSourceStack> context, String message, Exception exception) {
        NarrativeCraftMod.LOGGER.error(message, exception);
        context.getSource()
                .sendFailure(Component.literal(message + ": " + exception.getMessage())
                        .withStyle(ChatFormatting.RED));
        return 0;
    }
}
