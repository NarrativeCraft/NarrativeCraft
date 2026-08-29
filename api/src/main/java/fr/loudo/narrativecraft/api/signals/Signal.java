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

package fr.loudo.narrativecraft.api.signals;

import java.util.ArrayList;
import java.util.List;

public abstract class Signal {

    private final List<SignalArgument> arguments = new ArrayList<>();

    private void registerArgument(String argumentName, String argumentValue, SignalArgumentType type) {
        if (argumentAlreadyRegistered(argumentName)) {
            throw new IllegalStateException(String.format(
                    "Signal '%s' as already '%s' argument registered!",
                    this.getClass().getSimpleName(), argumentValue.toLowerCase()));
        }
        if (argumentValue == null) {
            argumentValue = "";
        }
        arguments.add(new SignalArgument(argumentName, argumentValue, type));
    }

    protected final void registerStringArgument(String argumentName, String argumentValue) {
        registerArgument(argumentName, argumentValue, SignalArgumentType.STRING);
    }

    protected final void registerIntArgument(String argumentName, int argumentValue) {
        registerArgument(argumentName, String.valueOf(argumentValue), SignalArgumentType.INT);
    }

    protected final void registerFloatArgument(String argumentName, float argumentValue) {
        registerArgument(argumentName, String.valueOf(argumentValue), SignalArgumentType.FLOAT);
    }

    protected final void registerBoolArgument(String argumentName, boolean argumentValue) {
        registerArgument(argumentName, String.valueOf(argumentValue), SignalArgumentType.BOOL);
    }

    public final List<SignalArgument> getSignalArguments() {
        return List.copyOf(arguments);
    }

    public final Object[] getArguments() {
        Object[] values = new Object[arguments.size()];
        for (int index = 0; index < arguments.size(); index++) {
            values[index] = arguments.get(index).toInkValue();
        }
        return values;
    }

    public final void emitOrThrow() {
        SignalType signalType = getSignalType();
        if (signalType == null) {
            throw new IllegalStateException(String.format(
                    "Signal '%s' as no signal type!", this.getClass().getSimpleName()));
        }
        if (arguments.size() != signalType.argumentCount()) {
            throw new IllegalStateException(String.format(
                    "Signal '%s' as %s arguments registered, while %s arguments is required!",
                    signalType.eventName(), arguments.size(), signalType.argumentCount()));
        }
        for (SignalArgument signalArgument : arguments) {
            if (!signalArgument.isValid()) {
                throw new IllegalStateException(
                        String.format("Signal '%s' ", signalType.eventName()) + signalArgument.errorMessage());
            }
        }
    }

    private boolean argumentAlreadyRegistered(String argumentName) {
        for (SignalArgument argument : arguments) {
            if (argument.argumentName().equalsIgnoreCase(argumentName)) {
                return true;
            }
        }
        return false;
    }

    public abstract SignalType getSignalType();
}
