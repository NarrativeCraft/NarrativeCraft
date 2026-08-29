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

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkCommand;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.api.utils.Side;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@InkCommand(
        keyword = "weather",
        description = "Changes the world weather (clear, rain, thunder), with an option to apply it instantly.",
        syntax = "weather <type:string> [--instant]",
        side = Side.SERVER)
public class WeatherInkAction extends InkAction {

    private String weatherType;
    private boolean instant;

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        weatherType = cmd.getString("type");
        instant = cmd.flag("instant");

        if (!weatherType.equals("clear") && !weatherType.equals("rain") && !weatherType.equals("thunder")) {
            return InkActionResult.error("Invalid weather type '" + weatherType + "'. Use: clear, rain, or thunder.");
        }
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        ServerLevel level = playerSession.getPlayer().level();
        ServerGamePacketListenerImpl connection = playerSession.getPlayer().connection;
        boolean singlePlayer = level.getServer().isSingleplayer();

        switch (weatherType) {
            case "clear" -> {
                if (instant || !singlePlayer) {
                    connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0.0F));
                    connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 0.0F));
                    connection.send(
                            new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, 0.0F));
                } else {
                    level.getServer().setWeatherParameters(999999, 0, false, false);
                }
            }
            case "rain" -> {
                if (instant || !singlePlayer) {
                    connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
                    connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 1.0F));
                    connection.send(
                            new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, 0.0F));
                } else {
                    level.getServer().setWeatherParameters(0, 999999, true, false);
                }
            }
            case "thunder" -> {
                if (instant || !singlePlayer) {
                    connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
                    connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 1.0F));
                    connection.send(
                            new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, 1.0F));
                } else {
                    level.getServer().setWeatherParameters(0, 999999, true, true);
                }
            }
        }

        return InkActionResult.singleOk();
    }
}
