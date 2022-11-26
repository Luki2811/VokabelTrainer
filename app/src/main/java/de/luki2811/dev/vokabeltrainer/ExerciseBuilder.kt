package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.util.Log
import android.widget.Toast

class ExerciseBuilder(
    private var allWordsToSelectFrom: ArrayList<VocabularyWord>,
    private val askAllWords: Boolean,
    private val readOut: ArrayList<Boolean>,
    private val typesOfLesson: ArrayList<Int>,
    private val askForSecondWordsOnly: Boolean,
    private val practiceMistake: Boolean,
    private val typesOfWordsToPractice: ArrayList<Int>,
    private val mistake: ArrayList<Mistake>? = null,
    private val context: Context) {


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
                    exercise.words.add(i, allWordsToSelectFrom.filter { !exercise.words.contains(it) && it.typeOfWord == VocabularyWord.TYPE_TRANSLATION }.random())

            } catch (e: IndexOutOfBoundsException){
                Log.e("ExerciseBuilder", e.toString())
                exercise.type = Exercise.TYPE_CHOOSE_OF_THREE_WORDS
            } catch (e: NoSuchElementException) {
                Log.w("ExerciseBuilder", e.toString())
                exercise.type = Exercise.TYPE_CHOOSE_OF_THREE_WORDS
            }
        }

        if (exercise.type == Exercise.TYPE_CHOOSE_OF_THREE_WORDS) {
            try{
                exercise.words.add(1, allWordsToSelectFrom.filter { !exercise.words.contains(it) && it.typeOfWord == VocabularyWord.TYPE_TRANSLATION }.random())
                exercise.words.add(2, allWordsToSelectFrom.filter { !exercise.words.contains(it) && it.typeOfWord == VocabularyWord.TYPE_TRANSLATION }.random())
            } catch (e: IndexOutOfBoundsException){
                Log.e("ExerciseBuilder", e.toString())
                exercise.type = Exercise.TYPE_TRANSLATE_TEXT
            }catch (e: NoSuchElementException){
                Log.w("ExerciseBuilder", e.toString())
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
            try {
                allWordsToSelectFrom.filter { typesOfWordsToPractice.contains(it.typeOfWord) &&  !it.alreadyUsedInExercise}.minWith(Comparator.comparingInt { it.level })
            } catch (e: NoSuchElementException){
                Log.e("ExerciseBuilder", "No words with selected filter")
                Toast.makeText(context, context.getText(R.string.err_no_words_found_with_filter), Toast.LENGTH_LONG).show()
                allWordsToSelectFrom.forEach {
                    it.alreadyUsedInExercise = false
                }
                allWordsToSelectFrom.filter { typesOfWordsToPractice.contains(it.typeOfWord) &&  !it.alreadyUsedInExercise}.minWith(Comparator.comparingInt { it.level })
            }
        }
    }

}