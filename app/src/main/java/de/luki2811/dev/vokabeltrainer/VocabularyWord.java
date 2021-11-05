package de.luki2811.dev.vokabeltrainer;

public class VocabularyWord {
    String knownWord;
    String newWord;
    boolean ignoreCase;
    boolean wrong;
    boolean used;

    public VocabularyWord(String knownWord, String newWord, Boolean ignoreCase){
        this.knownWord = knownWord;
        this.newWord = newWord;
        this.ignoreCase = ignoreCase;

    }

    public String getNewWord() {
        return newWord;
    }

    public boolean isWrong() {
        return wrong;
    }

    public boolean isAlreadyUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public void setWrong(boolean wrong) {
        this.wrong = wrong;
    }

    public String getKnownWord() {
        return knownWord;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }
}
