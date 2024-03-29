package de.luki2811.dev.vokabeltrainer

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class FileUtil {
    companion object {

        /**
         * Write a string in a file
         * @param text Text to write in file
         * @param file File to write text in
         * @param charset set using charset. Default: UTF_8
         */

        fun writeInFile(text: String, file: File, charset: Charset = StandardCharsets.UTF_8){
            try {
                file.writeText(text, charset)
            } catch (e: IOException) {
                Log.e("Exception", "File write failed: $e")
            }
        }

        fun loadFromFile(uri: Uri, contentResolver: ContentResolver, charset: Charset = StandardCharsets.UTF_8): String{
            val inputStream = contentResolver.openInputStream(uri)!!
            val stringBuilder = StringBuilder()
            BufferedReader(InputStreamReader(inputStream, charset)).forEachLine {
                stringBuilder.append(it).append('\n')
            }
            return stringBuilder.toString()
        }

        fun loadFromFile(file: File, charset: Charset = StandardCharsets.UTF_8): String {
            val stringBuilder = StringBuilder()
            try {
                file.forEachLine(charset) {
                    stringBuilder.append(it).appendLine()
                }
            }catch (e: FileNotFoundException){
                Log.e("Exception", "File not found: $e")
                throw e
            }catch (e: IOException){
                Log.e("Exception", "File load failed: $e")
            }

            return stringBuilder.toString()
        }

        const val NAME_FILE_STREAK = "streak_v2.json"
        const val NAME_FILE_INDEX_LESSONS = "indexLesson.json"
        const val NAME_FILE_INDEX_VOCABULARY_GROUPS = "indexVocabularyGroups.json"
        const val NAME_FILE_INDEX_ID = "indexId.json"
        const val NAME_FILE_LIST_WRONG_WORDS = "listWrongWords.json"
        const val NAME_FILE_SHORT_FORMS = "shortForms.json"
    }
}