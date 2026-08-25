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
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngleSerializer;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.interaction.Interaction;
import fr.loudo.narrativecraft.narrative.npc.Npc;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryCompilerHandler;
import fr.loudo.narrativecraft.narrative.story.locale.StoryTranslations;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.S2CNarrativeDataClear;
import fr.loudo.narrativecraft.network.mainScreen.S2CMainScreenData;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.server.settings.NarrativeServerSettings;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

public class NarrativeEntryInit {

    public static void init() {
        // Not supposed to be populated on servers, but on singleplayer world it stills on memory so data aren't updated
        NarrativeCraftMod.getInstance().getChapterManager().clear();
        NarrativeCraftMod.getInstance().getCharacterManager().clear();
        NarrativeCraftMod.getInstance().getCorruptedDeserialization().clear();

        NarrativeCraftFile file = NarrativeCraftMod.getInstance().getFile();

        // Order is important!! e.g. Chapters must be initialized before scenes of chapter can be initialized.
        characters();
        chapters();
        scenes();
        npcs();
        animations();
        subscenes();
        cutscenes();
        cameraAngles();
        interactions();

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

    private static void chapters() {
        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();
        List<DeserializationResult<Chapter>> deserializationResults =
                NarrativeCraftFileRegistry.getInstance().deserialize(Chapter.class);

        for (DeserializationResult<Chapter> deserializationResult : deserializationResults) {
            if (deserializationResult.corrupted()) {
                NarrativeCraftMod.getInstance().getCorruptedDeserialization().add(deserializationResult);
                continue;
            }
            Chapter chapter = deserializationResult.entry();
            chapterManager.add(chapter);
        }
    }

    private static void scenes() {
        List<DeserializationResult<Scene>> deserializationResults =
                NarrativeCraftFileRegistry.getInstance().deserialize(Scene.class);
        for (DeserializationResult<Scene> deserializationResult : deserializationResults) {
            if (deserializationResult.corrupted()) {
                NarrativeCraftMod.getInstance().getCorruptedDeserialization().add(deserializationResult);
                continue;
            }
            Scene scene = deserializationResult.entry();
            scene.getChapter().getSceneManager().add(scene);
        }
    }

    private static void animations() {
        List<DeserializationResult<Animation>> deserializationResults =
                NarrativeCraftFileRegistry.getInstance().deserialize(Animation.class);
        for (DeserializationResult<Animation> deserializationResult : deserializationResults) {
            if (deserializationResult.corrupted()) {
                NarrativeCraftMod.getInstance().getCorruptedDeserialization().add(deserializationResult);
                continue;
            }
            Animation animation = deserializationResult.entry();
            animation.getScene().getAnimationManager().add(animation);
        }
    }

    private static void subscenes() {
        List<DeserializationResult<Subscene>> deserializationResults =
                NarrativeCraftFileRegistry.getInstance().deserialize(Subscene.class);
        for (DeserializationResult<Subscene> deserializationResult : deserializationResults) {
            if (deserializationResult.corrupted()) {
                NarrativeCraftMod.getInstance().getCorruptedDeserialization().add(deserializationResult);
                continue;
            }
            Subscene subscene = deserializationResult.entry();
            subscene.getScene().getSubsceneManager().add(subscene);
        }
    }

    private static void cutscenes() {
        List<DeserializationResult<Cutscene>> deserializationResults =
                NarrativeCraftFileRegistry.getInstance().deserialize(Cutscene.class);
        for (DeserializationResult<Cutscene> deserializationResult : deserializationResults) {
            if (deserializationResult.corrupted()) {
                NarrativeCraftMod.getInstance().getCorruptedDeserialization().add(deserializationResult);
                continue;
            }
            Cutscene cutscene = deserializationResult.entry();
            cutscene.getScene().getCutsceneManager().add(cutscene);
        }
    }

    private static void cameraAngles() {
        List<DeserializationResult<CameraAngle>> deserializationResults =
                NarrativeCraftFileRegistry.getInstance().deserialize(CameraAngle.class);
        for (DeserializationResult<CameraAngle> deserializationResult : deserializationResults) {
            if (deserializationResult.corrupted()) {
                NarrativeCraftMod.getInstance().getCorruptedDeserialization().add(deserializationResult);
                continue;
            }
            CameraAngle cameraAngle = deserializationResult.entry();
            cameraAngle.getScene().getCameraAngleManager().add(cameraAngle);
        }
    }

    private static void interactions() {
        List<DeserializationResult<Interaction>> deserializationResults =
                NarrativeCraftFileRegistry.getInstance().deserialize(Interaction.class);
        for (DeserializationResult<Interaction> deserializationResult : deserializationResults) {
            if (deserializationResult.corrupted()) {
                NarrativeCraftMod.getInstance().getCorruptedDeserialization().add(deserializationResult);
                continue;
            }
            Interaction interaction = deserializationResult.entry();
            interaction.getScene().getInteractionManager().add(interaction);
        }
    }

    private static void npcs() {
        List<DeserializationResult<Npc>> deserializationResults =
                NarrativeCraftFileRegistry.getInstance().deserialize(Npc.class);
        for (DeserializationResult<Npc> deserializationResult : deserializationResults) {
            if (deserializationResult.corrupted()) {
                NarrativeCraftMod.getInstance().getCorruptedDeserialization().add(deserializationResult);
                continue;
            }
            Npc npc = deserializationResult.entry();
            npc.getScene().getNpcManager().add(npc);
        }
    }

    private static void characters() {
        CharacterManager characterManager = NarrativeCraftMod.getInstance().getCharacterManager();
        List<DeserializationResult<CharacterStory>> deserializationResults =
                NarrativeCraftFileRegistry.getInstance().deserialize(CharacterStory.class);

        for (DeserializationResult<CharacterStory> deserializationResult : deserializationResults) {
            if (deserializationResult.corrupted()) {
                NarrativeCraftMod.getInstance().getCorruptedDeserialization().add(deserializationResult);
                continue;
            }
            characterManager.add(deserializationResult.entry());
        }
    }

    public static void sendDataToPlayer(ServerPlayer player) {
        Services.PACKET.sendToPlayer(player, S2CNarrativeDataClear.INSTANCE);
        LocaleCommand.sendLocales(player);
        CameraAngle mainScreenData = NarrativeCraftMod.getInstance().getMainScreenData();
        if (mainScreenData != null) {
            Services.PACKET.sendToPlayer(
                    player, new S2CMainScreenData(CameraAngleSerializer.serializeData(mainScreenData)));
        }
        for (CharacterStory character :
                NarrativeCraftMod.getInstance().getCharacterManager().getList()) {
            Services.PACKET.sendToPlayer(
                    player, BiSyncNarrativeEntryPacket.add(character.getId(), character.toPayload()));
        }
        for (Chapter chapter :
                NarrativeCraftMod.getInstance().getChapterManager().getList()) {
            Services.PACKET.sendToPlayer(player, BiSyncNarrativeEntryPacket.add(chapter.getId(), chapter.toPayload()));
            for (Scene scene : chapter.getSceneManager().getList()) {
                Services.PACKET.sendToPlayer(player, BiSyncNarrativeEntryPacket.add(scene.getId(), scene.toPayload()));
                for (Npc npc : scene.getNpcManager().getList()) {
                    Services.PACKET.sendToPlayer(player, BiSyncNarrativeEntryPacket.add(npc.getId(), npc.toPayload()));
                }
                for (Animation animation : scene.getAnimationManager().getList()) {
                    Services.PACKET.sendToPlayer(
                            player, BiSyncNarrativeEntryPacket.add(animation.getId(), animation.toPayload()));
                }
                for (Subscene subscene : scene.getSubsceneManager().getList()) {
                    Services.PACKET.sendToPlayer(
                            player, BiSyncNarrativeEntryPacket.add(subscene.getId(), subscene.toPayload()));
                }
                for (Cutscene cutscene : scene.getCutsceneManager().getList()) {
                    Services.PACKET.sendToPlayer(
                            player, BiSyncNarrativeEntryPacket.add(cutscene.getId(), cutscene.toPayload()));
                }
                for (CameraAngle cameraAngle : scene.getCameraAngleManager().getList()) {
                    Services.PACKET.sendToPlayer(
                            player, BiSyncNarrativeEntryPacket.add(cameraAngle.getId(), cameraAngle.toPayload()));
                }
                for (Interaction interaction : scene.getInteractionManager().getList()) {
                    Services.PACKET.sendToPlayer(
                            player, BiSyncNarrativeEntryPacket.add(interaction.getId(), interaction.toPayload()));
                }
            }
        }
    }
}
