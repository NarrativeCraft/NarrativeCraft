package fr.loudo.narrativecraft.managers;

import fr.loudo.narrativecraft.narrative.chapter.Chapter;

public class ChapterManager extends Manager<Chapter> {

    public Chapter getChapterByIndex(int index)
    {
        for (Chapter chapter : list) {
            if (chapter.getChapterIndex() == index)
                return chapter;
        }
        return null;
    }

}
