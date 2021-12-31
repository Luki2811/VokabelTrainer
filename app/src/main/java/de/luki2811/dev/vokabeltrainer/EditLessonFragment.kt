package de.luki2811.dev.vokabeltrainer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.luki2811.dev.vokabeltrainer.databinding.FragmentEditLessonBinding

class EditLessonFragment : Fragment() {

    private var _binding: FragmentEditLessonBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditLessonBinding.inflate(inflater, container,false)



        return binding.root
    }
}