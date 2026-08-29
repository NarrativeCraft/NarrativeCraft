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

package fr.loudo.narrativecraft.client;

import fr.loudo.narrativecraft.api.client.ClientAPISetup;
import fr.loudo.narrativecraft.api.utils.Side;
import fr.loudo.narrativecraft.client.editors.cameraangle.ClientCameraAngleMakerEditorMaker;
import fr.loudo.narrativecraft.client.editors.cutscene.ClientCutsceneMakerEditorMaker;
import fr.loudo.narrativecraft.client.editors.interaction.ClientInteractionMakerEditorMaker;
import fr.loudo.narrativecraft.client.inkTag.ClientInkActionRegister;
import fr.loudo.narrativecraft.client.narrative.ClientNarrativeEditorsRegister;
import fr.loudo.narrativecraft.client.narrative.ui.ClientNarrativeUIActionRegister;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.client.signals.ClientPlayerStateSignalWatcher;
import fr.loudo.narrativecraft.client.signals.ClientSignalEmitterImpl;
import fr.loudo.narrativecraft.client.signals.ClientSignalRegistryRegister;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngle;
import fr.loudo.narrativecraft.narrative.inkTag.InkTagDispatcherImpl;
import fr.loudo.narrativecraft.signals.SignalRegistryImpl;
import java.util.UUID;

public class ClientNarrativeCraftMod {
    private static final ClientNarrativeCraftMod instance = new ClientNarrativeCraftMod();

    private final ChapterManager chapterManager = new ChapterManager();
    private final CharacterManager characterManager = new CharacterManager();
    private final ClientPlayerSession playerSession = new ClientPlayerSession();
    private final InkTagDispatcherImpl inkTagDispatcher = new InkTagDispatcherImpl();
    private final SignalRegistryImpl signalRegistry = new SignalRegistryImpl(Side.CLIENT);
    private final ClientSignalEmitterImpl signalEmitter = new ClientSignalEmitterImpl();
    private final ClientPlayerStateSignalWatcher playerStateSignalWatcher = new ClientPlayerStateSignalWatcher();
    private CameraAngle mainScreenData;

    public static void commonInit() {
        ClientAPISetup.init(getInstance());
        ClientNarrativeEditorsRegister.register();
        ClientNarrativeUIActionRegister.register();
        ClientInkActionRegister.register();
        ClientSignalRegistryRegister.register();
    }

    public ChapterManager getChapterManager() {
        return chapterManager;
    }

    public CharacterManager getCharacterManager() {
        return characterManager;
    }

    public ClientPlayerSession getPlayerSession() {
        return playerSession;
    }

    public CameraAngle getMainScreenData() {
        return mainScreenData;
    }

    public void setMainScreenData(CameraAngle mainScreenData) {
        this.mainScreenData = mainScreenData;
    }

    public ClientCutsceneMakerEditorMaker getCutsceneMakerEditor() {
        EditorMaker editorMaker = getPlayerSession().getEditor();
        if (!(editorMaker instanceof ClientCutsceneMakerEditorMaker)) return null;
        return (ClientCutsceneMakerEditorMaker) editorMaker;
    }

    public ClientCameraAngleMakerEditorMaker getCameraAngleMakerEditor() {
        EditorMaker editorMaker = getPlayerSession().getEditor();
        if (!(editorMaker instanceof ClientCameraAngleMakerEditorMaker)) return null;
        return (ClientCameraAngleMakerEditorMaker) editorMaker;
    }

    public ClientInteractionMakerEditorMaker getInteractionMakerEditor() {
        EditorMaker editorMaker = getPlayerSession().getEditor();
        if (!(editorMaker instanceof ClientInteractionMakerEditorMaker)) return null;
        return (ClientInteractionMakerEditorMaker) editorMaker;
    }

    public ClientInteractionMakerEditorMaker getInteractionMakerEditor(UUID interactionId) {
        ClientInteractionMakerEditorMaker editorMaker = getInteractionMakerEditor();
        if (editorMaker != null && editorMaker.getInteraction().getId().equals(interactionId)) {
            return editorMaker;
        }
        EditorMaker interactionSession = getPlayerSession().getInteractionSession(interactionId);
        if (!(interactionSession instanceof ClientInteractionMakerEditorMaker)) return null;
        return (ClientInteractionMakerEditorMaker) interactionSession;
    }

    public InkTagDispatcherImpl getInkTagDispatcher() {
        return inkTagDispatcher;
    }

    public SignalRegistryImpl getSignalRegistry() {
        return signalRegistry;
    }

    public ClientSignalEmitterImpl getSignalEmitter() {
        return signalEmitter;
    }

    public ClientPlayerStateSignalWatcher getPlayerStateSignalWatcher() {
        return playerStateSignalWatcher;
    }

    public static ClientNarrativeCraftMod getInstance() {
        return instance;
    }
}
