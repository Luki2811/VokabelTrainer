package de.luki2811.dev.vokabeltrainer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.luki2811.dev.vokabeltrainer.databinding.FragmentNewVocabularyGroupBinding

class NewVocabularyGroupFragment : Fragment() {

    // TODO: Funktionalität hinzufügen um Vokabelgruppen zu erstellen

    private var _binding: FragmentNewVocabularyGroupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewVocabularyGroupBinding.inflate(inflater, container, false)

        binding.buttonCreateVocabularyGroupNext.setOnClickListener {

        }

        binding.buttonCreateVocabularyGroupAddVoc.setOnClickListener {

        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}