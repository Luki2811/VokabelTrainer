package de.luki2811.dev.vokabeltrainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Lesson {
    String name;
    Language languageKnow;
    Language languageNew;
    VocabularyWord[] vocs;
    int count;

    public String getName() {
        return name;
    }

    public Lesson(String name, int count, Language languageKnow, Language languageNew, VocabularyWord[] vocs){
        this.name = name;
        this.languageKnow = languageKnow;
        this.languageNew = languageNew;
        this.vocs = vocs;
        this.count = count;
    }

    public Lesson(JSONObject json){
        try {
            name = json.getString("name");
            languageKnow = new Language(json.getInt("languageNative"));
            languageNew = new Language(json.getInt("languageNew"));

            vocs = new VocabularyWord[json.getJSONArray("vocabulary").length()];
            for (int i = 0; i < json.getJSONArray("vocabulary").length(); i++) {
                vocs[i] = new VocabularyWord(
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

    public VocabularyWord getRandomWord(){
        int random = (int)(Math.random()*count+1);
        return vocs[(random-1)];
    }

    public VocabularyWord getWordAtPos(int pos){
        return vocs[pos];
    }

    public void setWordAtPos(int pos, VocabularyWord voc){
        vocs[pos] = voc;
    }

    public int getCount() {
        return count;
    }


    public JSONObject getLessonAsJson(){
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
}
