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

package fr.loudo.narrativecraft.events.server;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.playback.Playback;
import fr.loudo.narrativecraft.recording.Recording;
import fr.loudo.narrativecraft.session.PlayerSession;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;

public class OnServerTickEvent {

    private static final NarrativeCraftMod instance = NarrativeCraftMod.getInstance();

    public static void tick(MinecraftServer server) {

        for (Recording recording : instance.getRecordingManager().getList()) {
            recording.tick();
        }

        List<Playback> toRemove = new ArrayList<>();

        for (Playback playback : instance.getPlaybackManager().getList()) {
            if (playback.forSpecificPlayers()) {
                playback.hideEntitiesToOtherPlayers();
            }
            playback.tick();
            if (playback.isEnded()) {
                toRemove.add(playback);
            }
        }

        instance.getPlaybackManager().getList().removeAll(toRemove);

        for (PlayerSession playerSession : instance.getPlayerSessionManager().getList()) {
            EditorMaker editorMaker = playerSession.getEditor();
            if (editorMaker != null) {
                editorMaker.tick();
            }

            StoryHandler storyHandler = playerSession.getStoryHandler();
            if (storyHandler != null) {
                storyHandler.tick();
            }
        }
    }
}
