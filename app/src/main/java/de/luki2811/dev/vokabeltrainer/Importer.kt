package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.util.Log
import org.json.JSONException
import org.json.JSONObject

class Importer(private val data: String, val context: Context) {
    // var lesson: Lesson? = null
    var vocabularyGroup: VocabularyGroup? = null

    fun tryAll(): Int{
        if(data.isEmpty())
            return IMPORT_EMPTY
        return try {
            val dataAsJson = JSONObject(data)
            try {
                when(dataAsJson.getInt("type")){
                    AppFile.TYPE_FILE_LESSON -> tryLesson()
                    AppFile.TYPE_FILE_VOCABULARY_GROUP -> tryVocabularyGroup()
                    else -> IMPORT_WRONG_OR_NONE_TYPE
                }
            }catch (e: JSONException){
                IMPORT_WRONG_OR_NONE_TYPE
            }
        }catch (e: JSONException){
            IMPORT_NO_JSON
        }

    }

    private fun tryVocabularyGroup(cancelWithWrongType: Boolean = true): Int{
        if(data.isEmpty())
            return IMPORT_EMPTY
        try {
            val dataAsJson = JSONObject(data)

            if(cancelWithWrongType){
                if(dataAsJson.getInt("type") != AppFile.TYPE_FILE_VOCABULARY_GROUP){
                    return IMPORT_WRONG_OR_NONE_TYPE
                }
            }

            if(dataAsJson.getJSONArray("vocabulary").length() < 2){
               return VOCABULARY_GROUP_TOO_SHORT
            }
            val vocabularyGroup = VocabularyGroup(dataAsJson, context = context)
            var tempInt = 0
            while(VocabularyGroup.isNameValid(context, vocabularyGroup.name) != 0){
                tempInt += 1
                var nameOfVocGroup = vocabularyGroup.name
                nameOfVocGroup = if(tempInt > 1)
                    nameOfVocGroup.replace("(${tempInt - 1})","(${tempInt})")
                else
                    "${vocabularyGroup.name} (1)"

                vocabularyGroup.name = nameOfVocGroup
            }
            this.vocabularyGroup = vocabularyGroup
            return IMPORT_SUCCESSFULLY_VOCABULARY_GROUP
        }catch (e: JSONException){
            e.printStackTrace()
            return IMPORT_NO_JSON
        }
    }

    private fun tryLesson(): Int{
        if(data.isEmpty())
            return IMPORT_EMPTY
        try {
            val dataAsJson = JSONObject(data)

            if(dataAsJson.getInt("type") != AppFile.TYPE_FILE_LESSON){
                return IMPORT_WRONG_OR_NONE_TYPE
            }

            val newIdsVocabularyGroups = arrayListOf<Int>()
            val vocabularyGroups = dataAsJson.getJSONArray("vocabularyGroups")

            for (i in 0 until vocabularyGroups.length()){
                val vocGroupFromLesson = VocabularyGroup(vocabularyGroups.getJSONObject(i), context = context, generateNewId = true)

                var tempInt = 0
                while(VocabularyGroup.isNameValid(context, vocGroupFromLesson.name) != 0){
                    tempInt += 1
                    var nameOfVocGroup = vocGroupFromLesson.name
                    nameOfVocGroup = if(tempInt > 1)
                        nameOfVocGroup.replace("(${tempInt - 1})","(${tempInt})")
                    else
                        "${vocGroupFromLesson.name} (1)"

                    vocGroupFromLesson.name = nameOfVocGroup
                }
                Log.i("Import ID", vocGroupFromLesson.id.number.toString())
                newIdsVocabularyGroups.add(vocGroupFromLesson.id.number)
                vocGroupFromLesson.saveInFile()
                vocGroupFromLesson.saveInIndex()
            }

            var nameOfLesson = dataAsJson.getString("name")
            var tempInt = 0
            while(Lesson.isNameValid(context, nameOfLesson) != 0){
                tempInt += 1

                nameOfLesson = if(tempInt > 1)
                    nameOfLesson.replace("(${tempInt - 1})","(${tempInt})")
                else
                    "${dataAsJson.getString("name")} (1)"

            }
            val askOnlyNewWords = dataAsJson.getJSONObject("settings").getBoolean("askOnlyNewWords")
            val readOutBoth = dataAsJson.getJSONObject("settings").getBoolean("readOutBoth")
            val numberOfExercises = try {
                dataAsJson.getJSONObject("settings").getInt("numberOfExercises")
            } catch (e: JSONException){
                10
            }

            val useTypes = arrayListOf<Int>()
            if(dataAsJson.getJSONObject("settings").getBoolean("useType1")) useTypes.add(Exercise.TYPE_TRANSLATE_TEXT)
            if(dataAsJson.getJSONObject("settings").getBoolean("useType2")) useTypes.add(Exercise.TYPE_CHOOSE_OF_THREE_WORDS)
            if(dataAsJson.getJSONObject("settings").getBoolean("useType3")) useTypes.add(Exercise.TYPE_MATCH_FIVE_WORDS)

            val lesson = Lesson(nameOfLesson, newIdsVocabularyGroups.toTypedArray() , context, readOutBoth, askOnlyNewWords, useTypes, numberOfExercises = numberOfExercises)
            lesson.saveInFile()
            lesson.saveInIndex()

            return IMPORT_SUCCESSFULLY_LESSON

        } catch (e: JSONException){
            // Toast.makeText(context, context.getText(R.string.err_could_not_import_lesson), Toast.LENGTH_LONG).show()
            e.printStackTrace()
            return IMPORT_NO_JSON
        }
    }

    companion object{
        const val IMPORT_SUCCESSFULLY_VOCABULARY_GROUP = 100
        const val IMPORT_SUCCESSFULLY_LESSON = 101

        const val IMPORT_WRONG_OR_NONE_TYPE = 1
        const val IMPORT_EMPTY = 2
        const val IMPORT_NO_JSON = 3

        const val VOCABULARY_GROUP_TOO_SHORT = 10


    }
}