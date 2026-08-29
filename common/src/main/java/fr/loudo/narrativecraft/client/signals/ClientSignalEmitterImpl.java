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

package fr.loudo.narrativecraft.client.signals;

import fr.loudo.narrativecraft.api.client.signals.ClientSignalEmitter;
import fr.loudo.narrativecraft.api.signals.Signal;
import fr.loudo.narrativecraft.api.utils.Side;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.network.signals.C2SEmitSignal;
import fr.loudo.narrativecraft.platform.Services;

public class ClientSignalEmitterImpl implements ClientSignalEmitter {

    @Override
    public void emit(Signal signal) {
        ClientPlayerSession playerSession =
                ClientNarrativeCraftMod.getInstance().getPlayerSession();
        if (playerSession == null) return;

        if (!playerSession.isInStory()) return;

        signal.emitOrThrow();

        if (!ClientNarrativeCraftMod.getInstance().getSignalRegistry().isRegistered(signal.getSignalType())) {
            throw new IllegalStateException(String.format(
                    "Signal '%s' must be registered to be able to emit.",
                    signal.getSignalType().eventName().toLowerCase()));
        }

        if (signal.getSignalType().side() == Side.CLIENT) {
            Services.PACKET.sendToServer(
                    new C2SEmitSignal(signal.getSignalType().eventName(), signal.getSignalArguments()));
        }
    }
}
