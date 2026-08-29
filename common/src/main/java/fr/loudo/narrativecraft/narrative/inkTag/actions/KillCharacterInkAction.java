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
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.api.utils.Side;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.world.entity.Entity;

@InkCommand(
        keyword = "kill",
        description = "Removes a named character from the scene, despawning their entity.",
        syntax = "kill <characterName:string>",
        side = Side.SERVER)
public class KillCharacterInkAction extends InkAction {

    private ICharacterStory character;

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        String characterName = cmd.getString("characterName");
        character = NarrativeCraftMod.getInstance().getCharacterManager().getByName(characterName);
        if (character == null && scene != null) {
            character = ((Scene) scene).getNpcManager().getByName(characterName);
        }
        if (character == null) {
            return InkActionResult.error(Translation.message(
                            NOT_EXISTS_KEY, Translation.message("character").getString(), characterName)
                    .getString());
        }
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        StoryHandler storyHandler = ((PlayerSession) playerSession).getStoryHandler();
        if (storyHandler == null) {
            return InkActionResult.ignored();
        }
        if (!storyHandler.characterInStory(character)) {
            return InkActionResult.ignored();
        }
        Entity entity =
                storyHandler.getCharacterEntities().get(character.getName().toLowerCase());
        if (entity != null) {
            storyHandler.unregisterEntity(character, entity);
            entity.remove(Entity.RemovalReason.KILLED);
        }
        return InkActionResult.singleOk();
    }
}
