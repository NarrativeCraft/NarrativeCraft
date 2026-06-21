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

package fr.loudo.narrativecraft.client.screens.story;

import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.screens.components.ChoiceButtonWidget;
import fr.loudo.narrativecraft.network.story.C2SChoiceSelected;
import fr.loudo.narrativecraft.platform.Services;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;

public class ChoiceScreen extends Screen {

    private static final double APPEAR_TIME = 0.25;
    private static final int OFFSET = 10;
    private static final int SPACING = 10;
    private static final int BASE_Y = 60;
    private static final int MAX_CHOICES = 4;

    private final List<String> choices;
    private final List<ChoiceButtonWidget> buttons = new ArrayList<>();
    private final int totalTick;
    private int currentTick;

    public ChoiceScreen(List<String> choices) {
        super(Component.empty());
        this.choices = choices.subList(0, Math.min(choices.size(), MAX_CHOICES));
        this.totalTick = (int) (APPEAR_TIME * 20.0);
    }

    @Override
    protected void init() {
        ResourceLocation soundId = ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "sfx.choice_appear");
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(soundId), 1.0f));

        buttons.clear();
        for (int i = 0; i < choices.size(); i++) {
            final int index = i;
            ChoiceButtonWidget button = new ChoiceButtonWidget(choices.get(i), i, this::select);
            button.setOpacity(5);
            button.setCanPress(false);
            buttons.add(button);
        }

        int maxWidthUpDown = 0;
        for (int i = 1; i < buttons.size(); i += 2) {
            if (buttons.get(i).getWidth() > maxWidthUpDown) {
                maxWidthUpDown = buttons.get(i).getWidth();
            }
        }

        for (int i = 0; i < buttons.size(); i++) {
            ChoiceButtonWidget button = buttons.get(i);
            int currentX = 0;
            int offsetX = 0;
            int offsetY = 0;
            int currentY = this.height - BASE_Y;
            if (buttons.size() == 4) currentY -= button.getHeight();

            switch (i) {
                case 0:
                    if (buttons.size() == 1) {
                        currentX = this.width / 2 - button.getWidth() / 2;
                    } else if (buttons.size() > 2) {
                        currentX = this.width / 2 - button.getWidth() - maxWidthUpDown / 2;
                    } else {
                        currentX = this.width / 2 - button.getWidth() - SPACING;
                    }
                    offsetX = OFFSET;
                    break;
                case 1:
                    if (buttons.size() > 2) {
                        currentY -= button.getHeight() + SPACING;
                        currentX = this.width / 2 - button.getWidth() / 2;
                        offsetY = OFFSET;
                    } else {
                        currentX = this.width / 2 + SPACING;
                        offsetX = -OFFSET;
                    }
                    break;
                case 2:
                    currentX = this.width / 2 + maxWidthUpDown / 2;
                    offsetX = -OFFSET;
                    break;
                case 3:
                    currentY += button.getHeight() + SPACING;
                    currentX = this.width / 2 - button.getWidth() / 2;
                    offsetY = -OFFSET;
                    break;
            }

            button.setX(currentX);
            button.setY(currentY);
            button.setBaseOffset(offsetX, offsetY);
            button.setRenderOffset(offsetX, offsetY);
            addRenderableWidget(button);
        }

        currentTick = 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (currentTick < totalTick) {
            currentTick++;
        }
        for (ChoiceButtonWidget button : buttons) {
            button.tick();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        double t = Mth.clamp((currentTick + partialTick) / (float) totalTick, 0.0, 1.0);
        int opacity = (int) Mth.lerp(t, 5.0, 255.0);

        for (ChoiceButtonWidget button : buttons) {
            button.setOpacity(opacity);
            button.setRenderOffset((float) Mth.lerp(t, button.getBaseOffsetX(), 0.0), (float)
                    Mth.lerp(t, button.getBaseOffsetY(), 0.0));
            if (t >= 1.0) {
                button.setCanPress(true);
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE) return false;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public void onClose() {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void select(int index) {
        Services.PACKET.sendToServer(new C2SChoiceSelected(index));
        ClientNarrativeCraftMod.getInstance().getPlayerSession().setChoiceScreen(null);
        minecraft.setScreen(null);
    }
}
