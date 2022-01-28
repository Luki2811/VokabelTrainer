package de.luki2811.dev.vokabeltrainer.ui.practice

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.databinding.FragmentPracticeMatchFiveWordsBinding

class PracticeMatchFiveWordsFragment : Fragment() {

    private var _binding: FragmentPracticeMatchFiveWordsBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPracticeMatchFiveWordsBinding.inflate(layoutInflater, container, false)

        val calback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            PracticeActivity.quitPractice(requireActivity(), requireContext())
        }

        return binding.root
    }
}