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

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.screens.PaginationsItemsScreen;
import fr.loudo.narrativecraft.files.NarrativeCraftFileUtil;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.screens.Screen;

public class SelectChaptersScreen extends PaginationsItemsScreen<Chapter> {

    private final Screen lastScreen;

    public SelectChaptersScreen(Screen lastScreen) {
        super(
                Translation.message("screen.main.select_chapter.title"),
                ClientNarrativeCraftMod.getInstance().getChapterManager().getList(),
                NarrativeCraftFileUtil.getChaptersFolder());
        this.lastScreen = lastScreen;
    }

    @Override
    protected String getItemName(Chapter chapter) {
        return chapter.formattedName();
    }

    @Override
    protected void onItemClicked(Chapter chapter) {
        minecraft.setScreen(new SelectScenesScreen(this, (MainScreen) lastScreen, chapter));
    }

    @Override
    protected boolean isItemClickable(Chapter chapter) {
        return !chapter.getSceneManager().getList().isEmpty();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }
}
