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
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.locale.StoryLocaleManager;
import fr.loudo.narrativecraft.narrative.story.locale.StoryTranslations;
import fr.loudo.narrativecraft.narrative.story.locale.TranslationKeyScanner;
import fr.loudo.narrativecraft.network.story.S2CSetStoryLocale;
import fr.loudo.narrativecraft.network.story.S2CStoryLocales;
import fr.loudo.narrativecraft.network.story.S2CStoryTranslations;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.server.settings.NarrativeServerSettings;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class LocaleCommand {

    private static final int ORPHAN_PREVIEW_LIMIT = 10;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("locale")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                        .then(Commands.literal("list").executes(LocaleCommand::list))
                        .then(Commands.literal("sync").executes(LocaleCommand::sync))
                        .then(Commands.literal("reload").executes(LocaleCommand::reload))
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
        List<String> allKeys = TranslationKeyScanner.scanAllKeys();

        context.getSource()
                .sendSuccess(
                        () -> Translation.message(
                                        "locale.list.header",
                                        Component.literal(defaultLocale).withStyle(ChatFormatting.GOLD))
                                .withStyle(ChatFormatting.GREEN),
                        false);

        for (String locale : StoryLocaleManager.listAvailableLocales()) {
            long translatedKeyCount = allKeys.stream()
                    .filter(key -> StoryTranslations.hasTranslation(locale, key))
                    .count();
            context.getSource()
                    .sendSuccess(
                            () -> Translation.message(
                                            "locale.list.entry",
                                            Component.literal(locale).withStyle(ChatFormatting.GOLD),
                                            Component.literal(String.valueOf(translatedKeyCount)),
                                            Component.literal(String.valueOf(allKeys.size())))
                                    .withStyle(ChatFormatting.WHITE),
                            false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int add(CommandContext<CommandSourceStack> context, String locale) {
        String normalized = locale.toLowerCase(Locale.ROOT);

        if (!StoryLocaleManager.isValidLocale(normalized)) {
            return fail(context, Translation.message("locale.invalid_code", Component.literal(locale)));
        }
        if (StoryLocaleManager.exists(normalized)) {
            return fail(context, Translation.message("locale.already_exists", Component.literal(normalized)));
        }

        int keyCount;
        try {
            keyCount = StoryLocaleManager.create(normalized);
        } catch (IOException exception) {
            NarrativeCraftMod.LOGGER.error("Failed to create locale '{}'", normalized, exception);
            return fail(context, Component.literal("Failed to create locale: " + exception.getMessage()));
        }

        StoryTranslations.reload();
        broadcastLocales();

        if (keyCount == 0) {
            context.getSource()
                    .sendSuccess(() -> Translation.message("locale.no_keys").withStyle(ChatFormatting.YELLOW), false);
        }
        context.getSource()
                .sendSuccess(
                        () -> Translation.message(
                                        "locale.added",
                                        Component.literal(normalized).withStyle(ChatFormatting.GOLD),
                                        Component.literal(String.valueOf(keyCount)))
                                .withStyle(ChatFormatting.GREEN),
                        false);
        return Command.SINGLE_SUCCESS;
    }

    private static int remove(CommandContext<CommandSourceStack> context, String locale) {
        String normalized = locale.toLowerCase(Locale.ROOT);

        if (StoryLocaleManager.isDefaultLocale(normalized)) {
            return fail(
                    context, Translation.message("locale.default_cannot_be_removed", Component.literal(normalized)));
        }
        if (!StoryLocaleManager.exists(normalized)) {
            return fail(context, Translation.message("locale.not_exists", Component.literal(normalized)));
        }

        try {
            StoryLocaleManager.remove(normalized);
        } catch (IOException exception) {
            NarrativeCraftMod.LOGGER.error("Failed to remove locale '{}'", normalized, exception);
            return fail(context, Component.literal("Failed to remove locale: " + exception.getMessage()));
        }

        StoryTranslations.reload();
        broadcastLocales();
        forEachSession(context, session -> {
            if (!normalized.equals(session.getStoryLocale())) return;
            session.setStoryLocale(null);
            Services.PACKET.sendToPlayer(
                    session.getPlayer(), new S2CSetStoryLocale(StoryLocaleManager.getDefaultLocale()));
        });
        refreshPlayingSessions(context);

        context.getSource()
                .sendSuccess(
                        () -> Translation.message(
                                        "locale.removed",
                                        Component.literal(normalized).withStyle(ChatFormatting.GOLD))
                                .withStyle(ChatFormatting.GREEN),
                        false);
        return Command.SINGLE_SUCCESS;
    }

    private static int sync(CommandContext<CommandSourceStack> context) {
        Map<String, StoryLocaleManager.SyncReport> reports;
        try {
            reports = StoryLocaleManager.syncAll();
        } catch (IOException exception) {
            NarrativeCraftMod.LOGGER.error("Failed to sync locales", exception);
            return fail(context, Component.literal("Failed to sync locales: " + exception.getMessage()));
        }

        if (reports.isEmpty()) {
            return fail(context, Translation.message("locale.none"));
        }

        reports.forEach((locale, report) -> {
            context.getSource()
                    .sendSuccess(
                            () -> Translation.message(
                                            "locale.synced",
                                            Component.literal(locale).withStyle(ChatFormatting.GOLD),
                                            Component.literal(String.valueOf(report.addedKeyCount())))
                                    .withStyle(ChatFormatting.GREEN),
                            false);

            if (report.orphanKeys().isEmpty()) return;
            context.getSource()
                    .sendSystemMessage(Translation.message(
                                    "locale.sync_orphans",
                                    Component.literal(locale).withStyle(ChatFormatting.GOLD),
                                    Component.literal(previewOrphanKeys(report.orphanKeys())))
                            .withStyle(ChatFormatting.YELLOW));
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        StoryTranslations.reload();
        List<String> locales = StoryTranslations.listLoadedLocales();
        int entryCount =
                locales.stream().mapToInt(StoryTranslations::entryCountOf).sum();

        refreshPlayingSessions(context);

        context.getSource()
                .sendSuccess(
                        () -> Translation.message(
                                        "locale.reloaded",
                                        Component.literal(String.valueOf(locales.size()))
                                                .withStyle(ChatFormatting.GOLD),
                                        Component.literal(String.valueOf(entryCount))
                                                .withStyle(ChatFormatting.GOLD))
                                .withStyle(ChatFormatting.GREEN),
                        false);
        return Command.SINGLE_SUCCESS;
    }

    private static int setDefault(CommandContext<CommandSourceStack> context, String locale) {
        String normalized = locale.toLowerCase(Locale.ROOT);

        if (!StoryLocaleManager.isValidLocale(normalized)) {
            return fail(context, Translation.message("locale.invalid_code", Component.literal(locale)));
        }
        if (StoryLocaleManager.isDefaultLocale(normalized)) {
            return fail(context, Translation.message("locale.default_already", Component.literal(normalized)));
        }

        String previousDefaultLocale = NarrativeServerSettings.defaultLocale;
        NarrativeServerSettings.defaultLocale = normalized;
        try {
            NarrativeServerSettings.save();
        } catch (IOException exception) {
            NarrativeServerSettings.defaultLocale = previousDefaultLocale;
            NarrativeCraftMod.LOGGER.error("Failed to save the default locale", exception);
            return fail(context, Component.literal("Failed to save the default locale: " + exception.getMessage()));
        }

        if (!StoryLocaleManager.exists(normalized)) {
            try {
                StoryLocaleManager.create(normalized);
            } catch (IOException exception) {
                NarrativeCraftMod.LOGGER.error("Failed to create locale '{}'", normalized, exception);
            }
        }

        StoryTranslations.reload();
        broadcastLocales();
        refreshPlayingSessions(context);

        context.getSource()
                .sendSuccess(
                        () -> Translation.message(
                                        "locale.default_changed",
                                        Component.literal(normalized).withStyle(ChatFormatting.GOLD))
                                .withStyle(ChatFormatting.GREEN),
                        false);
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
        sendTranslations(player);
    }

    public static void sendTranslations(ServerPlayer player) {
        PlayerSession session =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        String locale = session == null ? null : session.getStoryLocale();
        Services.PACKET.sendToPlayer(player, new S2CStoryTranslations(StoryTranslations.resolvedEntriesFor(locale)));
    }

    private static void refreshPlayingSessions(CommandContext<CommandSourceStack> context) {
        forEachSession(context, session -> {
            sendTranslations(session.getPlayer());
            StoryHandler storyHandler = session.getStoryHandler();
            if (storyHandler != null) {
                storyHandler.refreshLocalizedContent();
            }
        });
    }

    private static void forEachSession(CommandContext<CommandSourceStack> context, Consumer<PlayerSession> action) {
        MinecraftServer server = context.getSource().getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerSession session =
                    NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
            if (session != null) {
                action.accept(session);
            }
        }
    }

    private static String previewOrphanKeys(List<String> orphanKeys) {
        if (orphanKeys.size() <= ORPHAN_PREVIEW_LIMIT) return String.join(", ", orphanKeys);
        return String.join(", ", orphanKeys.subList(0, ORPHAN_PREVIEW_LIMIT)) + ", ... (+"
                + (orphanKeys.size() - ORPHAN_PREVIEW_LIMIT) + ")";
    }

    private static int fail(CommandContext<CommandSourceStack> context, Component message) {
        context.getSource().sendFailure(message.copy().withStyle(ChatFormatting.RED));
        return 0;
    }
}
