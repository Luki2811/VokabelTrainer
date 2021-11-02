package de.luki2811.dev.vokabeltrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class CreateNewVokabelActivity extends AppCompatActivity {

    private JSONObject allForLesson;
    private JSONArray allVoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_vokabel);

        Intent intent = getIntent();
        try {
            final TextView output = findViewById(R.id.outputCreateVocabulary);
            allForLesson = new JSONObject(intent.getStringExtra(NewLessonActivity.JSON_OBJECT));
            output.setText(getString(R.string.from_at_least_ten_vocs, allForLesson.getInt("count")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void finishVocActivity(View view){
        JSONObject json = null;
        try{
            json = allForLesson.put("vocabulary",allVoc);
            Datei file = new Datei(json.getString("name") + ".json");
            file.writeInFile(json.toString(), this);
        } catch (JSONException e){
            e.printStackTrace();
        }
        // Create new index

        File file = new File(getApplicationContext().getFilesDir(),"indexLections.json");
        JSONObject indexAsJson;
        JSONArray jsonArray = null;
        Datei index = new Datei("indexLections.json");
        if(file.exists()){
            try {
                indexAsJson = new JSONObject(index.loadFromFile(this));
                jsonArray = indexAsJson.getJSONArray("index");
            } catch (JSONException e) {
                e.printStackTrace();
                indexAsJson = new JSONObject();
            }
        }
        else
            indexAsJson = new JSONObject();

        try {
            if (json != null) {
                if(jsonArray == null)
                    jsonArray = new JSONArray();
                JSONObject jo = new JSONObject();
                jo.put("name", json.getString("name"));
                jo.put("file",json.getString("name") + ".json");
                jsonArray.put(jo);
                indexAsJson.put("index", jsonArray);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        index.writeInFile(indexAsJson.toString(), this);

        // Zurück zur MainActivity (und Verlauf löschen, damit man nicht zurückgehen kann)
        Intent sendIntent = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(sendIntent);

    }

    private JSONObject getEnteredVocabulary(){
        final EditText newVoc = findViewById(R.id.editTextNewLanguageVokabel);
        final EditText nativeVoc = findViewById(R.id.editTextNativeLanguageVokabel);
        final Switch switchSetting = findViewById(R.id.switch_settings_ignoreCase);
        if(newVoc.getText().toString().trim().equals("") || nativeVoc.getText().toString().trim().equals("")){
            Toast.makeText(this, getString(R.string.err_missing_input), Toast.LENGTH_LONG).show();
            return null;
        }

        JSONObject vocabs = new JSONObject();
        try {
            vocabs.put("ignoreCase", switchSetting.isChecked());
            vocabs.put("new", newVoc.getText().toString().trim());
            vocabs.put("native", nativeVoc.getText().toString().trim());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return vocabs;
    }

    public void saveAndResetVocActivity(View view){

        final Switch switchSetting = findViewById(R.id.switch_settings_ignoreCase);
        final EditText newVoc = findViewById(R.id.editTextNewLanguageVokabel);
        final EditText nativeVoc = findViewById(R.id.editTextNativeLanguageVokabel);
        final TextView output = findViewById(R.id.outputCreateVocabulary);
        final Button button_finish = findViewById(R.id.button_vokabel_next);

        try {
            // Falls allVoc = null -> Object erstellen
            if(allVoc == null)
            allVoc = new JSONArray();
            // Neue Vokabel zur Sammlung hinzufügen
            if(getEnteredVocabulary() != null){
                allVoc.put(getEnteredVocabulary());
                // Zähler erhöhen
                allForLesson.put("count",allForLesson.getInt("count") + 1);
                // Zurücksetzen der Eingaben
                switchSetting.setChecked(allVoc.getJSONObject(allVoc.length() - 1).getBoolean("ignoreCase"));
                newVoc.setText("");
                nativeVoc.setText("");
            }
            // Aktualisieren des UI
            if(allForLesson.getInt("count") >= 10)
                button_finish.setEnabled(true);
            output.setText(getString(R.string.from_at_least_ten_vocs, allForLesson.getInt("count")));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}