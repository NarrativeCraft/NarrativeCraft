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

package fr.loudo.narrativecraft.narrative;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.commands.LocaleCommand;
import fr.loudo.narrativecraft.files.DeserializationResult;
import fr.loudo.narrativecraft.files.InkFileGenerator;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.files.NarrativeCraftFileRegistry;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngle;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.interaction.Interaction;
import fr.loudo.narrativecraft.narrative.npc.Npc;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryCompilerHandler;
import fr.loudo.narrativecraft.narrative.story.locale.StoryTranslations;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.network.S2CNarrativeSnapshot;
import fr.loudo.narrativecraft.network.mainScreen.S2CMainScreenData;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.server.settings.NarrativeServerSettings;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

public class NarrativeEntryInit {

    public static void init() {
        // Not supposed to be populated on servers, but on singleplayer world it stills on memory so data aren't updated
        NarrativeCraftMod.getInstance().getChapterManager().clear();
        NarrativeCraftMod.getInstance().getCharacterManager().clear();
        NarrativeCraftMod.getInstance().getCorruptedDeserialization().clear();

        NarrativeCraftFile file = NarrativeCraftMod.getInstance().getFile();

        loadEntries();

        NarrativeServerSettings.init(file.getInit().getDataDirectory().toPath());
        StoryTranslations.reload();

        NarrativeCraftMod.getInstance().setMainScreenData(file.getMainScreenData());
        NarrativeCraftMod.getInstance().setGlobalDialogData(file.getGlobalDialogData());

        compileStories();
        initPrecompiledStory();
    }

    private static void compileStories() {
        InkFileGenerator.regenerateMainInk();
        try {
            NarrativeCraftMod.getInstance().setCompiledStoryJson(StoryCompilerHandler.compileToJson());
        } catch (Exception exception) {
            NarrativeCraftMod.LOGGER.error("Failed to compile story (init process)", exception);
            return;
        }

        for (StoryCompilerHandler.TagError tagError : StoryCompilerHandler.validateTags()) {
            NarrativeCraftMod.LOGGER.error(
                    "Invalid ink tag: {}", tagError.toMessage().getString());
        }
    }

    private static void initPrecompiledStory() {
        try {
            String preCompiledStory = NarrativeCraftMod.getInstance().getFile().getCompiledStoryContent();
            if (preCompiledStory != null) {
                NarrativeCraftMod.getInstance().setCompiledStoryJson(preCompiledStory);
                NarrativeCraftMod.LOGGER.info("Pre-compiled story loaded.");
            } else {
                NarrativeCraftMod.LOGGER.info("No pre-compiled story not available, ignoring.");
            }
        } catch (Exception exception) {
            NarrativeCraftMod.LOGGER.error(
                    "Failed to fetch pre-compiled story, trying to compile current story instead", exception);
            compileStories();
        }
    }

    private static void loadEntries() {
        NarrativeCraftFileRegistry registry = NarrativeCraftFileRegistry.getInstance();
        CharacterManager characterManager = NarrativeCraftMod.getInstance().getCharacterManager();
        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();

        accept(registry.load(CharacterStory.class)).forEach(characterManager::add);
        for (Chapter chapter : accept(registry.load(Chapter.class))) {
            chapterManager.add(chapter);
            for (Scene scene : accept(registry.load(Scene.class, chapter))) {
                chapter.getSceneManager().add(scene);
                loadSceneEntries(scene);
            }
        }
    }

    private static void loadSceneEntries(Scene scene) {
        loadInto(Npc.class, scene, scene.getNpcManager());
        loadInto(Animation.class, scene, scene.getAnimationManager());
        loadInto(Subscene.class, scene, scene.getSubsceneManager());
        loadInto(Cutscene.class, scene, scene.getCutsceneManager());
        loadInto(CameraAngle.class, scene, scene.getCameraAngleManager());
        loadInto(Interaction.class, scene, scene.getInteractionManager());
    }

    private static <T extends NarrativeEntry<?>> void loadInto(
            Class<T> entryClass, Scene scene, NarrativeManager<T> manager) {
        accept(NarrativeCraftFileRegistry.getInstance().load(entryClass, scene)).forEach(manager::add);
    }

    private static <T extends NarrativeEntry<?>> List<T> accept(List<DeserializationResult<T>> results) {
        List<T> entries = new ArrayList<>();
        for (DeserializationResult<T> result : results) {
            if (result.corrupted()) {
                NarrativeCraftMod.getInstance().getCorruptedDeserialization().add(result);
                continue;
            }
            entries.add(result.entry());
        }
        return entries;
    }

    public static void sendDataToPlayer(ServerPlayer player) {
        LocaleCommand.sendLocales(player);
        CameraAngle mainScreenData = NarrativeCraftMod.getInstance().getMainScreenData();
        if (mainScreenData != null) {
            Services.PACKET.sendToPlayer(player, new S2CMainScreenData(mainScreenData.getDetail()));
        }
        Services.PACKET.sendToPlayer(player, snapshot());
    }

    private static S2CNarrativeSnapshot snapshot() {
        List<S2CNarrativeSnapshot.Entry> entries = new ArrayList<>();
        for (CharacterStory character :
                NarrativeCraftMod.getInstance().getCharacterManager().getList()) {
            entries.add(S2CNarrativeSnapshot.Entry.of(character));
        }
        for (Chapter chapter :
                NarrativeCraftMod.getInstance().getChapterManager().getList()) {
            entries.add(S2CNarrativeSnapshot.Entry.of(chapter));
            for (Scene scene : chapter.getSceneManager().getList()) {
                entries.add(S2CNarrativeSnapshot.Entry.of(scene));
                scene.getNpcManager().getList().forEach(npc -> entries.add(S2CNarrativeSnapshot.Entry.of(npc)));
                scene.getAnimationManager()
                        .getList()
                        .forEach(animation -> entries.add(S2CNarrativeSnapshot.Entry.of(animation)));
                scene.getSubsceneManager()
                        .getList()
                        .forEach(subscene -> entries.add(S2CNarrativeSnapshot.Entry.of(subscene)));
                scene.getCutsceneManager()
                        .getList()
                        .forEach(cutscene -> entries.add(S2CNarrativeSnapshot.Entry.of(cutscene)));
                scene.getCameraAngleManager()
                        .getList()
                        .forEach(cameraAngle -> entries.add(S2CNarrativeSnapshot.Entry.of(cameraAngle)));
                scene.getInteractionManager()
                        .getList()
                        .forEach(interaction -> entries.add(S2CNarrativeSnapshot.Entry.of(interaction)));
            }
        }
        return new S2CNarrativeSnapshot(entries);
    }
}
