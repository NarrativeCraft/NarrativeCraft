package fr.loudo.narrativecraft.client.editors.cutscene.menu;

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.KeyframeMenu;
import fr.loudo.narrativecraft.client.editors.widgets.ToggleButton;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.DialogKeyframe;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public class DialogKeyframeMenu extends KeyframeMenu<DialogKeyframe> {

    private static final int ROW_HEIGHT = 16;
    private static final int LABEL_WIDTH = 84;

    ToggleButton autoSkipEnabled;

    public DialogKeyframeMenu(DialogKeyframe keyframe) {
        super(keyframe);
    }

    @Override
    protected int getContentHeight() {
        return ROW_HEIGHT;
    }

    @Override
    protected void renderContent(GuiGraphics graphics, DeltaTracker delta, int x, int y, int contentWidth, int mouseX, int mouseY) {
        autoSkipEnabled.setPosition(x, y);
        autoSkipEnabled.render(graphics, mouseX, mouseY, delta.getGameTimeDeltaTicks());
    }

    @Override
    protected void applyChanges() {
        this.keyframe.setData(
                new DialogData()
                        .setAutoSkipEnabled(autoSkipEnabled.getValue())
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean isDoubleClick) {
        if (autoSkipEnabled.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button, isDoubleClick);
    }

    @Override
    protected void initContent() {
        autoSkipEnabled = new ToggleButton(0, 0, width - padding*2 - LABEL_WIDTH, ROW_HEIGHT, keyframe.getData().isAutoSkipEnabled(), null);
    }
}
