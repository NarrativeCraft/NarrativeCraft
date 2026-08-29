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

package fr.loudo.narrativecraft.signals;

import fr.loudo.narrativecraft.api.client.signals.ClientSignalRegistry;
import fr.loudo.narrativecraft.api.signals.SignalRegistry;
import fr.loudo.narrativecraft.api.signals.SignalType;
import fr.loudo.narrativecraft.api.utils.Side;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SignalRegistryImpl implements SignalRegistry, ClientSignalRegistry {

    private final List<SignalType> signals = new ArrayList<>();
    private final Side side;

    public SignalRegistryImpl(Side side) {
        this.side = side;
    }

    @Override
    public void register(SignalType signalType) {
        if (signalType == null) {
            throw new IllegalStateException("Tried to register a signal type with null value!");
        }
        if (signalType.side() == Side.CLIENT_SERVER) {
            throw new IllegalStateException("Side CLIENT_SERVER for a signal is not allowed.");
        }
        if (!acceptsSide(signalType)) {
            throw new IllegalStateException(String.format(
                    "Signal '%s' is declared for side %s and cannot be registered on the %s registry.",
                    signalType.eventName().toLowerCase(), signalType.side(), side));
        }
        if (isRegistered(signalType)) {
            throw new IllegalStateException(String.format(
                    "Signal '%s' is already registered.", signalType.eventName().toLowerCase()));
        }

        signals.add(signalType);
    }

    @Override
    public boolean isRegistered(SignalType signalType) {
        if (signalType == null) return false;

        for (SignalType registeredSignalType : signals) {
            if (registeredSignalType.eventName().equalsIgnoreCase(signalType.eventName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void unregister(SignalType signalType) {
        signals.remove(signalType);
    }

    @Override
    public List<SignalType> getRegisteredSignals() {
        return Collections.unmodifiableList(signals);
    }

    public Side getSide() {
        return side;
    }

    private boolean acceptsSide(SignalType signalType) {
        return signalType.side() == side || signalType.side() == Side.CLIENT_SERVER;
    }
}
