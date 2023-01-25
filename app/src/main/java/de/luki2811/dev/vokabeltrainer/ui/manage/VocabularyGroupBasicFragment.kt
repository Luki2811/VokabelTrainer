package de.luki2811.dev.vokabeltrainer.ui.manage

import android.content.DialogInterface
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
import de.luki2811.dev.vokabeltrainer.Id
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.Settings
import de.luki2811.dev.vokabeltrainer.VocabularyGroup
import de.luki2811.dev.vokabeltrainer.databinding.FragmentNewVocabularyGroupBinding
import java.io.File
import java.util.Locale


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
        binding.editTextVocabularyGroupOtherLanguage.setAdapter(adapterFirst)
        binding.editTextVocabularyGroupMainLanguage.setAdapter(adapterSecond)

        if((args.vocabularyGroup != null) && (args.keyMode == MODE_IMPORT || args.keyMode == MODE_EDIT)){
            vocabularyGroup = args.vocabularyGroup!!
            binding.editTextVocabularyGroupName.setText(vocabularyGroup.name)
            binding.editTextVocabularyGroupOtherLanguage.setText(vocabularyGroup.otherLanguage.getDisplayLanguage(appSettings.appLanguage))
            binding.editTextVocabularyGroupMainLanguage.setText(vocabularyGroup.mainLanguage.getDisplayLanguage(appSettings.appLanguage))
        }

        binding.buttonResetLevels.apply {
            if(args.keyMode != MODE_CREATE){
                setOnClickListener {
                    MaterialAlertDialogBuilder(requireContext())
                        .setIcon(R.drawable.ic_baseline_refresh_24)
                        .setTitle(context.getString(R.string.title_reset_all_levels))
                        .setMessage(context.getString(R.string.message_reset_levels))
                        .setPositiveButton(R.string.ok){ _, _ ->
                            vocabularyGroup.resetLevels(requireContext())
                        }
                        .setNegativeButton(R.string.cancel){_, _ -> }
                        .show()
                }
            }else{
                visibility = View.GONE
            }
        }

        binding.buttonCreateVocabularyGroupNext.setOnClickListener { goNext() }

        if(args.keyMode == VocabularyGroupWordEditorFragment.MODE_EDIT)
            binding.buttonDeleteVocabularyGroup2.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.ic_outline_delete_24)
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
        vocabularyGroup.deleteFromIndex(requireContext())
        vocabularyGroup.id.unregister(requireContext())
        Log.i("Info", "Successfully deleted vocabularyGroup with id ${vocabularyGroup.id}")
        findNavController().popBackStack()
    }

    private fun goNext() {
        val listLocales = arrayListOf<Locale>()
        Locale.getISOLanguages().forEach { listLocales.add(Locale(it)) }

        val otherLanguage = listLocales.find { it.getDisplayLanguage(Settings(requireContext()).appLanguage) == binding.editTextVocabularyGroupOtherLanguage.text.toString()}
        val mainLanguage = listLocales.find { it.getDisplayLanguage(Settings(requireContext()).appLanguage) == binding.editTextVocabularyGroupMainLanguage.text.toString()}


        if(otherLanguage == null) {
            binding.editTextVocabularyGroupOtherLanguageLayout.error = getString(R.string.err_lang_not_available)
            return
        }

        if(mainLanguage == null){
            binding.editTextVocabularyGroupMainLanguageLayout.error = getString(R.string.err_lang_not_available)
            return
        }

        when (VocabularyGroup.isNameValid(requireContext(), binding.editTextVocabularyGroupName.text.toString(), if (args.keyMode == MODE_EDIT) vocabularyGroup.name else "")) {
            VocabularyGroup.VALID -> {
                binding.editTextVocabularyGroupNameLayout.error = null

                vocabularyGroup = if(args.keyMode == MODE_CREATE){
                    VocabularyGroup(
                        name = binding.editTextVocabularyGroupName.text.toString(),
                        otherLanguage = otherLanguage,
                        mainLanguage = mainLanguage,
                        vocabulary = arrayListOf(),
                        id = Id.generate(requireContext())
                    )
                }else{
                    VocabularyGroup(
                        name = binding.editTextVocabularyGroupName.text.toString(),
                        otherLanguage = otherLanguage,
                        mainLanguage = mainLanguage,
                        vocabulary = args.vocabularyGroup!!.vocabulary,
                        id = args.vocabularyGroup!!.id
                    )
                }



                findNavController().navigate(VocabularyGroupBasicFragmentDirections.actionNewVocabularyGroupFragmentToEditVocabularyGroupFragment(vocabularyGroup, args.keyMode))
            }
            VocabularyGroup.INVALID_TOO_MANY_LINES -> {
                binding.editTextVocabularyGroupNameLayout.error = getString(R.string.err_too_many_lines, VocabularyGroup.MAX_LINES)
            }
            VocabularyGroup.INVALID_NAME_ALREADY_USED -> {
                binding.editTextVocabularyGroupNameLayout.error = getString(R.string.err_name_already_taken)
                return
            }
            VocabularyGroup.INVALID_TOO_MANY_CHARS -> {
                binding.editTextVocabularyGroupNameLayout.error = getString(R.string.err_name_too_long_max, VocabularyGroup.MAX_CHARS)
                return
            }
            VocabularyGroup.INVALID_EMPTY -> {
                binding.editTextVocabularyGroupNameLayout.error = getString(R.string.err_missing_name)
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