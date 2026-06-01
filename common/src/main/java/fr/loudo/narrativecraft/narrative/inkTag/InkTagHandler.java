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

package fr.loudo.narrativecraft.narrative.inkTag;

import com.bladecoder.ink.runtime.Story;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkActionUtil;
import fr.loudo.narrativecraft.api.inkAction.Side;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.narrative.inkTag.InkTagDispatcherImpl.DispatchResult;
import fr.loudo.narrativecraft.network.inkAction.S2CRunInkAction;
import fr.loudo.narrativecraft.network.inkAction.S2CStopAllInkActions;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.session.PlayerSession;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages the server-side queue of Ink tags and drives their sequential execution.
 *
 * <h2>Blocking model</h2>
 * <p>When an action returns {@link InkActionResult#block()} (server side) or declares
 * {@link InkAction#isBlocking()} (client side), the queue pauses. Each server tick,
 * {@link #tick()} detects when the blocking action finishes and resumes draining.
 *
 * <h2>Client-side actions</h2>
 * <p>For actions with {@link Side#CLIENT}, the server sends a {@link S2CRunInkAction}
 * packet. If the action is blocking, a {@link ClientActionStub} holds the blocking slot
 * until a {@link fr.loudo.narrativecraft.network.inkAction.C2SInkActionFinished} ack arrives.
 *
 * <h2>Lifecycle integration</h2>
 * <p>Create one instance per story session. Pass a {@link Lifecycle} to react to errors
 * and to advance the story when all tags are drained. Wire {@link #tick()} to
 * {@link fr.loudo.narrativecraft.events.server.OnServerTickEvent}.
 */
public final class InkTagHandler {

    public interface Lifecycle {
        void onTagsDrained();

        void onError(InkTagHandlerException exception);
    }

    private static final AtomicLong nextInstanceId = new AtomicLong(1);

    private final PlayerSession playerSession;
    private final Lifecycle lifecycle;
    private final InkTagDispatcherImpl dispatcher =
            NarrativeCraftMod.getInstance().getInkTagDispatcher();

    private final Deque<String> pendingTags = new ArrayDeque<>();

    private final List<InkAction> runningServerActions = new ArrayList<>();

    private InkAction blockingAction = null;
    private Story story;

    private final Map<Long, ClientActionStub> pendingClientAcks = new HashMap<>();

    public InkTagHandler(PlayerSession playerSession, Lifecycle lifecycle) {
        this.playerSession = playerSession;
        this.lifecycle = lifecycle;
    }

    public InkTagHandler(PlayerSession playerSession, Lifecycle lifecycle, Story story) {
        this.playerSession = playerSession;
        this.lifecycle = lifecycle;
        this.story = story;
    }

    public void enqueue(List<String> tags) {
        pendingTags.addAll(tags);
        drain();
    }

    public void tick() {
        if (blockingAction != null && !blockingAction.isRunning()) {
            blockingAction = null;
            drain();
        }

        runningServerActions.removeIf(action -> {
            action.tick();
            return !action.isRunning();
        });
    }

    /**
     * Called by {@link fr.loudo.narrativecraft.network.handlers.ServerPacketHandler} when a
     * {@link fr.loudo.narrativecraft.network.inkAction.C2SInkActionFinished} ack arrives from the client.
     */
    public void onClientActionFinished(long instanceId) {
        ClientActionStub stub = pendingClientAcks.remove(instanceId);
        if (stub != null) {
            stub.finishRemotely();
        }
    }

    public void stopAll() {
        blockingAction = null;
        pendingClientAcks.clear();

        for (InkAction action : runningServerActions) {
            action.stop();
        }
        runningServerActions.clear();

        Services.PACKET.sendToPlayer(playerSession.getPlayer(), new S2CStopAllInkActions());
    }

    public List<String> getPendingTags() {
        return new ArrayList<>(pendingTags);
    }

    private void drain() {
        if (blockingAction != null) return;

        while (!pendingTags.isEmpty()) {
            String rawTag = pendingTags.poll();
            rawTag = InkActionUtil.parseVariables(story, rawTag);

            DispatchResult dispatchResult;
            try {
                dispatchResult = dispatcher.dispatch(rawTag, playerSession.getScene());
            } catch (InkTagHandlerException exception) {
                lifecycle.onError(exception);
                return;
            }

            if (dispatchResult == null) continue; // unknown keyword, already logged

            InkAction action = dispatchResult.action();
            ParsedCommand cmd = dispatchResult.cmd();

            InkActionResult result = runOrSend(action, cmd);

            if (result.isError()) {
                lifecycle.onError(new InkTagHandlerException(result.errorMessage()));
                return;
            }

            // blockingAction was set inside runOrSend when result.isBlock()
            if (result.isBlock()) return;
        }

        lifecycle.onTagsDrained();
    }

    /**
     * Executes the action on the server (if {@link Side#SERVER}) or sends it to the client
     * (if {@link Side#CLIENT}).
     *
     * <p>For server actions: the result of {@code doExecute} determines blocking.
     * For client actions: {@link InkAction#isBlocking()} (set during {@code doValidate}) determines
     * whether a stub is created and the queue pauses.
     */
    private InkActionResult runOrSend(InkAction action, ParsedCommand cmd) {
        long instanceId = nextInstanceId.getAndIncrement();
        action.setInstanceId(instanceId);

        if (action.getSide() == Side.SERVER) {
            InkActionResult result = action.execute(playerSession);
            if (!result.isError() && result.isBlock()) {
                runningServerActions.add(action);
                blockingAction = action;
            } else {
                action.setRunning(false);
            }
            return result;
        }

        boolean blocking = action.isBlocking();
        Services.PACKET.sendToPlayer(
                playerSession.getPlayer(),
                new S2CRunInkAction(instanceId, action.getKeyword(), cmd.toJson(), blocking));

        if (blocking) {
            ClientActionStub stub = new ClientActionStub(instanceId);
            pendingClientAcks.put(instanceId, stub);
            blockingAction = stub;
            return InkActionResult.block();
        }

        return InkActionResult.ok();
    }
}
