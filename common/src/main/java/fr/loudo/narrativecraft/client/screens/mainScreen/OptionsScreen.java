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

package fr.loudo.narrativecraft.client.screens.mainScreen;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.client.settings.ClientStoryLocales;
import fr.loudo.narrativecraft.client.settings.NarrativeClientSettings;
import fr.loudo.narrativecraft.network.story.C2SSetStoryLocale;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.Translation;
import java.io.IOException;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class OptionsScreen extends Screen {

    private static final int ELEMENT_WIDTH = 200;
    private static final int ELEMENT_HEIGHT = 20;
    private static final int ELEMENT_GAP = 5;

    private final Screen lastScreen;

    public OptionsScreen(Screen lastScreen) {
        super(Translation.message("screen.main.options.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        int currentY = height / 3;
        int middleX = width / 2 - ELEMENT_WIDTH / 2;

        AbstractSliderButton textSpeedSlider =
                new AbstractSliderButton(
                        middleX,
                        currentY,
                        ELEMENT_WIDTH,
                        ELEMENT_HEIGHT,
                        buildTextSpeedLabel(NarrativeClientSettings.textSpeed),
                        (NarrativeClientSettings.textSpeed - 1.0) / 4.0) {
                    @Override
                    protected void updateMessage() {
                        setMessage(buildTextSpeedLabel(sliderValueToSpeed(value)));
                    }

                    @Override
                    protected void applyValue() {
                        NarrativeClientSettings.textSpeed = sliderValueToSpeed(value);
                    }
                };
        addRenderableWidget(textSpeedSlider);

        currentY += ELEMENT_HEIGHT + ELEMENT_GAP;

        Checkbox autoSkip = Checkbox.builder(Translation.message("screen.main.options.auto_skip"), font)
                .pos(middleX, currentY)
                .selected(NarrativeClientSettings.autoSkip)
                .onValueChange((checkbox, value) -> NarrativeClientSettings.autoSkip = value)
                .build();
        addRenderableWidget(autoSkip);

        currentY += autoSkip.getHeight() + ELEMENT_GAP;

        if (ClientStoryLocales.getAvailable().size() > 1) {
            Button localeBtn = Button.builder(
                            buildLocaleLabel(), button -> minecraft.gui.setScreen(new SelectLocaleScreen(this)))
                    .bounds(middleX, currentY, ELEMENT_WIDTH, ELEMENT_HEIGHT)
                    .build();
            addRenderableWidget(localeBtn);
            currentY += ELEMENT_HEIGHT + ELEMENT_GAP;
        }

        addRenderableWidget(Button.builder(
                        Translation.message("screen.main.options.minecraft_options"),
                        button -> minecraft.gui.setScreen(new net.minecraft.client.gui.screens.options.OptionsScreen(
                                this, minecraft.options, false)))
                .bounds(middleX, currentY, ELEMENT_WIDTH, ELEMENT_HEIGHT)
                .build());

        addRenderableWidget(Button.builder(Translation.message("screen.main.options.done"), button -> {
                    minecraft.gui.setScreen(lastScreen);
                    Services.PACKET.sendToServer(
                            new C2SSetStoryLocale(ClientStoryLocales.resolve(NarrativeClientSettings.storyLocale)));
                    try {
                        NarrativeClientSettings.save();
                    } catch (IOException e) {
                        NarrativeCraftMod.LOGGER.error("Failed to save user settings!", e);
                        minecraft.player.sendSystemMessage(
                                Translation.message("error.save_settings").withStyle(ChatFormatting.RED));
                    }
                })
                .bounds(middleX, height - ELEMENT_HEIGHT - 10, ELEMENT_WIDTH, ELEMENT_HEIGHT)
                .build());
    }

    private static float sliderValueToSpeed(double sliderValue) {
        return (float) Math.clamp(sliderValue * 4.0 + 1.0, 1.0, 5.0);
    }

    private static Component buildLocaleLabel() {
        String locale = ClientStoryLocales.resolve(NarrativeClientSettings.storyLocale);
        return Translation.message("screen.main.options.language", SelectLocaleScreen.getLocaleName(locale));
    }

    private static Component buildTextSpeedLabel(double speed) {
        return Translation.message("screen.main.options.text_speed", String.format(Locale.ROOT, "%.2f", speed));
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(lastScreen);
    }
}
