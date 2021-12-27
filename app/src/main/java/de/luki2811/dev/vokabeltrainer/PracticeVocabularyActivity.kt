package de.luki2811.dev.vokabeltrainer

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject

class PracticeVocabularyActivity : AppCompatActivity() {

    // TODO: Als Fragment hinzufügen und Überarbeiten
    // TODO: Weitere Typen hinzufügen zum Abfragen
    // TODO: Vokabelgruppen "importieren" anhand von ID zur Lektion hinzufügen

    private var lesson: Lesson? = null
    private var voc: VocabularyWord? = null
    private var counter = 0
    private var counterRest = 10
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice_vocabulary)
        val closeLessonImageButton = findViewById<ImageButton>(R.id.imageButtonCloseLesson)
        closeLessonImageButton.setOnClickListener { view: View? ->
            AlertDialog.Builder(this)
                .setTitle("").setMessage("Möchtest du wirklich die Übung verlassen ??")
                .setIcon(R.drawable.ic_baseline_close_24)
                .setPositiveButton("Verlassen") { dialogInterface: DialogInterface?, i1: Int ->
                    val intent = Intent(
                        this,
                        MainActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
                .setNegativeButton("Zurück zur Übung", null).show()
        }

        val comingInt = intent
        val lektionName = comingInt.getStringExtra(MainActivity.LEKTION_NAME)
        val datei = AppFile("$lektionName.json")
        try {
            val lektionAsJSON = JSONObject(datei.loadFromFile(this))
            val nativeLan = Language(lektionAsJSON.getInt("languageNative"))
            val newLan = Language(lektionAsJSON.getInt("languageNew"))
            val vocabulary = Array(lektionAsJSON.getJSONArray("vocabulary").length()){ VocabularyWord("","",false ) }
            for (i in 0 until lektionAsJSON.getJSONArray("vocabulary").length()) {
                vocabulary[i] = VocabularyWord(
                    lektionAsJSON.getJSONArray("vocabulary").getJSONObject(i).getString("native"),
                    lektionAsJSON.getJSONArray("vocabulary").getJSONObject(i).getString("new"),
                    lektionAsJSON.getJSONArray("vocabulary").getJSONObject(i)
                        .getBoolean("ignoreCase")
                )
            }
            val countForLektion = lektionAsJSON.getInt("count")
            lesson = Lesson(
                lektionAsJSON.getString("name"),
                countForLektion,
                nativeLan,
                newLan,
                vocabulary
            )
            val lessonTranslateTo = findViewById<TextView>(R.id.lessonTranslateTo)
            val lessonTranslateFrom = findViewById<TextView>(R.id.lessonTranslateFrom)
            lessonTranslateFrom.text = lesson!!.languageKnow.getName()
            lessonTranslateTo.text = lesson!!.languageNew.getName()
            voc = lesson!!.randomWord
            val textViewToTranslate = findViewById<TextView>(R.id.textViewLessonToTranslate)
            textViewToTranslate.text = voc!!.knownWord
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun checkVoc(view: View) {
        val correctionTextView = findViewById<TextView>(R.id.textViewLessonCorrection)
        val inputAnswer = findViewById<TextInputLayout>(R.id.editTextTranslatedInput)
        val buttonCheck = findViewById<Button>(R.id.buttonCheckLesson)

        if (inputAnswer.editText?.text.toString().trim { it <= ' ' }.equals(voc!!.newWord, ignoreCase = voc!!.isIgnoreCase)) {
            correctionTextView.setText(R.string.correct)
            voc!!.isWrong = false
            inputAnswer.isEnabled = false

        } else {
            correctionTextView.text =
                getString(R.string.wrong_the_correct_solution_is, voc!!.newWord)
            counterRest += 1
            voc!!.isWrong = true
        }

        counter += 1
        voc!!.isAlreadyUsed = true
        buttonCheck.setText(R.string.next)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarLesson)
        val progress = counter.toDouble() / counterRest * 100
        progressBar.setProgress(progress.toInt(), true)
        if (counter < counterRest) {
            buttonCheck.setOnClickListener { v: View? -> continueToNext() }
        } else {
            buttonCheck.setOnClickListener { v: View? ->
                val intent = Intent(
                    this,
                    FinishedPracticeActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("counterRest", counterRest)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
        }
    }

    private fun continueToNext() {
        val correctionTextView = findViewById<TextView>(R.id.textViewLessonCorrection)
        val textViewToTranslate = findViewById<TextView>(R.id.textViewLessonToTranslate)
        val inputAnswer = findViewById<TextInputLayout>(R.id.editTextTranslatedInput)
        val buttonCheck = findViewById<Button>(R.id.buttonCheckLesson)
        correctionTextView.text = ""
        inputAnswer.editText?.setText("")
        inputAnswer.isEnabled = true
        buttonCheck.setText(R.string.check)
        buttonCheck.setOnClickListener { v: View -> checkVoc(v) }
        var newVoc = lesson!!.randomWord
        if (counter < 10) {
            while (voc == newVoc || newVoc.isAlreadyUsed) newVoc = lesson!!.randomWord
            voc = newVoc
        } else while (!newVoc.isWrong) newVoc = lesson!!.randomWord
        voc = newVoc
        textViewToTranslate.text = voc!!.knownWord
    }
}