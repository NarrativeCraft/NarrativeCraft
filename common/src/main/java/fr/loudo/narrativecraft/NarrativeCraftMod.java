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

package fr.loudo.narrativecraft;

import fr.loudo.narrativecraft.api.APISetup;
import fr.loudo.narrativecraft.files.DeserializationResult;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditorsRegister;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.managers.PlayerSessionManager;
import fr.loudo.narrativecraft.managers.RecordingManager;
import fr.loudo.narrativecraft.narrative.NarrativeEditorsRegister;
import fr.loudo.narrativecraft.narrative.events.EventBus;
import fr.loudo.narrativecraft.playback.PlaybackManager;
import fr.loudo.narrativecraft.recording.actions.ActionRegister;
import fr.loudo.narrativecraft.recording.actions.ActionRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NarrativeCraftMod {
    public static final String MOD_ID = "narrativecraft";
    public static final String MOD_NAME = "NarrativeCraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final EventBus EVENT_BUS = new EventBus();
    private static final NarrativeCraftMod instance = new NarrativeCraftMod();

    private final ChapterManager chapterManager = new ChapterManager();
    private final CharacterManager characterManager = new CharacterManager();
    private final RecordingManager recordingManager = new RecordingManager();
    private final PlaybackManager playbackManager = new PlaybackManager();
    private final PlayerSessionManager playerSessionManager = new PlayerSessionManager();
    private final ActionRegistry actionRegistry = new ActionRegistry();
    private final List<DeserializationResult<?>> corruptedDeserialization = new ArrayList<>();

    private final NarrativeCraftFile file = new NarrativeCraftFile();

    private MinecraftServer server;

    public static void commonInit() {
        NarrativeCraftFileEditorsRegister.register();
        NarrativeEditorsRegister.register();
        APISetup.init(getInstance());

        ActionRegister.register(getInstance().actionRegistry);
    }

    public ChapterManager getChapterManager() {
        return chapterManager;
    }

    public CharacterManager getCharacterManager() {
        return characterManager;
    }

    public PlaybackManager getPlaybackManager() {
        return playbackManager;
    }

    public RecordingManager getRecordingManager() {
        return recordingManager;
    }

    public PlayerSessionManager getPlayerSessionManager() {
        return playerSessionManager;
    }

    public ActionRegistry getActionRegistry() {
        return actionRegistry;
    }

    public NarrativeCraftFile getFile() {
        return file;
    }

    public MinecraftServer getServer() {
        return server;
    }

    public List<DeserializationResult<?>> getCorruptedDeserialization() {
        return corruptedDeserialization;
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    public static NarrativeCraftMod getInstance() {
        return instance;
    }
}
