package de.luki2811.dev.vokabeltrainer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Lektion {
    String name;
    Language languageKnow;
    Language languageNew;
    Vokabel[] vocs;

    public String getName() {
        return name;
    }

    public Lektion(String name, Language languageKnow, Language languageNew, Vokabel[] vocs){
        this.name = name;
        this.languageKnow = languageKnow;
        this.languageNew = languageNew;
        this.vocs = vocs;
    }

    public Lektion(String name, Language languageKnow, Language languageNew){
        this.name = name;
        this.languageKnow = languageKnow;
        this.languageNew = languageNew;
    }

    public Lektion(String name){
        this.name = name;
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
