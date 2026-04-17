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

package fr.loudo.narrativecraft.narrative;

import fr.loudo.narrativecraft.narrative.animation.AnimationEditor;
import fr.loudo.narrativecraft.narrative.animation.AnimationPayload;
import fr.loudo.narrativecraft.narrative.chapter.ChapterEditor;
import fr.loudo.narrativecraft.narrative.chapter.ChapterPayload;
import fr.loudo.narrativecraft.narrative.character.CharacterEditor;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryPayload;
import fr.loudo.narrativecraft.narrative.cutscene.CutsceneEditor;
import fr.loudo.narrativecraft.narrative.cutscene.CutscenePayload;
import fr.loudo.narrativecraft.narrative.scene.SceneEditor;
import fr.loudo.narrativecraft.narrative.scene.ScenePayload;
import fr.loudo.narrativecraft.narrative.subscene.SubsceneEditor;
import fr.loudo.narrativecraft.narrative.subscene.SubscenePayload;

public class NarrativeEditorsRegister {

    public static void register() {
        NarrativeEntryEditorRegistry.getInstance().register(ChapterPayload.class, new ChapterEditor());
        NarrativeEntryEditorRegistry.getInstance().register(ScenePayload.class, new SceneEditor());
        NarrativeEntryEditorRegistry.getInstance().register(AnimationPayload.class, new AnimationEditor());
        NarrativeEntryEditorRegistry.getInstance().register(SubscenePayload.class, new SubsceneEditor());
        NarrativeEntryEditorRegistry.getInstance().register(CutscenePayload.class, new CutsceneEditor());
        NarrativeEntryEditorRegistry.getInstance().register(CharacterStoryPayload.class, new CharacterEditor());
    }
}
