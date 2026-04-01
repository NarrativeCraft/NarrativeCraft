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

package fr.loudo.narrativecraft.network.handlers;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.cutscene.ClientCutsceneMakerEditor;
import fr.loudo.narrativecraft.client.editors.cutscene.CutsceneMakerEditorPlayHead;
import fr.loudo.narrativecraft.client.narrative.ClientNarrativeEntryEditorRegistry;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.S2CPlayerSession;
import fr.loudo.narrativecraft.network.cutscene.BiCutscenePlayHeadPacket;
import net.minecraft.client.Minecraft;

public class ClientPacketHandler {

    public static final Minecraft MINECRAFT = Minecraft.getInstance();

    public static void narrativeEntry(BiSyncNarrativeEntryPacket packet) {
        switch (packet.action()) {
            case ADD -> ClientNarrativeEntryEditorRegistry.getInstance().add(packet.entryId(), packet.entry());
            case EDIT -> ClientNarrativeEntryEditorRegistry.getInstance().edit(packet.entryId(), packet.entry());
            case DELETE -> ClientNarrativeEntryEditorRegistry.getInstance().delete(packet.entryId(), packet.entry());
        }
    }

    public static void clearNarrativeData() {
        ClientNarrativeCraftMod.getInstance().getChapterManager().clear();
    }

    public static void clearScreen() {
        MINECRAFT.setScreen(null);
    }

    public static void setSession(S2CPlayerSession packet) {

        ChapterManager chapterManager = ClientNarrativeCraftMod.getInstance().getChapterManager();
        Chapter chapter = chapterManager.getById(packet.chapterId());
        if (chapter == null) return;

        Scene scene = chapter.getSceneManager().getById(packet.sceneId());
        if (scene == null) return;

        ClientNarrativeCraftMod.getInstance().getPlayerSession().apply(chapter, scene);
    }

    public static void updatePlayHeadCutscene(BiCutscenePlayHeadPacket packet) {

        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        if (!(session.getEditor() instanceof ClientCutsceneMakerEditor editor)) return;

        CutsceneMakerEditorPlayHead playHead = editor.getPlayHead();
        playHead.setRatio(packet.ratio());
        if (packet.ratio() == 1.0f) {
            editor.getControl().pause();
        }
    }
}
