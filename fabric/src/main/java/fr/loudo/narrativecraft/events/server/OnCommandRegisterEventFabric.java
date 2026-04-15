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

package fr.loudo.narrativecraft.events.server;

import fr.loudo.narrativecraft.commands.DialogTestCommand;
import fr.loudo.narrativecraft.commands.PlaybackCommand;
import fr.loudo.narrativecraft.commands.PlayerSessionCommand;
import fr.loudo.narrativecraft.commands.RecordCommand;
import fr.loudo.narrativecraft.events.IFabricEventRegister;
import fr.loudo.narrativecraft.platform.Services;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class OnCommandRegisterEventFabric implements IFabricEventRegister {

    @Override
    public void register() {
        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> {
            RecordCommand.register(commandDispatcher);
            PlayerSessionCommand.register(commandDispatcher);
            PlaybackCommand.register(commandDispatcher);
            if (Services.PLATFORM.isDevelopmentEnvironment()) {
                DialogTestCommand.register(commandDispatcher);
            }
        });
    }
}
