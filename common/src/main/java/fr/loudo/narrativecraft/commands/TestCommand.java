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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import fr.loudo.narrativecraft.network.S2CRenderSaveIcon;
import fr.loudo.narrativecraft.network.dialog.S2CDialogTest;
import fr.loudo.narrativecraft.platform.Services;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class TestCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nctest")
                .then(Commands.literal("dialog")
                        .then(Commands.literal("2d")
                                .then(Commands.argument("text", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ServerPlayer player =
                                                    ctx.getSource().getPlayerOrException();
                                            String text = StringArgumentType.getString(ctx, "text");
                                            Services.PACKET.sendToPlayer(player, new S2CDialogTest("2d", text, 0));
                                            return 1;
                                        })))
                        .then(Commands.literal("3d")
                                .then(Commands.argument("text", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ServerPlayer player =
                                                    ctx.getSource().getPlayerOrException();
                                            String text = StringArgumentType.getString(ctx, "text");
                                            Entity nearest = findNearestEntity(player);
                                            int entityId = nearest != null ? nearest.getId() : player.getId();
                                            Services.PACKET.sendToPlayer(
                                                    player, new S2CDialogTest("3d", text, entityId));
                                            return 1;
                                        })))
                        .then(Commands.literal("stop").executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            Services.PACKET.sendToPlayer(player, new S2CDialogTest("stop", "", 0));
                            return 1;
                        })))
                .then(Commands.literal("saveicon")
                        .executes(ctx -> sendSaveIcon(ctx.getSource().getPlayerOrException(), 0.2, 0.9, 0.2))
                        .then(Commands.argument("in", DoubleArgumentType.doubleArg(0))
                                .executes(ctx -> sendSaveIcon(
                                        ctx.getSource().getPlayerOrException(),
                                        DoubleArgumentType.getDouble(ctx, "in"),
                                        0.9,
                                        0.2))
                                .then(Commands.argument("stay", DoubleArgumentType.doubleArg(0))
                                        .executes(ctx -> sendSaveIcon(
                                                ctx.getSource().getPlayerOrException(),
                                                DoubleArgumentType.getDouble(ctx, "in"),
                                                DoubleArgumentType.getDouble(ctx, "stay"),
                                                0.2))
                                        .then(Commands.argument("out", DoubleArgumentType.doubleArg(0))
                                                .executes(ctx -> sendSaveIcon(
                                                        ctx.getSource().getPlayerOrException(),
                                                        DoubleArgumentType.getDouble(ctx, "in"),
                                                        DoubleArgumentType.getDouble(ctx, "stay"),
                                                        DoubleArgumentType.getDouble(ctx, "out"))))))));
    }

    private static int sendSaveIcon(ServerPlayer player, double in, double stay, double out) {
        Services.PACKET.sendToPlayer(player, new S2CRenderSaveIcon(in, stay, out));
        return 1;
    }

    private static Entity findNearestEntity(ServerPlayer player) {
        return player
                .level()
                .getEntities(player, player.getBoundingBox().inflate(10.0), entity -> entity != player)
                .stream()
                .min((a, b) -> Double.compare(a.distanceToSqr(player), b.distanceToSqr(player)))
                .orElse(null);
    }
}
