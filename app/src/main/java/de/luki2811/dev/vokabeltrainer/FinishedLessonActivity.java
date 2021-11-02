package de.luki2811.dev.vokabeltrainer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FinishedLessonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished_lesson);

        final TextView textViewFinXP = findViewById(R.id.textViewFinishedXP);

        int counterRest = getIntent().getIntExtra("counterRest",1);
        int correctInPerCent = (int) MainActivity.round((((double) 10/counterRest)*100),0);

        ProgressBar progressBar = findViewById(R.id.progressBarFinishedLesson);
        TextView progressAsText = findViewById(R.id.textViewProgressBar);

        progressBar.setProgress(correctInPerCent);
        progressAsText.setText(getString(R.string.correct_in_percent,correctInPerCent));

        Streak streak = new Streak(this);
        // Basic XP (1XP for 1Word)
        streak.addXP(10);
        // MAX 5 extra XP (decrease 1 XP for each mistake)
        if((5-(counterRest-10))>=0){
            streak.addXP(5-(counterRest-10));
            textViewFinXP.setText(getString(R.string.xp_reached, 10,5-(counterRest-10)));
        }else
            textViewFinXP.setText(getString(R.string.xp_reached, 10,0));
    }

    public void goNext(View view){
        Intent intent = new Intent(this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}