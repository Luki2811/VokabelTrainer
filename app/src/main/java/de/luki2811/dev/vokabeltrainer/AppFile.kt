package de.luki2811.dev.vokabeltrainer

import android.util.Log
import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class AppFile(var name: String) {
    companion object {

        fun writeInFile(text: String ,file: File, charset: Charset = StandardCharsets.UTF_8){
            try {
                file.writeText(text, charset)
            } catch (e: IOException) {
                Log.e("Exception", "File write failed: $e")
            }
        }

        fun loadFromFile(file: File, charset: Charset = StandardCharsets.UTF_8): String {
            val stringBuilder = StringBuilder()
            try {
                file.forEachLine(charset) {
                    stringBuilder.append(it).append('\n')
                }
            }catch (e: FileNotFoundException){
                Log.e("Exception", "File not found: $e")
                throw e
            }catch (e: IOException){
                Log.e("Exception", "File load failed: $e")
            }

            return stringBuilder.toString()
        }

        const val NAME_FILE_STREAK = "streak.json"
        const val NAME_FILE_INDEX_LESSONS = "indexLesson.json"
        const val NAME_FILE_INDEX_VOCABULARY_GROUPS = "indexVocabularyGroups.json"
        const val NAME_FILE_INDEX_ID = "indexId.json"
        const val NAME_FILE_SETTINGS = "settings.json"
        const val NAME_FILE_INDEX_LANGUAGES = "indexLanguages.json"
        const val NAME_FILE_LIST_WRONG_WORDS = "listWrongWords.json"

        const val TYPE_FILE_UNKNOWN = 0
        const val TYPE_FILE_LESSON = 1
        const val TYPE_FILE_VOCABULARY_GROUP = 2

        fun isAppFile(name: String): Boolean{
            if(
                name == NAME_FILE_STREAK ||
                name == NAME_FILE_INDEX_LESSONS ||
                name == NAME_FILE_INDEX_VOCABULARY_GROUPS ||
                name == NAME_FILE_INDEX_ID ||
                name == NAME_FILE_SETTINGS ||
                name == NAME_FILE_INDEX_LANGUAGES ||
                name == NAME_FILE_LIST_WRONG_WORDS
            )
                    return true
            return false
        }
    }
}