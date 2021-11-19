package de.luki2811.dev.vokabeltrainer

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // set Streak
        val streak = Streak(this)
        val textViewStreakTop = findViewById<TextView>(R.id.textViewMainStreakTop)
        val textViewStreakProgress = findViewById<TextView>(R.id.textViewMainProgressStreak)
        val textViewStreakBottom = findViewById<TextView>(R.id.textViewMainStreakBottom)
        val progressBarStreak = findViewById<ProgressBar>(R.id.progressBarFinishedLesson)
        progressBarStreak.max = streak.xpGoal
        progressBarStreak.progress = streak.xpReached
        textViewStreakTop.text = getString(R.string.streak_in_days, streak.length)
        textViewStreakProgress.text =
            getString(R.string.streak_have_of_goal, streak.xpReached, streak.xpGoal)
        if (streak.xpReached < streak.xpGoal) textViewStreakBottom.text = getString(
            R.string.streak_left_to_do_for_next_day,
            streak.xpGoal - streak.xpReached,
            streak.length + 1
        ) else textViewStreakBottom.setText(R.string.streak_reached_goal)
        val indexFile = File(applicationContext.filesDir, Datei.NAME_FILE_INDEX)
        if (indexFile.exists()) {
            val indexDatei = Datei(Datei.NAME_FILE_INDEX)
            try {
                val indexJson = JSONObject(indexDatei.loadFromFile(this))
                val indexArrayJson = indexJson.getJSONArray("index")
                val layout = findViewById<LinearLayout>(R.id.cardsLayoutHome)
                for (i in 0..indexArrayJson.length() - 1) {
                    val layoutparams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutparams.bottomMargin = 25
                    layoutparams.rightMargin = 25
                    layoutparams.leftMargin = 25
                    layoutparams.topMargin = 25
                    val cardView = CardView(this)
                    cardView.layoutParams = layoutparams
                    cardView.radius = 25f
                    cardView.setContentPadding(10, 10, 10, 10)
                    cardView.setCardBackgroundColor(getColor(R.color.Aquamarine))
                    cardView.cardElevation = 3f
                    cardView.maxCardElevation = 5f
                    layout.addView(cardView)
                    val textInCard = TextView(this)
                    textInCard.id = i + 2000
                    val layoutparamsText = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                    textInCard.setPadding(10, 10, 10, 10)
                    textInCard.layoutParams = layoutparamsText
                    try {
                        textInCard.text = indexArrayJson.getJSONObject(i).getString("name")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    textInCard.setTextColor(Color.WHITE)
                    textInCard.gravity = Gravity.TOP

                    // H+W for icons
                    val layoutparamsIcons = RelativeLayout.LayoutParams(100, 100)

                    // Delete button
                    val delete = ImageButton(this)
                    delete.setBackgroundResource(R.drawable.rounded_red_button)
                    delete.setImageDrawable(
                        ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.outline_delete_24
                        )
                    )
                    delete.layoutParams = layoutparamsIcons
                    delete.setOnClickListener { view: View? ->
                        AlertDialog.Builder(this)
                            .setTitle("")
                            .setMessage("Möchtest du wirklich die Lektion löschen ??")
                            .setIcon(R.drawable.outline_delete_24)
                            .setPositiveButton(R.string.delete) { dialogInterface: DialogInterface?, i1: Int ->
                                val file = File(
                                    applicationContext.filesDir,
                                    textInCard.text.toString() + ".json"
                                )
                                if (file.exists()) {
                                    val deleted = file.delete()
                                    if (deleted) {
                                        try {
                                            for (i2 in 0..indexArrayJson.length() - 1) {
                                                if (indexArrayJson.getJSONObject(i2)
                                                        .getString("name")
                                                        .contentEquals(textInCard.text)
                                                ) {
                                                    indexArrayJson.remove(i2)
                                                    indexJson.put("index", indexArrayJson)
                                                    indexDatei.writeInFile(
                                                        indexJson.toString(),
                                                        applicationContext
                                                    )
                                                    cardView.visibility = View.INVISIBLE
                                                    cardView.layoutParams =
                                                        LinearLayout.LayoutParams(0, 0)
                                                    Toast.makeText(
                                                        applicationContext,
                                                        getString(R.string.deleted_succesfull),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        } catch (e: JSONException) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                            .setNegativeButton(R.string.cancel) { dialogInterface: DialogInterface?, i12: Int ->
                                Toast.makeText(
                                    applicationContext,
                                    getString(R.string.cancel),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .show()
                    }
                    // Edit Button
                    val cardEdit = ImageButton(this)
                    cardEdit.setBackgroundResource(R.drawable.rounded_blue_button)
                    cardEdit.setImageDrawable(
                        ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.ic_outline_edit_24
                        )
                    )
                    cardEdit.layoutParams = layoutparamsIcons
                    cardEdit.setOnClickListener { view: View? ->
                        val intent = Intent(this, EditLessonActivity::class.java)
                        intent.putExtra(LEKTION_NAME, textInCard.text)
                        startActivity(intent)
                    }
                    // Export Button
                    val exportButtonCard = ImageButton(this)
                    exportButtonCard.setBackgroundResource(R.drawable.rounded_orange_button)
                    exportButtonCard.setImageDrawable(
                        ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.ic_baseline_share_24
                        )
                    )
                    exportButtonCard.layoutParams = layoutparamsIcons
                    exportButtonCard.setOnClickListener { view: View? ->
                        val sharingIntent = Intent(Intent.ACTION_SEND)
                        val fileUri = FileProvider.getUriForFile(
                            this, this.applicationContext.packageName + ".provider", File(
                                applicationContext.filesDir, textInCard.text.toString() + ".json"
                            )
                        )
                        sharingIntent.type = "application/json"
                        sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
                        startActivity(Intent.createChooser(sharingIntent, "Lektion teilen mit"))
                    }


                    // Practice Button
                    val cardLearnButton = MaterialButton(this, null, R.attr.borderlessButtonStyle)
                    cardLearnButton.setText(R.string.practice)
                    cardLearnButton.setBackgroundDrawable(getDrawable(R.drawable.outline_button))
                    cardLearnButton.cornerRadius = 100
                    cardLearnButton.setOnClickListener { view: View? ->
                        val intent = Intent(this, PracticeVocabularyActivity::class.java)
                        intent.putExtra(LEKTION_NAME, textInCard.text)
                        startActivity(intent)
                    }

                    // Add all to a Layout
                    // TEMP without structure
                    val cardLayout = LinearLayout(this)
                    cardLayout.addView(delete)
                    cardLayout.addView(cardEdit)
                    cardLayout.addView(exportButtonCard)
                    cardLayout.addView(textInCard)
                    cardLayout.addView(cardLearnButton)
                    cardView.addView(cardLayout)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    fun createNewLektion(view: View?) {
        val intent = Intent(this@MainActivity, NewLessonActivity::class.java)
        startActivity(intent)
    }

    companion object {
        const val LEKTION_NAME = "de.luki2811.dev.vokabeltrainer"

        /**
         * Rundet den übergebenen Wert auf die Anzahl der übergebenen Nachkommastellen
         *
         * @param value ist der zu rundende Wert.
         * @param decimalPoints ist die Anzahl der Nachkommastellen, auf die gerundet werden soll.
         */
        fun round(value: Double, decimalPoints: Int): Double {
            val d = Math.pow(10.0, decimalPoints.toDouble())
            return Math.round(value * d) / d
        }
    }
}