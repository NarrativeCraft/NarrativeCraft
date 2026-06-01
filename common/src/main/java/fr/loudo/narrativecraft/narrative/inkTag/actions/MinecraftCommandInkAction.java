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

package fr.loudo.narrativecraft.narrative.inkTag.actions;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkCommand;
import fr.loudo.narrativecraft.api.inkAction.Side;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;

@InkCommand(
        keyword = "command",
        description = "Executes a vanilla Minecraft command as if run by an operator, with full permissions.",
        syntax = "command <commandValue:string>",
        side = Side.SERVER)
public class MinecraftCommandInkAction extends InkAction {

    private String commandValue;

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        commandValue = cmd.getString("commandValue").replace("\\{", "{").replace("\\}", "}");

        MinecraftServer server = NarrativeCraftMod.getInstance().getServer();
        CommandSourceStack source =
                new CommandSourceStack(null, null, null, null, PermissionSet.ALL_PERMISSIONS, null, null, server, null);
        ParseResults<CommandSourceStack> parse =
                server.getCommands().getDispatcher().parse(new StringReader(commandValue), source);

        if (parse.getReader().canRead() && parse.getExceptions().size() == 1) {
            String error = parse.getExceptions().values().iterator().next().getMessage();
            return InkActionResult.error("Command cannot be parsed: " + error);
        }
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        ServerPlayer player = playerSession.getPlayer();
        String resolved = commandValue.replace("@p", player.getName().getString());
        CommandSourceStack source = new CommandSourceStack(
                CommandSource.NULL,
                player.position(),
                player.getRotationVector(),
                player.level(),
                PermissionSet.ALL_PERMISSIONS,
                player.getName().getString(),
                player.getDisplayName(),
                player.level().getServer(),
                player);
        try {
            player.level().getServer().getCommands().getDispatcher().execute(resolved, source);
        } catch (CommandSyntaxException e) {
            return InkActionResult.error("Command execution failed: " + e.getMessage());
        }
        isRunning = false;
        return InkActionResult.ok();
    }
}
