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

package fr.loudo.narrativecraft.narrative.inkTag;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkCommand;
import fr.loudo.narrativecraft.api.inkAction.InkTagDispatcher;
import fr.loudo.narrativecraft.api.inkAction.syntax.CommandSpec;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.inkAction.syntax.SyntaxParser;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;

/**
 * Routes raw Ink tag strings to the matching {@link InkAction} subclass.
 *
 * <p>Register your action once at startup:
 * <pre>{@code
 * InkTagDispatcher.getInstance().register(MyAction.class, MyAction::new);
 * }</pre>
 *
 * <p>The dispatcher reads {@link InkCommand} at registration time, compiles the
 * {@link CommandSpec} once via {@link SyntaxParser}, and reuses it for every tag.
 */
public final class InkTagDispatcherImpl implements InkTagDispatcher {

    private final Map<String, Entry> registrations = new LinkedHashMap<>();

    private record Entry(CommandSpec spec, Supplier<InkAction> factory) {}

    public InkTagDispatcherImpl() {}

    /**
     * Registers an {@link InkAction} class by reading its {@link InkCommand} annotation.
     * The syntax is compiled once here; call this at startup only.
     *
     * @param clazz   the action class, must carry {@link InkCommand}
     * @param factory a no-arg supplier that creates a fresh instance (e.g. {@code MyAction::new})
     */
    public <T extends InkAction> void register(Class<T> clazz, Supplier<T> factory) {
        InkCommand annotation = clazz.getAnnotation(InkCommand.class);
        if (annotation == null) {
            throw new IllegalArgumentException(clazz.getName() + " is missing @InkCommand. "
                    + "Add @InkCommand(keyword = \"...\", syntax = \"...\") to the class.");
        }
        CommandSpec spec = SyntaxParser.parse(annotation.keyword(), annotation.syntax());
        registrations.put(annotation.keyword(), new Entry(spec, factory::get));
        NarrativeCraftMod.LOGGER.info("Registered Ink action '{}'", annotation.keyword());
    }

    /**
     * The result of a successful dispatch: the validated action and its parsed command.
     * The {@link ParsedCommand} is kept alongside the action so the handler can
     * serialise it for the network without storing it on the action itself.
     */
    public record DispatchResult(InkAction action, ParsedCommand cmd) {}

    /**
     * Parses a raw tag string, validates it against the matching command spec, and
     * returns a validated (but not yet executed) {@link InkAction} instance with its
     * {@link ParsedCommand}.
     *
     * <p>Returns {@code null} and logs a warning if the keyword is unknown.
     *
     * @throws InkTagHandlerException if the tag is syntactically or semantically invalid
     */
    @Nullable
    public DispatchResult dispatch(String rawTag, IScene scene) throws InkTagHandlerException {
        return dispatch(rawTag, scene, UnaryOperator.identity());
    }

    @Nullable
    public DispatchResult dispatch(String rawTag, IScene scene, UnaryOperator<String> tokenLocalizer)
            throws InkTagHandlerException {
        List<String> tokens;
        try {
            tokens = TagTokenizer.tokenize(rawTag).stream().map(tokenLocalizer).toList();
        } catch (IllegalArgumentException e) {
            throw new InkTagHandlerException(e.getMessage(), e);
        }
        if (tokens.isEmpty()) return null;

        String keyword = tokens.get(0);
        Entry entry = registrations.get(keyword);
        if (entry == null) {
            NarrativeCraftMod.LOGGER.warn(
                    "Unknown Ink tag keyword '{}'. Registered keywords: {}", keyword, registrations.keySet());
            return null;
        }

        ParsedCommand cmd;
        try {
            cmd = entry.spec().parse(tokens.subList(1, tokens.size()));
        } catch (IllegalArgumentException e) {
            throw new InkTagHandlerException("Tag '" + keyword + "': " + e.getMessage(), e);
        }

        InkAction action = entry.factory().get();
        var validationResult = action.validate(cmd, scene);
        if (validationResult.isError()) {
            throw new InkTagHandlerException(validationResult.errorMessage());
        }

        return new DispatchResult(action, cmd);
    }

    /**
     * Creates a fresh unvalidated instance of the action for {@code keyword}.
     * Used by the client when receiving a pre-parsed {@link ParsedCommand} from the server.
     *
     * @return {@code null} if the keyword is not registered
     */
    @Nullable
    public InkAction instantiate(String keyword) {
        Entry entry = registrations.get(keyword);
        if (entry == null) {
            NarrativeCraftMod.LOGGER.warn("Client received unknown Ink action keyword '{}'", keyword);
            return null;
        }
        return entry.factory().get();
    }
}
