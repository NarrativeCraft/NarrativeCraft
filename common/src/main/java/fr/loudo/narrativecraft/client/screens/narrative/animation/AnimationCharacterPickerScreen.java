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

package fr.loudo.narrativecraft.client.screens.narrative.animation;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.screens.PaginationsItemsScreen;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.NarrativeEntryAction;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.CustomFont;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AnimationCharacterPickerScreen extends PaginationsItemsScreen<ICharacterStory> {

    private final Animation animation;
    private final Screen lastScreen;
    private boolean npcMode = false;

    public AnimationCharacterPickerScreen(Animation animation, Screen lastScreen) {
        super(Translation.message("screen.animation_character_picker"), new ArrayList<>());
        this.animation = animation;
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        List<ICharacterStory> items = new ArrayList<>();
        if (npcMode) {
            items.addAll(animation.getScene().getNpcManager().getList());
        } else {
            items.addAll(
                    ClientNarrativeCraftMod.getInstance().getCharacterManager().getList());
        }
        this.list = items;

        super.init();

        if (npcMode) {
            Button characterButton = Button.builder(Component.literal(CustomFont.CHARACTER), b -> {
                        npcMode = false;
                        clearWidgets();
                        init();
                    })
                    .bounds(10, 10, 20, 20)
                    .build();
            characterButton.setTooltip(
                    Tooltip.create(Translation.message("npc").append(" -> ").append(Translation.message("character"))));
            addRenderableWidget(characterButton);
        } else {
            Button npcButton = Button.builder(Component.literal(CustomFont.CHARACTER), b -> {
                        npcMode = true;
                        clearWidgets();
                        init();
                    })
                    .bounds(10, 10, 20, 20)
                    .build();
            npcButton.setTooltip(Tooltip.create(
                    Translation.message("character").append(" -> ").append(Translation.message("npc"))));
            addRenderableWidget(npcButton);
        }
    }

    @Override
    protected String getItemName(ICharacterStory item) {
        String name = item.getName();
        UUID characterId = animation.getCharacterStory().getId();
        if (characterId != null && characterId.equals(item.getId())) {
            name += " ◀";
        }
        return name;
    }

    @Override
    protected void onItemClicked(ICharacterStory item) {
        animation.setCharacterStory(item);
        Services.PACKET.sendToServer(
                new BiSyncNarrativeEntryPacket(animation.getId(), animation.toPayload(), NarrativeEntryAction.EDIT));
        minecraft.gui.setScreen(lastScreen);
    }

    @Override
    protected boolean isItemClickable(ICharacterStory item) {
        return true;
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(lastScreen);
    }
}
