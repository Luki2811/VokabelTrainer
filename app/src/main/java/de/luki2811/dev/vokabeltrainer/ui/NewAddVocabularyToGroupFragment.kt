package de.luki2811.dev.vokabeltrainer.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import de.luki2811.dev.vokabeltrainer.AppFile.Companion.writeInFile
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.VocabularyGroup
import de.luki2811.dev.vokabeltrainer.VocabularyWord
import de.luki2811.dev.vokabeltrainer.databinding.FragmentNewAddVocabularyToGroupBinding
import java.io.File


class NewAddVocabularyToGroupFragment : Fragment() {

    private var _binding: FragmentNewAddVocabularyToGroupBinding? = null
    private val binding get() = _binding!!

    private var vocabulary:MutableList<VocabularyWord> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewAddVocabularyToGroupBinding.inflate(inflater, container, false)

        binding.textViewNumberOfVoc.text = getString(R.string.number_vocs, vocabulary.size)

        binding.buttonAddNewVocabularyWord.setOnClickListener { addVocabulary() }
        binding.buttonFinishAddVocabulary.setOnClickListener { finishCreateVocabularyGroup() }

        binding.textEditVocabularyWordNew.isFocusableInTouchMode = true
        binding.editTextVocabularyWordKnown.isFocusableInTouchMode = true

        // TODO: Nach dem letzten Textfeld soll mit "Enter" Vokabel hinzufügt werden
        /**
        binding.editTextVocabularyWordKnown.setOnKeyListener(View.OnKeyListener { view, keyCode, event ->

            // If the event is a key-down event on the "enter" button

            Log.e("KEYCODE", keyCode.toString())

            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                // Perform action on key press
                view.clearFocus()
                addVocabulary(true)
                return@OnKeyListener true
            }
            false
        })
         **/

        binding.textEditVocabularyWordNew.requestFocus()

        return binding.root
    }

    private fun addVocabulary(){
        // Get all for Vocabulary
        val newWord =
            binding.textEditVocabularyWordNew.text.toString().trim().ifBlank {
                binding.textEditVocabularyWordNew.error = getString(R.string.err_missing_name)
                return
            }
        val knownWord =
            binding.editTextVocabularyWordKnown.text.toString().trim().ifBlank {
                binding.editTextVocabularyWordKnown.error = getString(R.string.err_missing_name)
                return
            }

        val ignoreCase: Boolean = binding.switchVocabularyWordIgnoreCase.isChecked

        val vocWord = VocabularyWord(knownWord, newWord, ignoreCase)
        vocabulary.add(vocWord)

        // Remove text in EditTextFields
        binding.textEditVocabularyWordNew.text = null
        binding.editTextVocabularyWordKnown.text = null


        // Refresh number of vocabularyWords in list
        binding.textViewNumberOfVoc.text = getString(R.string.number_vocs, vocabulary.size)

        // enable finishButton if more than 2 Words are in one group
        if(vocabulary.size >= 2)
            binding.buttonFinishAddVocabulary.isEnabled = true

        // Focus upper TextView

        binding.textEditVocabularyWordNew.isFocusable = true
        binding.textEditVocabularyWordNew.requestFocus(1 )

        val imm: InputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.textEditVocabularyWordNew, InputMethodManager.SHOW_IMPLICIT)


    }



    private fun finishCreateVocabularyGroup(){
        val vocabularyGroup = VocabularyGroup(arguments?.getString("key_name")!!, vocabulary.toTypedArray() ,requireContext())
        // Register in Index
        vocabularyGroup.saveInIndex(requireContext())
        // Speichern in Datei
        // Name der Vokabelgruppe gleich der ID(.json)
        var file = File(requireContext().filesDir, "vocabularyGroups")
        file.mkdirs()
        file = File(file, vocabularyGroup.id.number.toString() + ".json" )
        writeInFile(vocabularyGroup.getAsJson().toString(), file)

        startActivity(Intent(requireContext(), MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}