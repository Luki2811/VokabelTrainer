package de.luki2811.dev.vokabeltrainer.ui.practice

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.databinding.ActivityPracticeBinding
import de.luki2811.dev.vokabeltrainer.ui.MainActivity
import org.json.JSONObject


class PracticeActivity : AppCompatActivity(), OnDataPass {

    private lateinit var binding: ActivityPracticeBinding
    // Typ 0 = FinishFragment
    private val correctInPercent = 100
    private var typeOfLessonNow = 0
    private var numberOfExercises = 0
    private var numberOfExercisesTotal = 10 - 1
    private var wrongWords: ArrayList<VocabularyWord> = arrayListOf()
    lateinit var lesson: Lesson
    private lateinit var vocabularyGroup: VocabularyGroup
    lateinit var dataPasser: OnDataPass
    private lateinit var word: VocabularyWord
    private var askKnownWord: Boolean = true
    private lateinit var solution: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPracticeBinding.inflate(layoutInflater)

        lesson = Lesson(JSONObject(intent.getStringExtra("data_lesson")!!), applicationContext)


        vocabularyGroup = VocabularyGroup("vocabularyForPractice", arrayOf(), applicationContext)
        vocabularyGroup.id.deleteId()

        for(i in lesson.vocabularyGroupIds){
            VocabularyGroup.loadFromFileWithId(Id(applicationContext,i), applicationContext)?.let { vocabularyGroup.addVocabularyFromVocabularyGroup(it) }
        }

        if(vocabularyGroup.vocabulary.size < 10) {
            Toast.makeText(this, getText(R.string.err_not_enough_words), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            return
        }

        typeOfLessonNow = -1

        binding.buttonCheckPractice.isEnabled = false
        binding.buttonCheckPractice.setOnClickListener {
            startCorrection()
        }

        changeTypOfPractice(getNextLessonType())

        binding.buttonExitPractice.setOnClickListener { quitPractice(this, this) }

        setContentView(binding.root)
    }

    override fun onDataPass(data: String) {
        solution = data
        binding.buttonCheckPractice.isEnabled = solution.isNotEmpty()
    }

    private fun startCorrection(){
        val correctionBottomSheet = CorrectionBottomSheet()

        if(!isInputCorrect()) {
            word.typeWrong = typeOfLessonNow
            word.isWrong = true
            word.askKnownWord = askKnownWord
            wrongWords.add(word)
            numberOfExercisesTotal += 1
        }

        if(askKnownWord)
            correctionBottomSheet.arguments = bundleOf("correctWord" to word.newWord, "isCorrect" to isInputCorrect())
        else
            correctionBottomSheet.arguments = bundleOf("correctWord" to word.knownWord, "isCorrect" to isInputCorrect())

        correctionBottomSheet.show(supportFragmentManager, CorrectionBottomSheet.TAG)

        supportFragmentManager.setFragmentResultListener("finishFragment", this){ requestKey, bundle ->
            if(bundle.getBoolean("isFinished"))
                next()
        }

    }

    fun next(){
        if(numberOfExercises < numberOfExercisesTotal + 1)
            changeTypOfPractice(getNextLessonType())
        else
            changeTypOfPractice(0)
    }

    private fun isInputCorrect(): Boolean{
        return if(askKnownWord)
            solution.trim().equals(word.newWord, word.isIgnoreCase)
        else
            solution.trim().equals(word.knownWord, word.isIgnoreCase)

    }


    private fun changeTypOfPractice(_type: Int){
        var type = _type
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_practice) as NavHostFragment
        // Toast.makeText(applicationContext,"$typeOfLessonNow->$type", Toast.LENGTH_SHORT).show()
        if(!this::word.isInitialized)
            word = vocabularyGroup.getRandomWord()
        if(numberOfExercises < 9){
            while(word.isAlreadyUsed){
                word = vocabularyGroup.getRandomWord()
            }
            word.isAlreadyUsed = true
            askKnownWord = (0..1).random() == 0
        }else if(wrongWords.size != 0){
            word = wrongWords[(0 until wrongWords.size).random()]
            askKnownWord = word.askKnownWord
            type = word.typeWrong
            wrongWords.remove(word)
        }else
            type = 0

        Log.i("Info", "$numberOfExercises/$numberOfExercisesTotal")

