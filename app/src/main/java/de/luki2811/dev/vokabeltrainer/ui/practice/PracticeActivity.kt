package de.luki2811.dev.vokabeltrainer.ui.practice

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
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
import java.time.LocalDate
import kotlin.math.roundToInt

class PracticeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPracticeBinding

    private var mode: Int = -1
    private var position = 0
    private var numberOfExercises = 10
    private var correctInARow: Int = 0
    private var allVocabularyWords: ArrayList<VocabularyWord> = arrayListOf()
    private var mistakes: ArrayList<Mistake> = arrayListOf()
    private lateinit var exercise: Exercise

    // Timer
    private var timeInSeconds = 0
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPracticeBinding.inflate(layoutInflater)
        // Setup Views
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_practice) as NavHostFragment
        binding.buttonExitPractice.setOnClickListener { quitPractice(this, this) }

        if(!intent.getStringExtra("data_lesson").isNullOrEmpty()){
            mode = MODE_NORMAL
            val lesson: Lesson = Lesson.fromJSON(JSONObject(intent.getStringExtra("data_lesson")!!), applicationContext, false)!!
            lesson.loadVocabularyGroups(applicationContext).forEach { group ->
                allVocabularyWords.addAll(group.vocabulary)
            }
            numberOfExercises = lesson.numberOfExercises

        }else{
            mode = MODE_PRACTICE_MISTAKES
            val allMistakes = Mistake.loadAllFromFile(this)
            numberOfExercises = intent.getIntExtra("numberOfMistakes",-1)

            for (i in 0 until numberOfExercises){
                allVocabularyWords.add(allMistakes.filter { !allVocabularyWords.contains(it.word) }.random().word)
            }
        }

        startTimer()

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

        changeFragment()

        supportFragmentManager.setFragmentResultListener("finished", this) { _, bundle ->

            val exerciseResult: ExerciseResult? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable("result", ExerciseResult::class.java)
            } else {
                bundle.getParcelable("result")
            }

            if(exerciseResult == null) {
                Log.e("Practice","Failed to get exercise result")
                startActivity(Intent(applicationContext, MainActivity::class.java))
                return@setFragmentResultListener
            }

            if(position > numberOfExercises){
                mistakes.forEach {
                    if(it.word == exercise.words[0])
                        it.isRepeated = true
                }
            }

            Log.i("Mistakes", mistakes.toString())

            if(exerciseResult.isCorrect){
                correctInARow += 1
            }else{
                correctInARow = 0

                val newMistake = Mistake(exercise.words[0], exerciseResult.answer, exercise.type, LocalDate.now(), position, exercise.isSecondWordAskedAsAnswer)
                mistakes.add(newMistake)
                newMistake.addToFile(this)
            }


            binding.progressBarPractice.setProgress(position, true)
            binding.progressBarPractice.max = numberOfExercises + mistakes.size


            if(mode == MODE_PRACTICE_MISTAKES){
                Mistake.loadAllFromFile(this).find { it.word == exercise.words[0] }?.removeFromFile(this)

            }else if(mode == MODE_NORMAL){
                val lesson = Lesson.fromJSON(JSONObject(intent.getStringExtra("data_lesson")!!), applicationContext, false)!!

                if(exerciseResult.isCorrect && (exercise.type != Exercise.TYPE_MATCH_FIVE_WORDS)) {
                    allVocabularyWords.find { it == exercise.words[0] }?.apply { level += 1 }

                    lesson.loadVocabularyGroups(applicationContext).forEach { group ->
                        val groupWord = group.vocabulary.find { it == exercise.words[0] }
                        if ((groupWord != null)) {
                            group.vocabulary[group.vocabulary.indexOf(groupWord)].level += 1
                            group.saveInFile()
                        }
                    }
                }
            }

            changeFragment()
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

    private fun setNextExercise() {
        if(mode == MODE_NORMAL){
            val lesson = Lesson.fromJSON(JSONObject(intent.getStringExtra("data_lesson")!!), applicationContext, false)!!

            exercise = ExerciseBuilder(
                allVocabularyWords,
                lesson.askForAllWords,
                lesson.readOut,
                lesson.typesOfExercises,
                lesson.askForSecondWordsOnly,
                position > numberOfExercises,
                mistakes
            ).build()
        }else if(mode == MODE_PRACTICE_MISTAKES){
            val readOut = if(intent.getBooleanExtra("readOutBoth", false)) arrayListOf(true, true) else arrayListOf(true, false)
            exercise = ExerciseBuilder(
                allVocabularyWords,
                intent.getBooleanExtra("askAllWords", false),
                readOut,
                arrayListOf(Exercise.TYPE_TRANSLATE_TEXT),
                false,
                position > numberOfExercises ,
                mistakes
            ).build()
        }
        allVocabularyWords[allVocabularyWords.indexOf(exercise.words[0])].alreadyUsedInExercise = true

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
     * Clear words and disable some views if necessary
     */
    private fun reset(){
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

        position += 1

        Log.i("Position", position.toString())
        Log.i("Mistakes", mistakes.none { !it.isRepeated }.toString())

        reset()

        if(position > numberOfExercises && mistakes.none { !it.isRepeated }){
            binding.progressBarPractice.visibility = View.GONE
            binding.buttonExitPractice.visibility = View.GONE
            binding.textViewPracticeInfoMistake.visibility = View.GONE
            binding.textViewPracticeCorrectInRow.visibility = View.GONE
            if(mode == MODE_NORMAL) {
                val lesson = Lesson.fromJSON(JSONObject(intent.getStringExtra("data_lesson")!!), applicationContext, false)!!

                lesson.saveInFile(applicationContext)
            }
            // Calculate
            val correctInPercent: Double = 100-(mistakes.size.toDouble()/(mistakes.size + numberOfExercises)*100)
            stopTimer()
            navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeFinishFragment(correctInPercent.roundToInt(), mistakes.size, timeInSeconds = this.timeInSeconds, numberOfExercises))

        }else{
            setNextExercise()
            when(exercise.type){
                Exercise.TYPE_TRANSLATE_TEXT -> {
                    navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeTranslateTextFragment(exercise.getJson().toString()))
                }
                Exercise.TYPE_CHOOSE_OF_THREE_WORDS -> {
                    navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeOutOfThreeFragment(exercise.getJson().toString()))
                }
                Exercise.TYPE_MATCH_FIVE_WORDS -> {
                    navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeMatchFiveWordsFragment(exercise.getJson().toString()))
                }
                else -> {
                    Toast.makeText(applicationContext, getString(R.string.err_type_not_valid, exercise.type.toString()), Toast.LENGTH_LONG).show()
                    Log.e("Exception", "Type (${exercise.type}) isn't valid -> return to MainActivity")
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                }
            }
            if(position > numberOfExercises){
                binding.textViewPracticeInfoMistake.visibility = View.VISIBLE
            }else{
                binding.textViewPracticeInfoMistake.visibility = View.GONE
            }
        }
    }

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