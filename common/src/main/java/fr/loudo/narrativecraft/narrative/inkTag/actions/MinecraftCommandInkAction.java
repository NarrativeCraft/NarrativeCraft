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

package fr.loudo.narrativecraft.narrative.inkTag.actions;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkCommand;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.api.utils.Side;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.utils.FakePlayer;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

@InkCommand(
        keyword = "command",
        description = "Executes a vanilla Minecraft command as if run by an operator, with full permissions.",
        syntax = "command <commandValue:string>",
        side = Side.SERVER)
public class MinecraftCommandInkAction extends InkAction {

    private static final Pattern CHARACTER_REG = Pattern.compile("@char\\(([^()]*)\\)");

    private String commandValue;

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        commandValue = cmd.getString("commandValue").replace("\\{", "{").replace("\\}", "}");

        String tempCmd = commandValue;
        Matcher charMatcher = parseCharSyntax(tempCmd);
        if (charMatcher.find()) {
            tempCmd = tempCmd.replace(charMatcher.group(0), "@s");
        }
        MinecraftServer server = NarrativeCraftMod.getInstance().getServer();
        CommandSourceStack source = new CommandSourceStack(null, null, null, null, 4, null, null, server, null);
        ParseResults<CommandSourceStack> parse =
                server.getCommands().getDispatcher().parse(new StringReader(tempCmd), source);

        if (parse.getReader().canRead() && parse.getExceptions().size() == 1) {
            String error = parse.getExceptions().values().iterator().next().getMessage();
            return InkActionResult.error("Command cannot be parsed: " + error);
        }
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        ServerPlayer player = playerSession.getPlayer();
        FakePlayer fakePlayer = new FakePlayer(
                playerSession.getPlayer().serverLevel(), new GameProfile(UUID.randomUUID(), "CommandExec"), true);
        String resolved = commandValue.replace("@p", player.getName().getString());
        ICharacterStory characterStory = resolveCharacter(resolved, (Scene) playerSession.getScene());
        Entity entityTargeted = null;
        if (characterStory != null && playerSession.getStoryHandler() != null) {
            entityTargeted =
                    playerSession.getStoryHandler().getCharacterEntities().get(characterStory.getName());
        }
        if (entityTargeted == null) {
            entityTargeted = fakePlayer;
        }
        Matcher charMatcher = parseCharSyntax(resolved);
        if (charMatcher.find()) {
            resolved = resolved.replace(charMatcher.group(0), "@s");
        }

        CommandSourceStack source = new CommandSourceStack(
                CommandSource.NULL,
                fakePlayer.position(),
                fakePlayer.getRotationVector(),
                fakePlayer.serverLevel(),
                4,
                fakePlayer.getName().getString(),
                fakePlayer.getDisplayName(),
                fakePlayer.level().getServer(),
                entityTargeted);
        try {
            fakePlayer.level().getServer().getCommands().getDispatcher().execute(resolved, source);
            NarrativeCraftMod.LOGGER.info("CommandExec executed command {} from command tag", resolved);
        } catch (CommandSyntaxException e) {
            NarrativeCraftMod.LOGGER.warn("Command execution failed: ", e);
        }
        return InkActionResult.singleOk();
    }

    private ICharacterStory resolveCharacter(String rawCommand, Scene scene) {

        Matcher matcher = parseCharSyntax(rawCommand);
        if (!matcher.find()) return null;
        String characterName = matcher.group(1);

        return NarrativeCraftMod.getInstance().getCharacterManager().resolveCharacter(characterName, scene);
    }

    private Matcher parseCharSyntax(String rawCommand) {
        return CHARACTER_REG.matcher(rawCommand);
    }
}
