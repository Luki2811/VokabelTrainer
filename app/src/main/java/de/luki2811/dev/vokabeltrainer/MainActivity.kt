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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.setPadding
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
                for (i in 0 until indexArrayJson.length()) {
                    val layoutparams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutparams.bottomMargin = 5
                    layoutparams.rightMargin = 25
                    layoutparams.leftMargin = 25
                    layoutparams.topMargin = 25
                    val cardView = MaterialCardView(this)
                    cardView.layoutParams = layoutparams
                    cardView.radius = 25f
                    cardView.setContentPadding(10, 10, 10, 10)
                    cardView.setCardBackgroundColor(Color.WHITE)
                    cardView.cardElevation = 3f
                    cardView.maxCardElevation = 5f
                    layout.addView(cardView)

                    // TextView Name
                    val textInCard = TextView(this)
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
                    textInCard.setTextColor(Color.BLACK)
                    textInCard.textSize = 18F

                    // H+W for icons
                    val layoutparamsIcons = RelativeLayout.LayoutParams(100, 100)

                    // Delete button
                    val delete = ImageButton(this)
                    delete.id = View.generateViewId()
                    delete.setBackgroundResource(R.drawable.rounded_red_button)
                    delete.setImageDrawable(
                        ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.outline_delete_24
                        )
                    )
                    delete.layoutParams = layoutparamsIcons
                    delete.setPadding(10, 10, 10, 10)
                    delete.setOnClickListener { view: View? ->
                        MaterialAlertDialogBuilder(this)
                            .setTitle("")
                            .setMessage("Möchtest du wirklich die Lektion löschen ??")
                            .setPositiveButton(R.string.delete) { dialogInterface: DialogInterface?, i1: Int ->
                                val file = File(
                                    applicationContext.filesDir,
                                    textInCard.text.toString() + ".json"
                                )
                                if (file.exists()) {
                                    val deleted = file.delete()
                                    if (deleted) {
                                        try {
                                            for (i2 in 0 until indexArrayJson.length()) {
                                                if (indexArrayJson.getJSONObject(i2).getString("name").contentEquals(textInCard.text)
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
                    val edit = ImageButton(this)
                    delete.id = View.generateViewId()
                    edit.setBackgroundResource(R.drawable.rounded_blue_button)
                    edit.setImageDrawable(
                        ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.ic_outline_edit_24
                        )
                    )
                    edit.layoutParams = layoutparamsIcons
                    edit.setOnClickListener { view: View? ->
                        val intent = Intent(this, EditLessonActivity::class.java)
                        intent.putExtra(LEKTION_NAME, textInCard.text)
                        startActivity(intent)
                    }
                    // Export Button
                    val export = ImageButton(this)
                    export.id = View.generateViewId()
                    export.setBackgroundResource(R.drawable.rounded_orange_button)
                    export.setImageDrawable(
                        ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.ic_baseline_share_24
                        )
                    )
                    export.layoutParams = layoutparamsIcons
                    export.setOnClickListener { view: View? ->
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
                    val cardLearnButton = MaterialButton(this, null, R.attr.materialButtonOutlinedStyle)
                    cardLearnButton.setText(R.string.practice)
                    cardLearnButton.setTextColor(getColor(R.color.Black))
                    cardLearnButton.cornerRadius = 100
                    cardLearnButton.setOnClickListener { view: View? ->
                        val intent = Intent(this, PracticeVocabularyActivity::class.java)
                        intent.putExtra(LEKTION_NAME, textInCard.text)
                        startActivity(intent)
                    }

                    // Add all to several Layouts

                    val cardLayout = LinearLayout(this)
                    cardLayout.orientation = LinearLayout.VERTICAL

                    val deleteIconLayout = LinearLayout(this)
                    deleteIconLayout.addView(delete)
                    deleteIconLayout.setPadding(0,0,7,0)
                    val editIconLayout = LinearLayout(this)
                    editIconLayout.addView(edit)
                    editIconLayout.setPadding(0,0,7,0)
                    val exportIconLayout = LinearLayout(this)
                    exportIconLayout.addView(export)
                    exportIconLayout.setPadding(0,0,7,0)

                    val iconsLayout = LinearLayout(this)
                    iconsLayout.addView(exportIconLayout)
                    iconsLayout.addView(editIconLayout)
                    iconsLayout.addView(deleteIconLayout)
                    iconsLayout.gravity = Gravity.END

                    val textLayout = LinearLayout(this)
                    textLayout.setPadding(5)

                    val buttonLayout = LinearLayout(this)
                    buttonLayout.gravity = Gravity.CENTER_HORIZONTAL


                    buttonLayout.addView(cardLearnButton)



                    textLayout.addView(textInCard)

                    cardLayout.addView(iconsLayout)
                    cardLayout.addView(textLayout)
                    cardLayout.addView(buttonLayout)
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