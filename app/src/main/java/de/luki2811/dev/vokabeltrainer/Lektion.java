package de.luki2811.dev.vokabeltrainer;

public class Lektion {
    String name;
    Language languageKnow;
    Language languageNew;
    Vokabel[] vocs;
    int count;

    public String getName() {
        return name;
    }

    public Lektion(String name, int count, Language languageKnow, Language languageNew, Vokabel[] vocs){
        this.name = name;
        this.languageKnow = languageKnow;
        this.languageNew = languageNew;
        this.vocs = vocs;
        this.count = count;
    }

    public Vokabel getRandomVokabel(){
        int random = (int)(Math.random()*count+1);
        System.out.println("1: " + (random-1));
        return vocs[(random-1)];
    }

    public Vokabel getVokabelAtPos(int pos){
        return vocs[pos];
    }

    public Language getLanguageKnow() {
        return languageKnow;
    }

    public Language getLanguageNew() {
        return languageNew;
    }

    //public void getDataFromJson(){
    //    Datei file = new Datei(name + ".json");
    //    try {
    //        JSONObject json = new JSONObject(file.loadFromFile(file.getApplicationContext()));
    //        switch (json.getInt("languageNative")){
    //            case Language.LANGUAGE_ENGLISH:
    //                this.languageKnow = new Language("Englisch", Language.LANGUAGE_ENGLISH);
    //
    //                case Language.LANGUAGE_GERMAN:
    //
    //                    this.languageKnow = new Language("Deutsch", Language.LANGUAGE_GERMAN);
    //
    //
    //        }
    //
    //
    //
    //
    //    } catch (JSONException e) {
    //        e.printStackTrace();
    //    }
    //}
}
