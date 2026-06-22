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

package fr.loudo.narrativecraft.api.inkAction;

import java.util.function.Supplier;

/**
 * Registry for {@link InkAction} implementations.
 *
 * <p>Call {@link #register} once at startup (before any Ink story runs) to make a custom action
 * available to the Ink tag system. Obtain the instance via
 * {@link fr.loudo.narrativecraft.api.NarrativeCraftAPI#getInkTagDispatcher()}.
 */
public interface InkTagDispatcher {

    /**
     * Registers an {@link InkAction} class so it can be triggered from Ink scripts.
     * The class must carry an {@link InkCommand} annotation that declares the keyword and syntax.
     *
     * @param clazz   the action class annotated with {@link InkCommand}
     * @param factory a no-arg supplier that creates a fresh instance (e.g. {@code MyAction::new})
     */
    <T extends InkAction> void register(Class<T> clazz, Supplier<T> factory);
}
