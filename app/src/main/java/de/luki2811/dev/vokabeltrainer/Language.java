package de.luki2811.dev.vokabeltrainer;

public class Language {
    String name;
    int type;

    final static int ENGLISH = 0;
    final static int GERMAN = 1;
    final static int FRENCH = 2;
    final static int SWEDISH = 3;

    public Language(int type){
        this.type = type;
    }

    public int getType(){
        return type;
    }

    public String getName() {
        switch (getType()){
            case 0:
                return "Englisch";
            case 1:
                return "Deutsch";
            case 2:
                return "Französisch";
            case 3:
                return "Schwedisch";
            default:
                return null;
        }
    }
}
