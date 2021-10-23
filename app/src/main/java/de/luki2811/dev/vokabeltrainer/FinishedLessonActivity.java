package de.luki2811.dev.vokabeltrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FinishedLessonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished_lesson);

        int counterRest = getIntent().getIntExtra("counterRest",1);
        int correctInPerCent = (int) MainActivity.round((((double) 10/counterRest)*100),0);

        ProgressBar progressBar = findViewById(R.id.progressBarFinishedLesson);
        TextView progressAsText = findViewById(R.id.textViewProgressBar);

        progressBar.setProgress(correctInPerCent);
        System.out.println(correctInPerCent);
        progressAsText.setText(getString(R.string.correct_in_percent,correctInPerCent));
    }
}