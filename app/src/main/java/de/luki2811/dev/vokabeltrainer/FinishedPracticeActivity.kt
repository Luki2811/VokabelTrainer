package de.luki2811.dev.vokabeltrainer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import de.luki2811.dev.vokabeltrainer.R
import android.widget.TextView
import de.luki2811.dev.vokabeltrainer.MainActivity
import android.widget.ProgressBar
import de.luki2811.dev.vokabeltrainer.Streak
import android.content.Intent
import android.view.View

class FinishedPracticeActivity : AppCompatActivity() {

    //TODO: Als Fragment ersetzen und überarbeiten

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finished_practice)
        val textViewFinXP = findViewById<TextView>(R.id.textViewFinishedXP)
        val counterRest = intent.getIntExtra("counterRest", 1)
        val correctInPerCent = MainActivity.round(10.toDouble() / counterRest * 100, 0)
            .toInt()
        val progressBar = findViewById<ProgressBar>(R.id.progressBarFinishedLesson)
        val progressAsText = findViewById<TextView>(R.id.textViewProgressBar)
        progressBar.progress = correctInPerCent
        progressAsText.text = getString(R.string.correct_in_percent, correctInPerCent)
        val streak = Streak(this)
        // Basic XP (1XP for 1Word)
        streak.addXP(10)
        // MAX 5 extra XP (decrease 1 XP for each mistake)
        if (5 - (counterRest - 10) >= 0) {
            streak.addXP(5 - (counterRest - 10))
            textViewFinXP.text = getString(R.string.xp_reached, 10, 5 - (counterRest - 10))
        } else textViewFinXP.text = getString(R.string.xp_reached, 10, 0)
    }

    fun goNext(view: View?) {
        val intent = Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}