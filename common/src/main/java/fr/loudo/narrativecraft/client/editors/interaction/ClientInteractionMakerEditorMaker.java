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

package fr.loudo.narrativecraft.client.editors.interaction;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.interaction.*;
import fr.loudo.narrativecraft.network.C2SNarrativeEntryDetailSave;
import fr.loudo.narrativecraft.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class ClientInteractionMakerEditorMaker implements EditorMaker {

    private final Minecraft minecraft = Minecraft.getInstance();
    private final ClientPlayerSession playerSession =
            ClientNarrativeCraftMod.getInstance().getPlayerSession();
    private final Interaction interaction;

    private boolean inCornerPlacementMode = false;
    private InteractionZone zoneBeingEdited = null;
    private Vec3 tempCorner1 = null;
    private Vec3 tempCorner2 = null;

    private boolean inPointPlacementMode = false;
    private InteractionPoint pointBeingPlaced = null;
    private Vec3 tempPointPosition = null;

    private final NarrativeEnvironment environment;

    public ClientInteractionMakerEditorMaker(Interaction interaction) {
        this.interaction = interaction;
        this.environment = NarrativeEnvironment.DEVELOPMENT;
    }

    public ClientInteractionMakerEditorMaker(Interaction interaction, NarrativeEnvironment environment) {
        this.environment = environment;
        this.interaction = interaction;
    }

    @Override
    public void init() {}

    @Override
    public void close() {}

    @Override
    public void tick() {}

    @Override
    public NarrativeEnvironment getEnvironment() {
        return environment;
    }

    public void save() {
        Services.PACKET.sendToServer(C2SNarrativeEntryDetailSave.of(interaction, interaction.getDetail()));
    }

    public void quit(boolean saveBeforeQuit) {
        if (saveBeforeQuit) {
            save();
        }
        quit();
    }

    public void enterCornerPlacementMode(InteractionZone zone) {
        this.zoneBeingEdited = zone;
        this.tempCorner1 = zone.getCorner1();
        this.tempCorner2 = zone.getCorner2();
        this.inCornerPlacementMode = true;
        minecraft.gui.setScreen(null);
    }

    public void placeCorner(int cornerNumber) {
        LocalPlayer player = minecraft.player;
        if (player == null) return;
        Vec3 position = player.position();
        if (cornerNumber == 1) {
            tempCorner1 = position;
        } else {
            tempCorner2 = position;
        }
    }

    public void saveCornersAndExit() {
        if (zoneBeingEdited != null) {
            zoneBeingEdited.setCorner1(tempCorner1);
            zoneBeingEdited.setCorner2(tempCorner2);
        }
        exitCornerPlacementMode();
    }

    public void cancelCornersAndExit() {
        exitCornerPlacementMode();
    }

    private void exitCornerPlacementMode() {
        this.inCornerPlacementMode = false;
        this.zoneBeingEdited = null;
        this.tempCorner1 = null;
        this.tempCorner2 = null;
    }

    public void enterPointPlacementMode(InteractionPoint point) {
        this.pointBeingPlaced = point;
        this.tempPointPosition = point.getPosition();
        this.inPointPlacementMode = true;
        minecraft.gui.setScreen(null);
    }

    public void placePoint() {
        LocalPlayer player = minecraft.player;
        if (player == null) return;
        tempPointPosition = player.position().add(0, player.getEyeHeight(), 0);
    }

    public void savePointAndExit() {
        if (pointBeingPlaced != null) {
            pointBeingPlaced.setPosition(tempPointPosition);
        }
        exitPointPlacementMode();
    }

    public void cancelPointAndExit() {
        exitPointPlacementMode();
    }

    private void exitPointPlacementMode() {
        this.inPointPlacementMode = false;
        this.pointBeingPlaced = null;
        this.tempPointPosition = null;
    }

    public void quit() {
        playerSession.requestEditorClose();
        minecraft.gui.setScreen(null);
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

    public boolean isInPointPlacementMode() {
        return inPointPlacementMode;
    }

    public InteractionPoint getPointBeingPlaced() {
        return pointBeingPlaced;
    }

    public Vec3 getTempPointPosition() {
        return tempPointPosition;
    }
}
