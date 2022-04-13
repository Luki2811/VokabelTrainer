package de.luki2811.dev.vokabeltrainer

class Exercise {

    /** var type = 0
    var languageKnown: Language
    var languageNew: Language
    var askKnownWord: Boolean
    val words: ArrayList<VocabularyWord> = arrayListOf()


    constructor(word: VocabularyWord, askKnownWord: Boolean){

        this.type = TYPE_TRANSLATE_TEXT

        this.words.add(word)
        this.languageKnown = word.languageKnown
        this.languageNew = word.languageNew
        this.askKnownWord = askKnownWord
    }

    constructor(words: ArrayList<VocabularyWord>){

        this.type = TYPE_MATCH_FIVE_WORDS

        for(w in words)
            this.words.add(w)

    }

    constructor(word: VocabularyWord, otherWords: ArrayList<VocabularyWord>){

        this.type = TYPE_CHOOSE_OF_THREE_WORDS

        this.words.add(word)

    } **/

    companion object{
        const val TYPE_TRANSLATE_TEXT = 1
        const val TYPE_CHOOSE_OF_THREE_WORDS = 2
        const val TYPE_MATCH_FIVE_WORDS = 3
    }
}