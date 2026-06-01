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

package fr.loudo.narrativecraft.session;

import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.network.BiStopEditorMaker;
import fr.loudo.narrativecraft.platform.Services;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class PlayerSession extends AbstractPlayerSession {

    private final ServerPlayer player;
    private GameType lastGameType;
    private boolean gameplayMode;
    private final List<UUID> characterIdsSkinLoaded = new ArrayList<>();

    @Nullable
    private StoryHandler storyHandler;

    public PlayerSession(ServerPlayer player, Chapter chapter, Scene scene) {
        super(chapter, scene);
        this.player = player;
    }

    @Override
    public void clear() {
        super.clear();
        characterIdsSkinLoaded.clear();
        Services.PACKET.sendToPlayer(player, BiStopEditorMaker.INSTANCE);
    }

    public PlayerSession(ServerPlayer player) {
        super(null, null);
        this.player = player;
    }

    public void changeGameMode(GameType gameType) {
        lastGameType = player.gameMode();
        player.setGameMode(gameType);
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public GameType getLastGameType() {
        return lastGameType;
    }

    public void setLastGameType(GameType lastGameType) {
        this.lastGameType = lastGameType;
    }

    @Nullable
    public StoryHandler getStoryHandler() {
        return storyHandler;
    }

    public void setStoryHandler(@Nullable StoryHandler storyHandler) {
        this.storyHandler = storyHandler;
    }

    public boolean isGameplayMode() {
        return gameplayMode;
    }

    public void setGameplayMode(boolean gameplayMode) {
        this.gameplayMode = gameplayMode;
    }

    public List<UUID> getCharacterIdsSkinLoaded() {
        return characterIdsSkinLoaded;
    }
}
