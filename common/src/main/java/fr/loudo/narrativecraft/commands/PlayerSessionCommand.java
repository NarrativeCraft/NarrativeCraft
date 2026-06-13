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
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.network.S2CPlayerSession;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.UtilsServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class PlayerSessionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .requires(stack -> stack.hasPermission(2))
                .then(Commands.literal("session")
                        .then(Commands.literal("clear").executes(PlayerSessionCommand::clear))
                        .then(Commands.literal("set")
                                .then(Commands.argument("chapter_index", IntegerArgumentType.integer())
                                        .suggests(CommandSuggestions::suggestChapters)
                                        .then(Commands.argument("scene_name", StringArgumentType.string())
                                                .suggests(CommandSuggestions::suggestSceneByChapter)
                                                .executes(context -> setSession(
                                                        context,
                                                        IntegerArgumentType.getInteger(context, "chapter_index"),
                                                        StringArgumentType.getString(context, "scene_name"))))))));
    }

    private static int clear(CommandContext<CommandSourceStack> context) {

        PlayerSession playerSession =
                UtilsServer.getPlayerSessionByPlayer(context.getSource().getPlayer());
        playerSession.clear();

        context.getSource().sendSuccess(() -> Translation.message("session.clear"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int setSession(CommandContext<CommandSourceStack> context, int chapterIndex, String sceneName) {

        ServerPlayer player = context.getSource().getPlayer();

        PlayerSession playerSession = UtilsServer.getPlayerSessionByPlayer(player);

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

        playerSession.apply(chapter, scene);
        Services.PACKET.sendToPlayer(player, new S2CPlayerSession(chapter.getId(), scene.getId()));

        context.getSource()
                .sendSuccess(
                        () -> Translation.message("session.set", chapter.getChapterIndex(), scene.getName()), false);

        return Command.SINGLE_SUCCESS;
    }
}
