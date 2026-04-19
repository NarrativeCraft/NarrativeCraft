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

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.cameraangle.ClientCameraAngleMakerEditor.CharacterPick;
import fr.loudo.narrativecraft.client.screens.AbstractNarrativeEntryPickerScreen;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterType;
import fr.loudo.narrativecraft.narrative.npc.Npc;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;

public class CameraAngleCharacterPickerScreen
        extends AbstractNarrativeEntryPickerScreen<CameraAngleCharacterPickerScreen.Entry> {

    public CameraAngleCharacterPickerScreen(Scene scene, Screen lastScreen, Consumer<CharacterPick> onPick) {
        super(
                Translation.message("screen.camera_angle_editor.add_character"),
                buildEntries(scene),
                lastScreen,
                entry -> onPick.accept(new CharacterPick(entry.type(), entry.characterId())));
    }

    private static List<Entry> buildEntries(Scene scene) {
        List<Entry> entries = new ArrayList<>();
        for (CharacterStory character :
                ClientNarrativeCraftMod.getInstance().getCharacterManager().getList()) {
            entries.add(new Entry(CharacterType.NORMAL, character.getId(), character.getName()));
        }
        for (Npc npc : scene.getNpcManager().getList()) {
            entries.add(new Entry(CharacterType.NPC, npc.getId(), npc.getName()));
        }
        return entries;
    }

    @Override
    protected String getItemName(Entry item) {
        return item.displayName();
    }

    public record Entry(CharacterType type, UUID characterId, String displayName) {}
}
