package de.luki2811.dev.vokabeltrainer;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class NewLessonActivity extends AppCompatActivity {

    public static final String JSON_OBJECT = "de.luki2811.dev.vokabeltrainer.JSON_Object";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_lesson);
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

            File indexFile = new File(getApplicationContext().getFilesDir(), Datei.NAME_FILE_INDEX);
            Datei indexDatei = new Datei(Datei.NAME_FILE_INDEX);
            if(indexFile.exists()){
                if(textName.getText().toString().trim().contains("/") ||
                        textName.getText().toString().trim().contains("<") ||
                        textName.getText().toString().trim().contains(">") ||
                        textName.getText().toString().trim().contains("\\") ||
                        textName.getText().toString().trim().contains("|") ||
                        textName.getText().toString().trim().contains("*") ||
                        textName.getText().toString().trim().contains(":") ||
                        textName.getText().toString().trim().contains("\"") ||
                        textName.getText().toString().trim().contains("?")
                ){
                    Toast.makeText(this,getString(R.string.err_name_contains_wrong_letter),Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    JSONObject indexJson = new JSONObject(indexDatei.loadFromFile(this));
                    JSONArray indexArray = indexJson.getJSONArray("index");
                    for(int i = 0; i <= indexArray.length() - 1; i++){
                        if(indexArray.getJSONObject(i).getString("name").equals(textName.getText().toString().trim())
                        || textName.getText().toString().trim().equalsIgnoreCase("streak")
                        || textName.getText().toString().trim().equalsIgnoreCase("settings")
                        || textName.getText().toString().trim().equalsIgnoreCase("indexLections")){
                            Toast.makeText(this,getString(R.string.err_name_not_avaible),Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            JSONObject JSONFile = new JSONObject();
            try {
                // Einstellungen der Lektion als .json
                // Name
                JSONFile.put("name", textName.getText().toString());
                // Setzen der Variable für "count"
                JSONFile.put("count", 0);
                // Type Native Sprache
                if(en_native.isChecked())
                    JSONFile.put("languageNative", Language.ENGLISH);
                else if(de_native.isChecked())
                    JSONFile.put("languageNative", Language.GERMAN);
                else if(fr_native.isChecked())
                    JSONFile.put("languageNative", Language.FRENCH);
                else if(sv_native.isChecked())
                    JSONFile.put("languageNative", Language.SWEDISH);
                else
                    Toast.makeText(this,getString(R.string.err_no_native_selected), Toast.LENGTH_LONG).show();
                // Type neue Sprache
                if(en_new.isChecked())
                    JSONFile.put("languageNew", Language.ENGLISH);
                else if(de_new.isChecked())
                    JSONFile.put("languageNew", Language.GERMAN);
                else if(fr_new.isChecked())
                    JSONFile.put("languageNew", Language.FRENCH);
                else if(sv_new.isChecked())
                    JSONFile.put("languageNew", Language.SWEDISH);
                else
                    Toast.makeText(this,getString(R.string.err_no_new_selected), Toast.LENGTH_LONG).show();
            }catch (JSONException e){
                e.printStackTrace();
            }
            Intent intent = new Intent(this, CreateNewVocabularyActivity.class);
            intent.putExtra(JSON_OBJECT, JSONFile.toString());
            startActivity(intent);

        }

    }

    int requestCode = 1;

    public void importLesson(View view){
        if(checkPermission()){
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
            chooseFile.setType("application/json");
            startActivityForResult(chooseFile, requestCode);
        }else
            requestPermission();

    }

    int PERMISSION_REQUEST_CODE = 100;

    private boolean checkPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(NewLessonActivity.this, READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(NewLessonActivity.this, WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(NewLessonActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
                    chooseFile.setType("application/json");
                    startActivityForResult(chooseFile, requestCode);
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }
            } else if (grantResults.length > 0) {
                boolean READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) {
                    Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
                    chooseFile.setType("application/json");
                    startActivityForResult(chooseFile, requestCode);
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode,data);
        if(resultCode == Activity.RESULT_OK){
            if(data == null){
                return;
            }
            Uri uri = data.getData();
            File file = new File(RealPathUtil.getRealPath(this, uri));
            Datei datei = new Datei(file.getName());

            // Create new Lesson

            JSONObject lessonAsJSON = null;
            try {
                lessonAsJSON = new JSONObject(datei.loadFromFile(file));
            }catch (JSONException e){
                e.printStackTrace();
            }
            if(lessonAsJSON == null){
                Toast.makeText(this, getString(R.string.err_could_not_import_lesson) , Toast.LENGTH_LONG).show();
                return;
            }
            Lesson lesson = new Lesson(lessonAsJSON);

            // Check if lesson is correct
            File indexFile = new File(getApplicationContext().getFilesDir(), Datei.NAME_FILE_INDEX);
            Datei indexDatei = new Datei(indexFile.getName());

            if(!indexFile.exists()){
                indexDatei.writeInFile("",this);
            }

            if(lesson.getName().contains("/") ||
                    lesson.getName().contains("<") ||
                    lesson.getName().contains(">") ||
                    lesson.getName().contains("\\") ||
                    lesson.getName().contains("|") ||
                    lesson.getName().contains("*") ||
                    lesson.getName().contains(":") ||
                    lesson.getName().contains("\"") ||
                    lesson.getName().contains("?")
            ){
                Toast.makeText(this,getString(R.string.err_name_contains_wrong_letter),Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject indexJson = new JSONObject(indexDatei.loadFromFile(this));
                JSONArray indexArray = indexJson.getJSONArray("index");
                for(int i = 0; i <= indexArray.length() - 1; i++){
                    if(indexArray.getJSONObject(i).getString("name").equals(lesson.getName())
                            || lesson.getName().equalsIgnoreCase("streak")
                            || lesson.getName().equalsIgnoreCase("settings")
                            || lesson.getName().equalsIgnoreCase("indexLections")){
                        Toast.makeText(this,getString(R.string.err_name_not_avaible),Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Create index
            JSONObject indexAsJson;
            JSONArray jsonArray = null;

            if(indexFile.exists()){
                try {
                    indexAsJson = new JSONObject(indexDatei.loadFromFile(this));
                    jsonArray = indexAsJson.getJSONArray("index");
                } catch (JSONException e) {
                    e.printStackTrace();
                    indexAsJson = new JSONObject();
                }
            }
            else
                indexAsJson = new JSONObject();
            try {
                if(jsonArray == null)
                    jsonArray = new JSONArray();
                JSONObject jo = new JSONObject();
                jo.put("name", lesson.getName());
                jo.put("file", lesson.getName() + ".json");
                jsonArray.put(jo);
                indexAsJson.put("index", jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            indexDatei.writeInFile(indexAsJson.toString(), this);

            // Save lesson as .json

            Datei saveDatei = new Datei(lesson.getName()+ ".json");
            saveDatei.writeInFile(lesson.getLessonAsJson().toString(), this);

            Toast.makeText(this, getString(R.string.import_lesson_successful), Toast.LENGTH_LONG).show();

            startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }else{
            Toast.makeText(NewLessonActivity.this, getString(R.string.err), Toast.LENGTH_LONG).show();
        }
    }
}