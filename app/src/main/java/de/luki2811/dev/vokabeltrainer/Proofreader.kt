package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.util.Log
import java.util.Locale

class Proofreader(private var originalStrings: ArrayList<String>,
                  private var stringToCorrect: String,
                  private val language: Locale,
                  private val allWordsNeeded: Boolean,
                  private val replaceShortForms: Boolean,
                  private val context: Context) {

    fun correct(ignoreCase: Boolean = false): Boolean{
        if(replaceShortForms){
            stringToCorrect = ShortForm.replaceShortFormsWithLongForms(stringToCorrect, language, context)
            for (i in 0 until originalStrings.size){
                originalStrings[i] = ShortForm.replaceShortFormsWithLongForms(originalStrings[i], language, context)
            }
        }

        val inputStrings = stringToCorrect.split(";").toMutableList()
        inputStrings.replaceAll { it.trim() }
        inputStrings.replaceAll { if(ignoreCase) it.lowercase() else it }

        Log.i("InputStrings",inputStrings.toString())

        val originalStringsFoo = ArrayList<String>()
        originalStringsFoo.addAll(originalStrings)
        originalStringsFoo.replaceAll { it.trim() }
        originalStringsFoo.replaceAll { if(ignoreCase) it.lowercase() else it }

        Log.i("originalStringsFoo",originalStringsFoo.toString())

        if(allWordsNeeded){
            originalStringsFoo.forEach {
                if(!inputStrings.contains(it.trim())){
                    return false
                }
            }
            return true
        }else{
            return originalStringsFoo.containsAll(inputStrings)
        }
    }

    fun getWrongCharIndices(ignoreCase: Boolean = false): ArrayList<Int>{
        if(allWordsNeeded || originalStrings.size > 1) return arrayListOf()

        val wrongChars = arrayListOf<Int>()

        for (char in 0 until originalStrings[0].length){
            try {
                if(!originalStrings[0][char].equals(stringToCorrect[char],ignoreCase)) {
                    Log.i("Proofreader", "Wrong char at: $char")
                    wrongChars.add(char)
                }
            }catch (e: StringIndexOutOfBoundsException){
                Log.i("Proofreader", "Wrong char at: $char")
                wrongChars.add(char)
            }
        }
        return wrongChars
    }

}