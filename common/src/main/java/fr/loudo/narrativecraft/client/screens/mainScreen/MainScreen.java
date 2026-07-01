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

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.mixin.invoker.PauseScreenInvoker;
import fr.loudo.narrativecraft.network.BiStopEditorMaker;
import fr.loudo.narrativecraft.network.story.C2SPlayStory;
import fr.loudo.narrativecraft.network.story.C2SStopStory;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.Translation;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.lwjgl.glfw.GLFW;

public class MainScreen extends Screen {

    private static final int BUTTON_WIDTH = 110;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;
    private static final int MARGIN_LEFT = 50;
    private static final int LOGO_GAP = 20;
    private static final int SECRET_CTRL_PRESSES = 10;

    private static final ResourceLocation LOGO_SPRITE = new ResourceLocation(NarrativeCraftMod.MOD_ID, "logo");
    private static final ResourceLocation LOGO_FILE =
            new ResourceLocation(NarrativeCraftMod.MOD_ID, "textures/gui/sprites/logo.png");
    private static final ResourceLocation BACKGROUND_MUSIC =
            new ResourceLocation(NarrativeCraftMod.MOD_ID, "music.main_screen");
    private static final SimpleSoundInstance MAIN_MUSIC_INSTANCE = new SimpleSoundInstance(
            SoundEvent.createVariableRangeEvent(BACKGROUND_MUSIC).getLocation(),
            SoundSource.MASTER,
            1.0F,
            1.0F,
            SoundInstance.createUnseededRandom(),
            true,
            0,
            SoundInstance.Attenuation.NONE,
            0.0F,
            0.0F,
            0.0F,
            true);

    private boolean canContinue;
    private boolean finishedStory;
    private boolean isPause;

    private int ctrlPressCount = 0;
    private Button leaveScreenButton;

    private boolean hasLogo;
    private int logoNativeWidth;
    private int logoNativeHeight;

    public MainScreen(boolean canContinue, boolean finishedStory) {
        super(Component.empty());
        this.canContinue = canContinue;
        this.finishedStory = finishedStory;
    }

    public MainScreen(boolean isPause) {
        super(Component.empty());
        this.isPause = isPause;
    }

