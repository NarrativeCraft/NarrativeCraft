package fr.loudo.narrativecraft.editors.cutscene.keyframes;

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.KeyframeMenu;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.client.editors.cutscene.menu.DialogKeyframeMenu;
import fr.loudo.narrativecraft.dialog.DialogData;

public class DialogKeyframe extends Keyframe {

    DialogData data = new DialogData();

    public DialogKeyframe(CutsceneLayer layer, int tick) {
        super(layer, tick);
    }

    public void setData(DialogData dialogData) {
        this.data = dialogData;
    }

    public DialogData getData() {
        return this.data;
    }

    @Override
    public KeyframeMenu<?> createMenu() {
        return new DialogKeyframeMenu(this);
    }
}
