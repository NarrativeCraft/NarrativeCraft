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
import fr.loudo.narrativecraft.api.events.interaction.InteractionTriggerEvent;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkCommand;
import fr.loudo.narrativecraft.api.inkAction.Side;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.editors.interaction.InteractionMakerEditorMaker;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.interaction.Interaction;
import fr.loudo.narrativecraft.narrative.interaction.InteractionSerializer;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.network.interaction.S2CInteractionEditorData;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;

@InkCommand(
        keyword = "interaction",
        description = "Activates or deactivates a named interaction zone within the current scene.",
        syntax = "interaction <action:string> <interactionName:string>",
        side = Side.SERVER)
public class InteractionInkAction extends InkAction {

    private String action;
    private Interaction interaction;

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {
        action = cmd.getString("action");
        if (!action.equals("start") && !action.equals("remove")) {
            return InkActionResult.error("Unknown interaction action '" + action + "' (expected 'start' or 'remove')");
        }
        String interactionName = cmd.getString("interactionName");
        interaction = ((Scene) scene).getInteractionManager().getByName(interactionName);
        if (interaction == null) {
            return InkActionResult.error(Translation.message(
                            NOT_EXISTS_KEY, Translation.message("interaction").getString(), interactionName)
                    .getString());
        }
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        PlayerSession session = (PlayerSession) playerSession;
        EditorMaker lastEditorMaker = session.getEditor();
        if (action.equals("start")) {
            NarrativeCraftMod.EVENT_BUS.post(new InteractionTriggerEvent(playerSession, interaction));
            InteractionMakerEditorMaker editorMaker =
                    new InteractionMakerEditorMaker(interaction, session, NarrativeEnvironment.PRODUCTION);
            session.openEditor(editorMaker);
            String dataJson = InteractionSerializer.serializeData(interaction);
            Services.PACKET.sendToPlayer(
                    session.getPlayer(), new S2CInteractionEditorData(interaction.getId(), dataJson));
        } else if (action.equals("remove")) {
            if (lastEditorMaker instanceof InteractionMakerEditorMaker) {
                session.closeEditor();
            }
        }

        return InkActionResult.singleOk();
    }
}
