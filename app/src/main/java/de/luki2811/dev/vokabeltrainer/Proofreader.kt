package de.luki2811.dev.vokabeltrainer

import android.util.Log

class Proofreader(private var originalStrings: ArrayList<String>,
                  private var stringToCorrect: String,
                  private val allWordsNeeded: Boolean) {

    fun replaceShortForms(listOfShortForm: ArrayList<ShortForm>){
        for (shortForm in listOfShortForm) {
            for(i in 0 until originalStrings.size){
                originalStrings[i] = originalStrings[i].replace(shortForm.shortForm, shortForm.longForm)
            }
            stringToCorrect = stringToCorrect.replace(shortForm.shortForm, shortForm.longForm)
        }
    }

    fun correct(ignoreCase: Boolean = false): Boolean{
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