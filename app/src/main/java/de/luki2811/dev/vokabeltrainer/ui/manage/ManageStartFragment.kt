package de.luki2811.dev.vokabeltrainer.ui.manage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.databinding.FragmentManageStartBinding


class ManageStartFragment : Fragment() {

    var _binding: FragmentManageStartBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentManageStartBinding.inflate(inflater, container,false)

        binding.buttonSettingsManageLanguages.setOnClickListener { findNavController().navigate(R.id.action_manageStartFragment_to_manageLanguagesFragment) }
        binding.buttonSettingsManageVocabularyGroups.setOnClickListener { findNavController().navigate(R.id.action_manageStartFragment_to_manageVocabularyGroupsFragment) }

        return binding.root
    }

}