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

package fr.loudo.narrativecraft.client.editors.cutscene;

import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneState;
import fr.loudo.narrativecraft.platform.Services;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class CutsceneEditorControl {

    private static final int LAYER_GAP = ClientCutsceneEditor.LAYER_GAP;
    private static final Component PLAY_LABEL = Component.literal("▶");
    private static final Component PAUSE_LABEL = Component.literal("⏸");

    private final Cutscene cutscene;
    private final Button button;
    private final int width;
    private final int height;
    private int x, y;
    private boolean playing = false;

    public CutsceneEditorControl(Cutscene cutscene, int width, int height) {
        this.cutscene = cutscene;
        this.width = width;
        this.height = height;
        this.button = Button.builder(PLAY_LABEL, b -> toggle())
                .bounds(0, 0, width, height)
                .build();
    }

    private void toggle() {
        playing = !playing;
        button.setMessage(playing ? PAUSE_LABEL : PLAY_LABEL);
        Services.PACKET.sendToServer(
                new C2SCutsceneState(playing ? C2SCutsceneState.State.PLAY : C2SCutsceneState.State.PAUSE, cutscene));
    }

    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, int mouseX, int mouseY) {
        button.setPosition(x, y);
        button.extractRenderState(graphics, mouseX, mouseY, deltaTracker.getGameTimeDeltaTicks());
    }

    public void mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        button.mouseClicked(event, isDoubleClick);
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Cutscene getCutscene() {
        return cutscene;
    }

    public Button getButton() {
        return button;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }
}
