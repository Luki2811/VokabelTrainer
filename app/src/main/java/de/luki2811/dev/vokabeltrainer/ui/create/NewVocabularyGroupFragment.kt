package de.luki2811.dev.vokabeltrainer.ui.create

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import de.luki2811.dev.vokabeltrainer.Language
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.VocabularyGroup
import de.luki2811.dev.vokabeltrainer.databinding.FragmentNewVocabularyGroupBinding
import org.json.JSONObject

class NewVocabularyGroupFragment : Fragment() {

    private var _binding: FragmentNewVocabularyGroupBinding? = null
    private val binding get() = _binding!!
    private val args:NewVocabularyGroupFragmentArgs by navArgs()
    private var vocabularyGroup: VocabularyGroup? = null
    private var arrayListGroup = ArrayList<Language>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewVocabularyGroupBinding.inflate(inflater, container, false)

        val languageIndex = Language.getLanguagesIndex(requireContext())
        for(i in 0 until languageIndex.getJSONArray("languages").length()){

            val language = Language(
                languageIndex.getJSONArray("languages").getJSONObject(i).getInt("type"),
                requireContext()
            )
            arrayListGroup.add(language)
            binding.chipGroupVocabularyGroupsLanguageNew.addView(newChip(language.name) as View)
            binding.chipGroupVocabularyGroupsLanguageKnown.addView(newChip(language.name) as View)
        }

        if((!args.keyVocGroup.isNullOrEmpty()) && (args.keyMode == MODE_IMPORT || args.keyMode == MODE_EDIT)){
            vocabularyGroup = VocabularyGroup(JSONObject(args.keyVocGroup.toString()), context = requireContext())
            binding.textVocabularyGroupName.setText(vocabularyGroup!!.name)

            for(i in 0 until binding.chipGroupVocabularyGroupsLanguageNew.size){
                if(arrayListGroup[i].name == vocabularyGroup!!.languageNew.name)
                    (binding.chipGroupVocabularyGroupsLanguageNew[i] as Chip).isChecked = true

                if(arrayListGroup[i].name == vocabularyGroup!!.languageKnown.name)
                    (binding.chipGroupVocabularyGroupsLanguageKnown[i] as Chip).isChecked = true

            }
        }

        binding.buttonCreateVocabularyGroupNext.setOnClickListener { goNext() }

        return binding.root
    }

    private fun newChip(name: String): Chip {
        val chip = Chip(requireContext())
        chip.text = name
        chip.chipIcon = ContextCompat.getDrawable(requireContext(),
            R.drawable.ic_launcher_background
        )
        chip.isChipIconVisible = false
        chip.isCloseIconVisible = false
        // necessary to get single selection working
        chip.isClickable = true
        chip.isCheckable = true
        // chip.setChipDrawable(ChipDrawable.createFromAttributes(requireContext(), null, 0, R.style.Widget_Material3_Chip_Filter))
        return chip
    }

    private fun goNext() {

        var languageNew = Language(-1, requireContext())
        var languageKnown = Language(-1, requireContext())

        for(i in 0 until binding.chipGroupVocabularyGroupsLanguageNew.size){

            if((binding.chipGroupVocabularyGroupsLanguageKnown[i] as Chip).isChecked)
                languageKnown = arrayListGroup[i]

            if((binding.chipGroupVocabularyGroupsLanguageNew[i] as Chip).isChecked)
                languageNew = arrayListGroup[i]

        }

        if(languageKnown.type == -1 || languageNew.type == -1) {
            Toast.makeText(requireContext(), "Fehler: Keine Sprache ausgewählt !!", Toast.LENGTH_LONG).show()
            return
        }

        when (VocabularyGroup.isNameValid(requireContext(), binding.textVocabularyGroupName, if (vocabularyGroup != null && args.keyMode == MODE_EDIT) vocabularyGroup!!.name else "")) {
            0 -> {
                binding.textVocabularyGroupName.error = null
                if(vocabularyGroup != null) {
                    vocabularyGroup!!.name = binding.textVocabularyGroupName.text.toString()
                    vocabularyGroup!!.languageNew = languageNew
                    vocabularyGroup!!.languageKnown = languageKnown
                    findNavController().navigate(NewVocabularyGroupFragmentDirections.actionNewVocabularyGroupFragmentToEditVocabularyGroupFragment(vocabularyGroup!!.getAsJson().toString(), "", args.keyMode))
                }
                else{
                    findNavController().navigate(NewVocabularyGroupFragmentDirections.actionNewVocabularyGroupFragmentToEditVocabularyGroupFragment(null, binding.textVocabularyGroupName.text.toString(), args.keyMode, languageKnown.type, languageNew.type ))
                }
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

    companion object{
        const val MODE_CREATE = 0
        const val MODE_EDIT = 1
        const val MODE_IMPORT = 2
    }
}