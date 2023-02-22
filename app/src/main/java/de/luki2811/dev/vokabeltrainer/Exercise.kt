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

    companion object{
        const val TYPE_UNKNOWN = -1
        const val TYPE_TRANSLATE_TEXT = 1
        const val TYPE_CHOOSE_OF_THREE_WORDS = 2
        const val TYPE_MATCH_FIVE_WORDS = 3
    }
}