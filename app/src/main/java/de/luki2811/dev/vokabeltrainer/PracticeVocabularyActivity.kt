package de.luki2811.dev.vokabeltrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class PracticeVocabularyActivity extends AppCompatActivity {

    private Lesson lesson;
    private VocabularyWord voc;
    private int counter = 0;
    private int counterRest = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_vocabulary);

        ImageButton closeLessonImageButton = findViewById(R.id.imageButtonCloseLesson);
        closeLessonImageButton.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("").setMessage("Möchtest du wirklich die Übung verlassen ??")
                .setIcon(R.drawable.ic_baseline_close_24)
                .setPositiveButton("Verlassen", (dialogInterface, i1) -> {
                    Intent intent = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                })
                .setNegativeButton("Zurück zur Übung", null).show());

        Intent comingInt = getIntent();
        String lektionName = comingInt.getStringExtra(MainActivity.LEKTION_NAME);

        Datei datei = new Datei(lektionName + ".json");
        try {
            JSONObject lektionAsJSON = new JSONObject(datei.loadFromFile(this));
            Language nativeLan = new Language(lektionAsJSON.getInt("languageNative"));
            Language newLan = new Language(lektionAsJSON.getInt("languageNew"));

            VocabularyWord[] vocabulary = new VocabularyWord[lektionAsJSON.getJSONArray("vocabulary").length()];
            for(int i = 0; i < lektionAsJSON.getJSONArray("vocabulary").length(); i++){
                vocabulary[i] = new VocabularyWord(
                    lektionAsJSON.getJSONArray("vocabulary").getJSONObject(i).getString("native"),
                    lektionAsJSON.getJSONArray("vocabulary").getJSONObject(i).getString("new"),
                    lektionAsJSON.getJSONArray("vocabulary").getJSONObject(i).getBoolean("ignoreCase")
                );
            }

            int countForLektion = lektionAsJSON.getInt("count");

            lesson = new Lesson(lektionAsJSON.getString("name"),countForLektion,nativeLan,newLan,vocabulary);

            TextView lessonTranslateTo = findViewById(R.id.lessonTranslateTo);
            TextView lessonTranslateFrom = findViewById(R.id.lessonTranslateFrom);

            lessonTranslateFrom.setText(lesson.getLanguageKnow().getName());
            lessonTranslateTo.setText(lesson.getLanguageNew().getName());

            voc = lesson.getRandomWord();

            TextView textViewToTranslate = findViewById(R.id.textViewLessonToTranslate);
            textViewToTranslate.setText(voc.getKnownWord());


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void checkVoc(View view){
        TextView correctionTextView = findViewById(R.id.textViewLessonCorrection);
        EditText inputAnswer = findViewById(R.id.editTextTranslatedInput);
        Button buttonCheck = findViewById(R.id.buttonCheckLesson);

        if(voc.isIgnoreCase()){
            if(inputAnswer.getText().toString().trim().equalsIgnoreCase(voc.getNewWord())){
                correctionTextView.setText(R.string.correct);
                voc.setWrong(false);
            }else{
                correctionTextView.setText(getString(R.string.wrong_the_correct_solution_is, voc.getNewWord()));
                counterRest = counterRest + 1;
                voc.setWrong(true);
            }
        }else{
            if(inputAnswer.getText().toString().trim().equals(voc.getNewWord())){
                correctionTextView.setText(R.string.correct);
                voc.setWrong(false);
            }
            else{
                correctionTextView.setText(getString(R.string.wrong_the_correct_solution_is, voc.getNewWord()));
                counterRest = counterRest + 1;
                voc.setWrong(true);
            }
        }
        counter = counter + 1;
        voc.setUsed(true);
        buttonCheck.setText(R.string.next);
        ProgressBar progressBar = findViewById(R.id.progressBarLesson);
        double progress = ((double) counter/counterRest)*100;
        progressBar.setProgress((int) progress, true);
        if(counter < counterRest){
            buttonCheck.setOnClickListener(v -> continueToNext());
        }else {
            buttonCheck.setOnClickListener(v -> {
                Intent intent = new Intent(this, FinishedPracticeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("counterRest",counterRest);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }
    }

    private void continueToNext(){
        TextView correctionTextView = findViewById(R.id.textViewLessonCorrection);
        TextView textViewToTranslate = findViewById(R.id.textViewLessonToTranslate);
        EditText inputAnswer = findViewById(R.id.editTextTranslatedInput);
        Button buttonCheck = findViewById(R.id.buttonCheckLesson);

        correctionTextView.setText("");
        inputAnswer.setText("");
        buttonCheck.setText(R.string.check);
        buttonCheck.setOnClickListener(v -> checkVoc(v));


        VocabularyWord newVoc = lesson.getRandomWord();
        if(counter < 10){
            while(voc.equals(newVoc) || newVoc.isAlreadyUsed())
                newVoc = lesson.getRandomWord();
            voc = newVoc;
        }else
            while(!newVoc.isWrong())

        newVoc = lesson.getRandomWord();
        voc = newVoc;
        textViewToTranslate.setText(voc.getKnownWord());
    }

}