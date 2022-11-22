package de.luki2811.dev.vokabeltrainer.ui.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.databinding.FragmentWelcomeStartBinding

class WelcomeStartFragment : Fragment() {

    private var _binding: FragmentWelcomeStartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWelcomeStartBinding.inflate(inflater, container, false)

        binding.buttonStartNext.apply {
            setOnClickListener {
                findNavController().navigate(WelcomeStartFragmentDirections.actionWelcomeStartFragmentToStreakStartFragment())
            }
        }

        return binding.root
    }
}