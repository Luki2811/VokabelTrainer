package de.luki2811.dev.vokabeltrainer

import android.app.Application
import android.content.Context
import android.util.Log
import java.io.*
import java.nio.charset.StandardCharsets

class AppFile(var name: String) : Application() {

    fun loadFromFile(context: Context): String {
        return context.openFileInput(name).bufferedReader().useLines { lines ->
            lines.fold("") { some, text ->
                "$some\n$text"
            }
        }
    }

    fun writeInFile(text: String, context: Context) {
        try {
            val outputStreamWriter = OutputStreamWriter(context.openFileOutput(name, MODE_PRIVATE))
            outputStreamWriter.write(text)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: $e")
        }
    }

    companion object {

        fun writeInFile(text: String ,file: File){
            try {
                val outputStreamWriter = OutputStreamWriter(FileOutputStream(file))
                outputStreamWriter.write(text)
                outputStreamWriter.close()
            } catch (e: IOException) {
                Log.e("Exception", "File write failed: $e")
            }
        }

        fun loadFromFile(file: File): String {
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(file)
            } catch (e: FileNotFoundException) {
                Log.e("Exception", "File load failed: $e")
            }
            val inputStreamReader = InputStreamReader(fis, StandardCharsets.UTF_8)
            val stringBuilder = StringBuilder()
            try {
                BufferedReader(inputStreamReader).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line).append('\n')
                        line = reader.readLine()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return stringBuilder.toString()
        }

        const val NAME_FILE_STREAK = "streak.json"
        const val NAME_FILE_INDEX_LESSONS = "indexLesson.json"
        const val NAME_FILE_INDEX_VOCABULARYGROUPS = "indexVocabularyGroups.json"
        const val NAME_FILE_INDEX_ID = "indexId.json"
        const val NAME_FILE_SETTINGS = "settings.json"
        const val NAME_FILE_INDEX_LANGUAGES = "indexLanguages.json"

        fun isAppFile(name: String): Boolean{
            if(
                name == NAME_FILE_STREAK ||
                name == NAME_FILE_INDEX_LESSONS ||
                name == NAME_FILE_INDEX_VOCABULARYGROUPS ||
                name == NAME_FILE_INDEX_ID ||
                name == NAME_FILE_SETTINGS ||
                name == NAME_FILE_INDEX_LANGUAGES)
                    return true
            return false
        }
    }
}