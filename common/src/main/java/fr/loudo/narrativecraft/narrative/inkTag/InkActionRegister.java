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

package fr.loudo.narrativecraft.narrative.inkTag;

import fr.loudo.narrativecraft.narrative.inkTag.actions.AnimationInkAction;
import fr.loudo.narrativecraft.narrative.inkTag.actions.BorderInkAction;
import fr.loudo.narrativecraft.narrative.inkTag.actions.CameraAngleInkAction;
import fr.loudo.narrativecraft.narrative.inkTag.actions.ChangeDayTimeInkAction;
import fr.loudo.narrativecraft.narrative.inkTag.actions.CooldownInkAction;
import fr.loudo.narrativecraft.narrative.inkTag.actions.CutsceneInkAction;
import fr.loudo.narrativecraft.narrative.inkTag.actions.FadeInkAction;
import fr.loudo.narrativecraft.narrative.inkTag.actions.GameplayInkAction;
import fr.loudo.narrativecraft.narrative.inkTag.actions.InteractionInkAction;
import fr.loudo.narrativecraft.narrative.inkTag.actions.KillCharacterInkAction;
import fr.loudo.narrativecraft.narrative.inkTag.actions.MinecraftCommandInkAction;
import fr.loudo.narrativecraft.narrative.inkTag.actions.OnEnterInkAction;
import fr.loudo.narrativecraft.narrative.inkTag.actions.SaveInkAction;
import fr.loudo.narrativecraft.narrative.inkTag.actions.ShakeScreenInkAction;
import fr.loudo.narrativecraft.narrative.inkTag.actions.SubsceneInkAction;
import fr.loudo.narrativecraft.narrative.inkTag.actions.WeatherInkAction;
import fr.loudo.narrativecraft.narrative.inkTag.actions.sound.SoundInkAction;
import fr.loudo.narrativecraft.narrative.inkTag.actions.text.TextInkAction;

public final class InkActionRegister {

    private InkActionRegister() {}

    public static void register() {
        InkTagDispatcherImpl dispatcher = InkTagDispatcherImpl.getInstance();

        dispatcher.register(CooldownInkAction.class, CooldownInkAction::new);
        dispatcher.register(WeatherInkAction.class, WeatherInkAction::new);
        dispatcher.register(MinecraftCommandInkAction.class, MinecraftCommandInkAction::new);
        dispatcher.register(FadeInkAction.class, FadeInkAction::new);
        dispatcher.register(ShakeScreenInkAction.class, ShakeScreenInkAction::new);
        dispatcher.register(BorderInkAction.class, BorderInkAction::new);
        dispatcher.register(SoundInkAction.class, SoundInkAction::new);
        dispatcher.register(AnimationInkAction.class, AnimationInkAction::new);
        dispatcher.register(SubsceneInkAction.class, SubsceneInkAction::new);
        dispatcher.register(CutsceneInkAction.class, CutsceneInkAction::new);
        dispatcher.register(CameraAngleInkAction.class, CameraAngleInkAction::new);
        dispatcher.register(InteractionInkAction.class, InteractionInkAction::new);
        dispatcher.register(KillCharacterInkAction.class, KillCharacterInkAction::new);
        dispatcher.register(GameplayInkAction.class, GameplayInkAction::new);
        dispatcher.register(OnEnterInkAction.class, OnEnterInkAction::new);
        dispatcher.register(SaveInkAction.class, SaveInkAction::new);
        dispatcher.register(ChangeDayTimeInkAction.class, ChangeDayTimeInkAction::new);
        dispatcher.register(TextInkAction.class, TextInkAction::new);
    }
}
