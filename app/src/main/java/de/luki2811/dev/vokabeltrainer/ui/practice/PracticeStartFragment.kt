package de.luki2811.dev.vokabeltrainer.ui.practice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import de.luki2811.dev.vokabeltrainer.databinding.FragmentPracticeStartBinding

class PracticeStartFragment: Fragment() {

    private var _binding: FragmentPracticeStartBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPracticeStartBinding.inflate(inflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            PracticeActivity.quitPractice(requireActivity(), requireContext())
        }

        return binding.root
    }
}