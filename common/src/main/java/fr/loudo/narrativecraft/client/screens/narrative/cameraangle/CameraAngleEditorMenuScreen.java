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

import fr.loudo.narrativecraft.client.editors.cameraangle.ClientCameraAngleMakerEditorMaker;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraView;
import fr.loudo.narrativecraft.narrative.cameraangle.CharacterPlacement;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleTeleportToTemplate;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.BiConsumer;

public class CameraAngleEditorMenuScreen extends Screen {

    private enum Tab {
        CAMERAS,
        CHARACTERS,
        CHARACTER_TEMPLATES
    }

    private static final int ROW_HEIGHT = 20;
    private static final int ROW_GAP = 4;
    private static final int NAME_WIDTH = 140;
    private static final int ACTION_WIDTH = 60;
    private static final int ACTION_SMALL_WIDTH = 20;
    private static final int MAX_PER_PAGE = 6;
    private static final int LIST_START_Y = 60;

    private final ClientCameraAngleMakerEditorMaker editor;
    private final Screen lastScreen;
    private Tab tab = Tab.CAMERAS;
    private int camerasPage = 1;
    private int entitiesPage = 1;
    private int templatesPage = 1;

    public CameraAngleEditorMenuScreen(ClientCameraAngleMakerEditorMaker editor, Screen lastScreen) {
        super(Component.literal("Camera Angle Menu"));
        this.editor = editor;
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        int tabTotalWidth = 3 * 100 + 2 * 10;
        int tabStartX = centerX - tabTotalWidth / 2;

        Button camerasTab = Button.builder(
                        Translation.message("screen.camera_angle_editor.menu.cameras"), b -> switchTab(Tab.CAMERAS))
                .bounds(tabStartX, 20, 100, ROW_HEIGHT)
                .build();
        camerasTab.active = tab != Tab.CAMERAS;
        addRenderableWidget(camerasTab);

        Button entitiesTab = Button.builder(
                        Translation.message("screen.camera_angle_editor.menu.entities"), b -> switchTab(Tab.CHARACTERS))
                .bounds(tabStartX + 110, 20, 100, ROW_HEIGHT)
                .build();
        entitiesTab.active = tab != Tab.CHARACTERS;
        addRenderableWidget(entitiesTab);

        Button templatesTab = Button.builder(
                        Translation.message("screen.camera_angle_editor.menu.templates"),
                        b -> switchTab(Tab.CHARACTER_TEMPLATES))
                .bounds(tabStartX + 220, 20, 100, ROW_HEIGHT)
                .build();
        templatesTab.active = tab != Tab.CHARACTER_TEMPLATES;
        addRenderableWidget(templatesTab);

        int paginationY = LIST_START_Y + MAX_PER_PAGE * (ROW_HEIGHT + ROW_GAP) + 4;

        if (tab == Tab.CAMERAS) {
            int rowX = centerX - (NAME_WIDTH + ACTION_WIDTH + ACTION_SMALL_WIDTH * 2 + ROW_GAP * 3) / 2;
            renderPagedList(
                    editor.getCameraViews(),
                    camerasPage,
                    centerX,
                    paginationY,
                    () -> {
                        camerasPage--;
                        rebuild();
                    },
                    () -> {
                        camerasPage++;
                        rebuild();
                    },
                    (rowY, camera) -> addCameraRow(rowX, rowY, camera));
        } else if (tab == Tab.CHARACTERS) {
            int rowX = centerX - (NAME_WIDTH + ACTION_SMALL_WIDTH + ROW_GAP) / 2;
            List<CharacterPlacement> nonTemplatePlacements = editor.getCharacterPlacements().stream()
                    .filter(p -> !p.isTemplate())
                    .toList();
            renderPagedList(
                    nonTemplatePlacements,
                    entitiesPage,
                    centerX,
                    paginationY,
                    () -> {
                        entitiesPage--;
                        rebuild();
                    },
                    () -> {
                        entitiesPage++;
                        rebuild();
                    },
                    (rowY, placement) -> addEntityRow(
                            rowX,
                            rowY,
                            Component.literal(placement.getCharacterStory().getName() + " "
                                    + placement.getCharacterStory().getCharacterType()),
                            () -> editor.teleportPlayerToPlacement(placement.getPosition()),
                            () -> {
                                editor.removeCharacterPlacement(placement);
                                rebuild();
                            }));
        } else {
            int rowX = centerX - (NAME_WIDTH + ACTION_SMALL_WIDTH + ROW_GAP) / 2;
            renderPagedList(
                    editor.getTemplateReferences(),
                    templatesPage,
                    centerX,
                    paginationY,
                    () -> {
                        templatesPage--;
                        rebuild();
                    },
                    () -> {
                        templatesPage++;
                        rebuild();
                    },
                    (rowY, template) -> addEntityRow(
                            rowX,
                            rowY,
                            Component.literal(template.sourceType() + " " + shortId(template.refId())),
                            () -> {
                                Services.PACKET.sendToServer(new C2SCameraAngleTeleportToTemplate(template.refId()));
                                minecraft.setScreen(null);
                            },
                            () -> {
                                editor.removeTemplateReference(template);
                                rebuild();
                            }));
        }

        Button closeButton = Button.builder(Translation.message("close"), b -> onClose())
                .bounds(this.width / 2 - 60, this.height - 30, 120, ROW_HEIGHT)
                .build();
        addRenderableWidget(closeButton);
    }

