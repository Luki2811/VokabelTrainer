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
import java.io.File
import kotlin.collections.ArrayList


class PracticeActivity : AppCompatActivity(), OnDataPass {

    private lateinit var binding: ActivityPracticeBinding
    // Typ 0 = FinishFragment
    private var correctInPercent: Int = 100
    private var typeOfLessonNow = 0
    private var numberOfExercises = 0
    private var numberOfExercisesTotal = 9
    private var numberOfWrongWords: Double = 0.0
    private var wrongWords: ArrayList<VocabularyWord> = arrayListOf()
    lateinit var lesson: Lesson
    private lateinit var vocabularyGroup: VocabularyGroup
    private lateinit var word: VocabularyWord
    private var askKnownWord: Boolean = true
    private lateinit var solution: String
    private var idOfLesson: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPracticeBinding.inflate(layoutInflater)

        lesson = Lesson(JSONObject(intent.getStringExtra("data_lesson")!!), applicationContext)
        idOfLesson = lesson.id.number

        vocabularyGroup = VocabularyGroup("vocabularyForPractice", arrayOf(), applicationContext)

        for(i in lesson.vocabularyGroupIds){
            VocabularyGroup.loadFromFileWithId(Id(applicationContext,i), applicationContext)?.let { vocabularyGroup.addVocabularyFromVocabularyGroup(it) }
        }

        if(vocabularyGroup.vocabulary.size < 10) {
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
        if(!isInputCorrect()) {
            word.typeWrong = typeOfLessonNow
            word.isWrong = true
            word.askKnownWord = askKnownWord
            wrongWords.add(word)
            numberOfWrongWords += 1
            numberOfExercisesTotal += 1
        }else{
            lesson.alreadyUsedWords.add(word.newWord)
        }

        val correctionBottomSheet = CorrectionBottomSheet()
        if(askKnownWord)
            correctionBottomSheet.arguments = bundleOf("correctWord" to word.newWord, "isCorrect" to isInputCorrect())
        else
            correctionBottomSheet.arguments = bundleOf("correctWord" to word.knownWord, "isCorrect" to isInputCorrect())

        correctionBottomSheet.show(supportFragmentManager, CorrectionBottomSheet.TAG)

        supportFragmentManager.setFragmentResultListener("finishFragment", this){ _, bundle ->
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

        // TODO: Change, that it shows the mistakes

        if(askKnownWord)
            return solution.trim().equals(word.newWord, word.isIgnoreCase)
        else{
            val knownWords = word.getKnownWordList()
            for(i in knownWords) {
                if(i.trim().equals(solution.trim(), word.isIgnoreCase)) {
                    return true
                }
            }
            return false
        }
    }

    private fun newRandomWord() {
        val randomWord = vocabularyGroup.getRandomWord()

        Log.e("sizeVocabularyGroup", vocabularyGroup.vocabulary.size.toString())
        Log.e("sizeAlreadyUsedWords", lesson.alreadyUsedWords.size.toString())

        if(vocabularyGroup.vocabulary.size <= lesson.alreadyUsedWords.size) {
            lesson.alreadyUsedWords = arrayListOf()
            Toast.makeText(applicationContext, getString(R.string.vocabulary_group_restart), Toast.LENGTH_SHORT).show()
        }

        for(i in lesson.alreadyUsedWords){
            if(i == randomWord.newWord) {
                newRandomWord()
                return
            }
        }
        word = randomWord
    }


    private fun changeTypOfPractice(_type: Int){
        var type = _type
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_practice) as NavHostFragment
        // Toast.makeText(applicationContext,"$typeOfLessonNow->$type", Toast.LENGTH_SHORT).show()
        if(!this::word.isInitialized)
            newRandomWord()
        when {
            numberOfExercises < 9 -> {
                while(word.isAlreadyUsed){
                    newRandomWord()
                }
                word.isAlreadyUsed = true

                askKnownWord = if(lesson.askOnlyNewWords)
                    true
                else
                    (0..1).random() == 0

                if(!word.isIgnoreCase) {
                    binding.textViewPracticeInfo.setText(R.string.look_for_case)
                    binding.textViewPracticeInfo.visibility = View.VISIBLE
                }
                else {
                    binding.textViewPracticeInfo.text = ""
                    binding.textViewPracticeInfo.visibility = View.GONE
                }
            }
            wrongWords.size != 0 -> {
                word = wrongWords[(0 until wrongWords.size).random()]
                askKnownWord = word.askKnownWord
                type = word.typeWrong
                wrongWords.remove(word)
                binding.textViewPracticeInfo.setText(R.string.word_previous_mistake)
                binding.textViewPracticeInfo.setTextColor(getColor(R.color.Red))
                binding.textViewPracticeInfo.visibility = View.VISIBLE
            }
            else -> {
                type = 0
                binding.textViewPracticeInfo.visibility = View.GONE
            }
        }

        Log.i("Info", "$numberOfExercises/$numberOfExercisesTotal")

        // Calculate
        correctInPercent = 100-(numberOfWrongWords/(numberOfExercises+1)*100).toInt()
        Log.e("correctInPercent", correctInPercent.toString())


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

            Log.e("idOfLesson", idOfLesson.toString())

            // Speichern in Datei
            // Name der Datei gleich der ID(.json)
            var file = File(applicationContext.filesDir, "lessons")
            file.mkdirs()
            file = File(file, "$idOfLesson.json")
            AppFile.writeInFile(lesson.getAsJson().toString(), file)
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