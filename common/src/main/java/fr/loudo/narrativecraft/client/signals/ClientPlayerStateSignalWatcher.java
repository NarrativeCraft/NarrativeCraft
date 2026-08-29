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

package fr.loudo.narrativecraft.client.signals;

import fr.loudo.narrativecraft.api.signals.Signal;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ClientPlayerStateSignalWatcher {

    private boolean tracking;
    private boolean sneaking;
    private boolean sprinting;
    private boolean swinging;
    private int swingTime;
    private Item heldItem;
    private int selectedSlot;
    private CameraType cameraType;
    private Screen screen;
    private boolean windowFocused;

    public void tick(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        ClientPlayerSession playerSession =
                ClientNarrativeCraftMod.getInstance().getPlayerSession();
        if (player == null || playerSession == null || !playerSession.isInStory()) {
            tracking = false;
            screen = null;
            return;
        }

        if (!tracking) {
            captureState(minecraft, player);
            tracking = true;
            return;
        }

        tickSneak(player);
        tickSprint(player);
        tickSwingHand(player);
        tickHeldItem(player);
        tickPerspective(minecraft);
        tickScreen(minecraft);
        tickWindowFocus(minecraft);
    }

    private void captureState(Minecraft minecraft, LocalPlayer player) {
        Inventory inventory = player.getInventory();
        sneaking = player.isShiftKeyDown();
        sprinting = player.isSprinting();
        swinging = player.swinging;
        swingTime = player.swingTime;
        heldItem = inventory.getSelected().getItem();
        selectedSlot = inventory.selected;
        cameraType = minecraft.options.getCameraType();
        screen = getTrackedScreen(minecraft);
        windowFocused = minecraft.isWindowActive();
    }

    private void tickSneak(LocalPlayer player) {
        boolean currentlySneaking = player.isShiftKeyDown();
        if (currentlySneaking == sneaking) return;
        sneaking = currentlySneaking;
        emit(new SignalPlayerSneak(currentlySneaking));
    }

    private void tickSprint(LocalPlayer player) {
        boolean currentlySprinting = player.isSprinting();
        if (currentlySprinting == sprinting) return;
        sprinting = currentlySprinting;
        emit(new SignalPlayerSprint(currentlySprinting));
    }

    private void tickSwingHand(LocalPlayer player) {
        boolean currentlySwinging = player.swinging;
        int currentSwingTime = player.swingTime;
        boolean swingStarted = currentlySwinging && (!swinging || currentSwingTime <= swingTime);
        swinging = currentlySwinging;
        swingTime = currentSwingTime;
        if (!swingStarted) return;
        emit(new SignalPlayerSwingHand(player.swingingArm));
    }

    private void tickHeldItem(LocalPlayer player) {
        Inventory inventory = player.getInventory();
        ItemStack currentItemStack = inventory.getSelected();
        int currentSlot = inventory.selected;
        if (currentItemStack.getItem() == heldItem && currentSlot == selectedSlot) return;
        heldItem = currentItemStack.getItem();
        selectedSlot = currentSlot;
        emit(new SignalPlayerChangeHeldItem(currentItemStack, currentSlot));
    }

    private void tickPerspective(Minecraft minecraft) {
        CameraType currentCameraType = minecraft.options.getCameraType();
        if (currentCameraType == cameraType) return;
        cameraType = currentCameraType;
        emit(new SignalPerspectiveChange(currentCameraType));
    }

    private void tickScreen(Minecraft minecraft) {
        Screen currentScreen = getTrackedScreen(minecraft);
        if (currentScreen == screen) return;
        Screen previousScreen = screen;
        screen = currentScreen;
        if (previousScreen != null) {
            emit(new SignalScreenClose(previousScreen));
        }
        if (currentScreen != null) {
            emit(new SignalScreenOpen(currentScreen));
        }
    }

    private static Screen getTrackedScreen(Minecraft minecraft) {
        Screen currentScreen = minecraft.screen;
        return currentScreen instanceof PauseScreen ? null : currentScreen;
    }

    private void tickWindowFocus(Minecraft minecraft) {
        boolean currentlyFocused = minecraft.isWindowActive();
        if (currentlyFocused == windowFocused) return;
        windowFocused = currentlyFocused;
        emit(currentlyFocused ? new SignalGameFocusGained() : new SignalGameFocusLost());
    }

    private void emit(Signal signal) {
        ClientNarrativeCraftMod.getInstance().getSignalEmitter().emit(signal);
    }
}
