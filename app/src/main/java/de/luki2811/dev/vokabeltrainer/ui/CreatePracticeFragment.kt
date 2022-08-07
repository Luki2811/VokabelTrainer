package de.luki2811.dev.vokabeltrainer.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.luki2811.dev.vokabeltrainer.databinding.FragmentCreatePracticeBinding
import de.luki2811.dev.vokabeltrainer.ui.practice.PracticeActivity

class CreatePracticeFragment : Fragment() {
    private var _binding: FragmentCreatePracticeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreatePracticeBinding.inflate(inflater, container, false)

        binding.buttonCreateMistakeLesson.setOnClickListener { createMistakeLesson() }

        return binding.root
    }

    private fun createMistakeLesson(){
        startActivity(Intent(requireContext(), PracticeActivity::class.java))
    }

}