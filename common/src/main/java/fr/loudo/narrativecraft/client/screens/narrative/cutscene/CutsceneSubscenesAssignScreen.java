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

package fr.loudo.narrativecraft.client.screens.narrative.cutscene;

import fr.loudo.narrativecraft.client.screens.AbstractAssignScreen;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.cutscene.CutscenePayload;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.NarrativeEntryAction;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CutsceneSubscenesAssignScreen extends AbstractAssignScreen<Subscene> {

    private final Cutscene cutscene;

    public CutsceneSubscenesAssignScreen(Cutscene cutscene, Screen lastScreen) {
        super(Component.literal("Assign Subscenes"), lastScreen);
        this.cutscene = cutscene;
    }

    @Override
    protected List<Subscene> buildAvailableList() {
        Set<UUID> assignedIds = new HashSet<>();
        for (Subscene subscene : cutscene.getSubscenes()) assignedIds.add(subscene.getId());
        List<Subscene> result = new ArrayList<>();
        for (Subscene subscene : cutscene.getScene().getSubsceneManager().getList()) {
            if (!assignedIds.contains(subscene.getId())) result.add(subscene);
        }
        return result;
    }

    @Override
    protected List<Subscene> buildAssignedList() {
        return new ArrayList<>(cutscene.getSubscenes());
    }

    @Override
    protected String getItemName(Subscene item) {
        return item.getName();
    }

    @Override
    protected String getBreadcrumb() {
        return cutscene.getScene().getChapter().getName()
                + ";" + cutscene.getScene().getName()
                + ";" + cutscene.getName();
    }

    @Override
    protected Component getAvailableColumnLabel() {
        return Translation.message("screen.cutscene_assign.available");
    }

    @Override
    protected Component getAssignedColumnLabel() {
        return Translation.message("screen.cutscene_assign.assigned");
    }

    @Override
    protected void onSave(List<Subscene> assigned) {
        List<UUID> animationIds =
                cutscene.getAnimations().stream().map(Animation::getId).toList();
        List<UUID> subsceneIds = assigned.stream().map(Subscene::getId).toList();
        Services.PACKET.sendToServer(new BiSyncNarrativeEntryPacket(
                cutscene.getId(),
                new CutscenePayload(
                        cutscene.getName(),
                        cutscene.getDescription(),
                        cutscene.getScene().getId(),
                        cutscene.getScene().getChapter().getId(),
                        animationIds,
                        subsceneIds),
                NarrativeEntryAction.EDIT));
        minecraft.gui.setScreen(lastScreen);
    }
}
