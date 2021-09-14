package de.luki2811.dev.vokabeltrainer;

public class Language {
    String name;
    int type;

    final static int LANGUAGE_ENGLISH = 0;
    final static int LANGUAGE_GERMAN = 1;
    final static int LANGUAGE_FRENCH = 2;
    final static int LANGUAGE_SWEDISH = 3;

    public Language(String name, int type){
        this.name = name;
        this.type = type;
    }

    public int getType(){
        return type;
    }

    public String getName() {
        return name;
    }
}
