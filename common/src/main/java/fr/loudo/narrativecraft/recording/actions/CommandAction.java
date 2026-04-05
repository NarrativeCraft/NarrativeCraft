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

package fr.loudo.narrativecraft.recording.actions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.playback.IPlaybackContext;
import fr.loudo.narrativecraft.api.playback.IPlaybackSession;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.api.recording.action.ActionResult;
import fr.loudo.narrativecraft.utils.FakePlayer;
import java.io.IOException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.PermissionSet;

public class CommandAction extends AbstractAction {

    public static final String ID = "minecraft_command";

    private String command;

    public CommandAction(int tick, String command) {
        super(tick);
        this.command = command;
    }

    public CommandAction(int tick) {
        super(tick);
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.addString(command);
    }

    @Override
    public void read(Reader reader) throws IOException {
        command = reader.readString();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ActionResult execute(IPlaybackContext context, IPlaybackSession session) {
        if (!(context.getEntity() instanceof FakePlayer player)) return ActionResult.IGNORED;

        CommandSourceStack sourceStack = new CommandSourceStack(
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
            CommandDispatcher<CommandSourceStack> dispatcher =
                    session.getLevel().getServer().getCommands().getDispatcher();
            dispatcher.execute(command, sourceStack);
        } catch (CommandSyntaxException e) {
            NarrativeCraftMod.LOGGER.error("Error while executing command", e);
            return ActionResult.ERROR;
        }

        return ActionResult.OK;
    }
}
