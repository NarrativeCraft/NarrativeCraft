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

package fr.loudo.narrativecraft.narrative.inkTag.actions;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.events.story.ChapterSceneChangeEvent;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkCommand;
import fr.loudo.narrativecraft.api.inkAction.Side;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.network.S2CPlayerSession;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.session.PlayerSession;

@InkCommand(
        keyword = "on_enter",
        description = "Registers a one-shot callback that runs the next time the player enters this scene.",
        syntax = "on_enter",
        side = Side.SERVER)
public class OnEnterInkAction extends InkAction {

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        PlayerSession session = (PlayerSession) playerSession;
        StoryHandler storyHandler = session.getStoryHandler();
        if (storyHandler == null) return InkActionResult.ignored();

        String currentPath = storyHandler.getStory().getState().getCurrentPathString();
        if (currentPath == null) return InkActionResult.ignored();

        String knotName = currentPath.split("\\.")[0];

        Chapter chapter = StoryHandler.getChapterFromKnotName(knotName);
        if (chapter == null) return InkActionResult.ignored();

        Scene scene = StoryHandler.getSceneFromKnotName(chapter, knotName);
        if (scene == null) return InkActionResult.ignored();

        if (chapter.getId().equals(session.getChapter().getId())
                && scene.getId().equals(session.getScene().getId())) {
            return InkActionResult.ignored();
        }
        session.apply(chapter, scene);
        NarrativeCraftMod.EVENT_BUS.post(new ChapterSceneChangeEvent(playerSession, chapter, scene));

        storyHandler.triggerChangeScene();
        Services.PACKET.sendToPlayer(session.getPlayer(), new S2CPlayerSession(chapter.getId(), scene.getId()));
        storyHandler.save(true);
        isRunning = false;
        return InkActionResult.ok();
    }
}