    private static String shortId(java.util.UUID id) {
        String s = id.toString();
        return s.substring(0, Math.min(8, s.length()));
    }

    private void addCameraRow(int x, int y, CameraView cameraView) {
        Button nameLabel = Button.builder(Component.literal(cameraView.getName()), b -> {
                    editor.teleportPlayerToPlacement(cameraView.getPosition());
                })
                .bounds(x, y, NAME_WIDTH, ROW_HEIGHT)
                .build();
        addRenderableWidget(nameLabel);

        int previewX = x + NAME_WIDTH + ROW_GAP;
        Button previewButton = Button.builder(Translation.message("screen.camera_angle_editor.menu.preview"), b -> {
                    editor.enterPreview(cameraView);
                    onClose();
                })
                .bounds(previewX, y, ACTION_WIDTH, ROW_HEIGHT)
                .build();
        addRenderableWidget(previewButton);

        int modifyX = previewX + ACTION_WIDTH + ROW_GAP;
        Button modifyButton = Button.builder(Component.literal("✎"), b -> openModifyScreen(cameraView))
                .bounds(modifyX, y, ACTION_SMALL_WIDTH, ROW_HEIGHT)
                .build();
        addRenderableWidget(modifyButton);

        int deleteX = modifyX + ACTION_SMALL_WIDTH + ROW_GAP;
        Button deleteButton = Button.builder(Component.literal("✖"), b -> {
                    editor.removeCamera(cameraView);
                    rebuild();
                })
                .bounds(deleteX, y, ACTION_SMALL_WIDTH, ROW_HEIGHT)
                .build();
        addRenderableWidget(deleteButton);
    }

    private void addEntityRow(int x, int y, Component label, Runnable onClick, Runnable onDelete) {
        Button nameLabel = Button.builder(label, b -> onClick.run())
                .bounds(x, y, NAME_WIDTH, ROW_HEIGHT)
                .build();
        addRenderableWidget(nameLabel);

        Button deleteButton = Button.builder(Component.literal("✖"), b -> onDelete.run())
                .bounds(x + NAME_WIDTH + ROW_GAP, y, ACTION_SMALL_WIDTH, ROW_HEIGHT)
                .build();
        addRenderableWidget(deleteButton);
    }

    private void openModifyScreen(CameraView cameraView) {
        minecraft.setScreen(new CameraAngleCameraNameScreen(
                Translation.message("screen.camera_angle_editor.camera_name_prompt"),
                cameraView.getName(),
                cameraView,
                newName -> {
                    editor.renameCamera(cameraView, newName);
                },
                this));
    }

    private <T> void renderPagedList(
            List<T> list,
            int page,
            int centerX,
            int paginationY,
            Runnable onPrev,
            Runnable onNext,
            BiConsumer<Integer, T> renderer) {
        int start = (page - 1) * MAX_PER_PAGE;
        int end = Math.min(start + MAX_PER_PAGE, list.size());
        for (int i = start; i < end; i++) {
            renderer.accept(LIST_START_Y + (i - start) * (ROW_HEIGHT + ROW_GAP), list.get(i));
        }
        buildPagination(centerX, paginationY, list.size(), page, onPrev, onNext);
    }

    private void buildPagination(int centerX, int y, int totalItems, int currentPage, Runnable prev, Runnable next) {
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / MAX_PER_PAGE));
        if (totalPages == 1) return;

        if (currentPage > 1) {
            addRenderableWidget(Button.builder(Component.literal("<"), b -> prev.run())
                    .bounds(centerX - 46, y, 20, 18)
                    .build());
        }
        StringWidget pageWidget = new StringWidget(Component.literal(currentPage + "/" + totalPages), this.font);
        pageWidget.setPosition(centerX - 12, y + 4);
        addRenderableWidget(pageWidget);
        if (currentPage < totalPages) {
            addRenderableWidget(Button.builder(Component.literal(">"), b -> next.run())
                    .bounds(centerX + 26, y, 20, 18)
                    .build());
        }
    }

    private void switchTab(Tab newTab) {
        this.tab = newTab;
        camerasPage = 1;
        entitiesPage = 1;
        templatesPage = 1;
        rebuild();
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
