package de.luki2811.dev.vokabeltrainer.ui.practice

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.luki2811.dev.vokabeltrainer.databinding.FragmentPracticeMistakesBinding


class PracticeMistakesFragment : Fragment() {
    private var _binding: FragmentPracticeMistakesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPracticeMistakesBinding.inflate(inflater, container, false)






        return binding.root
    }
}