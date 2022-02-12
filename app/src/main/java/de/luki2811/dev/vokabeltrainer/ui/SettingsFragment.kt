package de.luki2811.dev.vokabeltrainer.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import de.luki2811.dev.vokabeltrainer.BuildConfig
import de.luki2811.dev.vokabeltrainer.NavigationMainDirections
import de.luki2811.dev.vokabeltrainer.NavigationManageDirections
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.databinding.FragmentSettingsBinding
import de.luki2811.dev.vokabeltrainer.ui.manage.ManageStartFragment

class SettingsFragment : Fragment() {

    // TODO: Einstellungen hinzufügen/erstellen

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.textViewSettingsVersion.text = getString(R.string.app_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

        binding.buttonSettingsManageLanguage.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionGlobalNavigationManage(ManageStartFragment.NAV_MANAGE_LANGUAGE))
        }
        binding.buttonSettingsManageVocabularyGroups.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionGlobalNavigationManage(ManageStartFragment.NAV_MANAGE_VOCABULARY_GROUP))
        }

        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}