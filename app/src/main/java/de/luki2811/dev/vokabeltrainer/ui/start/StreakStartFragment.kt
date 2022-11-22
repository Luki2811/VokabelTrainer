package de.luki2811.dev.vokabeltrainer.ui.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.luki2811.dev.vokabeltrainer.databinding.FragmentStreakStartBinding

class StreakStartFragment : Fragment() {

    private var _binding: FragmentStreakStartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStreakStartBinding.inflate(inflater, container, false)
        return binding.root
    }
}