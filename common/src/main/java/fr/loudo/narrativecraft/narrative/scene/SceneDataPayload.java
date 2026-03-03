package fr.loudo.narrativecraft.narrative.scene;

import fr.loudo.narrativecraft.narrative.NarrativeEntryPayload;

import java.util.UUID;

public class SceneDataPayload extends NarrativeEntryPayload {
    public SceneDataPayload(UUID uuid, String name, String description) {
        super(uuid, name, description);
    }
}
