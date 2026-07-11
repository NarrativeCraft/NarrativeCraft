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

package fr.loudo.narrativecraft.narrative.story;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public final class StoryLibrary {

    private final Map<String, CompiledStory> compiledStories = new LinkedHashMap<>();
    private final String defaultLocale;

    public StoryLibrary(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public void add(CompiledStory compiledStory) {
        compiledStories.put(compiledStory.locale(), compiledStory);
    }

    public boolean isEmpty() {
        return compiledStories.isEmpty();
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public List<String> getLocales() {
        return List.copyOf(compiledStories.keySet());
    }

    @Nullable
    public CompiledStory get(String locale) {
        return compiledStories.get(locale);
    }

    @Nullable
    public CompiledStory getDefault() {
        return compiledStories.get(defaultLocale);
    }

    @Nullable
    public CompiledStory resolve(@Nullable String requestedLocale) {
        if (requestedLocale != null) {
            CompiledStory requested = compiledStories.get(requestedLocale);
            if (requested != null) return requested;
        }
        return getDefault();
    }

    @Nullable
    public CompiledStory resolveForSave(
            @Nullable String requestedLocale, @Nullable String savedLocale, @Nullable String savedStructureHash) {
        CompiledStory requested = resolve(requestedLocale);
        if (requested == null) return null;

        if (savedStructureHash == null || savedStructureHash.equals(requested.structureHash())) {
            return requested;
        }

        CompiledStory saved = savedLocale == null ? null : compiledStories.get(savedLocale);
        if (saved != null && savedStructureHash.equals(saved.structureHash())) {
            return saved;
        }

        for (CompiledStory candidate : compiledStories.values()) {
            if (savedStructureHash.equals(candidate.structureHash())) {
                return candidate;
            }
        }

        return requested;
    }

    public boolean isCompatibleWithSave(@Nullable String requestedLocale, @Nullable String savedStructureHash) {
        if (savedStructureHash == null) return true;
        CompiledStory requested = resolve(requestedLocale);
        return requested != null && savedStructureHash.equals(requested.structureHash());
    }

    public List<String> getLocalesSharingStructureWith(String structureHash) {
        List<String> locales = new ArrayList<>();
        for (CompiledStory compiledStory : compiledStories.values()) {
            if (compiledStory.structureHash().equals(structureHash)) {
                locales.add(compiledStory.locale());
            }
        }
        return locales;
    }
}
