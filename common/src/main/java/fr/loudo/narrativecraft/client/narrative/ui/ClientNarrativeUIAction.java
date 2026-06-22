/*
 * NarrativeCraft - Create narrative games inside Minecraft. No coding, no game engine, only text and logic.
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

package fr.loudo.narrativecraft.client.narrative.ui;

import fr.loudo.narrativecraft.client.screens.AbstractNarrativeEntryEditScreen;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import net.minecraft.client.gui.screens.Screen;

/**
 * Defines the UI actions available for narrative entries in the client-side narrative interface.
 * <p>
 * This interface handles the interaction logic for displaying, editing, and navigating
 * narrative entries. Click behaviour follows a priority rule:
 * <ul>
 *   <li>If {@link #subListSubScreen(NarrativeEntry, Screen)} returns a <b>non-null</b> screen,
 *       that screen takes priority and is opened on click.</li>
 *   <li>If it returns <b>null</b>, {@link #customClickAction(NarrativeEntry)} ()} is executed instead.</li>
 * </ul>
 *
 * @param <T> the type of {@link NarrativeEntry} this action operates on
 */
public interface ClientNarrativeUIAction<T extends NarrativeEntry<?>> {

    /**
     * Returns the sub-screen to display when the given entry is clicked, or {@code null}
     * if no sub-screen should be shown.
     * <p>
     * <b>Click priority:</b> when this method returns a non-null value, the returned screen
     * is opened and {@link #customClickAction(NarrativeEntry)} ()} is <em>not</em> called. If this method
     * returns {@code null}, {@link #customClickAction(NarrativeEntry)} ()} is executed instead.
     *
     * @param entry  the narrative entry that was clicked
     * @param parent the current parent screen, used as the "back" destination
     * @return the sub-screen to open, or {@code null} to fall through to {@link #customClickAction(NarrativeEntry)} ()}
     */
    Screen subListSubScreen(T entry, Screen parent);

    /**
     * Returns whether this entry is clickable in the UI.
     *
     * @return {@code true} if the entry can be clicked; {@code false} otherwise
     */
    boolean isClickable();

    /**
     * Executes the custom click action for this entry.
     * <p>
     * This method is only invoked when {@link #subListSubScreen(NarrativeEntry, Screen)}
     * returns {@code null}. If a sub-screen is provided, it takes priority and this
     * method is not called.
     *
     * @param entry the narrative entry that was clicked
     */
    void customClickAction(T entry);

    /**
     * Opens the edit screen for the given narrative entry.
     *
     * @param entry      the entry to edit, or {@code null} when creating a new entry
     * @param lastScreen the screen to return to when the edit screen is closed
     * @return the edit screen for the given entry
     */
    AbstractNarrativeEntryEditScreen<T> showEditScreen(T entry, Screen lastScreen);

    /**
     * Opens the creation screen for a new narrative entry under the given parent.
     * <p>
     * Defaults to delegating to {@link #showEditScreen(NarrativeEntry, Screen)}
     * with a {@code null} entry, indicating that a new entry should be created.
     *
     * @param parent     the parent narrative entry under which the new entry will be created
     * @param lastScreen the screen to return to when the creation screen is closed
     * @return the creation screen for a new child entry
     */
    default AbstractNarrativeEntryEditScreen<T> showCreateScreen(NarrativeEntry<?> parent, Screen lastScreen) {
        return showEditScreen(null, lastScreen);
    }
}
