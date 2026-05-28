package fr.loudo.narrativecraft.client.screens.narrative.cutscene;

import fr.loudo.narrativecraft.client.screens.AbstractAssignScreen;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.cutscene.CutscenePayload;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.NarrativeEntryAction;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CutsceneAnimationsAssignScreen extends AbstractAssignScreen<Animation> {

    private final Cutscene cutscene;

    public CutsceneAnimationsAssignScreen(Cutscene cutscene, Screen lastScreen) {
        super(Component.literal("Assign Animations"), lastScreen);
        this.cutscene = cutscene;
    }

    @Override
    protected List<Animation> buildAvailableList() {
        Set<UUID> assignedIds = new HashSet<>();
        for (Animation animation : cutscene.getAnimations()) assignedIds.add(animation.getId());
        List<Animation> result = new ArrayList<>();
        for (Animation animation : cutscene.getScene().getAnimationManager().getList()) {
            if (!assignedIds.contains(animation.getId())) result.add(animation);
        }
        return result;
    }

    @Override
    protected List<Animation> buildAssignedList() {
        return new ArrayList<>(cutscene.getAnimations());
    }

    @Override
    protected String getItemName(Animation item) {
        return item.getName();
    }

    @Override
    protected String getBreadcrumb() {
        return cutscene.getScene().getChapter().getName()
                + ";" + cutscene.getScene().getName()
                + ";" + cutscene.getName();
    }

    @Override
    protected Component getAvailableColumnLabel() {
        return Translation.message("screen.cutscene_assign.available");
    }

    @Override
    protected Component getAssignedColumnLabel() {
        return Translation.message("screen.cutscene_assign.assigned");
    }

    @Override
    protected void onSave(List<Animation> assigned) {
        List<UUID> animationIds = assigned.stream().map(Animation::getId).toList();
        List<UUID> subsceneIds = cutscene.getSubscenes().stream().map(Subscene::getId).toList();
        Services.PACKET.sendToServer(new BiSyncNarrativeEntryPacket(
                cutscene.getId(),
                new CutscenePayload(
                        cutscene.getName(),
                        cutscene.getDescription(),
                        cutscene.getScene().getId(),
                        cutscene.getScene().getChapter().getId(),
                        animationIds,
                        subsceneIds),
                NarrativeEntryAction.EDIT));
        minecraft.setScreen(lastScreen);
    }
}
