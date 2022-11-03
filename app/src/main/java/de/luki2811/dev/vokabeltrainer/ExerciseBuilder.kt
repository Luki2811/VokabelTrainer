package de.luki2811.dev.vokabeltrainer

import android.util.Log

class ExerciseBuilder(
    private var allWordsToSelectFrom: ArrayList<VocabularyWord>,
    private val askAllWords: Boolean,
    private val readOut: ArrayList<Boolean>,
    private val typesOfLesson: ArrayList<Int>,
    private val askForSecondWordsOnly: Boolean,
    private val practiceMistake: Boolean,
    private val mistake: ArrayList<Mistake>? = null) {


    fun build(): Exercise{
        val exercise = Exercise()

        exercise.words.add(0, getWordToPractice())
        exercise.type = getTypeToPractice(exercise.words[0].typeOfWord)
        exercise.askAllWords = askAllWords
        exercise.readOut = readOut
        exercise.isSecondWordAskedAsAnswer = getAskSecondWord(exercise.words[0])

        // Add more words for an exercise


        if(exercise.type == Exercise.TYPE_MATCH_FIVE_WORDS){
            try {
                for(i in 1..4)
                    exercise.words.add(i, allWordsToSelectFrom.filter { !exercise.words.contains(it) }.random())

            } catch (e: IndexOutOfBoundsException){
                Log.e("ExerciseBuilder", e.toString())
                exercise.type = Exercise.TYPE_CHOOSE_OF_THREE_WORDS
            }
        }

        if (exercise.type == Exercise.TYPE_CHOOSE_OF_THREE_WORDS) {
            try{
                exercise.words.add(1, allWordsToSelectFrom.filter { !exercise.words.contains(it) }.random())
                exercise.words.add(2, allWordsToSelectFrom.filter { !exercise.words.contains(it) }.random())
            } catch (e: IndexOutOfBoundsException){
                Log.e("ExerciseBuilder", e.toString())
                exercise.type = Exercise.TYPE_TRANSLATE_TEXT
            }
        }

        return exercise
    }

    private fun getAskSecondWord(word: VocabularyWord): Boolean{
        return if(practiceMistake){
            mistake?.filter { !it.isRepeated }?.find { it.word == word }!!.askedForSecondWord
        }else if(askForSecondWordsOnly || word.typeOfWord == VocabularyWord.TYPE_SYNONYM || word.typeOfWord == VocabularyWord.TYPE_ANTONYM) { true } else { (0..1).random() == 1 }
    }

    private fun getTypeToPractice(typeOfWord: Int): Int{
        return if(practiceMistake) {
            Exercise.TYPE_TRANSLATE_TEXT
        } else if(typeOfWord == VocabularyWord.TYPE_SYNONYM || typeOfWord == VocabularyWord.TYPE_ANTONYM){
            Exercise.TYPE_TRANSLATE_TEXT
        } else{
            typesOfLesson.random()
        }
    }

    private fun getWordToPractice(): VocabularyWord{
        return if(practiceMistake){
            mistake!!.filter { !it.isRepeated }.random().word
        }else{
            allWordsToSelectFrom.shuffle()
            allWordsToSelectFrom.filter { !it.alreadyUsedInExercise }.minWith(Comparator.comparingInt { it.level })
        }
    }

}