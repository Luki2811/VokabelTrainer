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
    private var typeOfPractice = -1
    private var typesOfLesson = arrayListOf<Int>()
    private var mistakes: ArrayList<Mistake> = arrayListOf()
    private var words: ArrayList<VocabularyWord> = arrayListOf()
    private var numberOfExercises = 10
    private var readOutBoth = true
    private var askOnlyNewWords = false
    private var allVocabularyWords: ArrayList<VocabularyWord> = arrayListOf()
    private var correctInARow: Int = 0
    private var alreadyUsedWords = arrayListOf<VocabularyWord>()

    // Timer
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
            mode = MODE_NORMAL
            val lesson = Lesson(JSONObject(intent.getStringExtra("data_lesson")!!), applicationContext)

            lesson.loadVocabularyGroups().forEach { group ->
                allVocabularyWords.addAll(group.vocabulary)
            }

            numberOfExercises = lesson.numberOfExercises
            readOutBoth = lesson.settingReadOutBoth
            typesOfLesson = lesson.typesOfLesson
            askOnlyNewWords = lesson.askOnlyNewWords
            alreadyUsedWords = lesson.alreadyUsedWords

        }
        // To practice mistakes
        else{
            mode = MODE_PRACTICE_MISTAKES
            val allMistakes = Mistake.loadAllFromFile(this)

            typesOfLesson.add(Exercise.TYPE_TRANSLATE_TEXT)
            numberOfExercises = intent.getIntExtra("numberOfMistakes",-1)
            readOutBoth = intent.getBooleanExtra("readOutBoth", false)
            alreadyUsedWords = arrayListOf()

            for (i in 0 until numberOfExercises){
                allVocabularyWords.add(allMistakes.filter { !allVocabularyWords.contains(it.word) }.random().word)
            }
        }

        if(allVocabularyWords.size < numberOfExercises) {
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

        setNext()

        supportFragmentManager.setFragmentResultListener("finished", this) { _, bundle ->

            correctInARow += 1

            if(!bundle.getString("wordMistake").isNullOrEmpty()){
                val mistake = Mistake(JSONObject(bundle.getString("wordMistake")!!))
                mistake.position = position
                mistakes.add(mistake)
                correctInARow = 0
                mistake.addToFile(this)
            }

            binding.progressBarPractice.progress = position
            binding.progressBarPractice.max = numberOfExercises + mistakes.size

            /** TODO: Add again after redesign of
            if(mode == MODE_PRACTICE_MISTAKES){
                Mistake.loadAllFromFile(this).forEach {
                    if(it.word.getJson().toString() == words[0].getJson().toString()) it.removeFromFile(this)
                }
            }
            **/
            setNext()
        }

        supportFragmentManager.setFragmentResultListener("sendToMistakes", this) { _, _ ->
            val mistakesAsArrayListString = arrayListOf<String>()
            mistakes.forEach { mistakesAsArrayListString.add(it.getAsJson().toString()) }

            binding.progressBarPractice.visibility = View.GONE
            binding.buttonExitPractice.visibility = View.GONE
            binding.textViewPracticeInfoMistake.visibility = View.GONE

            navHostFragment.navController.navigate(PracticeFinishFragmentDirections.actionPracticeFinishFragmentToPracticeMistakesFragment(mistakesAsArrayListString.toTypedArray(), numberOfExercises + mistakes.size))
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

    /**
     * Get a vocabulary word
     * @param checkForAlreadyUsed Set true if check for already used AND add random word to alreadyUsedWords
     * @return Random vocabulary word from allVocabularyWords
     */
    private fun getRandomWord(checkForAlreadyUsed: Boolean = true): VocabularyWord{

        /** allVocabularyWords.forEach { allWords ->
            alreadyUsedWords.forEach { usedWords ->
                if(usedWords.equalsVocabularyWord(allWords)){
                    Log.i("allWords", allWords.getJson(false).toString())
                    Log.i("usedWord",usedWords.getJson(false).toString())
                }
            }
        } **/

        // Log.i("Test", allVocabularyWords.filter { alreadyUsedWords.contains(it) }.toString())

        return if(checkForAlreadyUsed) {
            val word: VocabularyWord = if(allVocabularyWords.all {alreadyUsedWords.contains(it) }){
                Log.i("Practice", "Cleared alreadyUsedWords")
                alreadyUsedWords.clear()
                Toast.makeText(this, "clear alreadyUsedWords", Toast.LENGTH_SHORT).show()
                allVocabularyWords.random()
            }else
                allVocabularyWords.filter { !alreadyUsedWords.contains(it) }.random()

            alreadyUsedWords.add(word.apply { isKnownWordAskedAsAnswer = false })
            word
        }
        else
            allVocabularyWords.random()
    }

    private fun setNext(){
        reset()
        position += 1
        typeOfPractice = getNextLessonType()
        when{
            position <= numberOfExercises -> setNextWord()
            mistakes.any { !it.isRepeated } -> setNextMistake()
            else -> typeOfPractice = 0
        }
        changeFragment()
    }

    /**
     * Set as word a normal word from allVocabularyWords
     */
    private fun setNextWord(){
        if(typeOfPractice != Exercise.TYPE_MATCH_FIVE_WORDS){
            words.add(0, getRandomWord(true))
            words[0].isKnownWordAskedAsAnswer = if (askOnlyNewWords) false else (0..1).random() == 0
        }else {
            words.add(0, getRandomWord(false))
            for(i in 1..4)
                words.add(i, allVocabularyWords.filter { !words.contains(it) }.random())
        }

        if(typeOfPractice == Exercise.TYPE_CHOOSE_OF_THREE_WORDS){
            words.add(1, allVocabularyWords.filter { !words.contains(it) }.random())
            words.add(2, allVocabularyWords.filter { !words.contains(it) }.random())
        }

        words.forEach {
            it.isKnownWordAskedAsAnswer = words[0].isKnownWordAskedAsAnswer
        }
    }

    /**
     * Set as word a mistake made in this lesson before
     */
    private fun setNextMistake(){
        binding.textViewPracticeInfoMistake.visibility = View.VISIBLE

        val mistake = mistakes.filter { !it.isRepeated }.random()
        typeOfPractice = mistake.typeOfPractice
        words.add(0, mistake.word)

        if(typeOfPractice == Exercise.TYPE_CHOOSE_OF_THREE_WORDS){
            words.add(1, allVocabularyWords.filter { !words.contains(it) }.random())
            words.add(2, allVocabularyWords.filter { !words.contains(it) }.random())
            words.forEach {
                it.isKnownWordAskedAsAnswer = words[0].isKnownWordAskedAsAnswer
            }
        }

        mistakes[mistakes.indexOf(mistake)].isRepeated = true
    }

    /**
     * Clear words and disable some views if necessary
     */
    private fun reset(){
        words.clear()
        binding.textViewPracticeInfoMistake.apply {
            visibility = View.GONE
        }

        if(correctInARow >= 2){
            binding.textViewPracticeCorrectInRow.apply {
                visibility = View.VISIBLE
                text = getString(R.string.correct_in_row, correctInARow)
            }
        }else{
            binding.textViewPracticeCorrectInRow.apply {
                visibility = View.GONE
                text = ""
            }
        }
    }

    /**
     * Change fragment
     */
    private fun changeFragment(){
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_practice) as NavHostFragment

        val wordsInArray = ArrayList<String>()
        for (i in words){
            wordsInArray.add(i.getJson().toString())
        }
        when(typeOfPractice){
            0 -> {
                binding.progressBarPractice.visibility = View.GONE
                binding.buttonExitPractice.visibility = View.GONE
                binding.textViewPracticeInfoMistake.visibility = View.GONE
                binding.textViewPracticeCorrectInRow.visibility = View.GONE
                if(mode == MODE_NORMAL) {
                    val lesson = Lesson(JSONObject(intent.getStringExtra("data_lesson")!!), applicationContext)
                    lesson.alreadyUsedWords = alreadyUsedWords
                    lesson.saveInFile()
                }
                // Calculate
                val correctInPercent: Double = 100-(mistakes.size.toDouble()/(mistakes.size + numberOfExercises)*100)
                stopTimer()
                navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeFinishFragment(correctInPercent.roundToInt(), mistakes.size, timeInSeconds = this.timeInSeconds, numberOfExercises))
            }
            Exercise.TYPE_TRANSLATE_TEXT -> {
                navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeTranslateTextFragment(readOutBoth, words[0].getJson().toString()))
            }
            Exercise.TYPE_CHOOSE_OF_THREE_WORDS -> {
                navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeOutOfThreeFragment(wordAsJson = wordsInArray.toTypedArray(), settingsReadBoth = readOutBoth))
            }
            Exercise.TYPE_MATCH_FIVE_WORDS -> {
                navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeMatchFiveWordsFragment(wordAsJson = wordsInArray.toTypedArray(), settingsReadBoth = readOutBoth))
            }
            else -> {
                Toast.makeText(applicationContext, "Type ($typeOfPractice) isn't valid", Toast.LENGTH_LONG).show()
                Log.e("Exception", "Type ($typeOfPractice) isn't valid -> return to MainActivity")
                startActivity(Intent(applicationContext, MainActivity::class.java))
            }
        }
    }

    /**
     * Get next type for a normal lesson
     * @return Number between 1-3
     */
    private fun getNextLessonType(): Int = typesOfLesson.random()

    /**
     * Start stopwatch
     */
    private fun startTimer(){
        handler = Handler(Looper.getMainLooper())
        statusChecker.run()
    }

    /**
     * Stop stopwatch
     */
    private fun stopTimer() {
        handler.removeCallbacks(statusChecker)
    }

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

        const val MODE_NORMAL = 0
        const val MODE_PRACTICE_MISTAKES = 1

        /**
         * Add a dialog before leaving the activity
         *
         */
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