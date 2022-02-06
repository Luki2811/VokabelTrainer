package de.luki2811.dev.vokabeltrainer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.luki2811.dev.vokabeltrainer.databinding.FragmentImportWithURLBinding


class ImportWithURLFragment : Fragment() {

    private var _binding: FragmentImportWithURLBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImportWithURLBinding.inflate(layoutInflater, container, false)

        return binding.root
    }


}