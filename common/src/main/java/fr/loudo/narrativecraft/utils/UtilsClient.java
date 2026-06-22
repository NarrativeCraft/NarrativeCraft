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

package fr.loudo.narrativecraft.utils;

import com.mojang.blaze3d.platform.Window;
import fr.loudo.narrativecraft.client.screens.NarrativeEntryListScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public class UtilsClient {

    private static final Minecraft minecraft = Minecraft.getInstance();

    public static void reloadListScreen() {
        // Reload list for player if a new NarrativeEntry element was added
        if (minecraft.screen instanceof NarrativeEntryListScreen<?> screen) {
            screen.reload();
        }
    }

    public static int[] getScaledMousePos() {
        Window window = minecraft.getWindow();
        int mouseX = (int) (minecraft.mouseHandler.xpos()
                * (double) window.getGuiScaledWidth()
                / (double) window.getScreenWidth());
        int mouseY = (int) (minecraft.mouseHandler.ypos()
                * (double) window.getGuiScaledHeight()
                / (double) window.getScreenHeight());
        return new int[] {mouseX, mouseY};
    }

    public static void sendToast(Component title, Component message) {
        minecraft.getToasts().addToast(new SystemToast(new SystemToast.SystemToastId(), title, message));
    }

    public static void teleportPlayerTo(Vec3 position, Vec3 rotation) {
        LocalPlayer player = Minecraft.getInstance().player;
        player.setPos(position.subtract(0, player.getEyeHeight(), 0));
        player.setXRot((float) rotation.x);
        player.setYRot((float) rotation.y);
        player.setYHeadRot((float) rotation.y);
        player.connection.send(new ServerboundMovePlayerPacket.PosRot(
                position.x, position.y, position.z, (float) rotation.x, (float) rotation.y, player.onGround()));
    }
}
