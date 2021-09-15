package de.luki2811.dev.vokabeltrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class NewLektion extends AppCompatActivity {

    public static final String JSON_OBJECT = "de.luki2811.dev.vokabeltrainer.JSON_Object";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_lektion);

        RadioButton en_new = findViewById(R.id.radioButton_new_english);
        en_new.setChecked(true);
        RadioButton de_native = findViewById(R.id.radioButton_native_german);
        de_native.setChecked(true);
    }

    public void checkAndGoNext(View view) {
        EditText textName = findViewById(R.id.TextLektionName);
        RadioButton en_native = findViewById(R.id.radioButton_native_english);
        RadioButton de_native = findViewById(R.id.radioButton_native_german);
        RadioButton sv_native = findViewById(R.id.radioButton_native_swedish);
        RadioButton fr_native = findViewById(R.id.radioButton_native_french);

        RadioButton en_new = findViewById(R.id.radioButton_new_english);
        RadioButton de_new = findViewById(R.id.radioButton_new_german);
        RadioButton sv_new = findViewById(R.id.radioButton_new_swedish);
        RadioButton fr_new = findViewById(R.id.radioButton_new_french);


        if(textName.getText() == null || textName.getText().toString().trim().isEmpty()){
            Toast.makeText(this, getString(R.string.err_missing_name), Toast.LENGTH_SHORT).show();
        }else{
            File file = new File(getApplicationContext().getFilesDir(), textName.getText().toString().trim().toLowerCase() + ".json");
            if(file.exists())
                Toast.makeText(this,getString(R.string.err_name_not_avaible),Toast.LENGTH_SHORT).show();
            else{
                Datei datei = new Datei(textName.getText().toString().trim().toLowerCase() + ".json");
                JSONObject JSONFile = new JSONObject();
                try {
                    // Einstellungen der Lektion als .json
                    // Name
                    JSONFile.put("name", textName.getText().toString());
                    // Setzen der Variable für "count"
                    JSONFile.put("count", 0);

                    // Type Native Sprache
                    if(en_native.isChecked())
                        JSONFile.put("languageNative", Language.LANGUAGE_ENGLISH);
                    else if(de_native.isChecked())
                        JSONFile.put("languageNative", Language.LANGUAGE_GERMAN);
                    else if(fr_native.isChecked())
                        JSONFile.put("languageNative", Language.LANGUAGE_FRENCH);
                    else if(sv_native.isChecked())
                        JSONFile.put("languageNative", Language.LANGUAGE_SWEDISH);
                    else
                        Toast.makeText(this,getString(R.string.err_no_native_selected), Toast.LENGTH_LONG).show();
                    // Type neue Sprache
                    if(en_new.isChecked())
                        JSONFile.put("languageNew", Language.LANGUAGE_ENGLISH);
                    else if(de_new.isChecked())
                        JSONFile.put("languageNew", Language.LANGUAGE_GERMAN);
                    else if(fr_new.isChecked())
                        JSONFile.put("languageNew", Language.LANGUAGE_FRENCH);
                    else if(sv_new.isChecked())
                        JSONFile.put("languageNew", Language.LANGUAGE_SWEDISH);
                    else
                        Toast.makeText(this,getString(R.string.err_no_new_selected), Toast.LENGTH_LONG).show();
                }catch (JSONException e){
                    e.printStackTrace();
                }
                Intent intent = new Intent(this, CreateNewVokabelActivity.class);
                intent.putExtra(JSON_OBJECT, JSONFile.toString());
                startActivity(intent);
            }
        }

    }


}