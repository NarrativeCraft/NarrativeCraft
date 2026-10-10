package fr.loudo.narrativecraft.client.editors.cutscene.layers.dialogue;

import com.google.gson.JsonObject;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayerType;
import fr.loudo.narrativecraft.client.editors.widgets.DialogFieldSet;
import fr.loudo.narrativecraft.dialog.DialogDataIO;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.DialogKeyframe;

public class DialogLayerType implements ICutsceneLayerType {

    public static final String ID = "dialogue";

    private static final String TICK_FIELD = "tick";
    private static final String DIALOG_FIELD = "dialog";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Dialogue";
    }

    @Override
    public CutsceneLayer createLayer() {
        return new DialogLayer(this);
    }

    @Override
    public JsonObject serializeKeyframe(Keyframe keyframe) {
        if (!(keyframe instanceof DialogKeyframe dialogKeyframe)) return null;
        JsonObject json = new JsonObject();
        json.addProperty(TICK_FIELD, dialogKeyframe.getTick());
        json.add(DIALOG_FIELD, DialogDataIO.serialize(dialogKeyframe.getData(), DialogFieldSet.ALL));
        return json;
    }

    @Override
    public Keyframe deserializeKeyframe(CutsceneLayer layer, JsonObject json) {
        if (!json.has(TICK_FIELD) || !json.has(DIALOG_FIELD)) return null;

        DialogKeyframe dialogKeyframe = new DialogKeyframe(layer, json.get(TICK_FIELD).getAsInt());
        dialogKeyframe.setData(DialogDataIO.deserialize(json.getAsJsonObject(DIALOG_FIELD), DialogFieldSet.ALL));

        return dialogKeyframe;
    }
}
