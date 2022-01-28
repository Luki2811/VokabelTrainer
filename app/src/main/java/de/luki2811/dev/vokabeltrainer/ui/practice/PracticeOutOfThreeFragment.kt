package de.luki2811.dev.vokabeltrainer.ui.practice

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.luki2811.dev.vokabeltrainer.databinding.FragmentPracticeOutOfThreeBinding
import de.luki2811.dev.vokabeltrainer.ui.MainActivity
import de.luki2811.dev.vokabeltrainer.ui.practice.PracticeActivity.Companion.quitPractice

class PracticeOutOfThreeFragment: Fragment() {

    private var _binding: FragmentPracticeOutOfThreeBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPracticeOutOfThreeBinding.inflate(inflater, container, false)

        val calback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            quitPractice(requireActivity(),requireContext())
        }

        return binding.root
    }
}