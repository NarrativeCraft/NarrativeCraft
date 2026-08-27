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

package fr.loudo.narrativecraft.client.editors.cutscene.layers.text;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayerType;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.client.settings.ClientStoryTranslations;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.TextKeyframe;
import fr.loudo.narrativecraft.narrative.inkTag.InkTagDispatcherImpl;
import fr.loudo.narrativecraft.narrative.inkTag.InkTagDispatcherImpl.DispatchResult;
import fr.loudo.narrativecraft.narrative.inkTag.InkTagHandlerException;
import fr.loudo.narrativecraft.narrative.inkTag.actions.TextInkAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class TextLayer extends CutsceneLayer {

    private final HashMap<Integer, List<TextInkAction>> executedText = new HashMap<>();

    public TextLayer(ICutsceneLayerType layerType) {
        super(layerType);
    }

    @Override
    public TextKeyframe createDefaultKeyframe(int tick) {
        return new TextKeyframe(this, tick);
    }

    @Override
    public boolean execute(float tick) {
        for (TextKeyframe keyframe : getSortedKeyframes(TextKeyframe.class)) {
            if (tick < keyframe.getTick()) {
                removeExecutedText(keyframe.getTick());
            } else if ((int) tick == keyframe.getTick() && !executedText.containsKey(keyframe.getTick())) {
                executeKeyframe(keyframe);
            }
        }
        return false;
    }

    @Override
    public void stop() {
        for (int keyframeTick : new ArrayList<>(executedText.keySet())) {
            removeExecutedText(keyframeTick);
        }
    }

    private void removeExecutedText(int keyframeTick) {
        List<TextInkAction> textInkActions = executedText.remove(keyframeTick);
        if (textInkActions == null) return;

        ClientPlayerSession playerSession =
                ClientNarrativeCraftMod.getInstance().getPlayerSession();
        Iterator<InkAction> iterator = playerSession.getActiveClientInkActions().iterator();
        while (iterator.hasNext()) {
            InkAction inkAction = iterator.next();
            if (textInkActions.contains(inkAction)) {
                inkAction.stop();
                iterator.remove();
            }
        }
    }

    private void executeKeyframe(TextKeyframe keyframe) {
        ClientPlayerSession playerSession =
                ClientNarrativeCraftMod.getInstance().getPlayerSession();
        InkTagDispatcherImpl dispatcher = ClientNarrativeCraftMod.getInstance().getInkTagDispatcher();
        List<TextInkAction> textInkActions =
                executedText.computeIfAbsent(keyframe.getTick(), tick -> new ArrayList<>());

        for (String tag : keyframe.getTags()) {
            if (tag.isBlank()) continue;

            DispatchResult dispatchResult;
            try {
                dispatchResult = dispatcher.dispatch(tag, playerSession.getScene(), ClientStoryTranslations::localize);
            } catch (InkTagHandlerException exception) {
                NarrativeCraftMod.LOGGER.warn("Invalid text keyframe tag '{}': {}", tag, exception.getMessage());
                continue;
            }

            if (dispatchResult == null) continue;

            InkAction inkAction = dispatchResult.action();
            if (!(inkAction instanceof TextInkAction textInkAction)) continue;

            if (!inkAction.execute(playerSession).isOk()) continue;

            playerSession.addClientInkAction(inkAction);
            textInkActions.add(textInkAction);
        }
    }
}
