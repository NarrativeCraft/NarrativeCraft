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

package fr.loudo.narrativecraft.client.screens.narrative.chapter;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.screens.AbstractNarrativeEntryEditScreen;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
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
        if (entry == null) return; // If we're creating a new chapter, don't show chapter index field
        chapterIndexField = new EditBox(this.font, 20, 20, Translation.message("chapter_index"));
        chapterIndexField.setValue(entry != null ? String.valueOf(entry.getChapterIndex()) : "");
        widgets.add(chapterIndexField);

        chapterIndexLabel = new StringWidget(Translation.message("chapter_index"), this.font);
    }

    @Override
    protected boolean hasValidated() {
        if (!super.hasValidated()) {
            return false;
        }

        int chapterIndex = getChapterIndex();
        if (chapterIndex == -1) {
            sendToastError(Translation.message("error"), Translation.message("error.must_have_chapter_index"));
            return false;
        }

        int maxSize = ClientNarrativeCraftMod.getInstance().getChapterManager().size();
        if (chapterIndex > maxSize && entry != null) {
            sendToastError(
                    Translation.message("error"),
                    Translation.message("error.chapter_index_greather_than_size", chapterIndex, maxSize));
            return false;
        }

        return true;
    }

    @Override
    protected void renderWidget(AbstractWidget widget, int x, int y) {
        super.renderWidget(widget, x, y);
        if (widget.equals(chapterIndexField)) {
            chapterIndexLabel.setPosition(x, y + this.font.lineHeight / 2 + 2);
            chapterIndexField.setPosition(x + chapterIndexLabel.getWidth() + GAP, y);
            addRenderableWidget(chapterIndexLabel);
        }
    }

    private int getChapterIndex() {
        // If we're creating a new chapter, set the chapter index automatically
        if (entry == null) {
            return ClientNarrativeCraftMod.getInstance().getChapterManager().size() + 1;
        }
        if (chapterIndexField.getValue().isEmpty()) return -1;
        if (!chapterIndexField.getValue().matches(Utils.ONLY_NUMBERS)) return -1;
        return Integer.parseInt(chapterIndexField.getValue());
    }

    @Override
    protected Chapter createInstance() {
        return new Chapter(getName(), getDescription(), getChapterIndex());
    }
}
