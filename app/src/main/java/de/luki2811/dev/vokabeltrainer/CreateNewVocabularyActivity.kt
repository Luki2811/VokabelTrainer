package de.luki2811.dev.vokabeltrainer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

// TODO: Klasse entfernen !! -> ersetzt durch NewAddVocabularyToGroupFragment.kt

class CreateNewVocabularyActivity : AppCompatActivity() {
    /**
    private var allForLesson: JSONObject? = null
    private var allVoc: JSONArray? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_vokabel)
        intent
        try {
            val output = findViewById<TextView>(R.id.textViewNumberOfVoc)
            // allForLesson = JSONObject(intent.getStringExtra(NewLessonActivity.JSON_OBJECT))
            output.text = getString(R.string.from_at_least_ten_vocs, allForLesson!!.getInt("count"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun finishVocActivity(view: View) {
        var json: JSONObject? = null
        try {
            json = allForLesson!!.put("vocabulary", allVoc)
            val file = AppFile(json.getString("name") + ".json")
            file.writeInFile(json.toString(), this)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        // Create new index
        val file = File(applicationContext.filesDir, AppFile.NAME_FILE_INDEX_LESSONS)
        var indexAsJson: JSONObject
        var jsonArray: JSONArray? = null
        val index = AppFile(AppFile.NAME_FILE_INDEX_LESSONS)
        if (file.exists()) {
            try {
                indexAsJson = JSONObject(index.loadFromFile(this))
                jsonArray = indexAsJson.getJSONArray("index")
            } catch (e: JSONException) {
                e.printStackTrace()
                indexAsJson = JSONObject()
            }
        } else indexAsJson = JSONObject()
        try {
            if (json != null) {
                if (jsonArray == null) jsonArray = JSONArray()
                val jo = JSONObject()
                jo.put("name", json.getString("name"))
                jo.put("file", json.getString("name") + ".json")
                jsonArray.put(jo)
                indexAsJson.put("index", jsonArray)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        index.writeInFile(indexAsJson.toString(), this)

        // Zurück zur MainActivity (und Verlauf löschen, damit man nicht zurückgehen kann)
        val sendIntent =
            Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(sendIntent)
    }

    private val enteredVocabulary: JSONObject?
        get() {
            val newVoc = findViewById<EditText>(R.id.textEditVocabularyWordNewLayout)
            val nativeVoc = findViewById<EditText>(R.id.editTextVocabularyWordKnownLayout)
            val switchSetting = findViewById<Switch>(R.id.switchVocabularyWordIgnoreCase)
            if (newVoc.text.toString().trim { it <= ' ' } == "" || nativeVoc.text.toString()
                    .trim { it <= ' ' } == "") {
                Toast.makeText(this, getString(R.string.err_missing_input), Toast.LENGTH_LONG)
                    .show()
                return null
            }
            val vocabs = JSONObject()
            try {
                vocabs.put("ignoreCase", switchSetting.isChecked)
                vocabs.put("new", newVoc.text.toString().trim { it <= ' ' })
                vocabs.put("native", nativeVoc.text.toString().trim { it <= ' ' })
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return vocabs
        }

    fun saveAndResetVocActivity(view: View) {
        val switchSetting = findViewById<Switch>(R.id.switchVocabularyWordIgnoreCase)
        val newVoc = findViewById<EditText>(R.id.textEditVocabularyWordNewLayout)
        val nativeVoc = findViewById<EditText>(R.id.editTextVocabularyWordKnownLayout)
        val output = findViewById<TextView>(R.id.textViewNumberOfVoc)
        val buttonFinish = findViewById<Button>(R.id.buttonFinishAddVocabulary)
        try {
            // Falls allVoc = null -> Object erstellen
            if (allVoc == null) allVoc = JSONArray()
            // Neue Vokabel zur Sammlung hinzufügen
            if (enteredVocabulary != null) {
                allVoc!!.put(enteredVocabulary)
                // Zähler erhöhen
                allForLesson!!.put("count", allForLesson!!.getInt("count") + 1)
                // Zurücksetzen der Eingaben
                switchSetting.isChecked =
                    allVoc!!.getJSONObject(allVoc!!.length() - 1).getBoolean("ignoreCase")
                newVoc.setText("")
                nativeVoc.setText("")
            }
            // Aktualisieren des UI
            if (allForLesson!!.getInt("count") >= 10) buttonFinish.isEnabled = true
            output.text = getString(R.string.from_at_least_ten_vocs, allForLesson!!.getInt("count"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    } **/
}