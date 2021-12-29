package de.luki2811.dev.vokabeltrainer

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.luki2811.dev.vokabeltrainer.databinding.FragmentLearnBinding
import de.luki2811.dev.vokabeltrainer.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    // TODO: Einstellungen hinzufügen/erstellen

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.textViewSettingsVersion.text = getString(R.string.app_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
        binding.buttonSettingsManageVocabularyGroups.setOnClickListener { startActivity(Intent(requireContext(),ManageActivity::class.java)) }

        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}