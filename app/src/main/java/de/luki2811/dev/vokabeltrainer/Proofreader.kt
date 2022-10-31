package de.luki2811.dev.vokabeltrainer

import android.util.Log

class Proofreader(private val originalStrings: ArrayList<String>,
                  private val stringToCorrect: String,
                  private val allWordsNeeded: Boolean) {

    fun correct(ignoreCase: Boolean): Boolean{
        if(allWordsNeeded){
            val inputStrings = stringToCorrect.split(";").toMutableList()
            inputStrings.replaceAll { it.trim() }
            inputStrings.replaceAll { if(ignoreCase) it.lowercase() else it }

            val originalStringsFoo = ArrayList<String>()
            originalStringsFoo.addAll(originalStrings)
            originalStringsFoo.replaceAll { it.trim() }
            originalStringsFoo.replaceAll { if(ignoreCase) it.lowercase() else it }

            originalStringsFoo.forEach {
                if(!inputStrings.contains(it.trim())){
                    return false
                }
            }
            return true

        }else{
            originalStrings.forEach {
                if(it.trim().equals(stringToCorrect.trim(), ignoreCase))
                    return true
            }
            return false
        }
    }

    fun getWrongCharIndices(ignoreCase: Boolean = true): ArrayList<Int>{
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