package de.luki2811.dev.vokabeltrainer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.VocabularyGroup
import de.luki2811.dev.vokabeltrainer.databinding.FragmentNewVocabularyGroupBinding

class NewVocabularyGroupFragment : Fragment() {

    private var _binding: FragmentNewVocabularyGroupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewVocabularyGroupBinding.inflate(inflater, container, false)

        binding.buttonCreateVocabularyGroupNext.setOnClickListener {
            goNext()
        }

        return binding.root
    }

    private fun goNext() {
        when (VocabularyGroup.isNameValid(requireContext(), binding.textVocabularyGroupName)) {
            0 -> {
                binding.textVocabularyGroupName.error = null
                val bundle: Bundle = bundleOf("key_name" to binding.textVocabularyGroupName.text.toString())
                findNavController().navigate(R.id.action_newVocabularyGroupFragment_to_newAddVocabularyToGroupFragment, bundle)
            }
            1 -> {
                binding.textVocabularyGroupName.error = getString(R.string.err_name_contains_wrong_letter)
                return
            }
            2 -> {
                binding.textVocabularyGroupName.error = getString(R.string.err_name_already_taken)
                return
            }
            3 -> {
                binding.textVocabularyGroupName.error = getString(R.string.err_name_too_long_max, 50)
                return
            }
            4 -> {
                binding.textVocabularyGroupName.error = getString(R.string.err_missing_name)
                return
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}