package de.luki2811.dev.vokabeltrainer.ui.manage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.luki2811.dev.vokabeltrainer.Language
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.databinding.FragmentManageLanguagesBinding

class ManageLanguagesFragment : Fragment() {

    var _binding: FragmentManageLanguagesBinding? = null
    val binding get() = _binding!!
    private var languages: ArrayList<Language> = arrayListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentManageLanguagesBinding.inflate(inflater, container, false)

        for(i in 0..9){
            languages.add(Language(i, requireContext()))
        }

        binding.editTextLan0.setText(languages[0].name)
        binding.editTextLan1.setText(languages[1].name)
        binding.editTextLan2.setText(languages[2].name)
        binding.editTextLan3.setText(languages[3].name)
        binding.editTextLan4.setText(languages[4].name)
        binding.editTextLan5.setText(languages[5].name)
        binding.editTextLan6.setText(languages[6].name)
        binding.editTextLan7.setText(languages[7].name)
        binding.editTextLan8.setText(languages[8].name)
        binding.editTextLan9.setText(languages[9].name)

        binding.buttonSaveLanguages.setOnClickListener {
            refreshNameList()
            if(isAllCorrect()){
                Toast.makeText(requireContext(), R.string.saved, Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_global_navigationMain)
                for(lang in languages){
                    lang.refreshInIndex()
                }

            }else{
                Toast.makeText(requireContext(), R.string.err_missing_input, Toast.LENGTH_SHORT).show()
            }

        }

        binding.buttonResetLanguages.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.reset)
                .setIcon(R.drawable.ic_baseline_refresh_24)
                .setMessage("Möchten sie wirklich alle Sprachen zurücksetzen ??")
                .setPositiveButton(R.string.reset){_,_ -> setDefault()}
                .setNegativeButton(R.string.cancel){_,_ -> }
                .show()
        }

        return binding.root
    }

    private fun isAllCorrect(): Boolean{
        for(lang in languages){
            if(lang.name.trim().isBlank())
                return false
        }
        return true
    }

    private fun setDefault(){
        binding.editTextLan0.setText(Language.DEFAULT_0)
        binding.editTextLan1.setText(Language.DEFAULT_1)
        binding.editTextLan2.setText(Language.DEFAULT_2)
        binding.editTextLan3.setText(Language.DEFAULT_3)
        binding.editTextLan4.setText(Language.DEFAULT_4)
        binding.editTextLan5.setText(Language.DEFAULT_5)
        binding.editTextLan6.setText(Language.DEFAULT_6)
        binding.editTextLan7.setText(Language.DEFAULT_7)
        binding.editTextLan8.setText(Language.DEFAULT_8)
        binding.editTextLan9.setText(Language.DEFAULT_9)
        refreshNameList()
    }

    private fun refreshNameList(){
        languages[0].name = binding.editTextLan0.text.toString()
        languages[1].name = binding.editTextLan1.text.toString()
        languages[2].name = binding.editTextLan2.text.toString()
        languages[3].name = binding.editTextLan3.text.toString()
        languages[4].name = binding.editTextLan4.text.toString()
        languages[5].name = binding.editTextLan5.text.toString()
        languages[6].name = binding.editTextLan6.text.toString()
        languages[7].name = binding.editTextLan7.text.toString()
        languages[8].name = binding.editTextLan8.text.toString()
        languages[9].name = binding.editTextLan9.text.toString()

    }
}