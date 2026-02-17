package fr.loudo.narrativecraft.narrative.scene;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;

public class SceneData extends NarrativeEntry {

    protected Scene scene;

    public SceneData(String name, String description, Scene scene) {
        super(name, description);
        this.scene = scene;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}