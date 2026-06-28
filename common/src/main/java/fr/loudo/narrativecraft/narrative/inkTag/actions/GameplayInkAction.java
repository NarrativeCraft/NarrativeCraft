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
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.editors.interaction.InteractionMakerEditorMaker;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.session.PlayerSession;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

@InkCommand(
        keyword = "gameplay",
        description = "Enables or disables standard player gameplay controls (movement, inventory, …) during a scene.",
        syntax = "gameplay [gamemode:string=adventure]",
        side = Side.SERVER)
public class GameplayInkAction extends InkAction {

    private GameType gameType = GameType.ADVENTURE;

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        String gamemode = cmd.getString("gamemode");
        if (!List.of("adventure", "survival", "creative", "spectator").contains(gamemode)) {
            return InkActionResult.error(String.format(
                    "Gamemode %s is not a valid gamemode. Only adventure, survival, creative and spectator", gamemode));
        }
        try {
            gameType = GameType.byName(gamemode.toLowerCase());
        } catch (Exception ignored) {
        }
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        PlayerSession currentSession = (PlayerSession) playerSession;
        StoryHandler storyHandler = currentSession.getStoryHandler();
        if (storyHandler == null) return InkActionResult.ignored();

        EditorMaker editorMaker = currentSession.getEditor();
        if (!(editorMaker instanceof InteractionMakerEditorMaker) && editorMaker != null) {
            editorMaker.stop();
        }
        currentSession.setGameplayMode(true);
        Entity mainCharacterEntity = storyHandler.getMainCharacterEntity();
        CharacterStory mainCharacter =
                NarrativeCraftMod.getInstance().getCharacterManager().getMainCharacter();

        playerSession.getPlayer().setGameMode(gameType);
        if (mainCharacterEntity == null) return InkActionResult.ok();

        Vec3 entityPosition = mainCharacterEntity.position();
        playerSession
                .getPlayer()
                .connection
                .teleport(
                        entityPosition.x,
                        entityPosition.y,
                        entityPosition.z,
                        mainCharacterEntity.getYRot(),
                        mainCharacterEntity.getXRot());
        storyHandler.unregisterEntity(mainCharacter);
        mainCharacterEntity.remove(Entity.RemovalReason.KILLED);

        isRunning = false;
        return InkActionResult.ok();
    }
}
