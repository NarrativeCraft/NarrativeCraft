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

package fr.loudo.narrativecraft.api.client;

import fr.loudo.narrativecraft.api.client.inkAction.ClientInkTagDispatcher;
import fr.loudo.narrativecraft.api.client.signals.ClientSignalEmitter;
import fr.loudo.narrativecraft.api.client.signals.ClientSignalRegistry;

public class NarrativeCraftClientAPI {

    private static volatile NarrativeCraftClientAPI INSTANCE;

    private final ClientInkTagDispatcher inkTagDispatcher;
    private final ClientSignalRegistry signalRegistry;
    private final ClientSignalEmitter signalEmitter;

    private NarrativeCraftClientAPI(
            ClientInkTagDispatcher inkTagDispatcher,
            ClientSignalRegistry signalRegistry,
            ClientSignalEmitter signalEmitter) {
        this.inkTagDispatcher = inkTagDispatcher;
        this.signalRegistry = signalRegistry;
        this.signalEmitter = signalEmitter;
    }

    static void initialize(
            ClientInkTagDispatcher inkTagDispatcher,
            ClientSignalRegistry signalRegistry,
            ClientSignalEmitter signalEmitter) {
        INSTANCE = new NarrativeCraftClientAPI(inkTagDispatcher, signalRegistry, signalEmitter);
    }

    public static boolean isAvailable() {
        return INSTANCE != null;
    }

    public static NarrativeCraftClientAPI getInstance() {
        NarrativeCraftClientAPI instance = INSTANCE;
        if (instance == null) {
            throw new IllegalStateException(
                    "NarrativeCraft client API is not available. "
                            + "It only exists on a physical client, and only once NarrativeCraft client initialization has run. "
                            + "and guard the call with NarrativeCraftClientAPI.isAvailable() if the code also runs on a server.");
        }
        return instance;
    }

    public ClientInkTagDispatcher getInkTagDispatcher() {
        return inkTagDispatcher;
    }

    public ClientSignalRegistry getSignalRegistry() {
        return signalRegistry;
    }

    public ClientSignalEmitter getSignalEmitter() {
        return signalEmitter;
    }
}
