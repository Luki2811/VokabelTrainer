package de.luki2811.dev.vokabeltrainer

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class Lesson {
    var name: String = ""
    var languageKnow: Language = Language(0)
    var languageNew: Language = Language(0)
    var vocs: Array<VocabularyWord> = arrayOf()
    var count = 0

    constructor(
        name: String,
        count: Int,
        languageKnow: Language,
        languageNew: Language,
        vocs: Array<VocabularyWord>
    ) {
        this.name = name
        this.languageKnow = languageKnow
        this.languageNew = languageNew
        this.vocs = vocs
        this.count = count
    }

    constructor(json: JSONObject) {
        try {
            name = json.getString("name")
            languageKnow = Language(json.getInt("languageNative"))
            languageNew = Language(json.getInt("languageNew"))
            vocs = Array(json.getJSONArray("vocabulary").length()){ VocabularyWord("","",false ) }
            for (i in 0 until json.getJSONArray("vocabulary").length()) {
                vocs[i] = VocabularyWord(
                    json.getJSONArray("vocabulary").getJSONObject(i).getString("native"),
                    json.getJSONArray("vocabulary").getJSONObject(i).getString("new"),
                    json.getJSONArray("vocabulary").getJSONObject(i).getBoolean("ignoreCase")
                )
            }
            count = json.getInt("count")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    val randomWord: VocabularyWord
        get() {
            val random = (Math.random() * count + 1).toInt()
            return vocs[random - 1]
        }

    fun getWordAtPos(pos: Int): VocabularyWord {
        return vocs[pos]
    }

    fun setWordAtPos(pos: Int, voc: VocabularyWord) {
        vocs[pos] = voc
    }

    val lessonAsJson: JSONObject?
        get() {
            val lektionAsJSON = JSONObject()
            return try {
                lektionAsJSON.put("name", name)
                lektionAsJSON.put("count", count)
                lektionAsJSON.put("languageNative", languageKnow.type)
                lektionAsJSON.put("languageNew", languageNew.type)
                val jsonArray = JSONArray()
                for (i in vocs.indices) {
                    val voc = JSONObject()
                    voc.put("ignoreCase", vocs[i].isIgnoreCase)
                    voc.put("new", vocs[i].newWord)
                    voc.put("native", vocs[i].knownWord)
                    jsonArray.put(voc)
                }
                lektionAsJSON.put("vocabulary", jsonArray)
                lektionAsJSON
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }
        }
}