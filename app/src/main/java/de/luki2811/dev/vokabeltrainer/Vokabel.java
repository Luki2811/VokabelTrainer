package de.luki2811.dev.vokabeltrainer;

public class Vokabel {
    String knownWord;
    String newWord;

    Language knowLanguage;
    Language newLanguage;

    public Vokabel(String knownWord, String newWord, Language knowLanguage, Language newLanguage){
        this.knowLanguage = knowLanguage;
        this.knownWord = knownWord;
        this.newLanguage = newLanguage;
        this.newWord = newWord;

    }

}
