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

package fr.loudo.narrativecraft.narrative.inkTag.actions;

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkCommand;
import fr.loudo.narrativecraft.api.inkAction.Side;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.editors.cutscene.CutsceneMakerEditorMaker;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.network.cutscene.BiCutsceneEnter;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.playback.Playback;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.world.entity.Entity;

@InkCommand(
        keyword = "cutscene",
        description = "Triggers a pre-built cutscene sequence by name and blocks until it finishes.",
        syntax = "cutscene <cutsceneName:string>",
        side = Side.SERVER)
public class CutsceneInkAction extends InkAction {

    private Cutscene cutscene;
    private CutsceneMakerEditorMaker editorMaker;

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        blocking = true;
        String cutsceneName = cmd.getString("cutsceneName");
        cutscene = ((Scene) scene).getCutsceneManager().getByName(cutsceneName);
        if (cutscene == null) {
            return InkActionResult.error(Translation.message(
                            NOT_EXISTS_KEY, Translation.message("cutscene").getString(), cutsceneName)
                    .getString());
        }
        return InkActionResult.ok();
    }

    @Override
    public void tick() {
        if (editorMaker.isFinished()) {
            isRunning = false;
        }
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        editorMaker =
                new CutsceneMakerEditorMaker(cutscene, (PlayerSession) playerSession, NarrativeEnvironment.PRODUCTION);
        ((PlayerSession) playerSession).setEditor(editorMaker);
        Services.PACKET.sendToPlayer(
                playerSession.getPlayer(), new BiCutsceneEnter(cutscene, NarrativeEnvironment.PRODUCTION));
        StoryHandler storyHandler = ((PlayerSession) playerSession).getStoryHandler();
        editorMaker.init();

        // Re-use entity of a character if spawn animation point is close to current entity
        if (storyHandler != null) {
            for (Playback playback : editorMaker.getPlaybacks()) {
                Entity entity = storyHandler.getEntityFromCharacter(
                        playback.getAnimation().getCharacterStory());
                if (entity == null) continue;
                if (playback.getFirstPosition().distanceTo(entity.position()) <= 5.0) {
                    playback.setMasterEntity(entity);
                }
            }
        }
        editorMaker.start();
        if (storyHandler != null) {
            for (Playback playback : editorMaker.getPlaybacks()) {
                ICharacterStory characterStory = playback.getAnimation().getCharacterStory();
                storyHandler.registerEntity(characterStory, playback.getMasterEntity());
            }
        }
        return InkActionResult.block();
    }
}
