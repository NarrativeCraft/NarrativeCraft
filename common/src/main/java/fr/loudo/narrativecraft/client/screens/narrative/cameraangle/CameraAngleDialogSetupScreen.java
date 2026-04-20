/*
 * NarrativeCraft - Create your own stories, easily, and freely in Minecraft.
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

package fr.loudo.narrativecraft.client.screens.narrative.cameraangle;

import fr.loudo.narrativecraft.client.editors.cameraangle.ClientCameraAngleMakerEditor;
import fr.loudo.narrativecraft.client.screens.AbstractNarrativeEntryPickerScreen;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngle;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraView;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraViewDialogSetup;
import fr.loudo.narrativecraft.narrative.cameraangle.CharacterPlacement;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CameraAngleDialogSetupScreen extends Screen {

    private static final int ROW_HEIGHT = 20;
    private static final int ROW_GAP = 4;
    private static final int MAX_PER_PAGE = 5;
    private static final int LIST_START_Y = 65;
    private static final int NAME_WIDTH = 140;
    private static final int ACTION_EDIT_WIDTH = 36;
    private static final int ACTION_DELETE_WIDTH = 16;

    private final ClientCameraAngleMakerEditor editor;
    private final Screen lastScreen;
    private int selectedCameraIndex = 0;
    private int setupPage = 1;

    public CameraAngleDialogSetupScreen(ClientCameraAngleMakerEditor editor, Screen lastScreen) {
        super(Component.literal("Dialog Setups"));
        this.editor = editor;
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        CameraAngle cameraAngle = editor.getCameraAngle();
        List<CameraView> cameras = cameraAngle.getCameras();
        int centerX = this.width / 2;

        if (selectedCameraIndex >= cameras.size()) {
            selectedCameraIndex = 0;
        }

        if (cameras.isEmpty()) {
            StringWidget noCamera = new StringWidget(Component.literal("No camera views"), this.font);
            noCamera.setPosition(centerX - noCamera.getWidth() / 2, this.height / 2 - 5);
            addRenderableWidget(noCamera);
        } else {
            CameraView selectedCamera = cameras.get(selectedCameraIndex);

            if (selectedCameraIndex > 0) {
                addRenderableWidget(Button.builder(Component.literal("<"), b -> {
                            selectedCameraIndex--;
                            setupPage = 1;
                            rebuild();
                        })
                        .bounds(centerX - 84, 30, 16, ROW_HEIGHT)
                        .build());
            }

            Button cameraLabel = Button.builder(Component.literal(selectedCamera.getName()), b -> {})
                    .bounds(centerX - 64, 30, 128, ROW_HEIGHT)
                    .build();
            cameraLabel.active = false;
            addRenderableWidget(cameraLabel);

            if (selectedCameraIndex < cameras.size() - 1) {
                addRenderableWidget(Button.builder(Component.literal(">"), b -> {
                            selectedCameraIndex++;
                            setupPage = 1;
                            rebuild();
                        })
                        .bounds(centerX + 68, 30, 16, ROW_HEIGHT)
                        .build());
            }

            StringWidget cameraCountLabel =
                    new StringWidget(Component.literal((selectedCameraIndex + 1) + "/" + cameras.size()), this.font);
            cameraCountLabel.setPosition(centerX - cameraCountLabel.getWidth() / 2, 54);
            addRenderableWidget(cameraCountLabel);

            List<CameraViewDialogSetup> setups = selectedCamera.getDialogSetups();
            int rowX = centerX - (NAME_WIDTH + ROW_GAP + ACTION_EDIT_WIDTH + ROW_GAP + ACTION_DELETE_WIDTH) / 2;
            int paginationY = LIST_START_Y + MAX_PER_PAGE * (ROW_HEIGHT + ROW_GAP) + 4;

            renderPagedList(
                    setups,
                    setupPage,
                    centerX,
                    paginationY,
                    () -> {
                        setupPage--;
                        rebuild();
                    },
                    () -> {
                        setupPage++;
                        rebuild();
                    },
                    (rowY, setup) -> addSetupRow(rowX, rowY, selectedCamera, setup));

            int addButtonY = paginationY + 28;
            List<CharacterPlacement> available = buildAvailablePlacements(selectedCamera);
            Button addButton = Button.builder(Component.literal("+ Setup"), b -> openPlacementPicker(selectedCamera))
                    .bounds(centerX - 50, addButtonY, 100, ROW_HEIGHT)
                    .build();
            addButton.active = !available.isEmpty();
            addRenderableWidget(addButton);
        }

        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(centerX - 60, this.height - 30, 120, ROW_HEIGHT)
                .build());
    }

    private void addSetupRow(int x, int y, CameraView cameraView, CameraViewDialogSetup setup) {
        String characterName = resolveCharacterName(setup);

        Button nameLabel = Button.builder(Component.literal(characterName), b -> {})
                .bounds(x, y, NAME_WIDTH, ROW_HEIGHT)
                .build();
        nameLabel.active = false;
        addRenderableWidget(nameLabel);

        addRenderableWidget(Button.builder(Component.literal("Edit"), b -> {
                    editor.openAdvancedPanel(setup);
                    onClose();
                })
                .bounds(x + NAME_WIDTH + ROW_GAP, y, ACTION_EDIT_WIDTH, ROW_HEIGHT)
                .build());

        addRenderableWidget(Button.builder(Component.literal("✖"), b -> {
                    cameraView.getDialogSetups().remove(setup);
                    if (setupPage > 1
                            && (setupPage - 1) * MAX_PER_PAGE
                                    >= cameraView.getDialogSetups().size()) {
                        setupPage--;
                    }
                    rebuild();
                })
                .bounds(x + NAME_WIDTH + ROW_GAP + ACTION_EDIT_WIDTH + ROW_GAP, y, ACTION_DELETE_WIDTH, ROW_HEIGHT)
                .build());
    }

    private void openPlacementPicker(CameraView cameraView) {
        List<CharacterPlacement> available = buildAvailablePlacements(cameraView);
        if (available.isEmpty()) return;

        minecraft.setScreen(
                new AbstractNarrativeEntryPickerScreen<CharacterPlacement>(
                        Component.literal("Pick Character Placement"), available, this, placement -> cameraView
                                .getDialogSetups()
                                .add(new CameraViewDialogSetup(placement.getId()))) {
                    @Override
                    protected String getItemName(CharacterPlacement item) {
                        return item.getCharacterStory().getName();
                    }
                });
    }

    private List<CharacterPlacement> buildAvailablePlacements(CameraView cameraView) {
        return editor.getCameraAngle().getCharacterPlacements().stream()
                .filter(placement -> cameraView.getDialogSetups().stream()
                        .noneMatch(setup -> setup.getCharacterPlacementId().equals(placement.getId())))
                .toList();
    }

    private String resolveCharacterName(CameraViewDialogSetup setup) {
        for (CharacterPlacement placement : editor.getCameraAngle().getCharacterPlacements()) {
            if (placement.getId().equals(setup.getCharacterPlacementId())) {
                return placement.getCharacterStory().getName();
            }
        }
        return setup.getCharacterPlacementId().toString().substring(0, 8);
    }

    private <T> void renderPagedList(
            List<T> list,
            int page,
            int centerX,
            int paginationY,
            Runnable onPrev,
            Runnable onNext,
            BiConsumer<Integer, T> rowRenderer) {
        int start = (page - 1) * MAX_PER_PAGE;
        int end = Math.min(start + MAX_PER_PAGE, list.size());
        for (int i = start; i < end; i++) {
            rowRenderer.accept(LIST_START_Y + (i - start) * (ROW_HEIGHT + ROW_GAP), list.get(i));
        }
        buildPagination(centerX, paginationY, list.size(), page, onPrev, onNext);
    }

    private void buildPagination(
            int centerX, int y, int totalItems, int currentPage, Runnable onPrev, Runnable onNext) {
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / MAX_PER_PAGE));
        if (totalPages <= 1) return;

        if (currentPage > 1) {
            addRenderableWidget(Button.builder(Component.literal("<"), b -> onPrev.run())
                    .bounds(centerX - 46, y, 20, 18)
                    .build());
        }
        StringWidget pageWidget = new StringWidget(Component.literal(currentPage + "/" + totalPages), this.font);
        pageWidget.setPosition(centerX - pageWidget.getWidth() / 2, y + 4);
        addRenderableWidget(pageWidget);
        if (currentPage < totalPages) {
            addRenderableWidget(Button.builder(Component.literal(">"), b -> onNext.run())
                    .bounds(centerX + 26, y, 20, 18)
                    .build());
        }
    }

    private void rebuild() {
        this.clearWidgets();
        this.init();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }
}
