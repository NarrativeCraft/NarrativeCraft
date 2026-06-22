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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a handler for a specific Ink tag keyword.
 *
 * <p>Must be present on every concrete {@link InkAction} subclass. Read once at startup
 * to build the command spec and route incoming tags.
 *
 * <p>Example:
 * <pre>{@code
 * @InkCommand(
 *     keyword     = "sound",
 *     description = "Plays, stops, or fades a sound or music track on the client.",
 *     syntax      = "sound <action:string> <soundName:string> [volume:float=1.0] [--loop] [--block]",
 *     side        = Side.CLIENT
 * )
 * public class SoundInkAction extends InkAction { ... }
 * }</pre>
 *
 * <h2>Syntax grammar</h2>
 * <ul>
 *   <li>{@code <name:type>}, required positional argument</li>
 *   <li>{@code [name:type=default]}, optional named argument with default value</li>
 *   <li>{@code [--flag]}, boolean flag, absent = false</li>
 *   <li>Supported types: {@code string}, {@code int}, {@code float}, {@code boolean}</li>
 * </ul>
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InkCommand {

    /** The tag keyword that triggers this action (e.g. {@code "sound"}, {@code "emote"}). */
    String keyword();

    /** One sentence describing what this action does. Shown in tooling and error messages. */
    String description() default "";

    /**
     * The full syntax declaration used to build the command spec.
     *
     * <p>The first token must match {@link #keyword()}. Example:
     * {@code "sound <action:string> <soundName:string> [volume:float=1.0] [--loop]"}
     */
    String syntax();

    /** Which side executes this action. Defaults to {@link Side#SERVER}. */
    Side side() default Side.SERVER;
}
