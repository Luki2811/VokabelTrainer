package de.luki2811.dev.vokabeltrainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public Lektion(JSONObject json){
        try {
            name = json.getString("name");
            languageKnow = new Language(json.getInt("languageNative"));
            languageNew = new Language(json.getInt("languageNew"));

            vocs = new Vokabel[json.getJSONArray("vocabulary").length()];
            for (int i = 0; i < json.getJSONArray("vocabulary").length(); i++) {
                vocs[i] = new Vokabel(
                        json.getJSONArray("vocabulary").getJSONObject(i).getString("native"),
                        json.getJSONArray("vocabulary").getJSONObject(i).getString("new"),
                        json.getJSONArray("vocabulary").getJSONObject(i).getBoolean("ignoreCase")
                );
            }

            count = json.getInt("count");
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public Vokabel getRandomVokabel(){
        int random = (int)(Math.random()*count+1);
        return vocs[(random-1)];
    }

    public Vokabel getVokabelAtPos(int pos){
        return vocs[pos];
    }

    public void setVokabelAtPos(int pos, Vokabel voc){
        vocs[pos] = voc;
    }

    public int getCount() {
        return count;
    }


    public JSONObject getLektionAsJSON(){
        JSONObject lektionAsJSON = new JSONObject();
        try {
            lektionAsJSON.put("name", name);
            lektionAsJSON.put("count", count);
            lektionAsJSON.put("languageNative", languageKnow.getType());
            lektionAsJSON.put("languageNew", languageNew.getType());

            JSONArray jsonArray = new JSONArray();
            for(int i = 0; i < vocs.length; i++){
                JSONObject voc = new JSONObject();
                voc.put("ignoreCase", vocs[i].isIgnoreCase());
                voc.put("new", vocs[i].getNewWord());
                voc.put("native", vocs[i].getKnownWord());
                jsonArray.put(voc);
            }
            lektionAsJSON.put("vocabulary", jsonArray);
            return lektionAsJSON;

        }catch (JSONException e){
            e.printStackTrace();
            return null;
        }
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
