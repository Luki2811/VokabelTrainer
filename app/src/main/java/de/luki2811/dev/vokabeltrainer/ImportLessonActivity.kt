package de.luki2811.dev.vokabeltrainer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class ImportLessonActivity : AppCompatActivity() {

    private var lesson: Lesson = Lesson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_lesson)

        val uri: Uri = Uri.parse(intent.getStringExtra("uriOfLesson"))
        val file = File(uri.let { RealPathUtil.getRealPath(this, it) })
        val datei = Datei(file.name)

        // Create new Lesson
        var lessonAsJSON: JSONObject? = null
        try {
            lessonAsJSON = JSONObject(datei.loadFromFile(file))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        if (lessonAsJSON == null) {
            Toast.makeText(this, getString(R.string.err_could_not_import_lesson), Toast.LENGTH_LONG).show()
            return
        }
        lesson = Lesson(lessonAsJSON)

        val editTextName = findViewById<EditText>(R.id.EditLessonNameImport)
        editTextName.setText(lesson.name)
        editTextName.hint = lesson.name
    }

    fun finishImport(view: View) {
        // Set Text
        val editTextName = findViewById<EditText>(R.id.EditLessonNameImport)
        if(editTextName.text.toString().trim().isNotEmpty())
            lesson.name = editTextName.text.toString()
        // Check if lesson is correct
        if(!lesson.isNameValid(this))
            return

        val indexFile = File(applicationContext.filesDir, Datei.NAME_FILE_INDEX)
        val indexDatei = Datei(indexFile.name)

        // Create index
        if (!indexFile.exists()) {
            indexDatei.writeInFile("", this)
        }
        var indexAsJson: JSONObject
        var jsonArray: JSONArray? = null
        if (indexFile.exists()) {
            try {
                indexAsJson = JSONObject(indexDatei.loadFromFile(this))
                jsonArray = indexAsJson.getJSONArray("index")
            } catch (e: JSONException) {
                e.printStackTrace()
                indexAsJson = JSONObject()
            }
        } else indexAsJson = JSONObject()
        try {
            if (jsonArray == null) jsonArray = JSONArray()
            val jo = JSONObject()
            jo.put("name", lesson.name)
            jo.put("file", lesson.name + ".json")
            jsonArray.put(jo)
            indexAsJson.put("index", jsonArray)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        indexDatei.writeInFile(indexAsJson.toString(), this)

        // Save lesson as .json
        val saveDatei = Datei(lesson.name + ".json")
        saveDatei.writeInFile(lesson.lessonAsJson.toString(), this)
        Toast.makeText(this, getString(R.string.import_lesson_successful), Toast.LENGTH_LONG).show()
        startActivity(Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    fun cancelImportLesson(view: View){
        this.finish()
    }
}

