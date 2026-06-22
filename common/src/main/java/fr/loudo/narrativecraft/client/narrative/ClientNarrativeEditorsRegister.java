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

package fr.loudo.narrativecraft.client.narrative;

import fr.loudo.narrativecraft.client.narrative.animation.ClientAnimationEditor;
import fr.loudo.narrativecraft.client.narrative.cameraangle.ClientCameraAngleEditor;
import fr.loudo.narrativecraft.client.narrative.chapter.ClientChapterEditor;
import fr.loudo.narrativecraft.client.narrative.character.ClientCharacterEditor;
import fr.loudo.narrativecraft.client.narrative.character.ClientNpcEditor;
import fr.loudo.narrativecraft.client.narrative.cutscene.ClientCutsceneEditor;
import fr.loudo.narrativecraft.client.narrative.interaction.ClientInteractionEditor;
import fr.loudo.narrativecraft.client.narrative.scene.ClientSceneEditor;
import fr.loudo.narrativecraft.client.narrative.subscene.ClientSubsceneEditor;
import fr.loudo.narrativecraft.narrative.animation.AnimationPayload;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAnglePayload;
import fr.loudo.narrativecraft.narrative.chapter.ChapterPayload;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryPayload;
import fr.loudo.narrativecraft.narrative.cutscene.CutscenePayload;
import fr.loudo.narrativecraft.narrative.interaction.InteractionPayload;
import fr.loudo.narrativecraft.narrative.npc.NpcPayload;
import fr.loudo.narrativecraft.narrative.scene.ScenePayload;
import fr.loudo.narrativecraft.narrative.subscene.SubscenePayload;

public class ClientNarrativeEditorsRegister {

    public static void register() {
        ClientNarrativeEntryEditorRegistry.getInstance().register(ChapterPayload.class, new ClientChapterEditor());
        ClientNarrativeEntryEditorRegistry.getInstance().register(ScenePayload.class, new ClientSceneEditor());
        ClientNarrativeEntryEditorRegistry.getInstance().register(AnimationPayload.class, new ClientAnimationEditor());
        ClientNarrativeEntryEditorRegistry.getInstance().register(SubscenePayload.class, new ClientSubsceneEditor());
        ClientNarrativeEntryEditorRegistry.getInstance().register(CutscenePayload.class, new ClientCutsceneEditor());
        ClientNarrativeEntryEditorRegistry.getInstance()
                .register(CameraAnglePayload.class, new ClientCameraAngleEditor());
        ClientNarrativeEntryEditorRegistry.getInstance()
                .register(InteractionPayload.class, new ClientInteractionEditor());
        ClientNarrativeEntryEditorRegistry.getInstance()
                .register(CharacterStoryPayload.class, new ClientCharacterEditor());
        ClientNarrativeEntryEditorRegistry.getInstance().register(NpcPayload.class, new ClientNpcEditor());
    }
}
