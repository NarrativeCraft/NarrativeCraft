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

package fr.loudo.narrativecraft.recording.actions;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import fr.loudo.narrativecraft.api.playback.IPlaybackContext;
import fr.loudo.narrativecraft.api.playback.IPlaybackSession;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.api.recording.action.ActionResult;
import fr.loudo.narrativecraft.utils.FakePlayer;
import java.io.IOException;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class GameModeAction extends AbstractAction {

    public static final String ID = "gamemode";

    private static final BiMap<GameType, Integer> GAMEMODE_IDS = ImmutableBiMap.<GameType, Integer>builder()
            .put(GameType.ADVENTURE, 1)
            .put(GameType.CREATIVE, 2)
            .put(GameType.SPECTATOR, 3)
            .put(GameType.SURVIVAL, 4)
            .build();

    private GameType gameMode;

    public GameModeAction(int tick, GameType gameMode) {
        super(tick);
        this.gameMode = gameMode;
    }

    public GameModeAction(int tick, ServerPlayer player) {
        super(tick);
        this.gameMode = player.gameMode.getGameModeForPlayer();
    }

    public GameModeAction(int tick) {
        super(tick);
    }

    @Override
    public List<AbstractAction> createRewindSnapshot(IPlaybackContext context, IPlaybackSession session) {
        if (!(context.getEntity() instanceof FakePlayer player)) return List.of();
        GameType lastGameMode = player.gameMode.getGameModeForPlayer();
        return List.of(new GameModeAction(tick, lastGameMode));
    }

    @Override
    public boolean differs(AbstractAction other) {
        if (!(other instanceof GameModeAction that)) return false;
        return this.gameMode != that.gameMode;
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.addInt(GAMEMODE_IDS.getOrDefault(gameMode, -1));
    }

    @Override
    public void read(Reader reader) throws IOException {
        gameMode = GAMEMODE_IDS.inverse().get(reader.readInt());
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ActionResult execute(IPlaybackContext context, IPlaybackSession session) {
        if (gameMode == null) return ActionResult.IGNORED;
        if (!(context.getEntity() instanceof FakePlayer player)) return ActionResult.IGNORED;

        player.setGameMode(gameMode);
        return ActionResult.OK;
    }
}
