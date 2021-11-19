package de.luki2811.dev.vokabeltrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class EditLessonActivity extends AppCompatActivity {

    Lesson lesson;
    int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_lesson);

        Intent comingInt = getIntent();
        String lessonName = comingInt.getStringExtra(MainActivity.LEKTION_NAME);

        Datei datei = new Datei(lessonName + ".json");
        try {
            JSONObject lektionAsJSON = new JSONObject(datei.loadFromFile(this));
            lesson = new Lesson(lektionAsJSON);

        }catch (JSONException e){
            e.printStackTrace();
        }

        counter = 1;
        final TextView textViewKnown = findViewById(R.id.textViewEditLessonKnown);
        final TextView textViewNew = findViewById(R.id.textViewEditLessonNew);
        final TextView textViewTop = findViewById(R.id.textViewEditLessonTop);

        textViewKnown.setText(getString(R.string.voc_in, lesson.getLanguageKnow().getName()));
        textViewNew.setText(getString(R.string.voc_in, lesson.getLanguageNew().getName()));
        textViewTop.setText(getString(R.string.number_voc_of_rest,counter, lesson.getCount()));

        final EditText editTextKnown = findViewById(R.id.editTextEditLessonKnown);
        final EditText editTextNew = findViewById(R.id.editTextEditLessonNew);
        final Switch switchIgnoreCase = findViewById(R.id.switchEditLektionIgnoreCase);

        editTextKnown.setText(lesson.getWordAtPos(counter-1).getKnownWord());
        editTextKnown.setHint(lesson.getWordAtPos(counter-1).getKnownWord());
        editTextNew.setText(lesson.getWordAtPos(counter-1).getNewWord());
        editTextNew.setHint(lesson.getWordAtPos(counter-1).getNewWord());

        switchIgnoreCase.setChecked(lesson.getWordAtPos(counter-1).isIgnoreCase());

    }

    public void saveAndNext(View view){

        // Speichern der Vokabel in der Datei
        final EditText editTextKnown = findViewById(R.id.editTextEditLessonKnown);
        final EditText editTextNew = findViewById(R.id.editTextEditLessonNew);
        final Switch switchIgnoreCase = findViewById(R.id.switchEditLektionIgnoreCase);
        final TextView textViewTop = findViewById(R.id.textViewEditLessonTop);

        if(editTextKnown.getText().toString().trim().isEmpty() || editTextNew.getText().toString().trim().isEmpty()){
            Toast.makeText(this, getText(R.string.err_missing_input),Toast.LENGTH_LONG).show();
            return;
        }

        VocabularyWord vocNew = new VocabularyWord(editTextKnown.getText().toString(), editTextNew.getText().toString(), switchIgnoreCase.isChecked());
        lesson.setWordAtPos(counter-1, vocNew);

        Datei datei = new Datei(lesson.getName() + ".json");
        datei.writeInFile(lesson.getLessonAsJson().toString(),this);

        counter = counter+1;

        if(counter <= lesson.getCount()){
            // UI ändern auf die nächste Vokabel

            editTextKnown.setText(lesson.getWordAtPos(counter-1).getKnownWord());
            editTextKnown.setHint(lesson.getWordAtPos(counter-1).getKnownWord());
            editTextNew.setText(lesson.getWordAtPos(counter-1).getNewWord());
            editTextNew.setHint(lesson.getWordAtPos(counter-1).getNewWord());

            switchIgnoreCase.setChecked(lesson.getWordAtPos(counter-1).isIgnoreCase());

            textViewTop.setText(getString(R.string.number_voc_of_rest,counter, lesson.getCount()));
        }else{
            startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    }

    public void cancelOnClick(View view){
        startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

}