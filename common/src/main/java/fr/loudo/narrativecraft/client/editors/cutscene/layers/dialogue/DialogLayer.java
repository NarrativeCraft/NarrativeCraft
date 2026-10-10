package fr.loudo.narrativecraft.client.editors.cutscene.layers.dialogue;

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayerType;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.cutscene.ClientCutsceneMakerEditorMaker;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.dialog.DialogRenderer3D;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.DialogKeyframe;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DialogLayer extends CutsceneLayer {

    private static final Minecraft MINECRAFT = Minecraft.getInstance();
    private static DialogRenderer3D activeBlocking = null;

    private final HashSet<Integer> executed = new HashSet<>();
    private final HashMap<Integer, DialogRenderer3D> activeNonBlocking = new HashMap<>();

    public DialogLayer(ICutsceneLayerType layerType) {
        super(layerType);
    }

    @Override
    public Keyframe createDefaultKeyframe(int tick) {
        return new DialogKeyframe(this, tick);
    }

    @Override
    public boolean execute(float tick) {
        if (activeBlocking != null && !activeBlocking.getAnimator().isStopping()) {
            activeBlocking.stop();
        }

        List<DialogKeyframe> keyframes = getSortedKeyframes(DialogKeyframe.class);
        for (DialogKeyframe keyframe : keyframes) {
            if (!executed.contains(keyframe.getTick())) {
                ClientNarrativeCraftMod clientNarrativeCraftMod = ClientNarrativeCraftMod.getInstance();
                CharacterStory characterStory = clientNarrativeCraftMod.getCharacterManager().getByName("greg");
                Entity entity = resolveCharacterEntity(characterStory);

                if (entity != null) {
                    if (!keyframe.getData().isAutoSkipEnabled() && keyframe.getTick() == (int) tick) {
                        clientNarrativeCraftMod.getCutsceneMakerEditor().getPlayback().pause(true);
                        clientNarrativeCraftMod.getCutsceneMakerEditor().getControl().pause();

                        addDialogue(keyframe.getData(), entity, "test text hi hi hi", (int) tick, true);
                    } else {
                        if (keyframe.getTick() <= tick && tick < keyframe.getData().getAutoSkipSeconds() * 20) {
                            addDialogue(keyframe.getData(), entity, "test text hi hi hi", (int) tick, false);
                        } else {
                            DialogRenderer3D removed = activeNonBlocking.remove((int) tick);
                            if (removed != null) removed.stop();
                        }
                    }
                }

                return true;
            } else if (tick < keyframe.getTick()) {
                executed.remove(keyframe.getTick());
            }
        }

        return false;
    }

    private void addDialogue(DialogData data, Entity entity, String text, int tick, boolean isBlocking) {
        ClientPlayerSession clientPlayerSession = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        DialogRenderer3D dialogRenderer3D = new DialogRenderer3D(data, entity);

        if (isBlocking) {
            if (activeBlocking != null) activeBlocking.stop();
            activeBlocking = dialogRenderer3D;
        } else {
            activeNonBlocking.put(tick, dialogRenderer3D);
        }

        dialogRenderer3D.onStopped(() -> {
            clientPlayerSession.removeDialog3D(dialogRenderer3D);
            if (isBlocking && activeBlocking == dialogRenderer3D) activeBlocking = null;
        });

        dialogRenderer3D.start(text);
        clientPlayerSession.addDialog3D(dialogRenderer3D);
        executed.add(tick);
    }

    private static @Nullable Entity resolveCharacterEntity(ICharacterStory characterStory) {
        assert MINECRAFT.level != null;

        for (Map.Entry<UUID, ICharacterStory> entry : ClientNarrativeCraftMod.getInstance().getPlayerSession().getCharactersInWorld().entrySet()) {
            if (entry.getValue() == characterStory) {
                Entity entity = MINECRAFT.level.getPlayerByUUID(entry.getKey());

                if (entity == null) {
                    for (Entity entity1 : MINECRAFT.level.entitiesForRendering()) {
                        if (entity1.getUUID() == entry.getKey()) return entity1;
                    }
                }

                return entity;
            }
        }
        return null;
    }

    public static void advanceDialogue() {
        ClientCutsceneMakerEditorMaker cutsceneMakerEditorMaker = ClientNarrativeCraftMod.getInstance().getCutsceneMakerEditor();
        if (!cutsceneMakerEditorMaker.getPlayback().isPlaying() && activeBlocking != null) {
            if (activeBlocking.isAnimating()) return;
            if (!activeBlocking.isTextFinished()) {
                activeBlocking.forceFinishText();
                return;
            }
            cutsceneMakerEditorMaker.getPlayback().play(cutsceneMakerEditorMaker.getPlayback().getCurrentTick());
            cutsceneMakerEditorMaker.getControl().play();
            activeBlocking.stop();
        }
    }
}
