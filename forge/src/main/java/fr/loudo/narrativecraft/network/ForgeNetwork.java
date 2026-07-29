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

package fr.loudo.narrativecraft.network;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.network.cameraangle.*;
import fr.loudo.narrativecraft.network.cutscene.*;
import fr.loudo.narrativecraft.network.dialog.*;
import fr.loudo.narrativecraft.network.handlers.ClientPacketHandler;
import fr.loudo.narrativecraft.network.handlers.ServerPacketHandler;
import fr.loudo.narrativecraft.network.inkAction.*;
import fr.loudo.narrativecraft.network.interaction.*;
import fr.loudo.narrativecraft.network.mainScreen.*;
import fr.loudo.narrativecraft.network.story.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ForgeNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(NarrativeCraftMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static final Map<ResourceLocation, Function<FriendlyByteBuf, ? extends NarrativePacket>> READERS =
            new HashMap<>();
    private static final Map<ResourceLocation, Consumer<NarrativePacket>> CLIENT_HANDLERS = new HashMap<>();
    private static final Map<ResourceLocation, BiConsumer<NarrativePacket, ServerPlayer>> SERVER_HANDLERS =
            new HashMap<>();

    private ForgeNetwork() {}

    public static void init() {
        INSTANCE.registerMessage(0, Msg.class, ForgeNetwork::encode, ForgeNetwork::decode, ForgeNetwork::handle);
        registerServer();
    }

    public static void initClient() {
        registerClient();
    }

    public record Msg(NarrativePacket packet) {}

    private static void encode(Msg msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.packet.type());
        msg.packet.write(buf);
    }

    private static Msg decode(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        Function<FriendlyByteBuf, ? extends NarrativePacket> reader = READERS.get(id);
        return new Msg(reader.apply(buf));
    }

    private static void handle(Msg msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                Consumer<NarrativePacket> handler = CLIENT_HANDLERS.get(msg.packet.type());
                if (handler != null) handler.accept(msg.packet);
            } else {
                BiConsumer<NarrativePacket, ServerPlayer> handler = SERVER_HANDLERS.get(msg.packet.type());
                if (handler != null) handler.accept(msg.packet, context.getSender());
            }
        });
        context.setPacketHandled(true);
    }

    @SuppressWarnings("unchecked")
    private static <T extends NarrativePacket> void client(
            ResourceLocation type, Function<FriendlyByteBuf, T> reader, Consumer<T> handler) {
        READERS.put(type, reader);
        CLIENT_HANDLERS.put(type, packet -> handler.accept((T) packet));
    }

    private static <T extends NarrativePacket> void clientRun(
            ResourceLocation type, Function<FriendlyByteBuf, T> reader, Runnable handler) {
        READERS.put(type, reader);
        CLIENT_HANDLERS.put(type, packet -> handler.run());
    }

    @SuppressWarnings("unchecked")
    private static <T extends NarrativePacket> void server(
            ResourceLocation type, Function<FriendlyByteBuf, T> reader, BiConsumer<T, ServerPlayer> handler) {
        READERS.put(type, reader);
        SERVER_HANDLERS.put(type, (packet, player) -> handler.accept((T) packet, player));
    }

    private static void registerClient() {
        client(BiSyncNarrativeEntryPacket.TYPE, BiSyncNarrativeEntryPacket::read, ClientPacketHandler::narrativeEntry);
        client(BiCutsceneEnter.TYPE, BiCutsceneEnter::read, ClientPacketHandler::cutsceneState);
        clientRun(S2CNarrativeDataClear.TYPE, S2CNarrativeDataClear::read, ClientPacketHandler::clearNarrativeData);
        clientRun(S2CScreenClear.TYPE, S2CScreenClear::read, ClientPacketHandler::clearScreen);
        client(S2CPlayerSession.TYPE, S2CPlayerSession::read, ClientPacketHandler::setSession);
        client(S2CToastMessage.TYPE, S2CToastMessage::read, ClientPacketHandler::showToast);
        client(S2CCutsceneEditorData.TYPE, S2CCutsceneEditorData::read, ClientPacketHandler::loadCutsceneEditorData);
        client(
                BiCutscenePlayHeadPacket.TYPE,
                BiCutscenePlayHeadPacket::read,
                ClientPacketHandler::updatePlayHeadCutscene);
        client(S2CDialogTest.TYPE, S2CDialogTest::read, ClientPacketHandler::handleDialogTest);
        client(
                S2CCameraAngleEditorData.TYPE,
                S2CCameraAngleEditorData::read,
                ClientPacketHandler::loadCameraAngleEditorData);
        client(
                S2CCameraAngleCharacterCaptured.TYPE,
                S2CCameraAngleCharacterCaptured::read,
                ClientPacketHandler::addCameraAngleCharacter);
        client(
                S2CCameraAnglePlacementEntitySpawned.TYPE,
                S2CCameraAnglePlacementEntitySpawned::read,
                ClientPacketHandler::onPlacementEntitySpawned);
        client(
                S2CDialogEditorEntitySpawned.TYPE,
                S2CDialogEditorEntitySpawned::read,
                ClientPacketHandler::onDialogEditorEntitySpawned);
        client(S2CEnterCameraView.TYPE, S2CEnterCameraView::read, ClientPacketHandler::enterCameraView);
        client(
                S2CInteractionEditorData.TYPE,
                S2CInteractionEditorData::read,
                ClientPacketHandler::loadInteractionEditorData);
        client(S2CRunInkAction.TYPE, S2CRunInkAction::read, ClientPacketHandler::runInkAction);
        clientRun(S2CStopAllInkActions.TYPE, S2CStopAllInkActions::read, ClientPacketHandler::stopAllInkActions);
        client(S2CShowDialogue.TYPE, S2CShowDialogue::read, ClientPacketHandler::showDialogue);
        client(S2CShowChoices.TYPE, S2CShowChoices::read, ClientPacketHandler::showChoices);
        clientRun(S2CStopStory.TYPE, S2CStopStory::read, ClientPacketHandler::stopStory);
        clientRun(S2CDialogStop.TYPE, S2CDialogStop::read, ClientPacketHandler::dialogStop);
        client(BiCameraAngleEnter.TYPE, BiCameraAngleEnter::read, ClientPacketHandler::cameraAngleEnter);
        client(BiStopEditorMaker.TYPE, BiStopEditorMaker::read, ClientPacketHandler::stopEditorMaker);
        client(BiInteractionEnter.TYPE, BiInteractionEnter::read, ClientPacketHandler::interactionEnter);
        client(S2CCharacterSkin.TYPE, S2CCharacterSkin::read, ClientPacketHandler::characterSkin);
        client(S2CCharacterStoryAction.TYPE, S2CCharacterStoryAction::read, ClientPacketHandler::characterStoryAction);
        client(S2CClearLoadedSkins.TYPE, S2CClearLoadedSkins::read, ClientPacketHandler::clearLoadedSkins);
        client(S2CRenderSaveIcon.TYPE, S2CRenderSaveIcon::read, ClientPacketHandler::renderSaveIcon);
        client(BiMainScreenEnter.TYPE, BiMainScreenEnter::read, ClientPacketHandler::enterMainScreen);
        client(S2CMainScreenData.TYPE, S2CMainScreenData::read, ClientPacketHandler::receiveMainScreenData);
        client(S2COpenMainScreen.TYPE, S2COpenMainScreen::read, ClientPacketHandler::openMainScreen);
        clientRun(
                S2CNotifyClientPlayStory.TYPE,
                S2CNotifyClientPlayStory::read,
                ClientPacketHandler::notifyClientPlayStory);
        clientRun(S2CSessionClear.TYPE, S2CSessionClear::read, ClientPacketHandler::sessionClear);
        client(S2CStoryLocales.TYPE, S2CStoryLocales::read, ClientPacketHandler::storyLocales);
        client(S2CEnsureLocalExists.TYPE, S2CEnsureLocalExists::read, ClientPacketHandler::ensureLocalExists);
        client(S2CSetStoryLocale.TYPE, S2CSetStoryLocale::read, ClientPacketHandler::applyStoryLocale);
        client(S2CStoryTranslations.TYPE, S2CStoryTranslations::read, ClientPacketHandler::storyTranslations);
        client(S2CStoryVariables.TYPE, S2CStoryVariables::read, ClientPacketHandler::storyVariables);
    }

    private static void registerServer() {
        server(BiSyncNarrativeEntryPacket.TYPE, BiSyncNarrativeEntryPacket::read, ServerPacketHandler::narrativeEntry);
        server(BiCutsceneEnter.TYPE, BiCutsceneEnter::read, ServerPacketHandler::cutsceneState);
        server(C2SCutsceneControl.TYPE, C2SCutsceneControl::read, ServerPacketHandler::cutsceneControl);
        server(C2SCutsceneSave.TYPE, C2SCutsceneSave::read, ServerPacketHandler::cutsceneSave);
        server(BiCutscenePlayHeadPacket.TYPE, BiCutscenePlayHeadPacket::read, ServerPacketHandler::playHeadUpdate);
        server(BiCameraAngleEnter.TYPE, BiCameraAngleEnter::read, ServerPacketHandler::cameraAngleEnter);
        server(BiStopEditorMaker.TYPE, BiStopEditorMaker::read, ServerPacketHandler::stopEditorMaker);
        server(C2SCameraAngleSave.TYPE, C2SCameraAngleSave::read, ServerPacketHandler::cameraAngleSave);
        server(
                C2SCameraAngleCaptureCharacter.TYPE,
                C2SCameraAngleCaptureCharacter::read,
                ServerPacketHandler::cameraAngleCaptureCharacter);
        server(
                C2SCameraAngleRemovePlacement.TYPE,
                C2SCameraAngleRemovePlacement::read,
                ServerPacketHandler::cameraAngleRemovePlacement);
        server(
                C2SCameraAngleTeleportToTemplate.TYPE,
                C2SCameraAngleTeleportToTemplate::read,
                ServerPacketHandler::cameraAngleTeleportToTemplate);
        server(
                C2SCameraAngleSetEntityPose.TYPE,
                C2SCameraAngleSetEntityPose::read,
                ServerPacketHandler::cameraAngleSetEntityPose);
        server(
                C2SCameraAngleAddTemplateReference.TYPE,
                C2SCameraAngleAddTemplateReference::read,
                ServerPacketHandler::cameraAngleAddTemplateReference);
        server(
                C2SCameraAngleRemoveTemplateReference.TYPE,
                C2SCameraAngleRemoveTemplateReference::read,
                ServerPacketHandler::cameraAngleRemoveTemplateReference);
        server(BiInteractionEnter.TYPE, BiInteractionEnter::read, ServerPacketHandler::interactionEnter);
        server(C2SInteractionSave.TYPE, C2SInteractionSave::read, ServerPacketHandler::interactionSave);
        server(C2SInkActionFinished.TYPE, C2SInkActionFinished::read, ServerPacketHandler::inkActionFinished);
        server(C2SDialogueFinished.TYPE, C2SDialogueFinished::read, ServerPacketHandler::dialogueFinished);
        server(C2SChoiceSelected.TYPE, C2SChoiceSelected::read, ServerPacketHandler::choiceSelected);
        server(C2SPlayStitchStory.TYPE, C2SPlayStitchStory::read, ServerPacketHandler::playStitch);
        server(BiMainScreenEnter.TYPE, BiMainScreenEnter::read, ServerPacketHandler::enterMainScreen);
        server(
                C2SMainScreenCaptureCharacter.TYPE,
                C2SMainScreenCaptureCharacter::read,
                ServerPacketHandler::mainScreenCaptureCharacter);
        server(C2SMainScreenSave.TYPE, C2SMainScreenSave::read, ServerPacketHandler::mainScreenSave);
        server(
                C2SMainScreenRemovePlacement.TYPE,
                C2SMainScreenRemovePlacement::read,
                ServerPacketHandler::mainScreenRemovePlacement);
        server(C2SPlayStory.TYPE, C2SPlayStory::read, ServerPacketHandler::playStory);
        server(C2SEnterDialogEditor.TYPE, C2SEnterDialogEditor::read, ServerPacketHandler::enterDialogEditor);
        server(C2SStopStory.TYPE, C2SStopStory::read, ServerPacketHandler::stopStory);
        server(C2SChangeGamemodePacket.TYPE, C2SChangeGamemodePacket::read, ServerPacketHandler::changeGamemode);
        server(C2SSetStoryLocale.TYPE, C2SSetStoryLocale::read, ServerPacketHandler::setStoryLocale);
    }
}
