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

package fr.loudo.narrativecraft.files;

import fr.loudo.narrativecraft.files.narrrative.animation.NarrativeCraftFileAnimation;
import fr.loudo.narrativecraft.files.narrrative.chapter.NarrativeCraftFileChapter;
import fr.loudo.narrativecraft.files.narrrative.character.NarrativeCraftFileCharacter;
import fr.loudo.narrativecraft.files.narrrative.character.NarrativeCraftFileNpc;
import fr.loudo.narrativecraft.files.narrrative.cutscene.NarrativeCraftFileCutscene;
import fr.loudo.narrativecraft.files.narrrative.scene.NarrativeCraftFileScene;
import fr.loudo.narrativecraft.files.narrrative.subscene.NarrativeCraftFileSubscene;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.Npc;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;

public class NarrativeCraftFileEditorsRegister {

    public static void register() {
        NarrativeCraftFileRegistry.getInstance().register(Chapter.class, new NarrativeCraftFileChapter());
        NarrativeCraftFileRegistry.getInstance().register(Scene.class, new NarrativeCraftFileScene());
        NarrativeCraftFileRegistry.getInstance().register(Animation.class, new NarrativeCraftFileAnimation());
        NarrativeCraftFileRegistry.getInstance().register(Subscene.class, new NarrativeCraftFileSubscene());
        NarrativeCraftFileRegistry.getInstance().register(Cutscene.class, new NarrativeCraftFileCutscene());
        NarrativeCraftFileRegistry.getInstance().register(CharacterStory.class, new NarrativeCraftFileCharacter());
        NarrativeCraftFileRegistry.getInstance().register(Npc.class, new NarrativeCraftFileNpc());
    }
}
