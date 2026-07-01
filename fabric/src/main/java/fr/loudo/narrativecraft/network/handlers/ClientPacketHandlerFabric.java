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

package fr.loudo.narrativecraft.network.handlers;

import fr.loudo.narrativecraft.network.*;
import fr.loudo.narrativecraft.network.cameraangle.*;
import fr.loudo.narrativecraft.network.cutscene.BiCutsceneEnter;
import fr.loudo.narrativecraft.network.cutscene.BiCutscenePlayHeadPacket;
import fr.loudo.narrativecraft.network.cutscene.S2CCutsceneEditorData;
import fr.loudo.narrativecraft.network.dialog.S2CDialogEditorEntitySpawned;
import fr.loudo.narrativecraft.network.dialog.S2CDialogTest;
import fr.loudo.narrativecraft.network.inkAction.S2CRunInkAction;
import fr.loudo.narrativecraft.network.inkAction.S2CStopAllInkActions;
import fr.loudo.narrativecraft.network.interaction.BiInteractionEnter;
import fr.loudo.narrativecraft.network.interaction.S2CInteractionEditorData;
import fr.loudo.narrativecraft.network.mainScreen.BiMainScreenEnter;
import fr.loudo.narrativecraft.network.mainScreen.S2CMainScreenData;
import fr.loudo.narrativecraft.network.mainScreen.S2COpenMainScreen;
import fr.loudo.narrativecraft.network.story.S2CCharacterStoryAction;
import fr.loudo.narrativecraft.network.story.S2CDialogStop;
import fr.loudo.narrativecraft.network.story.S2CNotifyClientPlayStory;
import fr.loudo.narrativecraft.network.story.S2CShowChoices;
import fr.loudo.narrativecraft.network.story.S2CShowDialogue;
import fr.loudo.narrativecraft.network.story.S2CStopStory;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientPacketHandlerFabric {

    public static void handle() {
        ClientPlayNetworking.registerGlobalReceiver(
                BiSyncNarrativeEntryPacket.TYPE, (client, handler, buf, responseSender) -> {
                    BiSyncNarrativeEntryPacket packet = BiSyncNarrativeEntryPacket.read(buf);
                    client.execute(() -> {
                        ClientPacketHandler.narrativeEntry(packet);
                    });
                });
        ClientPlayNetworking.registerGlobalReceiver(BiCutsceneEnter.TYPE, (client, handler, buf, responseSender) -> {
            BiCutsceneEnter packet = BiCutsceneEnter.read(buf);
            client.execute(() -> {
                ClientPacketHandler.cutsceneState(packet);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(
                S2CNarrativeDataClear.TYPE, (client, handler, buf, responseSender) -> {
                    S2CNarrativeDataClear packet = S2CNarrativeDataClear.read(buf);
                    client.execute(() -> {
                        ClientPacketHandler.clearNarrativeData();
                    });
                });
        ClientPlayNetworking.registerGlobalReceiver(S2CScreenClear.TYPE, (client, handler, buf, responseSender) -> {
            S2CScreenClear packet = S2CScreenClear.read(buf);
            client.execute(() -> {
                ClientPacketHandler.clearScreen();
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CPlayerSession.TYPE, (client, handler, buf, responseSender) -> {
            S2CPlayerSession packet = S2CPlayerSession.read(buf);
            client.execute(() -> {
                ClientPacketHandler.setSession(packet);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CToastMessage.TYPE, (client, handler, buf, responseSender) -> {
            S2CToastMessage packet = S2CToastMessage.read(buf);
            client.execute(() -> {
                ClientPacketHandler.showToast(packet);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(
                S2CCutsceneEditorData.TYPE, (client, handler, buf, responseSender) -> {
                    S2CCutsceneEditorData packet = S2CCutsceneEditorData.read(buf);
                    client.execute(() -> {
                        ClientPacketHandler.loadCutsceneEditorData(packet);
                    });
                });
        ClientPlayNetworking.registerGlobalReceiver(
                BiCutscenePlayHeadPacket.TYPE, (client, handler, buf, responseSender) -> {
                    BiCutscenePlayHeadPacket packet = BiCutscenePlayHeadPacket.read(buf);
                    client.execute(() -> {
                        ClientPacketHandler.updatePlayHeadCutscene(packet);
                    });
                });
        ClientPlayNetworking.registerGlobalReceiver(S2CDialogTest.TYPE, (client, handler, buf, responseSender) -> {
            S2CDialogTest packet = S2CDialogTest.read(buf);
            client.execute(() -> {
                ClientPacketHandler.handleDialogTest(packet);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(
                S2CCameraAngleEditorData.TYPE, (client, handler, buf, responseSender) -> {
                    S2CCameraAngleEditorData packet = S2CCameraAngleEditorData.read(buf);
                    client.execute(() -> {
                        ClientPacketHandler.loadCameraAngleEditorData(packet);
                    });
                });
        ClientPlayNetworking.registerGlobalReceiver(
                S2CCameraAngleCharacterCaptured.TYPE, (client, handler, buf, responseSender) -> {
                    S2CCameraAngleCharacterCaptured packet = S2CCameraAngleCharacterCaptured.read(buf);
                    client.execute(() -> {
                        ClientPacketHandler.addCameraAngleCharacter(packet);
                    });
                });
        ClientPlayNetworking.registerGlobalReceiver(
                S2CCameraAnglePlacementEntitySpawned.TYPE, (client, handler, buf, responseSender) -> {
                    S2CCameraAnglePlacementEntitySpawned packet = S2CCameraAnglePlacementEntitySpawned.read(buf);
                    client.execute(() -> {
                        ClientPacketHandler.onPlacementEntitySpawned(packet);
                    });
                });
        ClientPlayNetworking.registerGlobalReceiver(
                S2CDialogEditorEntitySpawned.TYPE, (client, handler, buf, responseSender) -> {
                    S2CDialogEditorEntitySpawned packet = S2CDialogEditorEntitySpawned.read(buf);
                    client.execute(() -> {
                        ClientPacketHandler.onDialogEditorEntitySpawned(packet);
                    });
                });
        ClientPlayNetworking.registerGlobalReceiver(S2CEnterCameraView.TYPE, (client, handler, buf, responseSender) -> {
            S2CEnterCameraView packet = S2CEnterCameraView.read(buf);
            client.execute(() -> {
                ClientPacketHandler.enterCameraView(packet);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(
                S2CInteractionEditorData.TYPE, (client, handler, buf, responseSender) -> {
                    S2CInteractionEditorData packet = S2CInteractionEditorData.read(buf);
                    client.execute(() -> {
                        ClientPacketHandler.loadInteractionEditorData(packet);
                    });
                });
        ClientPlayNetworking.registerGlobalReceiver(S2CRunInkAction.TYPE, (client, handler, buf, responseSender) -> {
            S2CRunInkAction packet = S2CRunInkAction.read(buf);
            client.execute(() -> {
                ClientPacketHandler.runInkAction(packet);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(
                S2CStopAllInkActions.TYPE, (client, handler, buf, responseSender) -> {
                    S2CStopAllInkActions packet = S2CStopAllInkActions.read(buf);
                    client.execute(() -> {
                        ClientPacketHandler.stopAllInkActions();
                    });
                });
        ClientPlayNetworking.registerGlobalReceiver(S2CShowDialogue.TYPE, (client, handler, buf, responseSender) -> {
            S2CShowDialogue packet = S2CShowDialogue.read(buf);
            client.execute(() -> {
                ClientPacketHandler.showDialogue(packet);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CShowChoices.TYPE, (client, handler, buf, responseSender) -> {
            S2CShowChoices packet = S2CShowChoices.read(buf);
            client.execute(() -> {
                ClientPacketHandler.showChoices(packet);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CStopStory.TYPE, (client, handler, buf, responseSender) -> {
            S2CStopStory packet = S2CStopStory.read(buf);
            client.execute(() -> {
                ClientPacketHandler.stopStory();
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CDialogStop.TYPE, (client, handler, buf, responseSender) -> {
            S2CDialogStop packet = S2CDialogStop.read(buf);
            client.execute(() -> {
                ClientPacketHandler.dialogStop();
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(BiCameraAngleEnter.TYPE, (client, handler, buf, responseSender) -> {
            BiCameraAngleEnter packet = BiCameraAngleEnter.read(buf);
            client.execute(() -> {
                ClientPacketHandler.cameraAngleEnter(packet);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(BiStopEditorMaker.TYPE, (client, handler, buf, responseSender) -> {
            BiStopEditorMaker packet = BiStopEditorMaker.read(buf);
            client.execute(() -> {
                ClientPacketHandler.stopEditorMaker(packet);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(BiInteractionEnter.TYPE, (client, handler, buf, responseSender) -> {
            BiInteractionEnter packet = BiInteractionEnter.read(buf);
            client.execute(() -> {
                ClientPacketHandler.interactionEnter(packet);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CCharacterSkin.TYPE, (client, handler, buf, responseSender) -> {
            S2CCharacterSkin packet = S2CCharacterSkin.read(buf);
            client.execute(() -> {
                ClientPacketHandler.characterSkin(packet);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(
                S2CCharacterStoryAction.TYPE, (client, handler, buf, responseSender) -> {
                    S2CCharacterStoryAction packet = S2CCharacterStoryAction.read(buf);
                    client.execute(() -> {
                        ClientPacketHandler.characterStoryAction(packet);
                    });
                });
        ClientPlayNetworking.registerGlobalReceiver(
                S2CClearLoadedSkins.TYPE, (client, handler, buf, responseSender) -> {
                    S2CClearLoadedSkins packet = S2CClearLoadedSkins.read(buf);
                    client.execute(() -> {
                        ClientPacketHandler.clearLoadedSkins(packet);
                    });
                });
        ClientPlayNetworking.registerGlobalReceiver(S2CRenderSaveIcon.TYPE, (client, handler, buf, responseSender) -> {
            S2CRenderSaveIcon packet = S2CRenderSaveIcon.read(buf);
            client.execute(() -> {
                ClientPacketHandler.renderSaveIcon(packet);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(BiMainScreenEnter.TYPE, (client, handler, buf, responseSender) -> {
            BiMainScreenEnter packet = BiMainScreenEnter.read(buf);
            client.execute(() -> {
                ClientPacketHandler.enterMainScreen(packet);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CMainScreenData.TYPE, (client, handler, buf, responseSender) -> {
            S2CMainScreenData packet = S2CMainScreenData.read(buf);
            client.execute(() -> {
                ClientPacketHandler.receiveMainScreenData(packet);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(S2COpenMainScreen.TYPE, (client, handler, buf, responseSender) -> {
            S2COpenMainScreen packet = S2COpenMainScreen.read(buf);
            client.execute(() -> {
                ClientPacketHandler.openMainScreen(packet);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(
                S2CNotifyClientPlayStory.TYPE, (client, handler, buf, responseSender) -> {
                    S2CNotifyClientPlayStory packet = S2CNotifyClientPlayStory.read(buf);
                    client.execute(() -> {
                        ClientPacketHandler.notifyClientPlayStory();
                    });
                });
        ClientPlayNetworking.registerGlobalReceiver(S2CSessionClear.TYPE, (client, handler, buf, responseSender) -> {
            S2CSessionClear packet = S2CSessionClear.read(buf);
            client.execute(() -> {
                ClientPacketHandler.sessionClear();
            });
        });
    }
}
