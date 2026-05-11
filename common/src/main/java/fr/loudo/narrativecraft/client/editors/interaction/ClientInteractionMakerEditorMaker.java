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

package fr.loudo.narrativecraft.client.editors.interaction;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.screens.ClearScreen;
import fr.loudo.narrativecraft.client.screens.narrative.interaction.InteractionPointListScreen;
import fr.loudo.narrativecraft.client.screens.narrative.interaction.InteractionZoneListScreen;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.interaction.Interaction;
import fr.loudo.narrativecraft.narrative.interaction.InteractionDeserializer;
import fr.loudo.narrativecraft.narrative.interaction.InteractionSerializer;
import fr.loudo.narrativecraft.narrative.interaction.InteractionZone;
import fr.loudo.narrativecraft.network.interaction.C2SInteractionSave;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.CustomFont;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.UtilsClient;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class ClientInteractionMakerEditorMaker implements EditorMaker {

    private static final int BUTTON_WIDTH = 20;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 10;
    private static final int MENU_BUTTON_WIDTH = 80;

    private final Minecraft minecraft = Minecraft.getInstance();
    private final ClientPlayerSession playerSession =
            ClientNarrativeCraftMod.getInstance().getPlayerSession();
    private final Interaction interaction;
    private final List<Button> buttons = new ArrayList<>();
    private final List<Button> cornerPlacementButtons = new ArrayList<>();

    private boolean inCornerPlacementMode = false;
    private InteractionZone zoneBeingEdited = null;
    private Screen cornerPlacementReturnScreen = null;
    private Vec3 tempCorner1 = null;
    private Vec3 tempCorner2 = null;
    private NarrativeEnvironment environment;

    public ClientInteractionMakerEditorMaker(Interaction interaction) {
        this.interaction = interaction;
        this.environment = NarrativeEnvironment.DEVELOPMENT;
    }

    public ClientInteractionMakerEditorMaker(Interaction interaction, NarrativeEnvironment environment) {
        this.environment = environment;
        this.interaction = interaction;
    }

    @Override
    public void init() {
        buttons.clear();
        buttons.add(Button.builder(Component.literal("✖"), b -> openQuitConfirm())
                .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        buttons.add(Button.builder(Component.literal(CustomFont.SAVE), b -> save())
                .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        buttons.add(Button.builder(Translation.message("screen.interaction.zones"), b -> openZones())
                .bounds(0, 0, MENU_BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        buttons.add(Button.builder(Translation.message("screen.interaction.points"), b -> openPoints())
                .bounds(0, 0, MENU_BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        cornerPlacementButtons.clear();
        cornerPlacementButtons.add(Button.builder(Component.literal("1"), b -> placeCorner(1))
                .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        cornerPlacementButtons.add(Button.builder(Component.literal("2"), b -> placeCorner(2))
                .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        cornerPlacementButtons.add(Button.builder(Component.literal("✖"), b -> cancelCornersAndExit())
                .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        cornerPlacementButtons.add(Button.builder(Component.literal(CustomFont.SAVE), b -> saveCornersAndExit())
                .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void stop() {}

    @Override
    public void tick() {}

    @Override
    public void teleportToEditorOrigin() {}

    @Override
    public NarrativeEnvironment getEnvironment() {
        return environment;
    }

    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (environment != NarrativeEnvironment.DEVELOPMENT) return;
        int[] mousePos = UtilsClient.getScaledMousePos();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        float partialTick = deltaTracker.getGameTimeDeltaTicks();

        if (inCornerPlacementMode) {
            int totalWidth = BUTTON_WIDTH * 4 + BUTTON_GAP * 3;
            int startX = screenWidth / 2 - totalWidth / 2;
            int bottomY = screenHeight - BUTTON_HEIGHT - 30;

            Button place1Button = cornerPlacementButtons.get(0);
            Button place2Button = cornerPlacementButtons.get(1);
            Button cancelButton = cornerPlacementButtons.get(2);
            Button saveButton = cornerPlacementButtons.get(3);

            place1Button.setPosition(startX, bottomY);
            place2Button.setPosition(startX + BUTTON_WIDTH + BUTTON_GAP, bottomY);
            cancelButton.setPosition(startX + (BUTTON_WIDTH + BUTTON_GAP) * 2, bottomY);
            saveButton.setPosition(startX + (BUTTON_WIDTH + BUTTON_GAP) * 3, bottomY);

            place1Button.extractRenderState(graphics, mousePos[0], mousePos[1], partialTick);
            place2Button.extractRenderState(graphics, mousePos[0], mousePos[1], partialTick);
            cancelButton.extractRenderState(graphics, mousePos[0], mousePos[1], partialTick);
            saveButton.extractRenderState(graphics, mousePos[0], mousePos[1], partialTick);
        } else {
            Button quitButton = buttons.get(0);
            Button saveButton = buttons.get(1);
            Button zonesButton = buttons.get(2);
            Button pointsButton = buttons.get(3);

            quitButton.setPosition(5, 5);
            saveButton.setPosition(quitButton.getX() + quitButton.getWidth() + 5, 5);

            int totalBottomWidth = MENU_BUTTON_WIDTH * 2 + BUTTON_GAP;
            int bottomStartX = screenWidth / 2 - totalBottomWidth / 2;
            int bottomY = screenHeight - BUTTON_HEIGHT - 30;
            zonesButton.setPosition(bottomStartX, bottomY);
            pointsButton.setPosition(bottomStartX + MENU_BUTTON_WIDTH + BUTTON_GAP, bottomY);

            quitButton.extractRenderState(graphics, mousePos[0], mousePos[1], partialTick);
            saveButton.extractRenderState(graphics, mousePos[0], mousePos[1], partialTick);
            zonesButton.extractRenderState(graphics, mousePos[0], mousePos[1], partialTick);
            pointsButton.extractRenderState(graphics, mousePos[0], mousePos[1], partialTick);
        }
    }

    public void mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!clearScreenOpened() || environment != NarrativeEnvironment.DEVELOPMENT) return;
        List<Button> activeButtons = inCornerPlacementMode ? cornerPlacementButtons : buttons;
        for (Button button : activeButtons) {
            if (button.mouseClicked(event, isDoubleClick)) return;
        }
    }

    private boolean clearScreenOpened() {
        return minecraft.screen instanceof ClearScreen;
    }

    private void save() {
        String dataJson = InteractionSerializer.serializeData(interaction);
        Services.PACKET.sendToServer(new C2SInteractionSave(interaction, dataJson));
    }

    private void openZones() {
        minecraft.setScreen(new InteractionZoneListScreen(this, minecraft.screen));
    }

    private void openPoints() {
        minecraft.setScreen(new InteractionPointListScreen(this, minecraft.screen));
    }

    private void openQuitConfirm() {
        ConfirmScreen confirmScreen = new ConfirmScreen(
                b -> {
                    if (b) save();
                    quit();
                },
                Translation.message("screen.confirm.title"),
                Translation.message("screen.confirm.save"));
        minecraft.setScreen(confirmScreen);
    }

    public void enterCornerPlacementMode(InteractionZone zone, Screen returnScreen) {
        this.zoneBeingEdited = zone;
        this.cornerPlacementReturnScreen = returnScreen;
        this.tempCorner1 = zone.getCorner1();
        this.tempCorner2 = zone.getCorner2();
        this.inCornerPlacementMode = true;
        minecraft.setScreen(null);
    }

    private void placeCorner(int cornerNumber) {
        LocalPlayer player = minecraft.player;
        if (player == null) return;
        Vec3 position = player.position();
        if (cornerNumber == 1) {
            tempCorner1 = position;
        } else {
            tempCorner2 = position;
        }
    }

    private void saveCornersAndExit() {
        if (zoneBeingEdited != null) {
            zoneBeingEdited.setCorner1(tempCorner1);
            zoneBeingEdited.setCorner2(tempCorner2);
        }
        exitCornerPlacementMode();
    }

    private void cancelCornersAndExit() {
        exitCornerPlacementMode();
    }

    private void exitCornerPlacementMode() {
        this.inCornerPlacementMode = false;
        Screen returnTo = cornerPlacementReturnScreen;
        this.zoneBeingEdited = null;
        this.cornerPlacementReturnScreen = null;
        this.tempCorner1 = null;
        this.tempCorner2 = null;
        if (returnTo != null) {
            minecraft.setScreen(returnTo);
        }
    }

    public void loadData(String json) {
        InteractionDeserializer.deserializeInto(json, interaction);
    }

    public void quit() {
        playerSession.setEditor(null);
        minecraft.setScreen(null);
    }

    public Interaction getInteraction() {
        return interaction;
    }

    public boolean isInCornerPlacementMode() {
        return inCornerPlacementMode;
    }

    public InteractionZone getZoneBeingEdited() {
        return zoneBeingEdited;
    }

    public Vec3 getTempCorner1() {
        return tempCorner1;
    }

    public Vec3 getTempCorner2() {
        return tempCorner2;
    }
}
