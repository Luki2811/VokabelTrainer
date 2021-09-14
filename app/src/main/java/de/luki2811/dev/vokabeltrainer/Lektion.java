package de.luki2811.dev.vokabeltrainer;

public class Lektion {
    String name;
    Language languageKnow;
    Language languageNew;

    public String getName() {
        return name;
    }

    public Lektion(String name, Language languageKnow, Language languageNew, Vokabel[] vocs){
        this.name = name;
        this.languageKnow = languageKnow;
        this.languageNew = languageNew;
    }

    public Lektion(String name, Language languageKnow, Language languageNew){
        this.name = name;
        this.languageKnow = languageKnow;
        this.languageNew = languageNew;
    }
}
