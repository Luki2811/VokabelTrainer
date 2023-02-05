package de.luki2811.dev.vokabeltrainer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Exercise(var type: Int = TYPE_UNKNOWN,
                    var isOtherWordAskedAsAnswer: Boolean = false,
                    var askAllWords: Boolean = false,
                    var readOut: ArrayList<Pair<Int, Boolean>> = arrayListOf(Lesson.READ_MAIN_LANGUAGE to false, Lesson.READ_OTHER_LANGUAGE to false),
                    var words: ArrayList<VocabularyWord> = arrayListOf()
                    ): Parcelable {

    /** constructor(json: JSONObject): this(){
        this.type = json.getInt("type")
        this.words = arrayListOf()
        for(i in 0 until json.getJSONArray("words").length())
            //this.words.add(WordTranslation.loadFromJSON(json.getJSONArray("words").getJSONObject(i)))
        this.typeOfWord = words[0].typeOfWord
        val readOutList = json.getString("readOut").split(';')
        this.readOut.add(0, (readOutList[0] == "true"))
        this.readOut.add(1, (readOutList[1] == "true"))
        this.askAllWords = json.getBoolean("askAllWords")
        this.isSecondWordAskedAsAnswer = json.getBoolean("askForSecondWord")
    } **/

    /** fun getJson(): JSONObject{
        val wordsForJson = JSONArray()
        words.forEach { wordsForJson.put(it.getAsJSON(false)) }
        val readOut = "${this.readOut[0]};${this.readOut[1]}"

        return JSONObject()
            .put("type", this.type)
            .put("words", wordsForJson)
            .put("readOut", readOut)
            .put("askAllWords", askAllWords)
            .put("askForSecondWord", isSecondWordAskedAsAnswer)
    } **/

    companion object{
        const val TYPE_UNKNOWN = -1
        const val TYPE_TRANSLATE_TEXT = 1
        const val TYPE_CHOOSE_OF_THREE_WORDS = 2
        const val TYPE_MATCH_FIVE_WORDS = 3
    }
}