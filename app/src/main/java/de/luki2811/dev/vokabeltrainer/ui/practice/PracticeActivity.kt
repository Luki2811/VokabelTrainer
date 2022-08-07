package de.luki2811.dev.vokabeltrainer.ui.practice

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.databinding.ActivityPracticeBinding
import de.luki2811.dev.vokabeltrainer.ui.MainActivity
import kotlinx.coroutines.Runnable
import org.json.JSONObject
import kotlin.math.roundToInt

class PracticeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPracticeBinding

    private var mode = -1

    private var position = 0
    private var mistakes: ArrayList<Mistake> = arrayListOf()
    private var words: ArrayList<VocabularyWord> = arrayListOf()
    private lateinit var lesson: Lesson
    private var vocabularyGroups: ArrayList<VocabularyGroup> = arrayListOf()
    private var allVocabularyWords: ArrayList<VocabularyWord> = arrayListOf()

    private var mistakesToPractice: ArrayList<Mistake> = arrayListOf()
    private var alreadyUsedMistakes = arrayListOf<Mistake>()

    private var timeInSeconds = 0
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPracticeBinding.inflate(layoutInflater)
        // Setup Views
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_practice) as NavHostFragment
        binding.buttonExitPractice.setOnClickListener { quitPractice(this, this) }

        startTimer()

        // Setup Lesson
        if(!intent.getStringExtra("data_lesson").isNullOrEmpty()){
            mode = MODE_NORMALE
            lesson = Lesson(JSONObject(intent.getStringExtra("data_lesson")!!), applicationContext)
            vocabularyGroups = lesson.loadVocabularyGroups()
            vocabularyGroups.forEach { allVocabularyWords.addAll(it.vocabulary) }

            if(allVocabularyWords.size < lesson.numberOfExercises) {
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
            changeTypOfPractice()
        }
        // To practice mistakes
        else{
            mode = MODE_PRACTICE_MISTAKES
            val allMistakes = Mistake.loadAllFromFile(this)
            try {
                for (i in 0..9){
                    mistakesToPractice.add(allMistakes.filter { !mistakesToPractice.contains(it) }.random())
                }
            }catch (e: NoSuchElementException){
                MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.err)
                    .setTitle(R.string.err)
                    .setIcon(R.drawable.ic_outline_error_24)
                    .setNegativeButton(R.string.ok){ _, _ ->
                        startActivity(Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    }
                    .setOnCancelListener {
                        startActivity(Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    }
                    .show()
                e.printStackTrace()
                return
            }

            setNewMistake()
        }

        supportFragmentManager.setFragmentResultListener("finished", this) { _, bundle ->

            if(!bundle.getString("wordMistake").isNullOrEmpty()){
                val mistake = Mistake(JSONObject(bundle.getString("wordMistake")!!))
                mistake.position = position
                mistakes.add(mistake)
            }

            binding.progressBarPractice.progress = position

            if(mode == MODE_NORMALE) {
                binding.progressBarPractice.max = lesson.numberOfExercises + mistakes.size
                changeTypOfPractice()
            } else if(mode == MODE_PRACTICE_MISTAKES){
                binding.progressBarPractice.max = 10 + mistakes.size
                alreadyUsedMistakes.last().removeFromFile(this)
                setNewMistake()
            }
        }

        supportFragmentManager.setFragmentResultListener("sendToMistakes", this) { _, _ ->
            val mistakesAsArrayListString = arrayListOf<String>()
            mistakes.forEach { mistakesAsArrayListString.add(it.getAsJson().toString()) }

            binding.progressBarPractice.visibility = View.GONE
            binding.buttonExitPractice.visibility = View.GONE
            binding.textViewPracticeInfoMistake.visibility = View.GONE

            if(mode == MODE_NORMALE)
                navHostFragment.navController.navigate(PracticeFinishFragmentDirections.actionPracticeFinishFragmentToPracticeMistakesFragment(mistakesAsArrayListString.toTypedArray(), lesson.numberOfExercises + mistakes.size))
            else
                navHostFragment.navController.navigate(PracticeFinishFragmentDirections.actionPracticeFinishFragmentToPracticeMistakesFragment(mistakesAsArrayListString.toTypedArray(), 10 + mistakes.size))

        }

        setContentView(binding.root)
        hideSystemUI()

        /** ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutPracticeActivity)) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            if(imeVisible) showSystemUI() else hideSystemUI()
            insets
        } **/
    }

    private fun setNewMistake(){
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_practice) as NavHostFragment
        var type = 0

        words.clear()
        position += 1

        when{
            position <= 10 -> {
                val tempMistake = mistakesToPractice.filter { !alreadyUsedMistakes.contains(it) }.random()
                words.add(0, tempMistake.word)
                alreadyUsedMistakes.add(tempMistake)
                type = Exercise.TYPE_TRANSLATE_TEXT
            }
            mistakes.any { !it.isRepeated } -> {
                val mistake = mistakes.filter { !it.isRepeated }.random()
                words.add(mistake.word)
                type = Exercise.TYPE_TRANSLATE_TEXT
                mistakes[mistakes.indexOf(mistake)].isRepeated = true
                binding.textViewPracticeInfoMistake.visibility = View.VISIBLE
            }
        }

        when(type){
            0 -> {
                binding.progressBarPractice.visibility = View.GONE
                binding.buttonExitPractice.visibility = View.GONE
                binding.textViewPracticeInfoMistake.visibility = View.GONE
                // Calculate
                val correctInPercent: Double = 100-(mistakes.size.toDouble()/(mistakes.size + 10)*100)
                stopTimer()
                navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeFinishFragment(
                    correctInPercent.roundToInt(), mistakes.size, timeInSeconds = this.timeInSeconds))
            }
            Exercise.TYPE_TRANSLATE_TEXT -> {
                navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeTranslateTextFragment(false, words[0].getJson().toString()))
            }
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, findViewById(R.id.layoutPracticeActivity)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, findViewById(R.id.layoutPracticeActivity)).show(WindowInsetsCompat.Type.systemBars())
    }

    private fun newRandomWord(checkIfAlreadyUse: Boolean = true): VocabularyWord {
        var randomWord = vocabularyGroups[(0 until vocabularyGroups.size).random()].getRandomWord()

        if(checkIfAlreadyUse){
            if(allVocabularyWords.size <= lesson.alreadyUsedWords.size) {
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

    private fun changeTypOfPractice() {
        var type = getNextLessonType()
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_practice) as NavHostFragment

        words.clear()
        position += 1

        when{
            position <= lesson.numberOfExercises ->{
                binding.textViewPracticeInfoMistake.visibility = View.GONE
                if(type == Exercise.TYPE_TRANSLATE_TEXT || type == Exercise.TYPE_CHOOSE_OF_THREE_WORDS){
                    words.add(0, newRandomWord(true))

                    words[0].isAlreadyUsed = true
                    lesson.alreadyUsedWords.add(words[0].newWord)

                    words[0].isKnownWordAskedAsAnswer = if (lesson.askOnlyNewWords) false else (0..1).random() == 0

                    if(type == Exercise.TYPE_CHOOSE_OF_THREE_WORDS){
                        for (i in 1..2){
                            var newWord: VocabularyWord
                            do {
                                newWord = newRandomWord(false)
                            } while (newWord in words)
                            words.add(i, newWord)

                            words[i].isKnownWordAskedAsAnswer = words[0].isKnownWordAskedAsAnswer
                        }
                    }
                }else{
                    for(i in 0..4){
                        var newWord: VocabularyWord
                        do {
                            newWord = newRandomWord(false)
                        } while (newWord in words)
                        words.add(i, newWord)

                        words[i].isKnownWordAskedAsAnswer = words[0].isKnownWordAskedAsAnswer
                    }
                }
            }

            mistakes.any { !it.isRepeated } -> {
                val mistake = mistakes.filter { !it.isRepeated }.random()
                words.add(mistake.word)
                type = mistake.typeOfPractice
                mistake.addToFile(this)
                mistakes[mistakes.indexOf(mistake)].isRepeated = true

                if(type == Exercise.TYPE_CHOOSE_OF_THREE_WORDS){
                    for (i in 1 until 3){
                        var newWord: VocabularyWord
                        do {
                            newWord = newRandomWord(false)
                        } while (newWord in words)
                        Log.e("Test/TypeAddWord","Add a word")
                        words.add(i, newWord)

                        words[i].isKnownWordAskedAsAnswer = words[0].isKnownWordAskedAsAnswer
                    }
                }
                binding.textViewPracticeInfoMistake.visibility = View.VISIBLE
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

        when(type){
            0 -> {
                binding.progressBarPractice.visibility = View.GONE
                binding.buttonExitPractice.visibility = View.GONE
                binding.textViewPracticeInfoMistake.visibility = View.GONE
                // Calculate
                val correctInPercent: Double = 100-(mistakes.size.toDouble()/(mistakes.size + lesson.numberOfExercises)*100)
                stopTimer()
                navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeFinishFragment(correctInPercent.roundToInt(), mistakes.size, timeInSeconds = this.timeInSeconds))
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
                Log.e("Exception", "Type ($type) isn't valid -> return to MainActivity")
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

    // Stopwatch functions
    private fun startTimer(){
        handler = Handler(Looper.getMainLooper())
        statusChecker.run()
    }

    private fun stopTimer() {
        handler.removeCallbacks(statusChecker)
    }

    // private fun resetTimer(){ timeInSeconds = 0 }

    private var statusChecker: Runnable = object: Runnable{
        override fun run() {
            try {
                timeInSeconds += 1
               // Log.i("Runnable","statusChecker: $timeInSeconds")
            } finally {
                handler.postDelayed(this, 1000L)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        showSystemUI()
        stopTimer()
    }

    companion object {

        const val MODE_NORMALE = 0
        const val MODE_PRACTICE_MISTAKES = 1

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