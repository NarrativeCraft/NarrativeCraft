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

package fr.loudo.narrativecraft.client.session;

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.client.editors.cutscene.CutsceneDataSession;
import fr.loudo.narrativecraft.client.narrative.story.StorySaveIconRenderer;
import fr.loudo.narrativecraft.client.screens.story.ChoiceScreen;
import fr.loudo.narrativecraft.dialog.DialogRenderer;
import fr.loudo.narrativecraft.dialog.DialogRenderer2D;
import fr.loudo.narrativecraft.dialog.DialogRenderer3D;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraView;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.network.BiEditorClose;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.session.AbstractPlayerSession;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;

public class ClientPlayerSession extends AbstractPlayerSession {

    private final CutsceneDataSession cutsceneDataSession = new CutsceneDataSession();
    private final StorySaveIconRenderer saveIconRenderer = new StorySaveIconRenderer();
    private final Map<UUID, ICharacterStory> charactersInWorld = new ConcurrentHashMap<>();
    private final List<UUID> loadedCharactersSkin = new ArrayList<>();
    private final List<DialogRenderer2D> activeDialog2DRenderers = new ArrayList<>();
    private final List<DialogRenderer3D> activeDialog3DRenderers = new ArrayList<>();
    private final List<InkAction> activeClientInkActions = new ArrayList<>();
    private final Set<UUID> clickedInteractionPointIds = new HashSet<>();
    private ChoiceScreen choiceScreen;
    private boolean inStory;
    private CameraView cameraView;
    private DialogRenderer mainDialog;
    private boolean stopNextDialog;
    private HashMap<Integer, Boolean> choiceLocked = new HashMap<>() {};

    public ClientPlayerSession() {
        super(null, null);
        initChoiceLocked();
    }

    public ClientPlayerSession(Chapter chapter, Scene scene) {
        super(chapter, scene);
        initChoiceLocked();
    }

    private void initChoiceLocked() {
        choiceLocked.put(0, false);
        choiceLocked.put(1, false);
        choiceLocked.put(2, false);
        choiceLocked.put(3, false);
    }

    public CutsceneDataSession getCutsceneDataSession() {
        return cutsceneDataSession;
    }

    public Map<UUID, ICharacterStory> getCharactersInWorld() {
        return charactersInWorld;
    }

    public ICharacterStory getCharacterByProfileId(UUID profileId) {
        return charactersInWorld.get(profileId);
    }

    public List<DialogRenderer2D> getActiveDialog2DRenderers() {
        return Collections.unmodifiableList(activeDialog2DRenderers);
    }

    public List<DialogRenderer3D> getActiveDialog3DRenderers() {
        return Collections.unmodifiableList(activeDialog3DRenderers);
    }

    @Override
    public boolean isClientSide() {
        return true;
    }

    @Override
    public void setEditor(EditorMaker editorMaker) {
        EditorMaker previous = getEditor();
        super.setEditor(editorMaker);
        if (previous != null && previous != editorMaker) {
            previous.close();
        }
        editorSessionId = BiEditorClose.UNIDENTIFIED_SESSION;
    }

    public void requestEditorClose() {
        Services.PACKET.sendToServer(new BiEditorClose(editorSessionId));
        closeEditor();
    }

    @Override
    public void clear() {
        super.clear();
        new ArrayList<>(activeDialog2DRenderers).forEach(this::removeDialog2D);
        new ArrayList<>(activeDialog3DRenderers).forEach(this::removeDialog3D);
        activeClientInkActions.clear();
        cutsceneDataSession.reset();
        clickedInteractionPointIds.clear();
        charactersInWorld.clear();
        mainDialog = null;
        cameraView = null;
        stopNextDialog = false;
        inStory = false;
        choiceScreen = null;
    }

    public boolean hasClickedInteractionPoint(UUID pointId) {
        return clickedInteractionPointIds.contains(pointId);
    }

    public void addClickedInteractionPoint(UUID pointId) {
        clickedInteractionPointIds.add(pointId);
    }

    public void addDialog2D(DialogRenderer2D renderer) {
        activeDialog2DRenderers.add(renderer);
    }

    public void addDialog3D(DialogRenderer3D renderer) {
        activeDialog3DRenderers.add(renderer);
    }

    public void removeDialog2D(DialogRenderer2D renderer) {
        activeDialog2DRenderers.remove(renderer);
        Minecraft.getInstance().setScreen(null);
    }

    public void removeDialog3D(DialogRenderer3D renderer) {
        activeDialog3DRenderers.remove(renderer);
    }

    public CameraView getCameraView() {
        return cameraView;
    }

    public void setCameraView(CameraView cameraView) {
        this.cameraView = cameraView;
    }

    public List<InkAction> getActiveClientInkActions() {
        return activeClientInkActions;
    }

    public void addClientInkAction(InkAction action) {
        activeClientInkActions.add(action);
    }

    public void stopAllClientInkActions() {
        new ArrayList<>(activeClientInkActions).forEach(InkAction::stop);
        activeClientInkActions.clear();
    }

    public DialogRenderer getMainDialog() {
        return mainDialog;
    }

    public void setMainDialog(DialogRenderer mainDialog) {
        this.mainDialog = mainDialog;
    }

    public boolean isStopNextDialog() {
        return stopNextDialog;
    }

    public void setStopNextDialog(boolean stopNextDialog) {
        this.stopNextDialog = stopNextDialog;
    }

    public boolean isInStory() {
        return inStory;
    }

    public void setInStory(boolean inStory) {
        this.inStory = inStory;
    }

    public List<UUID> getLoadedCharactersSkin() {
        return loadedCharactersSkin;
    }

    public StorySaveIconRenderer getSaveIconRenderer() {
        return saveIconRenderer;
    }

    public boolean inCamera() {
        return cameraView != null || cutsceneDataSession.getKeyframePosition() != null;
    }

    public ChoiceScreen getChoiceScreen() {
        return choiceScreen;
    }

    public void setChoiceScreen(ChoiceScreen choiceScreen) {
        this.choiceScreen = choiceScreen;
    }

    public HashMap<Integer, Boolean> getChoiceLocked() {
        return choiceLocked;
    }

    public void setChoiceLocked(HashMap<Integer, Boolean> choiceLocked) {
        this.choiceLocked = choiceLocked;
    }
}
