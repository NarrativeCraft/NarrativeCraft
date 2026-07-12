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

package fr.loudo.narrativecraft.client.screens;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.cameraangle.ClientCameraAngleMakerEditorMaker;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngle;
import fr.loudo.narrativecraft.network.mainScreen.BiMainScreenEnter;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class NarrativeSettingsScreen extends Screen {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;

    private final Screen lastScreen;

    protected NarrativeSettingsScreen(Screen lastScreen) {
        super(Component.empty());
        this.lastScreen = lastScreen;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }

    @Override
    protected void init() {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        Button mainScreenMakerEditor = Button.builder(
                        Translation.message("screen.settings.main_screen_maker_editor"), button -> {
                            CameraAngle mainScreenData =
                                    ClientNarrativeCraftMod.getInstance().getMainScreenData();
                            if (mainScreenData == null) return;
                            ClientCameraAngleMakerEditorMaker cameraAngleEditor = new ClientCameraAngleMakerEditorMaker(
                                    mainScreenData, NarrativeEnvironment.DEVELOPMENT);
                            cameraAngleEditor.init();
                            session.setEditor(cameraAngleEditor);
                            Services.PACKET.sendToServer(new BiMainScreenEnter(NarrativeEnvironment.DEVELOPMENT));
                            minecraft.setScreen(null);
                        })
                .bounds(width / 2 - BUTTON_WIDTH / 2, 10, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        addRenderableWidget(mainScreenMakerEditor);
        mainScreenMakerEditor.active = minecraft.hasSingleplayerServer();
        if (!minecraft.hasSingleplayerServer()) {
            mainScreenMakerEditor.setTooltip(
                    Tooltip.create(Translation.message("screen.world_settings.tooltip.main_screen")));
        }

        Button worldSettings = Button.builder(
                        Translation.message("screen.settings.world_settings"),
                        button -> minecraft.setScreen(new NarrativeWorldSettingsScreen(this)))
                .bounds(width / 2 - BUTTON_WIDTH / 2, 10 + BUTTON_HEIGHT + 5, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        worldSettings.active = minecraft.hasSingleplayerServer();
        if (!minecraft.hasSingleplayerServer()) {
            worldSettings.setTooltip(Tooltip.create(Translation.message("screen.world_settings.tooltip.main_screen")));
        }
        addRenderableWidget(worldSettings);
    }
}