        when("$typeOfLessonNow->$type"){
            // From Type -1

            "-1->1" -> {
                navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeTranslateTextFragment(word.knownWord, word.newWord, lesson.languageNew.type, lesson.languageKnow.type ,lesson.settingReadOutBoth, askKnownWord))
                typeOfLessonNow = 1
            }
            "-1->2" -> {
                navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeOutOfThreeFragment(word.knownWord, word.newWord, lesson.languageNew.type, lesson.languageKnow.type ,lesson.settingReadOutBoth, askKnownWord))
                typeOfLessonNow = 2
            }
            "-1->3" -> {
                navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeMatchFiveWordsFragment(word.knownWord, word.newWord, lesson.languageNew.type, lesson.languageKnow.type ,lesson.settingReadOutBoth))
                typeOfLessonNow = 3
            }

            // From Type 1
            "1->0" -> {
                navHostFragment.navController.navigate(PracticeTranslateTextFragmentDirections.actionPracticeTranslateTextFragmentToPracticeFinishFragment(correctInPercent))
                typeOfLessonNow = 0
                refreshStates()
            }
            "1->1" -> {
                navHostFragment.navController.navigate(PracticeTranslateTextFragmentDirections.actionPracticeTranslateTextFragmentSelf(word.knownWord, word.newWord, lesson.languageNew.type, lesson.languageKnow.type ,lesson.settingReadOutBoth ,askKnownWord))
                refreshStates()
            }

            "1->2" -> {
                navHostFragment.navController.navigate(PracticeTranslateTextFragmentDirections.actionPracticeTranslateTextFragmentToPracticeOutOfThreeFragment(word.knownWord, word.newWord, lesson.languageNew.type, lesson.languageKnow.type ,lesson.settingReadOutBoth ,askKnownWord))
                typeOfLessonNow = 2
                refreshStates()
            }
            "1->3" -> {
                navHostFragment.navController.navigate(PracticeTranslateTextFragmentDirections.actionPracticeTranslateTextFragmentToPracticeMatchFiveWordsFragment(word.knownWord, word.newWord, lesson.languageNew.type, lesson.languageKnow.type ,lesson.settingReadOutBoth))
                typeOfLessonNow = 3
                refreshStates()
            }
            // From Type 2
            "2->0" -> {
                navHostFragment.navController.navigate(PracticeOutOfThreeFragmentDirections.actionPracticeOutOfThreeFragmentToPracticeFinishFragment(correctInPercent))
                typeOfLessonNow = 0
                refreshStates()
            }

            "2->1" -> {
                navHostFragment.navController.navigate(PracticeOutOfThreeFragmentDirections.actionPracticeOutOfThreeFragmentToPracticeTranslateTextFragment(word.knownWord, word.newWord, lesson.languageNew.type, lesson.languageKnow.type ,lesson.settingReadOutBoth ,askKnownWord))
                typeOfLessonNow = 1
                refreshStates()
            }
            "2->2" -> {
                navHostFragment.navController.navigate(PracticeOutOfThreeFragmentDirections.actionPracticeOutOfThreeFragmentSelf(word.knownWord, word.newWord, lesson.languageNew.type, lesson.languageKnow.type ,lesson.settingReadOutBoth ,askKnownWord))
                refreshStates()
            }

            "2->3" -> {
                navHostFragment.navController.navigate(PracticeOutOfThreeFragmentDirections.actionPracticeOutOfThreeFragmentToPracticeMatchFiveWordsFragment(word.knownWord, word.newWord, lesson.languageNew.type, lesson.languageKnow.type ,lesson.settingReadOutBoth))
                typeOfLessonNow = 3
                refreshStates()
            }
            // From Type 3
            "3->0" -> {
                navHostFragment.navController.navigate(PracticeMatchFiveWordsFragmentDirections.actionPracticeMatchFiveWordsFragmentToPracticeFinishFragment(correctInPercent))
                typeOfLessonNow = 0
                refreshStates()
            }
            "3->1" -> {
                navHostFragment.navController.navigate(PracticeMatchFiveWordsFragmentDirections.actionPracticeMatchFiveWordsFragmentToPracticeTranslateTextFragment(word.knownWord, word.newWord, lesson.languageNew.type, lesson.languageKnow.type ,lesson.settingReadOutBoth))
                typeOfLessonNow = 1
                refreshStates()
            }
            "3->2" -> {
                navHostFragment.navController.navigate(PracticeMatchFiveWordsFragmentDirections.actionPracticeMatchFiveWordsFragmentToPracticeOutOfThreeFragment(word.knownWord, word.newWord, lesson.languageNew.type, lesson.languageKnow.type ,lesson.settingReadOutBoth))
                typeOfLessonNow = 2
                refreshStates()
            }
            "3->3" -> {
                navHostFragment.navController.navigate(PracticeMatchFiveWordsFragmentDirections.actionPracticeMatchFiveWordsFragmentSelf(word.knownWord, word.newWord, lesson.languageNew.type, lesson.languageKnow.type ,lesson.settingReadOutBoth))
                refreshStates()
            }
            else -> {
                refreshStates()
            }
        }

        if(typeOfLessonNow == 0){
            binding.buttonCheckPractice.visibility = View.GONE
        }

    }

    private fun refreshStates(){
        numberOfExercises += 1

        binding.progressBarPractice.max = numberOfExercisesTotal + 1
        binding.progressBarPractice.progress = numberOfExercises

        binding.buttonCheckPractice.isEnabled = false
        solution = ""

    }

    private fun getNextLessonType(): Int {
        // return (1..3).random()
        return 1
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