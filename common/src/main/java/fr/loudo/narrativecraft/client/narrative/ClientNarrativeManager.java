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

package fr.loudo.narrativecraft.client.narrative;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.NarrativeEntryProcessor;
import fr.loudo.narrativecraft.network.NarrativeEntryAction;
import java.util.HashMap;
import java.util.Map;

public class ClientNarrativeManager {
    private static final Map<Class<? extends NarrativeEntry>, NarrativeEntryProcessor<?>> PROCESSORS = new HashMap<>();

    public static <T extends NarrativeEntry> void registerProcessor(
            Class<T> clazz, NarrativeEntryProcessor<T> processor) {
        PROCESSORS.put(clazz, processor);
    }

    @SuppressWarnings("unchecked")
    public static <T extends NarrativeEntry> void dispatch(NarrativeEntryAction action, T entry) {
        NarrativeEntryProcessor<T> processor = (NarrativeEntryProcessor<T>) PROCESSORS.get(entry.getClass());
        if (processor != null) {
            processor.process(action, entry);
        } else {
            NarrativeCraftMod.LOGGER.warn("Narrative entry processor not found for class {}", entry.getClass());
        }
    }
}
