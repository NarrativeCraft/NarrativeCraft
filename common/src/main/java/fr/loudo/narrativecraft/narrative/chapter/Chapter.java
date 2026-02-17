package fr.loudo.narrativecraft.narrative.chapter;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;

public class Chapter extends NarrativeEntry {

    private int chapterIndex;

    public Chapter(String name, String description, int chapterIndex) {
        super(name, description);
        this.chapterIndex = chapterIndex;
    }

    public int getChapterIndex() {
        return chapterIndex;
    }

    public void setChapterIndex(int chapterIndex) {
        this.chapterIndex = chapterIndex;
    }
}