    @Override
    protected void init() {
        loadLogoInfo();

        int buttonStartY = getButtonStartY();
        int buttonX = MARGIN_LEFT;

        int currentY = buttonStartY;

        if (canContinue || isPause) {
            addRenderableWidget(Button.builder(Translation.message("screen.main.continue"), button -> {
                        if (isPause) {
                            onClose();
                        } else {
                            close();
                            Services.PACKET.sendToServer(new C2SPlayStory(Optional.empty(), true, false));
                        }
                    })
                    .bounds(buttonX, currentY, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
            currentY += BUTTON_HEIGHT + BUTTON_GAP;
        }

        if (!isPause) {
            addRenderableWidget(Button.builder(Translation.message("screen.main.new_game"), button -> {
                        close();
                        Services.PACKET.sendToServer(new C2SPlayStory(Optional.empty(), false, true));
                    })
                    .bounds(buttonX, currentY, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
            currentY += BUTTON_HEIGHT + BUTTON_GAP;
        }

        if (isPause) {
            addRenderableWidget(Button.builder(Translation.message("screen.main.restart_scene"), button -> {
                        minecraft.setScreen(null);
                        ClientPlayerSession session =
                                ClientNarrativeCraftMod.getInstance().getPlayerSession();
                        Services.PACKET.sendToServer(
                                new C2SPlayStory(Optional.of(session.getScene().knotName()), true, false));
                    })
                    .bounds(buttonX, currentY, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
            currentY += BUTTON_HEIGHT + BUTTON_GAP;
        }

        if (finishedStory && !isPause) {
            addRenderableWidget(Button.builder(
                            Translation.message("screen.main.select_scene"),
                            button -> minecraft.setScreen(new SelectChaptersScreen(this)))
                    .bounds(buttonX, currentY, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
            currentY += BUTTON_HEIGHT + BUTTON_GAP;
        }

        addRenderableWidget(Button.builder(
                        Translation.message("screen.main.options"),
                        button -> minecraft.setScreen(new OptionsScreen(this)))
                .bounds(buttonX, currentY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        currentY += BUTTON_HEIGHT + BUTTON_GAP;

        addRenderableWidget(Button.builder(Translation.message("screen.main.quit"), button -> {
                    if (!isPause) {
                        close();
                        disconnect();
                    } else {
                        Services.PACKET.sendToServer(new C2SStopStory(true));
                    }
                })
                .bounds(buttonX, currentY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        if (!isPause) {
            leaveScreenButton = Button.builder(Translation.message("screen.main.leave_screen"), button -> {
                        minecraft.getSoundManager().stop(MAIN_MUSIC_INSTANCE);
                        Services.PACKET.sendToServer(BiStopEditorMaker.INSTANCE);
                    })
                    .bounds(width - BUTTON_WIDTH - 10, 10, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();
            leaveScreenButton.visible = ctrlPressCount >= SECRET_CTRL_PRESSES;
            addRenderableWidget(leaveScreenButton);
        }

        if (!minecraft.getSoundManager().isActive(MAIN_MUSIC_INSTANCE) && !isPause) {
            minecraft.getSoundManager().play(MAIN_MUSIC_INSTANCE);
        }
    }

    private int getButtonStartY() {
        int buttonCount = 3 + (canContinue ? 1 : 0) + (finishedStory ? 1 : 0) + (isPause ? 1 : 0);
        int totalButtonHeight = buttonCount * BUTTON_HEIGHT + (buttonCount - 1) * BUTTON_GAP;

        int logoDisplayWidth = Math.min(width / 3, 500);
        int logoDisplayHeight =
                (hasLogo && logoNativeWidth > 0) ? (logoDisplayWidth * logoNativeHeight / logoNativeWidth) : 0;

        int totalBlockHeight = (hasLogo ? logoDisplayHeight + LOGO_GAP : 0) + totalButtonHeight;
        int blockStartY = (height - totalBlockHeight) / 2;

        int buttonStartY = blockStartY + (hasLogo ? logoDisplayHeight + LOGO_GAP : 0);
        return buttonStartY;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        if (!hasLogo) return;

        int logoDisplayWidth = Math.min(width / 3, 500);
        int logoDisplayHeight =
                logoNativeWidth > 0 ? (logoDisplayWidth * logoNativeHeight / logoNativeWidth) : logoDisplayWidth / 4;

        int buttonCount = 3 + (canContinue ? 1 : 0) + (finishedStory ? 1 : 0);
        int totalButtonHeight = buttonCount * BUTTON_HEIGHT + (buttonCount - 1) * BUTTON_GAP;
        int totalBlockHeight = logoDisplayHeight + LOGO_GAP + totalButtonHeight;
        int blockStartY = (height - totalBlockHeight) / 2;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(
                LOGO_FILE,
                MARGIN_LEFT,
                blockStartY,
                logoDisplayWidth,
                logoDisplayHeight,
                0f,
                0f,
                logoNativeWidth,
                logoNativeHeight,
                logoNativeWidth,
                logoNativeHeight);
        RenderSystem.disableBlend();
    }

    public void close() {
        minecraft.setScreen(null);
        minecraft.getSoundManager().stop(MAIN_MUSIC_INSTANCE);
        EditorMaker editor =
                ClientNarrativeCraftMod.getInstance().getPlayerSession().getEditor();
        if (editor != null) {
            editor.stop();
        }
        ClientNarrativeCraftMod.getInstance().getPlayerSession().setEditor(null);
        Services.PACKET.sendToServer(BiStopEditorMaker.INSTANCE);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        if (isPause) super.renderBackground(guiGraphics);
    }

    @Override
    public boolean isPauseScreen() {
        return isPause;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL) {
            ctrlPressCount++;
            if (ctrlPressCount >= SECRET_CTRL_PRESSES && leaveScreenButton != null) {
                leaveScreenButton.visible = true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (isPause) {
            ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
            if (session.getChoiceScreen() != null) {
                minecraft.setScreen(session.getChoiceScreen());
                return;
            }
            super.onClose();
        }
    }

    private void loadLogoInfo() {
        Optional<Resource> resource = minecraft.getResourceManager().getResource(LOGO_FILE);
        if (resource.isEmpty()) {
            hasLogo = false;
            return;
        }
        try (InputStream stream = resource.get().open()) {
            NativeImage image = NativeImage.read(stream);
            logoNativeWidth = image.getWidth();
            logoNativeHeight = image.getHeight();
            image.close();
            hasLogo = true;
        } catch (IOException e) {
            hasLogo = false;
        }
    }

    private void disconnect() {
        PauseScreen pauseScreen = new PauseScreen(false);
        minecraft.setScreen(pauseScreen);
        ((PauseScreenInvoker) pauseScreen).callOnDisconnect();
    }
}
