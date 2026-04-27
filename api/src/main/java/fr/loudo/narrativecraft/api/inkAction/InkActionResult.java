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

package fr.loudo.narrativecraft.api.inkAction;

/**
 * The outcome of an {@link InkAction}'s execution.
 *
 * <p>Return one of the static factory methods from {@code doExecute()}:
 * <ul>
 *   <li>{@link #ok()}, action ran and finished immediately.</li>
 *   <li>{@link #block()}, action started and will complete asynchronously;
 *       the tag queue is paused until the action sets {@code isRunning = false}.</li>
 *   <li>{@link #ignored()}, action intentionally did nothing (e.g. condition not met).</li>
 *   <li>{@link #error(String)}, something went wrong; the story will be stopped.</li>
 * </ul>
 */
public record InkActionResult(Status status, String errorMessage) {

    public enum Status {
        OK,
        IGNORED,
        BLOCK,
        ERROR,
        WARN
    }

    public static InkActionResult ok() {
        return new InkActionResult(Status.OK, null);
    }

    public static InkActionResult ignored() {
        return new InkActionResult(Status.IGNORED, null);
    }

    /** Pauses the tag queue until the action flips {@code isRunning = false}. */
    public static InkActionResult block() {
        return new InkActionResult(Status.BLOCK, null);
    }

    public static InkActionResult error(String message) {
        return new InkActionResult(Status.ERROR, message);
    }

    public static InkActionResult warn(String message) {
        return new InkActionResult(Status.WARN, message);
    }

    public boolean isOk() {
        return status == Status.OK || status == Status.IGNORED;
    }

    public boolean isIgnore() {
        return status == Status.IGNORED;
    }

    public boolean isBlock() {
        return status == Status.BLOCK;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }

    public boolean isWarn() {
        return status == Status.WARN;
    }
}
