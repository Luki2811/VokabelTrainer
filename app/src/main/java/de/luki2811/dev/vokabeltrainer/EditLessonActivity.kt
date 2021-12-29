package de.luki2811.dev.vokabeltrainer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONException
import org.json.JSONObject

class EditLessonActivity : AppCompatActivity() {

    // TODO: Komplett überarbeiten

    /** var lesson: Lesson? = null
    var counter = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_lesson)
        val comingInt = intent
        val lessonName = comingInt.getStringExtra(MainActivity.LEKTION_NAME)
        val datei = AppFile("$lessonName.json")
        try {
            val lektionAsJSON = JSONObject(datei.loadFromFile(this))
            lesson = Lesson(lektionAsJSON)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        counter = 1
        val textViewKnown = findViewById<TextView>(R.id.textViewEditLessonKnown)
        val textViewNew = findViewById<TextView>(R.id.textViewEditLessonNew)
        val textViewTop = findViewById<TextView>(R.id.textViewEditLessonTop)
        textViewKnown.text = getString(R.string.voc_in, lesson!!.languageKnow.getName())
        textViewNew.text = getString(R.string.voc_in, lesson!!.languageNew.getName())
        textViewTop.text = getString(R.string.number_voc_of_rest, counter, lesson!!.count)
        val editTextKnown = findViewById<EditText>(R.id.editTextEditLessonKnown)
        val editTextNew = findViewById<EditText>(R.id.editTextEditLessonNew)
        val switchIgnoreCase = findViewById<Switch>(R.id.switchEditLektionIgnoreCase)
        editTextKnown.setText(lesson!!.getWordAtPos(counter - 1).knownWord)
        editTextKnown.hint = lesson!!.getWordAtPos(counter - 1).knownWord
        editTextNew.setText(lesson!!.getWordAtPos(counter - 1).newWord)
        editTextNew.hint = lesson!!.getWordAtPos(counter - 1).newWord
        switchIgnoreCase.isChecked = lesson!!.getWordAtPos(counter - 1).isIgnoreCase
    }

    fun saveAndNext(view: View?) {

        // Speichern der Vokabel in der Datei
        val editTextKnown = findViewById<EditText>(R.id.editTextEditLessonKnown)
        val editTextNew = findViewById<EditText>(R.id.editTextEditLessonNew)
        val switchIgnoreCase = findViewById<Switch>(R.id.switchEditLektionIgnoreCase)
        val textViewTop = findViewById<TextView>(R.id.textViewEditLessonTop)
        if (editTextKnown.text.toString().trim { it <= ' ' }
                .isEmpty() || editTextNew.text.toString().trim { it <= ' ' }.isEmpty()) {
            Toast.makeText(this, getText(R.string.err_missing_input), Toast.LENGTH_LONG).show()
            return
        }
        val vocNew = VocabularyWord(
            editTextKnown.text.toString(),
            editTextNew.text.toString(),
            switchIgnoreCase.isChecked
        )
        lesson!!.setWordAtPos(counter - 1, vocNew)
        val datei = AppFile(lesson!!.name + ".json")
        datei.writeInFile(lesson!!.lessonAsJson.toString(), this)
        counter = counter + 1
        if (counter <= lesson!!.count) {
            // UI ändern auf die nächste Vokabel
            editTextKnown.setText(lesson!!.getWordAtPos(counter - 1).knownWord)
            editTextKnown.hint = lesson!!.getWordAtPos(counter - 1).knownWord
            editTextNew.setText(lesson!!.getWordAtPos(counter - 1).newWord)
            editTextNew.hint = lesson!!.getWordAtPos(counter - 1).newWord
            switchIgnoreCase.isChecked = lesson!!.getWordAtPos(counter - 1).isIgnoreCase
            textViewTop.text = getString(R.string.number_voc_of_rest, counter, lesson!!.count)
        } else {
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }
    }

    fun cancelOnClick(view: View?) {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    } **/
}