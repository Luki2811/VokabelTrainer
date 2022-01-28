package de.luki2811.dev.vokabeltrainer.ui.practice

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.findFragment
import androidx.fragment.app.findFragment
import androidx.navigation.findNavController
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
    lateinit var lesson: Lesson
    private lateinit var vocabularyGroup: VocabularyGroup
    lateinit var dataPasser: OnDataPass



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

        // TEMP
        typeOfLessonNow = -1

        binding.buttonCheckPractice.setOnClickListener {
            if(numberOfExercises < numberOfExercisesTotal)
                changeTypOfPractice(getNextLessonType())
            else
                changeTypOfPractice(0)
        }

        changeTypOfPractice(getNextLessonType())

        binding.buttonExitPractice.setOnClickListener { quitPractice(this, this) }

        setContentView(binding.root)
    }

    override fun onDataPass(data: String) {
        Log.e("LOG", "data: $data")
    }


    private fun changeTypOfPractice(type: Int){
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_practice) as NavHostFragment
        // Toast.makeText(applicationContext,"$typeOfLessonNow->$type", Toast.LENGTH_SHORT).show()

        val word = vocabularyGroup.getRandomWord()

        when("$typeOfLessonNow->$type"){
            // From Type -1

            "-1->1" -> {
                navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeTranslateTextFragment(word.knownWord, word.newWord, lesson.languageNew.type, lesson.languageKnow.type ,lesson.settingReadOutBoth))
                typeOfLessonNow = 1
            }
            "-1->2" -> {
                navHostFragment.navController.navigate(PracticeStartFragmentDirections.actionPracticeStartFragmentToPracticeOutOfThreeFragment(word.knownWord, word.newWord, lesson.languageNew.type, lesson.languageKnow.type ,lesson.settingReadOutBoth))
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
                navHostFragment.navController.navigate(PracticeTranslateTextFragmentDirections.actionPracticeTranslateTextFragmentSelf(word.knownWord, word.newWord, lesson.languageNew.type, lesson.languageKnow.type ,lesson.settingReadOutBoth))
                refreshStates()
            }

            "1->2" -> {
                navHostFragment.navController.navigate(PracticeTranslateTextFragmentDirections.actionPracticeTranslateTextFragmentToPracticeOutOfThreeFragment(word.knownWord, word.newWord, lesson.languageNew.type, lesson.languageKnow.type ,lesson.settingReadOutBoth))
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
                navHostFragment.navController.navigate(PracticeOutOfThreeFragmentDirections.actionPracticeOutOfThreeFragmentToPracticeTranslateTextFragment(word.knownWord, word.newWord, lesson.languageNew.type, lesson.languageKnow.type ,lesson.settingReadOutBoth))
                typeOfLessonNow = 1
                refreshStates()
            }
            "2->2" -> {
                navHostFragment.navController.navigate(PracticeOutOfThreeFragmentDirections.actionPracticeOutOfThreeFragmentSelf(word.knownWord, word.newWord, lesson.languageNew.type, lesson.languageKnow.type ,lesson.settingReadOutBoth))
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