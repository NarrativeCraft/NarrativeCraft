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

package fr.loudo.narrativecraft.screens.narrative.chapter;

import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.screens.AbstractNarrativeEntryEditScreen;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;

public class ChapterEntryEditScreen extends AbstractNarrativeEntryEditScreen<Chapter> {

    private EditBox chapterIndexField;
    private StringWidget chapterIndexLabel;

    public ChapterEntryEditScreen(Screen lastScreen) {
        super(lastScreen);
    }

    public ChapterEntryEditScreen(Chapter entry, Screen lastScreen) {
        super(entry, lastScreen);
    }

    @Override
    protected void addCustomFields() {
        chapterIndexField = new EditBox(this.font, 20, 20, Translation.message("chapter_index"));
        chapterIndexField.setFilter(s -> s.matches(Utils.ONLY_NUMBERS));
        chapterIndexField.setValue(entry != null ? String.valueOf(entry.getChapterIndex()) : "");
        widgets.add(chapterIndexField);

        chapterIndexLabel = new StringWidget(Translation.message("chapter_index"), this.font);
    }

    @Override
    protected void renderWidget(AbstractWidget widget, int x, int y) {
        super.renderWidget(widget, x, y);
        if (widget.equals(chapterIndexField)) {
            chapterIndexLabel.setPosition(x, y + this.font.lineHeight / 2 + 2);
            chapterIndexField.setPosition(x + chapterIndexLabel.getWidth() + GAP, y);
        }
    }

    private int getChapterIndex() {
        return Integer.parseInt(chapterIndexField.getValue());
    }

    @Override
    protected Chapter handleValidation() {
        return new Chapter(getName(), getDescription(), getChapterIndex());
    }
}
