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

package fr.loudo.narrativecraft.client.screens.narrative.scene;

import fr.loudo.narrativecraft.client.screens.NarrativeEntryListScreen;
import fr.loudo.narrativecraft.client.screens.components.BreadcrumbWidget;
import fr.loudo.narrativecraft.client.screens.narrative.cutscene.CutsceneEntryListScreen;
import fr.loudo.narrativecraft.client.screens.narrative.subscene.SubsceneEntryListScreen;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.character.Npc;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SceneMenuScreen extends Screen {

    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;

    private final Screen lastScreen;
    private final Scene scene;

    public SceneMenuScreen(Scene scene, Screen lastScreen) {
        super(Translation.message("screen.scene_menu.title"));
        this.lastScreen = lastScreen;
        this.scene = scene;
    }

    @Override
    protected void init() {
        Map<Component, Runnable> labels = new LinkedHashMap<>();

        labels.put(Translation.message("screen.scene_menu.animations"), () -> {
            minecraft.setScreen(new NarrativeEntryListScreen<>(
                    Translation.message("animation"),
                    scene.getAnimationManager().getList(),
                    this,
                    Animation.class,
                    scene,
                    String.format(
                            "%s;%s;%s",
                            scene.getChapter().getName(),
                            scene.getName(),
                            Translation.message("animation").getString())));
        });

        labels.put(Translation.message("screen.scene_menu.camera_angles"), () -> {});
        labels.put(Translation.message("screen.scene_menu.cutscenes"), () -> {
            minecraft.setScreen(new CutsceneEntryListScreen(
                    Translation.message("cutscene"),
                    scene.getCutsceneManager().getList(),
                    this,
                    scene,
                    String.format(
                            "%s;%s;%s",
                            scene.getChapter().getName(),
                            scene.getName(),
                            Translation.message("cutscene").getString())));
        });
        labels.put(Translation.message("screen.scene_menu.interactions"), () -> {});
        labels.put(Translation.message("screen.scene_menu.npc"), () -> {
            minecraft.setScreen(new NarrativeEntryListScreen<>(
                    Translation.message("npc"),
                    scene.getNpcManager().getList(),
                    this,
                    Npc.class,
                    scene,
                    String.format(
                            "%s;%s;%s",
                            scene.getChapter().getName(),
                            scene.getName(),
                            Translation.message("npc").getString())));
        });
        labels.put(Translation.message("screen.scene_menu.subscenes"), () -> {
            minecraft.setScreen(new SubsceneEntryListScreen(
                    Translation.message("subscene"),
                    scene.getSubsceneManager().getList(),
                    this,
                    scene,
                    String.format(
                            "%s;%s;%s",
                            scene.getChapter().getName(),
                            scene.getName(),
                            Translation.message("subscene").getString())));
        });

        int totalHeight = labels.size() * BUTTON_HEIGHT + (labels.size() - 1) * BUTTON_GAP;
        int startY = (this.height - totalHeight) / 2;
        int buttonX = (this.width - BUTTON_WIDTH) / 2;

        int i = 0;
        for (Map.Entry<Component, Runnable> entry : labels.entrySet()) {
            int y = startY + i * (BUTTON_HEIGHT + BUTTON_GAP);
            this.addRenderableWidget(
                    Button.builder(entry.getKey(), b -> entry.getValue().run())
                            .bounds(buttonX, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                            .build());
            i++;
        }

        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> onClose())
                .bounds(this.width / 2 - BUTTON_WIDTH / 2, 10, 20, 20)
                .build());

        String breadcrumb = scene.getChapter().getName() + ";" + scene.getName();
        addRenderableWidget(new BreadcrumbWidget(20, 20, breadcrumb, this.font));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }
}
