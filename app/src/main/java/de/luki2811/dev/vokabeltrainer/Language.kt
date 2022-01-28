package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.File

class Language(var type: Int, var context: Context) {
    var name: String = getNameFromType()

    fun refreshInIndex() {
        if (isTypeValid()){
            val indexFile = File(context.filesDir, AppFile.NAME_FILE_INDEX_LANGUAGES)
            val index = JSONObject(AppFile.loadFromFile(indexFile))
            index.put("lang$type", name)
            AppFile.writeInFile(index.toString(), indexFile)
        }else{
            Log.w("Warning","Language type \"$type\" is not valid !!")
        }
    }

     private fun getNameFromType(): String{
         return if (isTypeValid()){
             val indexFile = File(context.filesDir, AppFile.NAME_FILE_INDEX_LANGUAGES)
             val index = JSONObject(AppFile.loadFromFile(indexFile))
             index.getString("lang$type")

         }else{
             Log.w("Warning","Language type \"$type\" is not valid !!")
             "null"
         }
    }

    private fun isTypeValid() = type > 0 || type <= 10

    fun getShortName(): String? {
        if(name.equals("deutsch", ignoreCase = true)){
            return "de"
        }
        if(name.equals("englisch", ignoreCase = true)){
            return "en"
        }
        if(name.equals("französisch", ignoreCase = true)){
            return "fr"
        }
        if(name.equals("spanisch", ignoreCase = true)){
            return "es"
        }
        if(name.equals("russisch", ignoreCase = true)){
            return "ru"
        }
        if(name.equals("schwedisch", ignoreCase = true)){
            return "sv"
        }
        if(name.equals("norwegisch", ignoreCase = true)){
            return "no"
        }
        if(name.equals("chinesisch", ignoreCase = true)){
            return "zh"
        }
        if(name.equals("japanisch", ignoreCase = true)){
            return "ja"
        }
        if(name.equals("niederländisch", ignoreCase = true)){
            return "nl"
        }
        if(name.equals("dänisch", ignoreCase = true)){
            return "da"
        }
        if(name.equals("arabisch", ignoreCase = true)){
            return "ar"
        }
        if(name.equals("bulgarisch", ignoreCase = true)){
            return "bg"
        }
        if(name.equals("kroatisch", ignoreCase = true)){
            return "cz"
        }
        if(name.equals("finnisch", ignoreCase = true)){
            return "fi"
        }
        if(name.equals("koreanisch", ignoreCase = true)){
            return "ko"
        }
        if(name.equals("polnisch", ignoreCase = true)){
            return "po"
        }
        if(name.equals("portugisisch", ignoreCase = true)){
            return "pt"
        }
        if(name.equals("romänisch", ignoreCase = true)){
            return "ro"
        }
        if(name.equals("slovakisch", ignoreCase = true)){
            return "sk"
        }
        if(name.equals("slowenisch", ignoreCase = true)){
            return "sl"
        }
        if(name.equals("thailendisch", ignoreCase = true)){
            return "th"
        }
        if(name.equals("türkisch", ignoreCase = true)){
            return "tr"
        }
        if(name.equals("ukrainisch", ignoreCase = true)){
            return "uk"
        }
        if(name.equals("afrikanisch", ignoreCase = true)){
            return "af"
        }
        if(name.equals("tschechisch", ignoreCase = true)){
            return "cs"
        }

        return null
    }


    companion object {

        const val DEFAULT_0 = "Deutsch"
        const val DEFAULT_1 = "Englisch"
        const val DEFAULT_2 = "Französisch"
        const val DEFAULT_3 = "Spanisch"
        const val DEFAULT_4 = "Russisch"
        const val DEFAULT_5 = "Schwedisch"
        const val DEFAULT_6 = "Norwegisch"
        const val DEFAULT_7 = "Chinesisch"
        const val DEFAULT_8 = "Japanisch"
        const val DEFAULT_9 = "Lateinisch"

        fun getDefaultLanguageIndex(): JSONObject{
            return JSONObject()
                .put("lang0", DEFAULT_0)
                .put("lang1", DEFAULT_1)
                .put("lang2", DEFAULT_2)
                .put("lang3", DEFAULT_3)
                .put("lang4", DEFAULT_4)
                .put("lang5", DEFAULT_5)
                .put("lang6", DEFAULT_6)
                .put("lang7", DEFAULT_7)
                .put("lang8", DEFAULT_8)
                .put("lang9", DEFAULT_9)
        }
    }
}