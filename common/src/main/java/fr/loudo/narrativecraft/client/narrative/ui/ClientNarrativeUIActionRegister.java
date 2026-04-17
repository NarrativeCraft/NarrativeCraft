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

package fr.loudo.narrativecraft.client.narrative.ui;

import fr.loudo.narrativecraft.client.narrative.animation.ClientAnimationNarrativeUIAction;
import fr.loudo.narrativecraft.client.narrative.chapter.ClientChapterNarrativeUIAction;
import fr.loudo.narrativecraft.client.narrative.character.ClientCharacterNarrativeUIAction;
import fr.loudo.narrativecraft.client.narrative.cutscene.ClientCutsceneNarrativeUIAction;
import fr.loudo.narrativecraft.client.narrative.scene.ClientSceneNarrativeUIAction;
import fr.loudo.narrativecraft.client.narrative.subscene.ClientSubsceneNarrativeUIAction;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;

public class ClientNarrativeUIActionRegister {

    public static void register() {
        ClientNarrativeUIActionRegistry.getInstance().register(Chapter.class, new ClientChapterNarrativeUIAction());
        ClientNarrativeUIActionRegistry.getInstance().register(Scene.class, new ClientSceneNarrativeUIAction());
        ClientNarrativeUIActionRegistry.getInstance().register(Animation.class, new ClientAnimationNarrativeUIAction());
        ClientNarrativeUIActionRegistry.getInstance().register(Subscene.class, new ClientSubsceneNarrativeUIAction());
        ClientNarrativeUIActionRegistry.getInstance().register(Cutscene.class, new ClientCutsceneNarrativeUIAction());
        ClientNarrativeUIActionRegistry.getInstance()
                .register(CharacterStory.class, new ClientCharacterNarrativeUIAction());
    }
}
