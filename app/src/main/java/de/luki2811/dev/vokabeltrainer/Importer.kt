package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.util.Log
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject

class Importer(private val data: String, private val context: Context) {
    var vocabularyGroup: VocabularyGroup? = null

    fun start(): Int{
        if(data.isEmpty())
            return IMPORT_EMPTY
        return try {
            val dataAsJson = JSONObject(data)
            try {
                when(dataAsJson.getInt("type")){
                    Exportable.TYPE_LESSON -> tryLesson(dataAsJson)
                    Exportable.TYPE_VOCABULARY_GROUP -> importVocabularyGroup(dataAsJson.toString())
                    Exportable.TYPE_SHORT_FORM -> importShortForm(dataAsJson)
                    else -> IMPORT_WRONG_OR_NONE_TYPE
                }
            }catch (e: JSONException){
                IMPORT_WRONG_OR_NONE_TYPE
            }
        }catch (e: JSONException){
            importVocabularyGroup(data, isCSV = true)
        }
    }

    private fun importShortForm(dataObject: JSONObject): Int{

        return try {
            val dataArray = dataObject.getJSONArray("items")
            val allShortForms = ShortForm.loadAllShortForms(context)

            for (i in 0 until dataArray.length()){
                val newShortForm = ShortForm.fromJson(dataArray.getJSONObject(i))
                if(allShortForms.contains(newShortForm)){
                    Log.i("Importer","Short Form ($newShortForm) already in list")
                }else{
                    Log.i("Importer","Add short form ($newShortForm) in list")
                    allShortForms.add(newShortForm)
                }
                ShortForm.setNewShortForms(context, allShortForms)
            }

            IMPORT_SUCCESSFULLY_SHORT_FORM
        }catch (e: JSONException){
            e.printStackTrace()
            IMPORT_NO_JSON
        }
    }

    private fun importVocabularyGroup(data: String,cancelWithWrongType: Boolean = true, isCSV: Boolean = false): Int{

        try {
            if(!isCSV){
                val dataAsJson = JSONObject(data)
                if(cancelWithWrongType){
                    if(dataAsJson.getInt("type") != Exportable.TYPE_VOCABULARY_GROUP){
                        return IMPORT_WRONG_OR_NONE_TYPE
                    }
                }
                this.vocabularyGroup = VocabularyGroup.loadFromJSON(dataAsJson, context = context, generateNewId = true)
            }else{
                if (data.lines().first().toInt() != Exportable.TYPE_VOCABULARY_GROUP_QR)
                    return IMPORT_WRONG_OR_NONE_TYPE
                try {
                    this.vocabularyGroup = VocabularyGroup.loadFromCSV(data, context)
                }catch (e: Exception){
                    e.printStackTrace()
                    return IMPORT_WRONG_OR_NONE_TYPE
                }

            }

            if(vocabularyGroup == null){
                return IMPORT_FAILED_NULL
            }else{
                var tempInt = 0
                while(VocabularyGroup.isNameValid(context, vocabularyGroup!!.name) != 0){
                    tempInt += 1
                    var nameOfVocGroup = vocabularyGroup!!.name
                    nameOfVocGroup = if(tempInt > 1)
                        nameOfVocGroup.replace("(${tempInt - 1})","(${tempInt})")
                    else
                        "${vocabularyGroup!!.name} (1)"

                    vocabularyGroup!!.name = nameOfVocGroup
                }

                return IMPORT_SUCCESSFULLY_VOCABULARY_GROUP
            }

        }catch (e: JSONException){
            e.printStackTrace()
            return IMPORT_NO_JSON
        }
    }

    private fun tryLesson(dataAsJson: JSONObject): Int{

        try {
            val groups = arrayListOf<VocabularyGroup>()
            val vocabularyGroups = dataAsJson.getJSONArray("vocabularyGroups")

            for (i in 0 until vocabularyGroups.length()){

                when(val errorCode = importVocabularyGroup(vocabularyGroups.getJSONObject(i).toString(), cancelWithWrongType = false)){
                    IMPORT_SUCCESSFULLY_VOCABULARY_GROUP -> {
                        if(vocabularyGroup != null){
                            Log.i("Import", "ID: ${vocabularyGroup!!.id}")
                            groups.add(vocabularyGroup!!)
                            vocabularyGroup!!.saveInFile(context)
                            vocabularyGroup!!.saveInIndex(context)
                        }else{
                            Log.e("Import","Failed to add vocabularyGroup at $i because is null")
                        }
                    }
                    else -> {
                        Log.e("Import", "Import  of vocabulary group failed with error code $errorCode")
                    }
                }


            }

            var nameOfLesson = dataAsJson.getString("name").trim()
            var tempInt = 0
            while(Lesson.isNameValid(context, nameOfLesson) != Lesson.VALID){
                tempInt += 1

                nameOfLesson = if(tempInt > 1)
                    nameOfLesson.replace("(${tempInt - 1})","(${tempInt})")
                else
                    "${dataAsJson.getString("name")} (1)"

            }

            val ids = ArrayList<Id>()
            groups.forEach {
                ids.add(it.id)
            }

            val lesson = Lesson.fromJSON(dataAsJson, context, true, alternativeIdsOfVocGroups = ids)
            return if(lesson != null){
                lesson.saveInFile(context)
                lesson.saveInIndex(context)
                IMPORT_SUCCESSFULLY_LESSON
            }else{
                Log.e("Import", "Failed to import lesson because is null")
                IMPORT_FAILED_NULL
            }





        } catch (e: JSONException){
            Toast.makeText(context, context.getText(R.string.err_could_not_import_lesson), Toast.LENGTH_LONG).show()
            e.printStackTrace()
            return IMPORT_NO_JSON
        }
    }

    companion object{
        const val IMPORT_SUCCESSFULLY_VOCABULARY_GROUP = 100
        const val IMPORT_SUCCESSFULLY_LESSON = 101
        const val IMPORT_SUCCESSFULLY_SHORT_FORM = 102

        const val IMPORT_WRONG_OR_NONE_TYPE = 1
        const val IMPORT_EMPTY = 2
        const val IMPORT_NO_JSON = 3
        const val IMPORT_FAILED_NULL = 4

        const val VOCABULARY_GROUP_TOO_SHORT = 10


    }
}