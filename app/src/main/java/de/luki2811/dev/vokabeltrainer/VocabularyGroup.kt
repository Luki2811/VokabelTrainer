package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.os.Parcelable
import android.util.Log
import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.Locale

@Parcelize
data class VocabularyGroup(var name: String, var id: Id, var otherLanguage: Locale, var mainLanguage: Locale, var vocabulary: ArrayList<VocabularyWord> = ArrayList(), override val type: Int = Exportable.TYPE_VOCABULARY_GROUP): Exportable, Parcelable {

    fun exportShort(): String {
        return StringBuilder().apply {
            // Type for reading out (first line)
            append(Exportable.TYPE_VOCABULARY_GROUP_QR)
            appendLine()

            // Base data (second line)
            append(name).append(";;")
            append(mainLanguage.language).append(";;")
            append(otherLanguage.language)
            appendLine()

            // Each line one word
            vocabulary.forEach {
                appendLine(it.getAsCSV())
            }

        }.toString()
    }

    fun getShareFileName(): String{
        val stringBuilder = StringBuilder()
        for(i in name.indices){
            if(name[i] == '/' || name[i] == ' ' || name[i] == '\\' || name[i] == '\"' || name[i] == '|' || name[i] == '*' || name[i] == '?' ||
                name[i] == '<' || name[i] == '>' || name[i] == ':' || name[i] == '+' || name[i] == '[' || name[i] == ']' || name[i] == '\'' ||
                name[i] == ':' || name[i] == ';' || name[i] == '.'){
                stringBuilder.append("_")
            }else
                stringBuilder.append(name[i].lowercase())
        }
        return stringBuilder.apply { append("_voc.json") }.toString()
    }

    fun refreshNameInIndex(context: Context){
        val indexFile = File(context.filesDir, FileUtil.NAME_FILE_INDEX_VOCABULARY_GROUPS)
        val index = JSONObject(FileUtil.loadFromFile(File(context.filesDir ,FileUtil.NAME_FILE_INDEX_VOCABULARY_GROUPS)))
        for(i in 0 until index.getJSONArray("index").length()){
            if(index.getJSONArray("index").getJSONObject(i).getInt("id") == id.number)
                index.getJSONArray("index").getJSONObject(i).put("name", name)
        }
        FileUtil.writeInFile(index.toString(),indexFile)
    }

    fun saveInIndex(context: Context){
        val indexFile = File(context.filesDir, FileUtil.NAME_FILE_INDEX_VOCABULARY_GROUPS)
        if(File(context.filesDir, FileUtil.NAME_FILE_INDEX_VOCABULARY_GROUPS).exists()){
            val index = JSONObject(FileUtil.loadFromFile(File(context.filesDir ,FileUtil.NAME_FILE_INDEX_VOCABULARY_GROUPS)))
            val toIndexJson = JSONObject().put("name", name).put("id", id.number)
            index.getJSONArray("index").put(index.getJSONArray("index").length(), toIndexJson)
            FileUtil.writeInFile(index.toString(),indexFile)
        }else{
            val toIndexJson = JSONObject().put("name", name).put("id", id.number)
            val index = JSONObject().put("index", JSONArray().put(0, toIndexJson))
            FileUtil.writeInFile(index.toString(),indexFile)
        }
    }

    fun deleteFromIndex(context: Context){
        val indexFile = File(context.filesDir, FileUtil.NAME_FILE_INDEX_VOCABULARY_GROUPS)
        val index = JSONObject(FileUtil.loadFromFile(indexFile))
        val fieldToDelete = arrayListOf<Int>()

        for(i in 0 until index.getJSONArray("index").length()){
            if(index.getJSONArray("index").getJSONObject(i).getInt("id") == id.number)
                fieldToDelete.add(i)
        }

        for(i in fieldToDelete){
            index.getJSONArray("index").remove(i)
        }

        FileUtil.writeInFile(index.toString(),indexFile)
    }


    fun getAsJson(): JSONObject{
        val jsonArray = JSONArray()
        vocabulary.forEach { jsonArray.put(it.getAsJSON(true)) }

        return JSONObject()
            .put("name", name)
            .put("id", id.number)
            .put("type", type)
            .put("otherLanguage", otherLanguage.language)
            .put("mainLanguage", mainLanguage.language)
            .put("vocabulary", jsonArray)
    }

    /**
     * Creates a file with the ID of VocabularyGroup
     */
    fun saveInFile(context: Context) {
        if(id.number == 0){
            Log.e("Error", "Couldn't save vocabulary group \"${this.name}\", because ID is 0")
            return
        }
        var file = File(context.filesDir, "vocabularyGroups")
        file.mkdirs()
        file = File(file, id.number.toString() + ".json" )
        FileUtil.writeInFile(getAsJson().toString(), file)
    }

    fun resetLevels(context: Context) {
        Log.i("VocabularyGroup", "Reset all levels of vocabularyGroup \"$name\" (${id.number})")
        vocabulary.forEach {
            it.levelMain = 0
            it.levelOther = 0
        }
        saveInFile(context)
    }

