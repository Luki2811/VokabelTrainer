package de.luki2811.dev.vokabeltrainer.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.google.android.material.sidesheet.SideSheetDialog
import de.luki2811.dev.vokabeltrainer.Lesson
import de.luki2811.dev.vokabeltrainer.Mistake
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.adapter.ListMistakesAdapter
import de.luki2811.dev.vokabeltrainer.databinding.FragmentCreatePracticeBinding
import de.luki2811.dev.vokabeltrainer.databinding.SideSheetSettingsMistakeLessonBinding
import de.luki2811.dev.vokabeltrainer.ui.practice.PracticeActivity
import kotlin.math.roundToInt

class CreatePracticeFragment : Fragment() {
    private var _binding: FragmentCreatePracticeBinding? = null
    private val binding get() = _binding!!

    // Settings
    private var numberOfExercises = 5
    private var requestAllOtherWords = false
    private var read = ArrayList<Pair<Int, Boolean>>()
    private var allMistakes = ArrayList<Mistake>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreatePracticeBinding.inflate(inflater, container, false)
        allMistakes.addAll(Mistake.loadAllFromFile(requireContext()))
        allMistakes.sortWith(compareBy { it.lastTimeWrong })

        binding.appBarCreatePractice.apply {
            setNavigationOnClickListener { findNavController().popBackStack() }
            subtitle = getString(R.string.number_of_mistakes, allMistakes.size)
        }

        binding.buttonCreateMistakeLessonSettings.setOnClickListener {
            SideSheetDialog(requireContext()).apply {
                val sideSheetBinding = SideSheetSettingsMistakeLessonBinding.inflate(inflater)
                setContentView(sideSheetBinding.root)

                setOnShowListener {
                    val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
                    sideSheetBinding.sliderCreatePracticeNumberOfExercises.apply {
                        val allMistakeSize = binding.recyclerViewCreatePractice.adapter?.itemCount ?: allMistakes.size
                        if(allMistakeSize > 5) {
                            valueFrom = 5F
                            value = if(sharedPreferences.getInt("numberOfExercises", 5) > allMistakeSize || sharedPreferences.getInt("numberOfExercises", 5) < 5){
                                allMistakeSize.toFloat()
                            }else{
                                sharedPreferences.getInt("numberOfExercises", 5).toFloat()
                            }
                            valueTo = allMistakeSize.toFloat()
                        }else{
                            isEnabled = false
                        }
                    }
                    sideSheetBinding.switchCreatePracticeSettingsAskAllWords.isChecked = sharedPreferences.getBoolean("requestAllOtherWords", false)
                    sideSheetBinding.chipCreatePracticeReadMainLanguage.isChecked = sharedPreferences.getBoolean("readMainLang", true)
                    sideSheetBinding.chipCreatePracticeReadOtherLanguage.isChecked = sharedPreferences.getBoolean("readOtherLang", true)

                }
                setOnDismissListener {
                    val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
                    if(sideSheetBinding.sliderCreatePracticeNumberOfExercises.isEnabled){
                        numberOfExercises = sideSheetBinding.sliderCreatePracticeNumberOfExercises.value.roundToInt()
                    }
                    requestAllOtherWords = sideSheetBinding.switchCreatePracticeSettingsAskAllWords.isChecked
                    read.clear()
                    read.add(Lesson.READ_MAIN_LANGUAGE to sideSheetBinding.chipCreatePracticeReadMainLanguage.isChecked)
                    read.add(Lesson.READ_OTHER_LANGUAGE to sideSheetBinding.chipCreatePracticeReadOtherLanguage.isChecked)

                    with(sharedPreferences.edit()){
                        putBoolean("requestAllOtherWords", requestAllOtherWords)
                        putBoolean("readMainLang", read.contains(Lesson.READ_MAIN_LANGUAGE to true))
                        putBoolean("readOtherLang", read.contains(Lesson.READ_OTHER_LANGUAGE to true))
                        putInt("numberOfExercises", numberOfExercises)
                        apply()
                    }
                }
            }.show()
        }

        binding.recyclerViewCreatePractice.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ListMistakesAdapter(allMistakes, -1, requireContext())
            val dataObserver: AdapterDataObserver = object: AdapterDataObserver(){
                override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                    super.onItemRangeRemoved(positionStart, itemCount)
                    binding.appBarCreatePractice.subtitle = getString(R.string.number_of_mistakes, adapter?.itemCount)
                    allMistakes = (adapter as ListMistakesAdapter).dataSet
                }
            }
            adapter?.registerAdapterDataObserver(dataObserver)
        }

        return binding.root
    }

    private fun createMistakeLesson(): Lesson{
        TODO()
    }

    private fun startLesson(lesson: Lesson){
        startActivity(Intent(requireContext(), PracticeActivity::class.java).apply {
            putExtra("mode", PracticeActivity.MODE_PRACTICE_MISTAKES)
            putExtra("lesson", lesson)
        })
    }

}