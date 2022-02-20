package de.luki2811.dev.vokabeltrainer.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.databinding.FragmentSettingsBinding
import de.luki2811.dev.vokabeltrainer.ui.manage.ManageStartFragment
import org.json.JSONObject
import java.io.File

class SettingsFragment : Fragment() {

    // TODO: Einstellungen hinzufügen/erstellen

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    // Settings
    private lateinit var settings: Settings

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        settings = Settings(requireContext())
        binding.menuStreakDailyObjectiveXPAutoComplete.setText(settings.dailyObjectiveStreak, false)
        binding.switchSettingsReadOutVocabularyCentralForbidden.isChecked = !settings.readOutVocabularyGeneral

        binding.textViewSettingsVersion.text = getString(R.string.app_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

        binding.buttonSettingsManageLanguage.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionGlobalNavigationManage(ManageStartFragment.NAV_MANAGE_LANGUAGE))
        }
        binding.buttonSettingsManageVocabularyGroups.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionGlobalNavigationManage(ManageStartFragment.NAV_MANAGE_VOCABULARY_GROUP))
        }



        // Setup Views
        val items = listOf("10XP","20XP","30XP","40XP","50XP","60XP","70XP","80XP","90XP","100XP")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_default, items)
        (binding.menuStreakDailyObjectiveXPLayout.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        binding.menuStreakDailyObjectiveXPAutoComplete.setOnItemClickListener { parent, view, position, id ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.warning))
                .setIcon(R.drawable.ic_outline_warning_24)
                .setMessage(R.string.warning_lose_of_progress)
                .setPositiveButton(R.string.ok){_, _ ->
                    // TODO: Refresh Streak
                    settings.dailyObjectiveStreak = binding.menuStreakDailyObjectiveXPAutoComplete.text.toString()
                    saveSettings()
                }
                .setNegativeButton(R.string.cancel){_, _ ->
                    binding.menuStreakDailyObjectiveXPAutoComplete.setText(settings.dailyObjectiveStreak, false)
                }
                .setOnCancelListener {
                    binding.menuStreakDailyObjectiveXPAutoComplete.setText(settings.dailyObjectiveStreak, false)
                }
                .show()
        }

        binding.switchSettingsReadOutVocabularyCentralForbidden.setOnCheckedChangeListener { buttonView, isChecked ->
            settings.readOutVocabularyGeneral = !isChecked
            saveSettings()
        }

        return binding.root
    }

    private fun saveSettings(){
        settings.saveSettingsInFile()
    }

    override fun onResume() {
        super.onResume()
        val items = listOf("10XP","20XP","30XP","40XP","50XP","60XP","70XP","80XP","90XP","100XP")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_default, items)
        (binding.menuStreakDailyObjectiveXPLayout.editText as? AutoCompleteTextView)?.setAdapter(adapter)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}