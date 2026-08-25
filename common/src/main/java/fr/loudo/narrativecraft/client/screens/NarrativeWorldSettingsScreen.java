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

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.server.settings.NarrativeServerSettings;
import fr.loudo.narrativecraft.utils.Translation;
import java.io.IOException;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class NarrativeWorldSettingsScreen extends Screen {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;

    private final Screen lastScreen;

    public NarrativeWorldSettingsScreen(Screen lastScreen) {
        super(Translation.message("screen.world_settings.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        int currentY = height / 3;
        int middleX = width / 2 - BUTTON_WIDTH / 2;

        Button showMainScreenOnJoin = buildToggleButton(
                "screen.world_settings.show_main_screen_on_join",
                NarrativeServerSettings.showMainScreenOnJoin,
                middleX,
                currentY,
                aBoolean -> {
                    NarrativeServerSettings.showMainScreenOnJoin = aBoolean;
                });
        showMainScreenOnJoin.active = minecraft.isSingleplayer();
        if (!minecraft.isSingleplayer()) {
            showMainScreenOnJoin.setTooltip(
                    Tooltip.create(Translation.message("screen.world_settings.tooltip.main_screen")));
        }
        addRenderableWidget(showMainScreenOnJoin);

        currentY = showMainScreenOnJoin.getY() + showMainScreenOnJoin.getHeight() + 5;
        Button showNametagGlobalCharacter = buildToggleButton(
                "screen.world_settings.show_name_global_characters",
                NarrativeServerSettings.showNametagGlobalCharacter,
                middleX,
                currentY,
                aBoolean -> {
                    NarrativeServerSettings.showNametagGlobalCharacter = aBoolean;
                });
        addRenderableWidget(showNametagGlobalCharacter);

        addRenderableWidget(Button.builder(Translation.message("screen.world_settings.done"), button -> {
                    try {
                        NarrativeServerSettings.save();
                    } catch (IOException e) {
                        NarrativeCraftMod.LOGGER.error("Failed to save world settings!", e);
                        minecraft.player.sendSystemMessage(
                                Translation.message("error.save_settings").withStyle(ChatFormatting.RED));
                    }
                    minecraft.setScreen(lastScreen);
                })
                .bounds(middleX, height - BUTTON_HEIGHT - 10, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    private Button buildToggleButton(String labelKey, boolean initialValue, int x, int y, Consumer<Boolean> onChange) {
        boolean[] current = {initialValue};

        return Button.builder(buildToggleLabel(labelKey, current[0]), button -> {
                    current[0] = !current[0];
                    onChange.accept(current[0]);
                    button.setMessage(buildToggleLabel(labelKey, current[0]));
                })
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
    }

    private Component buildToggleLabel(String labelKey, boolean value) {
        Component yesNo = value ? Translation.message("yes") : Translation.message("no");
        return Translation.message(labelKey, yesNo);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }
}
