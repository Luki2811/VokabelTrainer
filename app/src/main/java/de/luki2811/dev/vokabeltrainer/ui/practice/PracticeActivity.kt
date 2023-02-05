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
import de.luki2811.dev.vokabeltrainer.Exercise
import de.luki2811.dev.vokabeltrainer.ExerciseBuilder
import de.luki2811.dev.vokabeltrainer.ExerciseResult
import de.luki2811.dev.vokabeltrainer.Lesson
import de.luki2811.dev.vokabeltrainer.Mistake
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.VocabularyWord
import de.luki2811.dev.vokabeltrainer.databinding.ActivityPracticeBinding
import de.luki2811.dev.vokabeltrainer.ui.MainActivity
import kotlinx.coroutines.Runnable
import java.time.LocalDate
import kotlin.math.roundToInt

@Suppress("DEPRECATION")
class PracticeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPracticeBinding

    private var mode: Int = -1
    private var position = 0
    private var correctInARow: Int = 0
    private var mistakes: ArrayList<Mistake> = arrayListOf()
    private lateinit var exercise: Exercise
    private lateinit var lesson: Lesson
    private var allVocabularyWords = arrayListOf<VocabularyWord>()
    // Timer
    private var timeInSeconds = 0
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPracticeBinding.inflate(layoutInflater)

        binding.buttonExitPractice.setOnClickListener { quitPractice(this, this) }
        startTimer()

        mode = intent.getIntExtra("mode", MODE_NORMAL)

        // Setup and check lesson

        lesson = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("lesson", Lesson::class.java)
        }else{
            intent.getParcelableExtra("lesson")
        } ?: return

        for (group in lesson.vocabularyGroups){
            allVocabularyWords.addAll(group.vocabulary)
        }
        allVocabularyWords.trimToSize()

        if(allVocabularyWords.filter { lesson.typesOfWordToPractice.contains(it.typeOfWord) }.size < lesson.numberOfExercises || allVocabularyWords.isEmpty()) {
            MaterialAlertDialogBuilder(this)
                .setMessage(R.string.err_lesson_enough_words_long)
                .setTitle(R.string.err)
                .setIcon(R.drawable.ic_outline_error_24)
                .setNegativeButton(R.string.ok) { _, _ ->
                    startActivity(
                        Intent(
                            this,
                            MainActivity::class.java
                        ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    )
                }
                .setOnCancelListener {
                    startActivity(
                        Intent(
                            this,
                            MainActivity::class.java
                        ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    )
                }
                .show()
            return
        }


        Log.d("Practice", "Loaded lesson $lesson successfully and ready for using!")

        setupListeners()

        changeFragment()

        setContentView(binding.root)

        hideSystemUI()

    }

    private fun setupListeners() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_practice) as NavHostFragment

        // Result listener
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

            if(position > lesson.numberOfExercises){
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

                val newMistake = Mistake(
                    word = exercise.words[0],
                    askedForSecondWord = exercise.isOtherWordAskedAsAnswer,
                    typeOfPractice = exercise.type,
                    wrongAnswer = exerciseResult.answer,
                    position = position,
                    lastTimeWrong = LocalDate.now())
                mistakes.add(newMistake)
                newMistake.addToFile(this)
            }

            binding.progressBarPractice.setProgress(position, true)
            binding.progressBarPractice.max = lesson.numberOfExercises + mistakes.size

            if(mode == MODE_NORMAL){
                exercise.words[0].level += 1
                lesson.vocabularyGroups.forEach {
                    Log.d("Practice", "Saved vocGroup ${lesson.vocabularyGroups}")
                    it.saveInFile(this)
                }
            }

            changeFragment()
        }

        // Send to mistakes
        supportFragmentManager.setFragmentResultListener("sendToMistakes", this) { _, _ ->
            val mistakesAsArrayListString = arrayListOf<String>()
            mistakes.forEach { mistakesAsArrayListString.add(it.getAsJson().toString()) }

            binding.progressBarPractice.visibility = View.GONE
            binding.buttonExitPractice.visibility = View.GONE
            binding.textViewPracticeInfoMistake.visibility = View.GONE

            navHostFragment.navController.navigate(PracticeFinishFragmentDirections.actionPracticeFinishFragmentToPracticeMistakesFragment(mistakesAsArrayListString.toTypedArray(), lesson.numberOfExercises + mistakes.size))
        }
    }

    private fun setNextExercise() {

        exercise = ExerciseBuilder(
            allVocabularyWords,
            lesson.askForAllWords,
            lesson.readOut,
            lesson.typesOfExercises,
            lesson.isOnlyMainWordAskedAsAnswer,
            position > lesson.numberOfExercises,
            lesson.typesOfWordToPractice,
            mistakes,
            this).build()


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

        if(position > lesson.numberOfExercises && mistakes.none { !it.isRepeated }){
            binding.progressBarPractice.visibility = View.GONE
            binding.buttonExitPractice.visibility = View.GONE
            binding.textViewPracticeInfoMistake.visibility = View.GONE
            binding.textViewPracticeCorrectInRow.visibility = View.GONE

            // Calculate
            val correctInPercent: Double = 100-(mistakes.size.toDouble()/(mistakes.size + lesson.numberOfExercises)*100)
            stopTimer()
            navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeFinishFragment(correctInPercent.roundToInt(), mistakes.size, timeInSeconds = this.timeInSeconds, lesson.numberOfExercises))

        }else{
            setNextExercise()

            Log.d("Practice", "Set exercise on $position of ${lesson.numberOfExercises + mistakes.size} as $exercise")

            when(exercise.type){
                Exercise.TYPE_TRANSLATE_TEXT -> {
                    navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeTranslateTextFragment(exercise))
                }
                Exercise.TYPE_CHOOSE_OF_THREE_WORDS -> {
                    navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeOutOfThreeFragment(exercise))
                }
                Exercise.TYPE_MATCH_FIVE_WORDS -> {
                    navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeMatchFiveWordsFragment(exercise))
                }
                else -> {
                    Toast.makeText(applicationContext, getString(R.string.err_type_not_valid, exercise.type.toString()), Toast.LENGTH_LONG).show()
                    Log.e("Practice", "Type (${exercise.type}) isn't valid -> return to MainActivity")
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                }
            }
            if(position > lesson.numberOfExercises){
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