package de.luki2811.dev.vokabeltrainer

class VocabularyWord(var knownWord: String, var newWord: String, var isIgnoreCase: Boolean) {
    var isWrong = false
    var typeWrong = 1
    var askKnownWord = false
    var isAlreadyUsed = false

    fun getKnownWordList(): List<String>{
        val knownWordsList = knownWord.split(";")
        for (i in knownWordsList){
            i.trim()
        }
        return knownWordsList
    }

}