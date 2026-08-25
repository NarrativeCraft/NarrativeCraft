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
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkCommand;
import fr.loudo.narrativecraft.api.inkAction.Side;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.playback.Playback;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@InkCommand(
        keyword = "subscene",
        description = "Plays a named subscene sequence, optionally looping or blocking until it finishes.",
        syntax = "subscene <action:string> <subsceneName:string> [loop:boolean=false] [unique:boolean=false] [--block]",
        side = Side.SERVER)
public class SubsceneInkAction extends InkAction {

    private String action;
    private Subscene subscene;
    private final List<Playback> playbacks = new ArrayList<>();
    private boolean loop;
    private boolean unique;

    @Nullable
    private StoryHandler storyHandler;

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        action = cmd.getString("action");
        if (!action.equals("play") && !action.equals("stop")) {
            return InkActionResult.error("Unknown subscene action '" + action + "' (expected 'play' or 'stop')");
        }
        String subsceneName = cmd.getString("subsceneName");
        subscene = ((Scene) scene).getSubsceneManager().getByName(subsceneName);
        if (subscene == null) {
            return InkActionResult.error(Translation.message(
                            NOT_EXISTS_KEY, Translation.message("subscene").getString(), subsceneName)
                    .getString());
        }
        if (action.equals("play")) {
            loop = cmd.getBoolean("loop");
            unique = cmd.getBoolean("unique");
            if (cmd.flag("block")) blocking = true;
        }
        return InkActionResult.ok();
    }

    @Override
    public void tick() {
        if (playbacks.isEmpty() || playbacks.stream().anyMatch(playback -> !playback.isEnded())) return;

        if (loop) {
            for (Playback playback : playbacks) {
                Animation animation = playback.getAnimation();
                Entity oldMasterEntity = playback.getMasterEntity();
                Vec3 firstPosition = playback.getFirstPosition();

                if (oldMasterEntity.position().distanceTo(firstPosition) <= 5.0) {
                    playback.rewindTo(0);
                    playback.play();
                    NarrativeCraftMod.getInstance().getPlaybackManager().add(playback);
                    continue;
                }

                ICharacterStory characterStory = animation.getCharacterStory();
                if (storyHandler != null && characterStory != null) {
                    storyHandler.unregisterEntity(characterStory, oldMasterEntity);
                }
                playback.stopAndKill();
                playback.setKillOnEnd(false);
                playback.start();
                NarrativeCraftMod.getInstance().getPlaybackManager().add(playback);
                if (storyHandler != null && characterStory != null) {
                    Entity newMasterEntity = playback.getMasterEntity();
                    if (newMasterEntity != null) {
                        storyHandler.registerEntity(characterStory, newMasterEntity);
                    }
                }
            }
        } else {
            stop();
        }
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        PlayerSession session = (PlayerSession) playerSession;
        storyHandler = session.getStoryHandler();

        if (action.equals("stop")) {
            stopRunningPlaybacks();
            return InkActionResult.singleOk();
        }

        for (Animation animation : subscene.getAnimations()) {
            if (!animation.initialize()) {
                stop();
                return InkActionResult.error("Failed to load animation data: " + animation.getName());
            }

            Playback playback = new Playback(animation, session.getPlayer());
            if (unique && !loop) {
                playback.setKillOnEnd(true);
            }

            ICharacterStory characterStory = animation.getCharacterStory();
            Entity initialEntity = null;
            if (storyHandler != null && characterStory != null) {
                initialEntity = storyHandler
                        .getCharacterEntities()
                        .get(characterStory.getName().toLowerCase());
                if (initialEntity != null && initialEntity.position().distanceTo(playback.getFirstPosition()) <= 1) {
                    playback.setMasterEntity(initialEntity);
                }
            }

            playback.start(Collections.singleton(playerSession.getPlayer()));
            NarrativeCraftMod.getInstance().getPlaybackManager().add(playback);

            if (storyHandler != null && characterStory != null) {
                Entity masterEntity = playback.getMasterEntity();
                if (masterEntity != null && (initialEntity == null || !masterEntity.is(initialEntity))) {
                    storyHandler.registerEntity(characterStory, masterEntity);
                }
            }
            playbacks.add(playback);
        }

        if (blocking) {
            return InkActionResult.block();
        }
        if (!loop) {
            return InkActionResult.singleOk();
        }
        return InkActionResult.ok();
    }

    private void stopRunningPlaybacks() {
        List<Playback> toRemove = new ArrayList<>();
        for (Playback running :
                NarrativeCraftMod.getInstance().getPlaybackManager().getList()) {
            if (subscene.getAnimations().contains(running.getAnimation())) {
                toRemove.add(running);
            }
        }
        for (Playback playback : toRemove) {
            playback.stopAndKill();
            NarrativeCraftMod.getInstance().getPlaybackManager().remove(playback);
        }
    }
}
