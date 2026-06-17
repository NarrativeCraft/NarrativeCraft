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

package fr.loudo.narrativecraft.client.screens.narrative.subscene;

import fr.loudo.narrativecraft.client.screens.AbstractAssignScreen;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.subscene.SubscenePayload;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.NarrativeEntryAction;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SubsceneAnimationsAssignScreen extends AbstractAssignScreen<Animation> {

    private final Subscene subscene;

    public SubsceneAnimationsAssignScreen(Subscene subscene, Screen lastScreen) {
        super(Component.literal("Assign Animations"), lastScreen);
        this.subscene = subscene;
    }

    @Override
    protected List<Animation> buildAvailableList() {
        Set<UUID> assignedIds = new HashSet<>();
        for (Animation anim : subscene.getAnimations()) assignedIds.add(anim.getId());
        List<Animation> result = new ArrayList<>();
        for (Animation anim : subscene.getScene().getAnimationManager().getList()) {
            if (!assignedIds.contains(anim.getId())) result.add(anim);
        }
        return result;
    }

    @Override
    protected List<Animation> buildAssignedList() {
        return new ArrayList<>(subscene.getAnimations());
    }

    @Override
    protected String getItemName(Animation item) {
        return item.getName();
    }

    @Override
    protected String getBreadcrumb() {
        return subscene.getScene().getChapter().getName()
                + ";" + subscene.getScene().getName()
                + ";" + subscene.getName();
    }

    @Override
    protected Component getAvailableColumnLabel() {
        return Translation.message("screen.subscene_assign.available");
    }

    @Override
    protected Component getAssignedColumnLabel() {
        return Translation.message("screen.subscene_assign.assigned");
    }

    @Override
    protected void onSave(List<Animation> assigned) {
        List<UUID> newAnimationIds = assigned.stream().map(Animation::getId).toList();
        Services.PACKET.sendToServer(new BiSyncNarrativeEntryPacket(
                subscene.getId(),
                new SubscenePayload(
                        subscene.getName(),
                        subscene.getDescription(),
                        subscene.getScene().getId(),
                        subscene.getScene().getChapter().getId(),
                        newAnimationIds),
                NarrativeEntryAction.EDIT));
        minecraft.gui.setScreen(lastScreen);
    }
}
