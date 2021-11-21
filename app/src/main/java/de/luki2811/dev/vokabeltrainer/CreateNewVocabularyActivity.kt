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

class CreateNewVocabularyActivity : AppCompatActivity() {
    private var allForLesson: JSONObject? = null
    private var allVoc: JSONArray? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_vokabel)
        val intent = intent
        try {
            val output = findViewById<TextView>(R.id.outputCreateVocabulary)
            allForLesson = JSONObject(intent.getStringExtra(NewLessonActivity.JSON_OBJECT))
            output.text = getString(R.string.from_at_least_ten_vocs, allForLesson!!.getInt("count"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun finishVocActivity(view: View) {
        var json: JSONObject? = null
        try {
            json = allForLesson!!.put("vocabulary", allVoc)
            val file = Datei(json.getString("name") + ".json")
            file.writeInFile(json.toString(), this)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        // Create new index
        val file = File(applicationContext.filesDir, Datei.NAME_FILE_INDEX)
        var indexAsJson: JSONObject
        var jsonArray: JSONArray? = null
        val index = Datei(Datei.NAME_FILE_INDEX)
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
            val newVoc = findViewById<EditText>(R.id.editTextNewLanguageVokabel)
            val nativeVoc = findViewById<EditText>(R.id.editTextNativeLanguageVokabel)
            val switchSetting = findViewById<Switch>(R.id.switch_settings_ignoreCase)
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
        val switchSetting = findViewById<Switch>(R.id.switch_settings_ignoreCase)
        val newVoc = findViewById<EditText>(R.id.editTextNewLanguageVokabel)
        val nativeVoc = findViewById<EditText>(R.id.editTextNativeLanguageVokabel)
        val output = findViewById<TextView>(R.id.outputCreateVocabulary)
        val buttonFinish = findViewById<Button>(R.id.button_vokabel_next)
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
    }
}