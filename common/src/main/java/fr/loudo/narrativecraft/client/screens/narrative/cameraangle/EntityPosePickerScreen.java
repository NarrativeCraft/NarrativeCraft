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

package fr.loudo.narrativecraft.client.screens.narrative.cameraangle;

import fr.loudo.narrativecraft.narrative.cameraangle.CharacterPlacement;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleSetEntityPose;
import fr.loudo.narrativecraft.platform.Services;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Pose;

public class EntityPosePickerScreen extends Screen {

    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 2;
    private static final int MARGIN_RIGHT = 5;

    private final CharacterPlacement characterPlacement;

    public EntityPosePickerScreen(CharacterPlacement characterPlacement) {
        super(Component.empty());
        this.characterPlacement = characterPlacement;
    }

    @Override
    protected void init() {
        Pose[] poses = new Pose[] {Pose.STANDING, Pose.CROUCHING, Pose.SLEEPING, Pose.FALL_FLYING, Pose.SHOOTING};
        int totalHeight = poses.length * (BUTTON_HEIGHT + BUTTON_GAP) - BUTTON_GAP;
        int startY = (height - totalHeight) / 2;
        int x = width - BUTTON_WIDTH - MARGIN_RIGHT;
        for (int i = 0; i < poses.length; i++) {
            Pose pose = poses[i];
            int y = startY + i * (BUTTON_HEIGHT + BUTTON_GAP);
            addRenderableWidget(Button.builder(Component.literal(pose.name()), b -> pickPose(pose))
                    .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
        }
    }

    private void pickPose(Pose pose) {
        characterPlacement.setPose(pose);
        Services.PACKET.sendToServer(new C2SCameraAngleSetEntityPose(characterPlacement.getId(), pose));
    }

    @Override
    protected void extractBlurredBackground(GuiGraphicsExtractor graphics) {}

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
