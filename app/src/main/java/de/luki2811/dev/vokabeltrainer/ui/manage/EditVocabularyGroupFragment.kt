package de.luki2811.dev.vokabeltrainer.ui.manage

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.databinding.FragmentEditVocabularyGroupBinding
import de.luki2811.dev.vokabeltrainer.ui.MainActivity
import org.json.JSONObject
import java.io.File

class EditVocabularyGroupFragment : Fragment() {

    private var _binding: FragmentEditVocabularyGroupBinding? = null
    private val binding get() = _binding!!
    lateinit var vocabularyGroup: VocabularyGroup
    lateinit var vocabulary: ArrayList<VocabularyWord>
    private lateinit var languageNew: Language
    private lateinit var languageKnown: Language

    var pos: Int = 0
    private val args:EditVocabularyGroupFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditVocabularyGroupBinding.inflate(inflater, container, false)

        if(args.keyMode == MODE_EDIT || args.keyMode == MODE_IMPORT){
            vocabularyGroup = VocabularyGroup(JSONObject(args.keyVocGroupWithName!!), context = requireContext())

            languageKnown = vocabularyGroup.languageKnown
            languageNew = vocabularyGroup.languageNew

            if(args.keyMode == MODE_IMPORT){
                vocabularyGroup.id = Id(requireContext())
            }
            vocabulary = ArrayList(vocabularyGroup.vocabulary.asList())
        } else {
            vocabulary = ArrayList()

            languageKnown = Language(args.vocGroupLangTypeKnown, requireContext())
            languageNew = Language(args.vocGroupLangTypeNew, requireContext())

            vocabulary.add(VocabularyWord("", languageKnown , "", languageNew, true))
            vocabularyGroup = VocabularyGroup(args.keyVocGroupName, languageKnown, languageNew, vocabulary.toTypedArray(), requireContext())
        }

        binding.buttonBackVocabularyWord.setOnClickListener {
            if(refreshVocabularyWord()){
                pos -= 1
                refresh()
            }
        }

        binding.buttonNextVocabularyWord.setOnClickListener {
            if(refreshVocabularyWord()){
                pos += 1
                refresh()
            }
        }

        binding.buttonAddNewVocabularyWordRightManage.setOnClickListener { addVocabularyWord(0) }
        binding.buttonAddNewVocabularyWordLeftManage.setOnClickListener { addVocabularyWord(1) }
        binding.buttonSaveAndGoBackManage.setOnClickListener { finish() }

        binding.buttonDeleteVocabularyWord.setOnClickListener { removeVocabularyWord() }

        if(args.keyMode == MODE_EDIT)
            binding.buttonDeleteVocabularyGroup.setOnClickListener {
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
            binding.buttonDeleteVocabularyGroup.visibility = View.GONE

        binding.editTextVocabularyWordKnownManage.isFocusableInTouchMode = true
        binding.textEditVocabularyWordNewManage.isFocusableInTouchMode = true

        // TODO: Nach dem letzten Textfeld soll mit "Enter" Vokabel hinzufügt werden

        /** binding.editTextVocabularyWordKnownManage.setOnKeyListener(View.OnKeyListener { view, keyCode, event ->

        // If the event is a key-down event on the "enter" button

        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
        // Perform action on key press
        view.clearFocus()
        addVocabularyWord(0)
        return@OnKeyListener true
        }
        false
        }) **/

        refresh()

        return binding.root
    }

    private fun refreshVocabularyWord(): Boolean {
        vocabulary[pos].newWord =
            binding.textEditVocabularyWordNewManage.text.toString().trim().ifBlank {
                binding.textEditVocabularyWordNewManage.error = getString(R.string.err_missing_name)
                return false
            }
        vocabulary[pos].knownWord =
            binding.editTextVocabularyWordKnownManage.text.toString().trim().ifBlank {
                binding.editTextVocabularyWordKnownManage.error = getString(R.string.err_missing_name)
                return false
            }
        vocabulary[pos].isIgnoreCase = binding.switchVocabularyWordIgnoreCaseManage.isChecked
        return true
    }

    private fun addVocabularyWord(direction: Int) {
        if(refreshVocabularyWord()){
                when(direction){
                    0 -> {
                        pos += 1
                    }
                    1 -> {
                        // DO NOTHING
                    }
                }
                binding.textEditVocabularyWordNewManage.text = null
                binding.editTextVocabularyWordKnownManage.text = null
                vocabulary.add(pos,VocabularyWord("", languageKnown, "", languageNew, binding.switchVocabularyWordIgnoreCaseManage.isChecked))
                binding.textEditVocabularyWordNewManage.requestFocus()

                val imm: InputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.textEditVocabularyWordNewManage, InputMethodManager.SHOW_IMPLICIT)
                refresh()
            }
    }

    private fun deleteVocabularyGroup() {

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
        findNavController().navigate(R.id.action_editVocabularyGroupFragment_to_manageVocabularyGroupsFragment)
    }

    private fun removeVocabularyWord() {
        vocabulary.removeAt(pos)
        if(pos != 0)
            pos -= 1
        refresh()
    }

    private fun finish(){
        if(refreshVocabularyWord()) {
            vocabularyGroup.vocabulary = vocabulary.toTypedArray()

            if(args.keyMode == MODE_EDIT)
                vocabularyGroup.refreshNameInIndex()

            if(args.keyMode == MODE_IMPORT || args.keyMode == MODE_CREATE)
                vocabularyGroup.saveInIndex()

            vocabularyGroup.saveInFile()
            findNavController().navigate(EditVocabularyGroupFragmentDirections.actionEditVocabularyGroupFragmentToLearnFragment())
        }
    }

    private fun refresh() {
        binding.textEditVocabularyWordNewManage.error = null
        binding.editTextVocabularyWordKnownManage.error = null
        binding.editTextVocabularyWordKnownManage.setText(vocabulary[pos].knownWord)
        binding.textEditVocabularyWordNewManage.setText(vocabulary[pos].newWord)
        binding.switchVocabularyWordIgnoreCaseManage.isChecked = vocabulary[pos].isIgnoreCase
        binding.textViewNumberOfVocManage.text =
            getString(R.string.number_voc_of_rest, (pos + 1), vocabulary.size)

        when (pos) {
            vocabulary.size - 1 -> {
                binding.buttonNextVocabularyWord.isEnabled = false
                binding.buttonBackVocabularyWord.isEnabled = true
            }
            0 -> {
                binding.buttonBackVocabularyWord.isEnabled = false
                binding.buttonNextVocabularyWord.isEnabled = true
            }
            else -> {
                binding.buttonBackVocabularyWord.isEnabled = true
                binding.buttonNextVocabularyWord.isEnabled = true

            }
        }

        binding.buttonDeleteVocabularyWord.isEnabled = vocabulary.size > 2
        binding.buttonSaveAndGoBackManage.isEnabled = vocabulary.size > 1

        if(vocabulary.size <= 2){
            binding.buttonDeleteVocabularyWord.setColorFilter(ContextCompat.getColor(
                requireContext(),
                R.color.Gray),
                android.graphics.PorterDuff.Mode.SRC_IN)
        }else{
            binding.buttonDeleteVocabularyWord.setColorFilter(ContextCompat.getColor(
                requireContext(),
                R.color.White),
                android.graphics.PorterDuff.Mode.SRC_IN)
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