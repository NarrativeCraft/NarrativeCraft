/*
 * NarrativeCraft - Create narrative games inside Minecraft. No coding, no game engine, only text and logic.
 * Copyright (c) 2026 LOUDO and contributors
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

package fr.loudo.narrativecraft.client.studio;

import fr.loudo.narrativecraft.client.imgui.ImGuiClient;
import fr.loudo.narrativecraft.client.network.C2SFreezeWorld;
import fr.loudo.narrativecraft.client.network.C2SRequestTree;
import fr.loudo.narrativecraft.client.network.C2SStudioToggle;
import fr.loudo.narrativecraft.client.studio.assets.AssetsClient;
import fr.loudo.narrativecraft.client.studio.layout.LayoutWindowsClient;
import fr.loudo.narrativecraft.client.studio.menu.NarrativeStudioMenuBarClient;
import fr.loudo.narrativecraft.client.studio.options.NarrativeStudioOptionsClient;
import fr.loudo.narrativecraft.client.studio.shortcut.NarrativeStudioShortcutsClient;
import fr.loudo.narrativecraft.client.studio.tab.TabsManagerClient;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.studio.assets.Tree;
import imgui.ImGui;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public class NarrativeStudioClient {

    private static final Minecraft MINECRAFT = Minecraft.getInstance();
    private static final NarrativeStudioClient INSTANCE = new NarrativeStudioClient();

    private final NarrativeStudioCameraHandlerClient cameraHandler = new NarrativeStudioCameraHandlerClient();
    private final NarrativeStudioMenuBarClient menuBar = new NarrativeStudioMenuBarClient();
    private final NarrativeStudioShortcutsClient shortcuts = new NarrativeStudioShortcutsClient();
    private final TabsManagerClient tabsManager = new TabsManagerClient();
    private final AssetsClient assets = new AssetsClient();
    private NarrativeStudioOptionsClient options = new NarrativeStudioOptionsClient();

    private NarrativeStudioClient() {}

    private volatile boolean isOpened;
    private volatile boolean isMoving;

    private GameType lastGameMode;

    public void toggle() {
        if (isOpened) {
            close();
        } else {
            open();
        }
    }

    /**
     * Capture the mouse and allow the camera to move
     */
    public void grabMouseAndMove() {
        if (ImGui.getIO().getWantCaptureMouse()) return;
        if (MINECRAFT.gui.screen() != null) return;
        if (isMoving) return;
        isMoving = true;
        MINECRAFT.mouseHandler.grabMouse();
        KeyMapping.setAll();
    }

    /**
     * Release the mouse from the game and disable moving the camera
     */
    public void releaseAndStopMoving() {
        if (!isMoving) return;
        isMoving = false;
        if (MINECRAFT.gui.screen() != null) return;
        MINECRAFT.mouseHandler.releaseMouse();
        KeyMapping.releaseAll();
    }

    public void moveCamera(Vec3 input, LocalPlayer player) {
        cameraHandler.moveCamera(input, player, options.getCamera().getCameraSpeed());
    }

    public void setAssetsRoot(Tree root) {
        assets.setRoot(root);
    }

    public AssetsClient getAssets() {
        return assets;
    }

    public void open() {
        if (MINECRAFT.level == null || !MINECRAFT.hasSingleplayerServer()) return;
        MINECRAFT.gui.setScreen(null);
        isOpened = true;
        isMoving = false;
        KeyMapping.releaseAll();
        MINECRAFT.mouseHandler.releaseMouse();
        if (!MINECRAFT.gui.hud.isHidden()) {
            MINECRAFT.gui.hud.toggle();
        }
        lastGameMode = MINECRAFT.gameMode.getPlayerMode();
        MINECRAFT.player.connection.send(new ServerboundChangeGameModePacket(GameType.SPECTATOR));
        Services.PACKET.sendToServer(new C2SFreezeWorld(true));
        Services.PACKET.sendToServer(new C2SStudioToggle(true));
        Services.PACKET.sendToServer(new C2SRequestTree(assets.getHash()));
        MINECRAFT.player.setDeltaMovement(Vec3.ZERO);
        MINECRAFT.options.setCameraType(CameraType.FIRST_PERSON);
        ImGuiClient.getInstance().addOverlay(menuBar);
        ImGuiClient.getInstance().addOverlay(shortcuts);
        ImGuiClient.getInstance().addOverlay(LayoutWindowsClient.getInstance());
        ImGuiClient.getInstance().setFullWindowOutput(LayoutWindowsClient.getInstance()::drawGame);
        LayoutWindowsClient.getInstance().setGameAreaActive(true);
        tabsManager.openNew();
    }

    public void close() {
        reset();
        if (MINECRAFT.gui.screen() == null && MINECRAFT.level != null) {
            MINECRAFT.mouseHandler.grabMouse();
        }
        if (MINECRAFT.gui.hud.isHidden()) {
            MINECRAFT.gui.hud.toggle();
        }
        MINECRAFT.player.connection.send(new ServerboundChangeGameModePacket(lastGameMode));
        Services.PACKET.sendToServer(new C2SFreezeWorld(false));
        Services.PACKET.sendToServer(new C2SStudioToggle(false));
    }

    public void reset() {
        isOpened = false;
        isMoving = false;
        ImGuiClient.getInstance().removeOverlay(menuBar);
        ImGuiClient.getInstance().removeOverlay(shortcuts);
        ImGuiClient.getInstance().removeOverlay(LayoutWindowsClient.getInstance());
        ImGuiClient.getInstance().setFullWindowOutput(null);
        LayoutWindowsClient.getInstance().setGameAreaActive(false);
    }

    public boolean isOpened() {
        return isOpened;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public NarrativeStudioCameraHandlerClient getCameraHandler() {
        return cameraHandler;
    }

    public TabsManagerClient getTabsManager() {
        return tabsManager;
    }

    public NarrativeStudioOptionsClient getOptions() {
        return options;
    }

    public static NarrativeStudioClient getInstance() {
        return INSTANCE;
    }
}
