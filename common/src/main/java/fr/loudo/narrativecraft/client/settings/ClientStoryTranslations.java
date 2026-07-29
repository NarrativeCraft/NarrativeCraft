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

package fr.loudo.narrativecraft.client.settings;

import fr.loudo.narrativecraft.narrative.story.locale.TranslationResolver;
import java.util.Map;
import net.minecraft.client.Minecraft;

public final class ClientStoryTranslations {

    private static final String USER_PLACEHOLDER = "%user%";

    private static Map<String, String> entries = Map.of();

    private ClientStoryTranslations() {}

    public static void set(Map<String, String> entries) {
        ClientStoryTranslations.entries = Map.copyOf(entries);
    }

    public static String localize(String text) {
        String resolved =
                TranslationResolver.resolve(ClientStoryVariables.interpolate(text), ClientStoryTranslations::find);
        if (Minecraft.getInstance().player == null) return resolved;
        return resolved.replace(
                USER_PLACEHOLDER, Minecraft.getInstance().player.getName().getString());
    }

    private static String find(String key) {
        String value = entries.get(key);
        return value == null || value.isBlank() ? null : value;
    }
}
