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

public class CreateNewVokabelActivity extends AppCompatActivity {

    public static final String JSON_VOCABULARY = "de.luki2811.dev.vokabeltrainer.JSON_VOCABULARY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_vokabel);

        Button button_finish = findViewById(R.id.button_vokabel_next);
        Switch switchSetting = findViewById(R.id.switch_settings_ignoreCase);

        Intent intent = getIntent();
        try {
            JSONObject jsonObject;
            if(intent.getStringExtra(CreateNewVokabelActivity.JSON_VOCABULARY) != null){
                jsonObject = new JSONObject(intent.getStringExtra(CreateNewVokabelActivity.JSON_VOCABULARY));
                JSONArray jsonArray = jsonObject.getJSONArray("vocabulary");
                switchSetting.setChecked(jsonArray.getJSONObject(jsonArray.length() - 1).getBoolean("ignoreCase"));
            }
            else
                jsonObject = new JSONObject(intent.getStringExtra(NewLektion.JSON_OBJECT));

            if(jsonObject.getInt("count") >= 10)
                button_finish.setEnabled(true);

            TextView output = findViewById(R.id.outputCreateVocabulary);
            output.setText(getString(R.string.from_at_least_ten_vocs, jsonObject.getInt("count")));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void finishVocActivity(View view){
        Intent intent = getIntent();
        try{
            JSONObject json = new JSONObject(intent.getStringExtra(CreateNewVokabelActivity.JSON_VOCABULARY));
            Datei file = new Datei(json.getString("name") + ".json");
            file.writeInFile(json.toString(), this);
            Intent sendIntent = new Intent(this, MainActivity.class);
            startActivity(sendIntent);

        } catch (JSONException e){
            e.printStackTrace();
        }

    }

    private JSONObject getEnteredVocabulary(){
        EditText newVoc = findViewById(R.id.editTextNewLanguageVokabel);
        EditText nativeVoc = findViewById(R.id.editTextNativeLanguageVokabel);
        Switch switchSetting = findViewById(R.id.switch_settings_ignoreCase);

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

    public void createAndRestartActivity(View view){
        int count = 0;
        Intent intent = getIntent();

        JSONObject enteredVocabulary = getEnteredVocabulary();
        if(enteredVocabulary == null)
            return;

        JSONObject jsonObject = null;

        try {
            if(intent.getStringExtra(CreateNewVokabelActivity.JSON_VOCABULARY) != null)
                jsonObject = new JSONObject(intent.getStringExtra(CreateNewVokabelActivity.JSON_VOCABULARY));
            else
                jsonObject = new JSONObject(intent.getStringExtra(NewLektion.JSON_OBJECT));
            System.out.println(jsonObject.toString());
            count = jsonObject.getInt("count") + 1;
            JSONArray jsonArray;
            try {
                jsonArray = jsonObject.getJSONArray("vocabulary");
            } catch (JSONException e){
                jsonArray = new JSONArray();
                e.printStackTrace();
            }
            jsonArray.put(enteredVocabulary);
            jsonObject.put("vocabulary", jsonArray);
            jsonObject.put("count", count);
            System.out.println(jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent newIntent = new Intent(this, CreateNewVokabelActivity.class);
        if (jsonObject != null) {
            newIntent.putExtra(JSON_VOCABULARY, jsonObject.toString());
        } else
            Toast.makeText(this, getString(R.string.err), Toast.LENGTH_LONG).show();
        startActivity(newIntent);
    }
}