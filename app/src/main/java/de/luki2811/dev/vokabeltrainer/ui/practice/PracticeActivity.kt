package de.luki2811.dev.vokabeltrainer.ui.practice

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.databinding.ActivityPracticeBinding
import de.luki2811.dev.vokabeltrainer.ui.MainActivity
import org.json.JSONObject


class PracticeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPracticeBinding
    // Typ 0 = FinishFragment
    private var correctInPercent: Int = 100
    private var typeOfLessonNow = 0
    private var numberOfExercises = 0
    private var numberOfExercisesTotal = 10
    private var numberOfWrongWords: Double = 0.0
    private var wrongWords: ArrayList<VocabularyWord> = arrayListOf()
    private lateinit var lesson: Lesson
    private var vocabularyGroups: ArrayList<VocabularyGroup> = arrayListOf()
    private var numberOfVocabularyWords = 0

    private var words: ArrayList<VocabularyWord> = arrayListOf()
    private lateinit var wordResult: VocabularyWord


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPracticeBinding.inflate(layoutInflater)

        lesson = Lesson(JSONObject(intent.getStringExtra("data_lesson")!!), applicationContext)

        for(i in lesson.vocabularyGroupIds){
            VocabularyGroup.loadFromFileWithId(Id(applicationContext,i), applicationContext)?.let {
                vocabularyGroups.add(it)
                numberOfVocabularyWords += it.vocabulary.size
            }
        }

        if(numberOfVocabularyWords < 10) {
            MaterialAlertDialogBuilder(this)
                .setMessage(R.string.err_lesson_enough_words_long)
                .setTitle(R.string.err)
                .setIcon(R.drawable.ic_outline_error_24)
                .setNegativeButton(R.string.ok){ _, _ ->
                    startActivity(Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                }
                .setOnCancelListener { startActivity(Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)) }
                .show()

            return
        }

        typeOfLessonNow = -1

        changeTypOfPractice(getNextLessonType())

        binding.buttonExitPractice.setOnClickListener { quitPractice(this, this) }


        supportFragmentManager.setFragmentResultListener("finished", this) { _, bundle ->
            if(!bundle.getString("wordResult").isNullOrEmpty()){
                wordResult = VocabularyWord(JSONObject(bundle.getString("wordResult")!!), applicationContext)

                if(wordResult.isWrong){
                    wrongWords.add(wordResult)
                    numberOfWrongWords += 1
                    numberOfExercisesTotal += 1
                }
            }

            numberOfExercises += 1

            binding.progressBarPractice.max = numberOfExercisesTotal
            binding.progressBarPractice.progress = numberOfExercises


            if(numberOfExercises < numberOfExercisesTotal + 1)
                changeTypOfPractice(getNextLessonType())
            else
                changeTypOfPractice(0)

        }

        setContentView(binding.root)
    }

    private fun newRandomWord(checkIfAlreadyUse: Boolean = true): VocabularyWord {
        var randomWord = vocabularyGroups[(0 until vocabularyGroups.size).random()].getRandomWord()

        if(checkIfAlreadyUse){
            if(numberOfVocabularyWords <= lesson.alreadyUsedWords.size) {
                lesson.alreadyUsedWords = arrayListOf()
                Toast.makeText(applicationContext, getString(R.string.vocabulary_group_restart), Toast.LENGTH_SHORT).show()
            }

            for(i in lesson.alreadyUsedWords){
                while(i == randomWord.newWord) {
                    randomWord = vocabularyGroups[(0 until vocabularyGroups.size).random()].getRandomWord()
                }
            }
        }

        return randomWord
    }


    private fun changeTypOfPractice(_type: Int){
        var type = _type
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_practice) as NavHostFragment

        words.clear()

        when{
            numberOfExercises < 10 ->{
                if(type == Exercise.TYPE_TRANSLATE_TEXT || type == Exercise.TYPE_CHOOSE_OF_THREE_WORDS){
                    words.add(0, newRandomWord(true))

                    words[0].isAlreadyUsed = true
                    lesson.alreadyUsedWords.add(words[0].newWord)

                    words[0].askKnownWord = if (lesson.askOnlyNewWords) false else (0..1).random() == 0

                    if(type == Exercise.TYPE_CHOOSE_OF_THREE_WORDS){
                        for (i in 1..2){
                            var newWord: VocabularyWord
                            do {
                                newWord = newRandomWord(false)
                            } while (newWord in words)
                            words.add(i, newWord)

                            words[i].askKnownWord = words[0].askKnownWord
                        }
                    }
                }else{
                    for(i in 0..4){
                        var newWord: VocabularyWord
                        do {
                            newWord = newRandomWord(false)
                        } while (newWord in words)
                        words.add(i, newWord)

                        words[i].askKnownWord = words[0].askKnownWord
                    }
                }
            }
            wrongWords.size != 0 -> {
                words.add(0, wrongWords[(0 until wrongWords.size).random()])
                words[0].isWrong = false
                type = words[0].typeWrong
                wrongWords.remove(words[0])

                if(type == Exercise.TYPE_CHOOSE_OF_THREE_WORDS){
                    for (i in 1 until 2){
                        var newWord: VocabularyWord
                        do {
                            newWord = newRandomWord(false)
                        } while (newWord in words)
                        words.add(i, newWord)

                        words[i].askKnownWord = words[0].askKnownWord
                    }
                }

                Toast.makeText(applicationContext, "Fehlerwort", Toast.LENGTH_SHORT).show()
            }
            else -> {
                type = 0
                lesson.saveInFile()
            }
        }

        val wordsInArray = ArrayList<String>()
        for (i in words){
            wordsInArray.add(i.getJson().toString())
        }

        Log.e("Info0", wordsInArray.toString())

        when(type){
            0 -> {
                // Calculate
                correctInPercent = 100-(numberOfWrongWords/(numberOfExercises+1)*100).toInt()
                navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeFinishFragment(correctInPercent))
            }
            Exercise.TYPE_TRANSLATE_TEXT -> {
                navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeTranslateTextFragment(lesson.settingReadOutBoth, words[0].getJson().toString()))
            }
            Exercise.TYPE_CHOOSE_OF_THREE_WORDS -> {
                navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeOutOfThreeFragment(wordAsJson = wordsInArray.toTypedArray(), settingsReadBoth = lesson.settingReadOutBoth))
            }
            Exercise.TYPE_MATCH_FIVE_WORDS -> {
                navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeMatchFiveWordsFragment(wordAsJson = wordsInArray.toTypedArray(), settingsReadBoth = lesson.settingReadOutBoth))
            }
            else -> {
                Toast.makeText(applicationContext, "Type ($type) isn't valid", Toast.LENGTH_LONG).show()
                Log.e("Warning", "Type ($type) isn't valid -> return to MainActivity")
                startActivity(Intent(applicationContext, MainActivity::class.java))
            }
        }
    }

    private fun getNextLessonType(): Int {
        var type = 0

        while(!lesson.typesOfLesson.contains(type)){
            type = (1..3).random()
        }
        return type
    }

    companion object {
        fun quitPractice(activity: Activity, context: Context){
            MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.quite_lesson))
                .setMessage(context.getString(R.string.do_you_really_want_to_leave_practice))
                .setPositiveButton(R.string.quite){_, _ ->
                    activity.startActivity(Intent(context, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                }
                .setNegativeButton(R.string.cancel){_,_ ->  }
                .show()
        }
    }
}