    companion object{

        const val MAX_LINES = 3
        const val MAX_CHARS = 50

        const val VALID = 0
        const val INVALID_TOO_MANY_CHARS = -1
        const val INVALID_TOO_MANY_LINES = -2
        const val INVALID_EMPTY = -3
        const val INVALID_NAME_ALREADY_USED = -4
        const val INVAILD_CONTAINS_SEPERATORS = -5

        fun loadFromJSON(json: JSONObject, context: Context, name: String? = null, generateNewId: Boolean = false): VocabularyGroup{
            val nameOfGroup: String = if(name.isNullOrBlank()) json.getString("name") else name

            val id = if(name.isNullOrBlank()) {
                if(generateNewId) Id.generate(context).apply { register(context) } else Id(json.getInt("id"))
            } else { Id.generate(context).apply { register(context) } }

            var otherLanguage: Locale
            var mainLanguage: Locale
            try {
                otherLanguage = try {
                    Locale(json.getString("otherLanguage"))
                } catch (e: JSONException){
                    try {
                        Locale(json.getString("firstLanguage"))
                    }catch (e: JSONException){
                        Locale(json.getString("languageKnown"))
                    }
                }

                mainLanguage = try {
                    Locale(json.getString("mainLanguage"))
                }catch (e: JSONException){
                    try {
                        Locale(json.getString("secondLanguage"))
                    }catch (e: JSONException){
                        Locale(json.getString("languageNew"))
                    }
                }

            }catch (e: JSONException){
                otherLanguage = Locale.GERMAN
                mainLanguage = Locale.ENGLISH
            }

            val vocabulary = ArrayList<VocabularyWord>()

            for (i in 0 until json.getJSONArray("vocabulary").length()){
                val type = try {
                    json.getJSONArray("vocabulary").getJSONObject(i).getInt("type")
                }catch (e: JSONException){
                    VocabularyWord.TYPE_TRANSLATION
                }
                when(type){
                    VocabularyWord.TYPE_ANTONYM, VocabularyWord.TYPE_SYNONYM -> {
                        vocabulary.add(Synonym.loadFromJSON(json.getJSONArray("vocabulary").getJSONObject(i), mainLanguage))
                    }
                    VocabularyWord.TYPE_TRANSLATION -> {
                        vocabulary.add(WordTranslation.loadFromJSON(json.getJSONArray("vocabulary").getJSONObject(i), mainLanguage = mainLanguage, otherLanguage = otherLanguage))
                    }
                    VocabularyWord.TYPE_WORD_FAMILY -> {
                        vocabulary.add(WordFamily.loadFromJSON(json.getJSONArray("vocabulary").getJSONObject(i), mainLanguage))
                    }
                    else -> {
                        Log.w("VocabularyGroup", "Unknown Type of word in group \"$name\" (${id.number}) at $i of ${json.getJSONArray("vocabulary").length()}")
                    }
                }
            }
            return VocabularyGroup(nameOfGroup, id, otherLanguage, mainLanguage, vocabulary)
        }

        fun loadFromFileWithId(id: Id, context: Context): VocabularyGroup?{
            var file = File(context.filesDir, "vocabularyGroups")
            file.mkdirs()
            file = File(file, id.number.toString() + ".json" )
            return if(file.exists())
                loadFromJSON(JSONObject(FileUtil.loadFromFile(file)), context = context)
            else null
        }

        fun loadFromCSV(csv: String, context: Context): VocabularyGroup{
            val csvArray = csv.lines()
            val baseData = csvArray[1].split(";;")
            val name = baseData[0]
            val langMain = Locale.forLanguageTag(baseData[1])
            val langOther = Locale.forLanguageTag(baseData[2])

            val vocabulary = ArrayList<VocabularyWord>()
            for (line in 2 until csvArray.size-1){
                val wordAsList = csvArray[line].split(";;")
                Log.e("Test", wordAsList.toString())
                when(wordAsList[0].toInt()){
                    VocabularyWord.TYPE_TRANSLATION -> vocabulary.add(WordTranslation.loadFromCSV(csvArray[line], langMain = langMain, langOther = langOther))
                    VocabularyWord.TYPE_ANTONYM,  VocabularyWord.TYPE_SYNONYM ->  vocabulary.add(Synonym.loadFromCSV(csvArray[line], language = langMain))
                    VocabularyWord.TYPE_WORD_FAMILY ->  vocabulary.add(WordFamily.loadFromCSV(csvArray[line], language = langMain))
                }
            }

            return VocabularyGroup(name, Id.generate(context).apply { register(context) }, otherLanguage = langOther, mainLanguage = langMain, vocabulary = vocabulary)
        }


        fun isNameValid(context: Context, nameToCheck: String, ignoreName: String = ""): Int {
            val indexAppFile = File(context.filesDir, FileUtil.NAME_FILE_INDEX_VOCABULARY_GROUPS)

            if(nameToCheck.lines().size > MAX_LINES) {
                return INVALID_TOO_MANY_LINES
            }

            if (nameToCheck.length > MAX_CHARS)
                return INVALID_TOO_MANY_CHARS

            if(nameToCheck.contains(";;"))
                return INVAILD_CONTAINS_SEPERATORS

            if(nameToCheck.trim().isEmpty())
                return INVALID_EMPTY

            if (indexAppFile.exists()) {
                val indexVocabularyGroups = JSONObject(FileUtil.loadFromFile(indexAppFile)).getJSONArray("index")
                for (i in 0 until indexVocabularyGroups.length()) {
                    if ((indexVocabularyGroups.getJSONObject(i).getString("name") == nameToCheck.trim()) && (nameToCheck.trim() != ignoreName)) {
                        return INVALID_NAME_ALREADY_USED
                    }
                }
            }
            return VALID
        }
    }
}
