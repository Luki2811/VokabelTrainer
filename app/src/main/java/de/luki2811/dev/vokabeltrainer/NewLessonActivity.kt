package de.luki2811.dev.vokabeltrainer

import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class NewLessonActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_lesson)
        val en_new = findViewById<RadioButton>(R.id.radioButton_new_english)
        en_new.isChecked = true
        val de_native = findViewById<RadioButton>(R.id.radioButton_native_german)
        de_native.isChecked = true
    }

    fun checkAndGoNext(view: View?) {
        val textName = findViewById<EditText>(R.id.TextLektionName)
        val en_native = findViewById<RadioButton>(R.id.radioButton_native_english)
        val de_native = findViewById<RadioButton>(R.id.radioButton_native_german)
        val sv_native = findViewById<RadioButton>(R.id.radioButton_native_swedish)
        val fr_native = findViewById<RadioButton>(R.id.radioButton_native_french)
        val en_new = findViewById<RadioButton>(R.id.radioButton_new_english)
        val de_new = findViewById<RadioButton>(R.id.radioButton_new_german)
        val sv_new = findViewById<RadioButton>(R.id.radioButton_new_swedish)
        val fr_new = findViewById<RadioButton>(R.id.radioButton_new_french)
        if (textName.text == null || textName.text.toString().trim { it <= ' ' }.isEmpty()) {
            Toast.makeText(this, getString(R.string.err_missing_name), Toast.LENGTH_SHORT).show()
        } else {
            val indexFile = File(applicationContext.filesDir, Datei.NAME_FILE_INDEX)
            val indexDatei = Datei(Datei.NAME_FILE_INDEX)
            if (indexFile.exists()) {
                if (textName.text.toString().trim { it <= ' ' }.contains("/") ||
                    textName.text.toString().trim { it <= ' ' }.contains("<") ||
                    textName.text.toString().trim { it <= ' ' }.contains(">") ||
                    textName.text.toString().trim { it <= ' ' }.contains("\\") ||
                    textName.text.toString().trim { it <= ' ' }.contains("|") ||
                    textName.text.toString().trim { it <= ' ' }.contains("*") ||
                    textName.text.toString().trim { it <= ' ' }.contains(":") ||
                    textName.text.toString().trim { it <= ' ' }.contains("\"") ||
                    textName.text.toString().trim { it <= ' ' }.contains("?")
                ) {
                    Toast.makeText(
                        this,
                        getString(R.string.err_name_contains_wrong_letter),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                try {
                    val indexJson = JSONObject(indexDatei.loadFromFile(this))
                    val indexArray = indexJson.getJSONArray("index")
                    for (i in 0..indexArray.length() - 1) {
                        if (indexArray.getJSONObject(i)
                                .getString("name") == textName.text.toString()
                                .trim { it <= ' ' } || textName.text.toString().trim { it <= ' ' }
                                .equals("streak", ignoreCase = true)
                            || textName.text.toString().trim { it <= ' ' }
                                .equals("settings", ignoreCase = true)
                            || textName.text.toString().trim { it <= ' ' }
                                .equals("indexLections", ignoreCase = true)
                        ) {
                            Toast.makeText(
                                this,
                                getString(R.string.err_name_not_avaible),
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            val JSONFile = JSONObject()
            try {
                // Einstellungen der Lektion als .json
                // Name
                JSONFile.put("name", textName.text.toString())
                // Setzen der Variable für "count"
                JSONFile.put("count", 0)
                // Type Native Sprache
                if (en_native.isChecked) JSONFile.put(
                    "languageNative",
                    Language.ENGLISH
                ) else if (de_native.isChecked) JSONFile.put(
                    "languageNative",
                    Language.GERMAN
                ) else if (fr_native.isChecked) JSONFile.put(
                    "languageNative",
                    Language.FRENCH
                ) else if (sv_native.isChecked) JSONFile.put(
                    "languageNative",
                    Language.SWEDISH
                ) else Toast.makeText(
                    this,
                    getString(R.string.err_no_native_selected),
                    Toast.LENGTH_LONG
                ).show()
                // Type neue Sprache
                if (en_new.isChecked) JSONFile.put(
                    "languageNew",
                    Language.ENGLISH
                ) else if (de_new.isChecked) JSONFile.put(
                    "languageNew",
                    Language.GERMAN
                ) else if (fr_new.isChecked) JSONFile.put(
                    "languageNew",
                    Language.FRENCH
                ) else if (sv_new.isChecked) JSONFile.put(
                    "languageNew",
                    Language.SWEDISH
                ) else Toast.makeText(
                    this,
                    getString(R.string.err_no_new_selected),
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val intent = Intent(this, CreateNewVocabularyActivity::class.java)
            intent.putExtra(JSON_OBJECT, JSONFile.toString())
            startActivity(intent)
        }
    }

    var requestCode = 1
    fun importLesson(view: View?) {
        if (checkPermission()) {
            val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
            chooseFile.type = "application/json"
            startActivityForResult(chooseFile, requestCode)
        } else requestPermission()
    }

    var PERMISSION_REQUEST_CODE = 100
    private fun checkPermission(): Boolean {
        return if (VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val result = ContextCompat.checkSelfPermission(
                this@NewLessonActivity,
                permission.READ_EXTERNAL_STORAGE
            )
            val result1 = ContextCompat.checkSelfPermission(
                this@NewLessonActivity,
                permission.WRITE_EXTERNAL_STORAGE
            )
            result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                startActivityForResult(intent, 2296)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, 2296)
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(
                this@NewLessonActivity,
                arrayOf(permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2296) {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
                    chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
                    chooseFile.type = "application/json"
                    startActivityForResult(chooseFile, requestCode)
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT)
                        .show()
                }
            } else if (grantResults.size > 0) {
                val READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) {
                    val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
                    chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
                    chooseFile.type = "application/json"
                    startActivityForResult(chooseFile, requestCode)
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (data == null) {
                return
            }
            val uri = data.data
            val file = File(uri?.let { RealPathUtil.getRealPath(this, it) })
            val datei = Datei(file.name)

            // Create new Lesson
            var lessonAsJSON: JSONObject? = null
            try {
                lessonAsJSON = JSONObject(datei.loadFromFile(file))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            if (lessonAsJSON == null) {
                Toast.makeText(
                    this,
                    getString(R.string.err_could_not_import_lesson),
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            val lesson = Lesson(lessonAsJSON)

            // Check if lesson is correct
            val indexFile = File(applicationContext.filesDir, Datei.NAME_FILE_INDEX)
            val indexDatei = Datei(indexFile.name)
            if (!indexFile.exists()) {
                indexDatei.writeInFile("", this)
            }
            if (lesson.name!!.contains("/") ||
                lesson.name!!.contains("<") ||
                lesson.name!!.contains(">") ||
                lesson.name!!.contains("\\") ||
                lesson.name!!.contains("|") ||
                lesson.name!!.contains("*") ||
                lesson.name!!.contains(":") ||
                lesson.name!!.contains("\"") ||
                lesson.name!!.contains("?")
            ) {
                Toast.makeText(
                    this,
                    getString(R.string.err_name_contains_wrong_letter),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            try {
                val indexJson = JSONObject(indexDatei.loadFromFile(this))
                val indexArray = indexJson.getJSONArray("index")
                for (i in 0..indexArray.length() - 1) {
                    if (indexArray.getJSONObject(i)
                            .getString("name") == lesson.name || lesson.name.equals(
                            "streak",
                            ignoreCase = true
                        )
                        || lesson.name.equals("settings", ignoreCase = true)
                        || lesson.name.equals("indexLections", ignoreCase = true)
                    ) {
                        Toast.makeText(
                            this,
                            getString(R.string.err_name_not_avaible),
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            // Create index
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
            Toast.makeText(this, getString(R.string.import_lesson_successful), Toast.LENGTH_LONG)
                .show()
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        } else {
            Toast.makeText(this@NewLessonActivity, getString(R.string.err), Toast.LENGTH_LONG)
                .show()
        }
    }

    companion object {
        const val JSON_OBJECT = "de.luki2811.dev.vokabeltrainer.JSON_Object"
    }
}