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

import fr.loudo.narrativecraft.api.narrative.IStoryHandlerManager;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.session.PlayerSession;

public class StoryHandlerManager implements IStoryHandlerManager {
    @Override
    public void start(IPlayerSession playerSession) throws Exception {
        StoryHandler storyHandler = new StoryHandler((PlayerSession) playerSession);
        storyHandler.start();
        ((PlayerSession) playerSession).setStoryHandler(storyHandler);
    }

    @Override
    public void start(String path, IPlayerSession playerSession) throws Exception {
        StoryHandler storyHandler = new StoryHandler((PlayerSession) playerSession);
        storyHandler.start(path);
        ((PlayerSession) playerSession).setStoryHandler(storyHandler);
    }

    @Override
    public void stop(IPlayerSession playerSession) {
        StoryHandler storyHandler = (StoryHandler) playerSession.getStoryHandler();
        if (storyHandler == null) return;
        storyHandler.stop();
    }
}
