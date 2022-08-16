package de.luki2811.dev.vokabeltrainer.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import de.luki2811.dev.vokabeltrainer.Mistake
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.Settings
import de.luki2811.dev.vokabeltrainer.adapter.ListMistakesAdapter
import de.luki2811.dev.vokabeltrainer.databinding.FragmentCreatePracticeBinding
import de.luki2811.dev.vokabeltrainer.ui.practice.PracticeActivity
import kotlin.math.roundToInt

class CreatePracticeFragment : Fragment() {
    private var _binding: FragmentCreatePracticeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreatePracticeBinding.inflate(inflater, container, false)
        val allMistakes = Mistake.loadAllFromFile(requireContext())
        allMistakes.sortWith(compareBy { it.lastTimeWrong })

        binding.appBarCreatePractice.apply {
            setNavigationOnClickListener { findNavController().popBackStack() }
        }

        binding.switchCreatePracticeSettingsReadOutBoth.apply {
            isChecked = Settings(requireContext()).readOnlyNewWordsPracticeMistake
        }

        binding.recyclerViewCreatePractice.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ListMistakesAdapter(allMistakes, -1, requireContext())
        }

        if(allMistakes.size < 5){
            binding.buttonCreateMistakeLesson.isEnabled = false
            binding.sliderCreatePracticeNumberOfExercises.isEnabled = false
        }else{
            binding.sliderCreatePracticeNumberOfExercises.apply {
                valueFrom = 1F
                value = if(allMistakes.size > Settings(requireContext()).numberOfExercisesToPracticeMistakes)
                    Settings(requireContext()).numberOfExercisesToPracticeMistakes.toFloat()
                else
                    1F
                valueTo = allMistakes.size.toFloat()
            }
            binding.buttonCreateMistakeLesson.setOnClickListener { createMistakeLesson() }
        }
        return binding.root
    }

    private fun createMistakeLesson(){
        val intent = Intent(requireContext(), PracticeActivity::class.java)
        Settings(requireContext()).apply {
            readOnlyNewWordsPracticeMistake = binding.switchCreatePracticeSettingsReadOutBoth.isChecked
            numberOfExercisesToPracticeMistakes = binding.sliderCreatePracticeNumberOfExercises.value.roundToInt()
        }.saveSettingsInFile()
        intent.apply {
            putExtra("numberOfMistakes", binding.sliderCreatePracticeNumberOfExercises.value.roundToInt())
            putExtra("readOutBoth", !binding.switchCreatePracticeSettingsReadOutBoth.isChecked)
        }
        startActivity(intent)
    }

}