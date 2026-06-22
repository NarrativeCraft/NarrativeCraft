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

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Base class for all Ink tag actions.
 *
 * <p>An action is bound to a keyword via {@link InkCommand} and triggered when that keyword
 * appears as a tag in an Ink script. The dispatcher parses the raw tag, validates its arguments
 * against the declared syntax, then calls {@link #validate} and {@link #execute} in sequence.
 *
 * <p>Actions that need to pause the tag queue must set {@code blocking = true} in
 * {@link #doValidate} and return {@link InkActionResult#block()} from {@link #doExecute}.
 * The queue resumes once {@code isRunning} is set to {@code false}.
 */
public abstract class InkAction {

    public static final String NOT_EXISTS_KEY = "story.validation_error.not_exists";

    /** Unique id assigned by the dispatcher before the action enters the queue. */
    protected long instanceId;

    protected boolean canBeExecuted = false;
    protected boolean isRunning = false;
    /**
     * Set to {@code true} in {@link #doValidate} when the action needs to pause the tag queue.
     * For server-side actions this is inferred from the {@link InkActionResult#block()} return value;
     * for client-side actions the handler reads this field before sending the packet.
     */
    protected boolean blocking = false;

    protected int tick;
    protected int totalTick;

    public void tick() {}

    public void partialTick(float partialTick) {}

    public void render(GuiGraphicsExtractor guiGraphics, float partialTick) {}

    public void render(PoseStack poseStack, float partialTick) {}

    public void stop() {
        isRunning = false;
    }

    /**
     * Initializes the action's fields from the parsed command and optionally validates
     * business rules against the current scene. Called once before {@link #execute}.
     *
     * <p>Return {@link InkActionResult#error} only for business-logic violations
     * (e.g. referenced character not found). Syntax errors are caught earlier by
     * {@link fr.loudo.narrativecraft.api.inkAction.syntax.CommandSpec}.
     */
    protected abstract InkActionResult doValidate(ParsedCommand cmd, IScene scene);

    /**
     * Runs the action. Use the provided {@link IPlayerSession} to access the player
     * and other server-side context.
     *
     * <p>Return {@link InkActionResult#block()} if the tag queue must wait for this action
     * to finish. When done, flip {@code isRunning = false} from any thread or tick.
     */
    protected abstract InkActionResult doExecute(IPlayerSession playerSession);

    /** @see #doValidate */
    public final InkActionResult validate(ParsedCommand cmd, IScene scene) {
        InkActionResult result = doValidate(cmd, scene);
        if (!result.isError()) canBeExecuted = true;
        return result;
    }

    /** @see #doExecute */
    public final InkActionResult execute(IPlayerSession playerSession) {
        if (!canBeExecuted) {
            return InkActionResult.error("Action executed before validation: " + getKeyword());
        }
        isRunning = true;
        return doExecute(playerSession);
    }

    /** The tag keyword declared in {@link InkCommand#keyword()}. */
    public String getKeyword() {
        InkCommand annotation = getClass().getAnnotation(InkCommand.class);
        return annotation != null ? annotation.keyword() : getClass().getSimpleName();
    }

    /** The execution side declared in {@link InkCommand#side()}. Defaults to {@link Side#SERVER}. */
    public Side getSide() {
        InkCommand annotation = getClass().getAnnotation(InkCommand.class);
        return annotation != null ? annotation.side() : Side.SERVER;
    }

    public long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    /**
     * Whether this action should pause the tag queue until it finishes.
     * For client-side actions, the server reads this before sending the packet.
     * Set {@code this.blocking = true} in {@link #doValidate} when needed.
     */
    public boolean isBlocking() {
        return blocking;
    }
}
