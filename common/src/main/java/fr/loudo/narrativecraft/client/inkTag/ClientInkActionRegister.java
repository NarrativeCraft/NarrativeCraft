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

package fr.loudo.narrativecraft.client.inkTag;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.client.inkTag.actions.ClientBorderInkAction;
import fr.loudo.narrativecraft.client.inkTag.actions.ClientFadeInkAction;
import fr.loudo.narrativecraft.client.inkTag.actions.ClientShakeScreenInkAction;
import fr.loudo.narrativecraft.client.inkTag.actions.sound.ClientSoundInkAction;
import fr.loudo.narrativecraft.client.inkTag.actions.text.ClientTextInkAction;
import fr.loudo.narrativecraft.narrative.inkTag.InkTagDispatcherImpl;

/**
 * Registers the client-only implementations of {@code Side.CLIENT} InkActions.
 *
 * <p>Called from {@link fr.loudo.narrativecraft.client.ClientNarrativeCraftMod#commonInit()},
 * which runs only when a client is present. Each registration overrides the server-safe base
 * class that was registered earlier in the common init, replacing it with the full client
 * implementation that handles rendering and execution.
 */
public final class ClientInkActionRegister {

    private ClientInkActionRegister() {}

    public static void register() {
        InkTagDispatcherImpl dispatcher = NarrativeCraftMod.getInstance().getInkTagDispatcher();
        dispatcher.register(ClientFadeInkAction.class, ClientFadeInkAction::new);
        dispatcher.register(ClientShakeScreenInkAction.class, ClientShakeScreenInkAction::new);
        dispatcher.register(ClientBorderInkAction.class, ClientBorderInkAction::new);
        dispatcher.register(ClientSoundInkAction.class, ClientSoundInkAction::new);
        dispatcher.register(ClientTextInkAction.class, ClientTextInkAction::new);
    }
}
