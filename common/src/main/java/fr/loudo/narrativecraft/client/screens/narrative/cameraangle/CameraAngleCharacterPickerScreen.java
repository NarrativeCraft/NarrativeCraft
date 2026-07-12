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

package fr.loudo.narrativecraft.client.screens.narrative.cameraangle;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.cameraangle.ClientCameraAngleMakerEditorMaker.CharacterPick;
import fr.loudo.narrativecraft.client.screens.AbstractNarrativeEntryPickerScreen;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterType;
import fr.loudo.narrativecraft.narrative.npc.Npc;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class CameraAngleCharacterPickerScreen
        extends AbstractNarrativeEntryPickerScreen<CameraAngleCharacterPickerScreen.Entry> {

    private static final int FILTER_BUTTON_HEIGHT = 20;
    private static final int FILTER_BUTTON_GAP = 4;

    private final List<Entry> allEntries;
    private CharacterType filter = CharacterType.NORMAL;

    public CameraAngleCharacterPickerScreen(Scene scene, Screen lastScreen, Consumer<CharacterPick> onPick) {
        super(
                Translation.message("screen.camera_angle_editor.add_character"),
                buildEntries(scene),
                lastScreen,
                entry -> onPick.accept(new CharacterPick(entry.type(), entry.characterId())));
        this.allEntries = this.list;
    }

    private static List<Entry> buildEntries(Scene scene) {
        List<Entry> entries = new ArrayList<>();
        for (CharacterStory character :
                ClientNarrativeCraftMod.getInstance().getCharacterManager().getList()) {
            entries.add(new Entry(CharacterType.NORMAL, character.getId(), character.getName()));
        }
        if (scene != null) {
            for (Npc npc : scene.getNpcManager().getList()) {
                entries.add(new Entry(CharacterType.NPC, npc.getId(), npc.getName()));
            }
        }
        return entries;
    }

    @Override
    protected void init() {
        this.list = allEntries.stream().filter(entry -> entry.type() == filter).toList();
        super.init();
    }

    @Override
    protected int addHeaderWidgets(int x, int y) {
        int filterButtonWidth = (buttonWidth - FILTER_BUTTON_GAP) / 2;
        addFilterButton(
                Translation.message("screen.camera_angle_editor.filter.characters"),
                CharacterType.NORMAL,
                x,
                y,
                filterButtonWidth);
        addFilterButton(
                Translation.message("screen.camera_angle_editor.filter.npc"),
                CharacterType.NPC,
                x + filterButtonWidth + FILTER_BUTTON_GAP,
                y,
                filterButtonWidth);
        return FILTER_BUTTON_HEIGHT;
    }

    private void addFilterButton(Component label, CharacterType type, int x, int y, int width) {
        Button button = Button.builder(label, b -> {
                    filter = type;
                    page = 1;
                    reload();
                })
                .bounds(x, y, width, FILTER_BUTTON_HEIGHT)
                .build();
        button.active = filter != type;
        this.addRenderableWidget(button);
    }

    @Override
    protected String getItemName(Entry item) {
        return item.displayName();
    }

    public record Entry(CharacterType type, UUID characterId, String displayName) {}
}
