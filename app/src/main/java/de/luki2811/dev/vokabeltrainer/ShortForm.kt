package de.luki2811.dev.vokabeltrainer

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*

data class ShortForm(
    var shortForm: String,
    var longForm: String,
    var language: Locale): Exportable {

    override val type = Exportable.TYPE_SHORT_FORM

    fun getAsJson(): JSONObject{
        return JSONObject()
            .put("short",shortForm)
            .put("long", longForm)
            .put("language", language.language)
    }

    fun saveInFile(context: Context){
        val file = File(context.filesDir, FileUtil.NAME_FILE_SHORT_FORMS)
        val json = JSONArray(FileUtil.loadFromFile(file))
        json.put(getAsJson())
        FileUtil.writeInFile(json.toString(), file)
    }

    fun deleteInFile(context: Context){
        val file = File(context.filesDir, FileUtil.NAME_FILE_SHORT_FORMS)
        val json = JSONArray(FileUtil.loadFromFile(file))
        for (i in 0 until json.length()){
            if(json.getJSONObject(i).equals(getAsJson()))
                json.remove(i)
        }
        FileUtil.writeInFile(json.toString(), file)
    }

    companion object{
        fun setNewShortForms(context: Context, arrayList: ArrayList<ShortForm>) {
            val jsonArray = JSONArray()
            arrayList.forEach{
                jsonArray.put(it.getAsJson())
            }

            val file = File(context.filesDir, FileUtil.NAME_FILE_SHORT_FORMS)
            FileUtil.writeInFile(jsonArray.toString(), file)
        }

        fun fromJson(jsonObject: JSONObject): ShortForm{
            return ShortForm(
                jsonObject.getString("short"),
                jsonObject.getString("long"),
                Locale(jsonObject.getString("language"))
            )
        }

        fun loadAllShortForms(context: Context): ArrayList<ShortForm>{
            val allShortForms = arrayListOf<ShortForm>()
            val file = File(context.filesDir, FileUtil.NAME_FILE_SHORT_FORMS)
            val json = JSONArray(FileUtil.loadFromFile(file))
            for (i in 0 until json.length()){
                allShortForms.add(ShortForm(json.getJSONObject(i).getString("short"), json.getJSONObject(i).getString("long"), Locale(json.getJSONObject(i).getString("language"))))
            }
            return allShortForms
        }

        fun getSomeKnownShortForms() = ArrayList<ShortForm>().apply {
            add(ShortForm("etw.", "etwas", Locale.GERMAN))
            add(ShortForm("pl.", "plural", Locale.GERMAN))
            add(ShortForm("sg.", "singular", Locale.GERMAN))

            add(ShortForm("m.", "masculin", Locale.FRENCH))
            add(ShortForm("f.", "féminin", Locale.FRENCH))
            add(ShortForm("qn.", "quelqu'un", Locale.FRENCH))
            add(ShortForm("qc.", "quelque chose", Locale.FRENCH))
            add(ShortForm("pl.", "pluriel", Locale.FRENCH))
            add(ShortForm("sg.", "singulier", Locale.FRENCH))

            add(ShortForm("pl.", "plural", Locale.ENGLISH))
            add(ShortForm("sg.", "singular", Locale.ENGLISH))
            add(ShortForm("sth.", "something", Locale.ENGLISH))
            add(ShortForm("sb.", "somebody", Locale.ENGLISH))
        }
    }

    override fun export(): JSONObject {
        return getAsJson()
    }
}
