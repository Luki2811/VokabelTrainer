package de.luki2811.dev.vokabeltrainer.ui.manage

import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.Settings
import de.luki2811.dev.vokabeltrainer.VocabularyGroup
import de.luki2811.dev.vokabeltrainer.databinding.FragmentNewVocabularyGroupBinding
import org.json.JSONObject
import java.io.File
import java.util.*


class VocabularyGroupBasicFragment : Fragment() {

    private var _binding: FragmentNewVocabularyGroupBinding? = null
    private val binding get() = _binding!!
    private val args:VocabularyGroupBasicFragmentArgs by navArgs()
    private lateinit var vocabularyGroup: VocabularyGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewVocabularyGroupBinding.inflate(inflater, container, false)

        val listLocales = arrayListOf<Locale>()
        Locale.getISOLanguages().filter { it != "und" && it != "mdh" && it != "mis" }.forEach { listLocales.add(Locale(it)) }

        val listNames = arrayListOf<String>()
        val appSettings = Settings(requireContext())

        listLocales.forEach { listNames.add(it.getDisplayLanguage(appSettings.appLanguage)) }
        listNames.sortBy { it }


        val adapterFirst = ArrayAdapter(requireContext(),R.layout.default_list_item, listNames.toTypedArray())
        val adapterSecond = ArrayAdapter(requireContext(),R.layout.default_list_item, listNames.toTypedArray())
        binding.editTextVocabularyGroupFirstLanguage.setAdapter(adapterFirst)
        binding.editTextVocabularyGroupSecondLanguage.setAdapter(adapterSecond)

        if((!args.keyVocGroup.isNullOrEmpty()) && (args.keyMode == MODE_IMPORT || args.keyMode == MODE_EDIT)){
            vocabularyGroup = VocabularyGroup(JSONObject(args.keyVocGroup.toString()), context = requireContext())
            binding.editTextVocabularyGroupName.setText(vocabularyGroup.name)
            binding.editTextVocabularyGroupFirstLanguage.setText(vocabularyGroup.firstLanguage.getDisplayLanguage(appSettings.appLanguage))
            binding.editTextVocabularyGroupSecondLanguage.setText(vocabularyGroup.secondLanguage.getDisplayLanguage(appSettings.appLanguage))
        }

        binding.buttonCreateVocabularyGroupNext.setOnClickListener { goNext() }

        if(args.keyMode == VocabularyGroupWordEditorFragment.MODE_EDIT)
            binding.buttonDeleteVocabularyGroup2.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.outline_delete_24)
                    .setTitle(getString(R.string.delete_vocabulary_group))
                    .setMessage(R.string.do_you_really_want_to_delete_vocabulary_group)
                    .setPositiveButton(getString(R.string.delete)){ _: DialogInterface, _: Int ->
                        deleteVocabularyGroup()
                    }
                    .setNegativeButton(getString(R.string.cancel)){ _: DialogInterface, _: Int ->
                        Toast.makeText(requireContext(), getString(R.string.cancelled), Toast.LENGTH_SHORT).show()
                    }
                    .show()
            }
        else
            binding.buttonDeleteVocabularyGroup2.visibility = View.GONE

        return binding.root
    }

    private fun deleteVocabularyGroup() {
        if (args.keyMode == MODE_CREATE) {
            Toast.makeText(requireContext(), getText(R.string.err_could_not_delete_vocabulary_group), Toast.LENGTH_LONG).show()
            return
        }

        var file = File(requireContext().filesDir, "vocabularyGroups")
        file.mkdirs()
        file = File(file, vocabularyGroup.id.number.toString() + ".json" )
        if(!file.delete()){
            Toast.makeText(requireContext(), getString(R.string.err_could_not_delete_vocabulary_group), Toast.LENGTH_LONG).show()
            Log.w("Error", "Failed to delete vocabularyGroup with id ${vocabularyGroup.id}")
            return
        }
        vocabularyGroup.deleteFromIndex()
        vocabularyGroup.id.deleteId()
        Log.i("Info", "Successfully deleted vocabularyGroup with id ${vocabularyGroup.id}")
        findNavController().popBackStack()
    }

    private fun goNext() {
        val listLocales = arrayListOf<Locale>()
        Locale.getISOLanguages().forEach { listLocales.add(Locale(it)) }

        val firstLanguage = listLocales.find { it.getDisplayLanguage(Settings(requireContext()).appLanguage) == binding.editTextVocabularyGroupFirstLanguage.text.toString()}
        val secondLanguage = listLocales.find { it.getDisplayLanguage(Settings(requireContext()).appLanguage) == binding.editTextVocabularyGroupSecondLanguage.text.toString()}


        if(firstLanguage == null || secondLanguage == null) {
            Toast.makeText(requireContext(), R.string.err_lang_not_available, Toast.LENGTH_LONG).show()
            return
        }

        when (VocabularyGroup.isNameValid(requireContext(), binding.editTextVocabularyGroupName.text.toString(), if (args.keyMode == MODE_EDIT) vocabularyGroup.name else "")) {
            0 -> {
                binding.editTextVocabularyGroupName.error = null

                if(args.keyMode == MODE_CREATE){
                    vocabularyGroup = VocabularyGroup(
                        name = binding.editTextVocabularyGroupName.text.toString(),
                        firstLanguage = firstLanguage,
                        secondLanguage = secondLanguage,
                        vocabulary = arrayListOf(),
                        context = requireContext()
                    )
                }else
                    vocabularyGroup = VocabularyGroup(
                        name = binding.editTextVocabularyGroupName.text.toString(),
                        firstLanguage = firstLanguage,
                        secondLanguage = secondLanguage,
                        vocabulary = VocabularyGroup(JSONObject(args.keyVocGroup.toString()), context = requireContext()).vocabulary,
                        context = requireContext(),
                        id = VocabularyGroup(JSONObject(args.keyVocGroup.toString()), context = requireContext()).id
                    )



                findNavController().navigate(VocabularyGroupBasicFragmentDirections.actionNewVocabularyGroupFragmentToEditVocabularyGroupFragment(
                    vocabularyGroup.getAsJson().toString(), "", args.keyMode))
            }
            1 -> {
                binding.editTextVocabularyGroupName.error = getString(R.string.err_name_contains_wrong_letter)
                return
            }
            2 -> {
                binding.editTextVocabularyGroupName.error = getString(R.string.err_name_already_taken)
                return
            }
            3 -> {
                binding.editTextVocabularyGroupName.error = getString(R.string.err_name_too_long_max, 50)
                return
            }
            4 -> {
                binding.editTextVocabularyGroupName.error = getString(R.string.err_missing_name)
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