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

package fr.loudo.narrativecraft.client.screens.mainScreen;

import fr.loudo.narrativecraft.client.screens.PaginationsItemsScreen;
import fr.loudo.narrativecraft.files.NarrativeCraftFileUtil;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.network.story.C2SPlayStory;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.Optional;
import net.minecraft.client.gui.screens.Screen;

public class SelectScenesScreen extends PaginationsItemsScreen<Scene> {

    private final Screen lastScreen;
    private final MainScreen mainScreen;

    public SelectScenesScreen(Screen lastScreen, MainScreen mainScreen, Chapter chapter) {
        super(
                Translation.message("screen.main.select_scene.title"),
                chapter.getSceneManager().getList(),
                NarrativeCraftFileUtil.getScenesFolder(chapter));
        this.lastScreen = lastScreen;
        this.mainScreen = mainScreen;
    }

    @Override
    protected String getItemName(Scene scene) {
        return scene.getRank() + " - " + scene.formattedName();
    }

    @Override
    protected void onItemClicked(Scene scene) {
        mainScreen.close();
        Services.PACKET.sendToServer(new C2SPlayStory(Optional.of(scene.knotName()), false));
    }

    @Override
    protected boolean isItemClickable(Scene scene) {
        return true;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }
}
