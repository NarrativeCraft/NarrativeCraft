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

package fr.loudo.narrativecraft.screens.narrative.scene;

import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.screens.AbstractNarrativeEntryEditScreen;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;

public class SceneEntryEditScreen extends AbstractNarrativeEntryEditScreen<Scene> {

    private final Chapter chapter;
    private EditBox sceneRankField;
    private StringWidget sceneRankLabel;

    public SceneEntryEditScreen(Chapter chapter, Screen lastScreen) {
        super(lastScreen);
        this.chapter = chapter;
    }

    public SceneEntryEditScreen(Scene entry, Screen lastScreen) {
        super(entry, lastScreen);
        this.chapter = entry.getChapter();
    }

    @Override
    protected void addCustomFields() {
        if (entry == null) return; // If we're creating a new scene, don't show the rank field

        sceneRankField = new EditBox(this.font, 20, 20, Translation.message("scene_rank"));
        sceneRankField.setValue(String.valueOf(entry.getRank()));
        widgets.add(sceneRankField);

        sceneRankLabel = new StringWidget(Translation.message("scene_rank"), this.font);
    }

    @Override
    protected boolean hasValidated() {
        if (!super.hasValidated()) {
            return false;
        }

        int sceneRank = getSceneRank();
        if (sceneRank == -1) {
            sendToastError(Translation.message("error"), Translation.message("error.must_have_scene_rank"));
            return false;
        }

        Scene scene = chapter.getSceneManager().getByName(getName());
        if (scene != null) {
            sendToastError(
                    Translation.message("error"),
                    Translation.message(
                            "error.already_exists", Translation.message("scene").getString(), scene.getName()));
            return false;
        }

        int maxSize = chapter.getSceneManager().size();
        if (sceneRank > maxSize + (entry == null ? 1 : 0)) { // Allow +1 for new scene
            sendToastError(
                    Translation.message("error"),
                    Translation.message("error.scene_rank_greather_than_size", sceneRank, maxSize));
            return false;
        }

        return true;
    }

    @Override
    protected void renderWidget(AbstractWidget widget, int x, int y) {
        super.renderWidget(widget, x, y);
        if (widget.equals(sceneRankField)) {
            sceneRankLabel.setPosition(x, y + this.font.lineHeight / 2 + 2);
            sceneRankField.setPosition(x + sceneRankLabel.getWidth() + GAP, y);
            addRenderableWidget(sceneRankLabel);
        }
    }

    private int getSceneRank() {
        if (entry == null) {
            return chapter.getSceneManager().size() + 1;
        }
        if (sceneRankField == null) return -1;
        if (sceneRankField.getValue().isEmpty()) return -1;
        if (!sceneRankField.getValue().matches(Utils.ONLY_NUMBERS)) return -1;
        return Integer.parseInt(sceneRankField.getValue());
    }

    @Override
    protected Scene createInstance() {
        return new Scene(getName(), getDescription(), this.chapter, getSceneRank());
    }
}
