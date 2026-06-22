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
import fr.loudo.narrativecraft.api.AddonContext;
import fr.loudo.narrativecraft.api.NarrativeCraftAPI;
import fr.loudo.narrativecraft.utils.Translation;
import java.net.URI;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public class AddonsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .requires(stack -> stack.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                .then(Commands.literal("addon")
                        .then(Commands.literal("list").executes(AddonsCommand::listAddons))
                        .then(Commands.literal("info")
                                .then(Commands.argument("addon_id", StringArgumentType.string())
                                        .suggests(CommandSuggestions::suggestAddons)
                                        .executes(AddonsCommand::addonInfo)))));
    }

    private static int listAddons(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        List<AddonContext> addons =
                NarrativeCraftAPI.getInstance().getAddonRegistry().getAll();

        if (addons.isEmpty()) {
            player.sendSystemMessage(Translation.message("addon.empty").withStyle(ChatFormatting.GRAY));
            return Command.SINGLE_SUCCESS;
        }

        player.sendSystemMessage(Translation.message("addons").withStyle(ChatFormatting.GOLD));

        for (AddonContext addon : addons) {
            ChatFormatting color = addon.isEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED;
            MutableComponent line = Component.literal("- ")
                    .append(Component.literal(addon.getName()).withStyle(color));
            player.sendSystemMessage(line);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int addonInfo(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        String addonId = StringArgumentType.getString(context, "addon_id");

        AddonContext addon = NarrativeCraftAPI.getInstance().getAddonRegistry().getAll().stream()
                .filter(a -> a.getModId().equals(addonId))
                .findFirst()
                .orElse(null);

        if (addon == null) {
            player.sendSystemMessage(
                    Translation.message("addon.unknown", addonId).withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }

        boolean enabled = addon.isEnabled();
        ChatFormatting stateColor = enabled ? ChatFormatting.GREEN : ChatFormatting.RED;

        player.sendSystemMessage(Component.literal("=== ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(addon.getName()).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" ===").withStyle(ChatFormatting.GRAY)));

        player.sendSystemMessage(field("addon.mod_id", addon.getModId()));
        player.sendSystemMessage(field("description", addon.getDescription()));
        player.sendSystemMessage(field("addon.author", addon.getAuthor()));
        player.sendSystemMessage(field("addon.api_version", String.valueOf(addon.getApiVersion())));

        if (addon.getHomeLink() != null && !addon.getHomeLink().isBlank()) {
            player.sendSystemMessage(linkField("addon.home_link", addon.getHomeLink()));
        }

        MutableComponent statusLine = Translation.message("addon.status")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": "))
                .append(Translation.message(enabled ? "addon.status.enabled" : "addon.status.disabled")
                        .withStyle(stateColor));
        player.sendSystemMessage(statusLine);

        if (!enabled) {
            player.sendSystemMessage(
                    Translation.message("addon.version_mismatch", addon.getApiVersion(), NarrativeCraftAPI.VERSION)
                            .withStyle(ChatFormatting.YELLOW));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static MutableComponent field(String translationKey, String value) {
        return Translation.message(translationKey)
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": "))
                .append(Component.literal(value).withStyle(ChatFormatting.WHITE));
    }

    private static MutableComponent linkField(String translationKey, String url) {
        return Translation.message(translationKey)
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": "))
                .append(Component.literal(url).withStyle(style -> style.withColor(ChatFormatting.AQUA)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent.OpenUrl(URI.create(url)))));
    }
}
