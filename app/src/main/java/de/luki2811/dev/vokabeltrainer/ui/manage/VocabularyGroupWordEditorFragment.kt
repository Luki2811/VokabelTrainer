package de.luki2811.dev.vokabeltrainer.ui.manage

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.databinding.FragmentEditVocabularyGroupBinding
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.math.roundToInt

class VocabularyGroupWordEditorFragment : Fragment() {

    private var _binding: FragmentEditVocabularyGroupBinding? = null
    private val binding get() = _binding!!
    private lateinit var vocabularyGroup: VocabularyGroup
    private var vocabulary = ArrayList<VocabularyWord>()
    private lateinit var firstLanguage: Locale
    private lateinit var secondLanguage: Locale

    private var pos: Int = 0
    private val args:VocabularyGroupWordEditorFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditVocabularyGroupBinding.inflate(inflater, container, false)

        vocabularyGroup = VocabularyGroup(JSONObject(args.keyVocGroupWithName!!), context = requireContext())
        firstLanguage = vocabularyGroup.firstLanguage
        secondLanguage = vocabularyGroup.secondLanguage


        if(args.keyMode == MODE_EDIT || args.keyMode == MODE_IMPORT){

            if(args.keyMode == MODE_IMPORT){
                vocabularyGroup.id = Id.generate(requireContext()).apply { register(requireContext()) }
            }
            vocabulary.addAll(vocabularyGroup.vocabulary)
        } else {
            vocabulary.add(WordTranslation("", firstLanguage , arrayListOf(), secondLanguage, true))
        }

        if(Settings(requireContext()).suggestTranslation && TranslateLanguage.fromLanguageTag(vocabularyGroup.firstLanguage.language) != null && TranslateLanguage.fromLanguageTag(vocabularyGroup.secondLanguage.language) != null && vocabulary[pos].typeOfWord == VocabularyWord.TYPE_TRANSLATION){
            val options = TranslatorOptions.Builder()
                .setTargetLanguage(TranslateLanguage.fromLanguageTag(vocabularyGroup.firstLanguage.language)!!)
                .setSourceLanguage(TranslateLanguage.fromLanguageTag(vocabularyGroup.secondLanguage.language)!!)
                .build()
            val secondToFirstTranslator = Translation.getClient(options)

            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()

            Log.i("Translator", "Start download if necessary")

            secondToFirstTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    Log.i("Translator", "Download successfully")
                    binding.textEditEditorSecondWord.addTextChangedListener {
                        if(!it.isNullOrBlank() && vocabulary[pos].typeOfWord == VocabularyWord.TYPE_TRANSLATION) {
                            secondToFirstTranslator.translate(it.toString())
                                .addOnSuccessListener { translatedText ->
                                    binding.chipGroupEditorSuggestions.removeAllViews()
                                    if(!binding.textEditEditorFirstWord.text.toString().contains(translatedText, ignoreCase = true)) {
                                        binding.chipGroupEditorSuggestions.addView(
                                            Chip(requireContext()).apply {
                                                setOnClickListener {
                                                    if(binding.textEditEditorFirstWord.text.isNullOrBlank()){
                                                        binding.textEditEditorFirstWord.setText(translatedText)
                                                        binding.chipGroupEditorSuggestions.removeAllViews()
                                                    }else{
                                                        val sb = StringBuilder(binding.textEditEditorFirstWord.text!!).append("; ").append(translatedText)
                                                        binding.textEditEditorFirstWord.setText(sb.toString())
                                                        binding.chipGroupEditorSuggestions.removeAllViews()
                                                    }
                                                }
                                                text = translatedText
                                                chipIcon = getDrawable(requireContext(), R.drawable.ic_outline_add_24)
                                                isChipIconVisible = true
                                            }
                                        )
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    binding.chipGroupEditorSuggestions.removeAllViews()
                                    Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
                                }
                        }else{
                            binding.chipGroupEditorSuggestions.removeAllViews()
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
                }


        }else
            Log.w("Translator", args.vocGroupLangTypeKnown + " or " + args.vocGroupLangTypeKnown + " are no TranslatorLanguages")

        Log.d("Editor VocabularyGroups", "Length: ${vocabulary.size}")

        binding.buttonToggleGroupTypeOfWord.apply {
            isSingleSelection = true
            isSelectionRequired = true
        }

        binding.buttonToggleGroupTypeOfWord.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if(isChecked){
                when(checkedId){
                    binding.buttonToggleTypeofWord1.id -> setLayoutType(VocabularyWord.TYPE_TRANSLATION)
                    binding.buttonToggleTypeofWord2.id -> setLayoutType(VocabularyWord.TYPE_SYNONYM)
                    binding.buttonToggleTypeofWord3.id -> setLayoutType(VocabularyWord.TYPE_ANTONYM)
                }
            }
        }

        binding.buttonEditorChangeWords.setOnClickListener {
            val oldFirstWord = binding.textEditEditorFirstWord.text.toString()
            val oldSecondWord = binding.textEditEditorSecondWord.text.toString()
            binding.textEditEditorSecondWord.setText(oldFirstWord)
            binding.textEditEditorFirstWord.setText(oldSecondWord)
        }

        binding.buttonBackVocabularyWord.apply {
            // setBackgroundColor(MaterialColors.harmonizeWithPrimary(requireContext(), context.getColor(R.color.Blue)))
            setOnClickListener {
                if(refreshVocabularyWord()){
                    pos -= 1
                    if(pos == -1)
                        pos = vocabulary.size-1
                    refresh()
                }
            }
        }

        binding.buttonNextVocabularyWord.apply {
            // setBackgroundColor(MaterialColors.harmonizeWithPrimary(requireContext(), context.getColor(R.color.Blue)))
            setOnClickListener {
                if(refreshVocabularyWord()){
                    pos += 1
                    if(pos == vocabulary.size)
                        pos = 0
                    refresh()
                }
            }
        }

        binding.buttonAddNewVocabularyWordRightManage.setOnClickListener { addVocabularyWord(0) }
        binding.buttonAddNewVocabularyWordLeftManage.setOnClickListener { addVocabularyWord(1) }
        binding.buttonSaveAndGoBackManage.setOnClickListener { finish() }

        binding.sliderEditVocabularyGroupPosition.apply {
            if(args.keyMode != MODE_CREATE){
                valueFrom = 1F
                stepSize = 1F
                value = pos+1F
                valueTo = vocabulary.size.toFloat()
                addOnChangeListener { slider, value, _ ->
                    if(refreshVocabularyWord()) {
                        pos = value.roundToInt()-1
                        refresh()
                    }else{
                        slider.value = pos.toFloat()+1
                    }
                }
            }else{
                visibility = View.GONE
            }
        }

        binding.buttonDeleteVocabularyWord.apply {
            setBackgroundColor(MaterialColors.harmonizeWithPrimary(requireContext(), context.getColor(R.color.Red)))
            setOnClickListener { removeVocabularyWord() }
        }

        if(args.keyMode == MODE_EDIT)
            binding.buttonDeleteVocabularyGroup.setOnClickListener {
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
            binding.buttonDeleteVocabularyGroup.visibility = View.GONE

        binding.textEditEditorFirstWord.isFocusableInTouchMode = true
        binding.textEditEditorSecondWord.isFocusableInTouchMode = true

        // TODO: After last text edit -> press enter to create new vocabulary word

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

    private fun setLayoutType(type: Int) {
        // binding.textEditEditorFirstWordLayout.hint = "First word"
        // binding.textEditEditorSecondWordLayout.hint = "Second word"

        when(type){
            VocabularyWord.TYPE_TRANSLATION -> {
                binding.textEditEditorFirstWordLayout.helperText = getString(R.string.word_in_first_language)
                binding.textEditEditorSecondWordLayout.helperText = getString(R.string.word_in_second_language)
            }
            VocabularyWord.TYPE_SYNONYM -> {
                binding.textEditEditorFirstWordLayout.helperText = getString(R.string.synonym_main)
                binding.textEditEditorSecondWordLayout.helperText = getString(R.string.synonym_other)
                binding.chipGroupEditorSuggestions.removeAllViews()
            }
            VocabularyWord.TYPE_ANTONYM -> {
                binding.textEditEditorFirstWordLayout.helperText = getString(R.string.antonym_main)
                binding.textEditEditorSecondWordLayout.helperText = getString(R.string.antonym_second)
                binding.chipGroupEditorSuggestions.removeAllViews()
            }
        }
    }

    private fun refreshVocabularyWord(): Boolean {
        var isCorrect = true

        val secondWord = binding.textEditEditorSecondWord.text.toString().split(";").onEach {
            it.trim()
        } as ArrayList<String>

        binding.textEditEditorSecondWord.text.toString().ifBlank {
            binding.textEditEditorSecondWordLayout.error = getString(R.string.err_missing_input)
            isCorrect = false
        }

        val firstWord = binding.textEditEditorFirstWord.text.toString().trim()
        firstWord.ifBlank {
            binding.textEditEditorFirstWordLayout.error = getString(R.string.err_missing_input)
            isCorrect = false
        }

        val typeOfWord = when(binding.buttonToggleGroupTypeOfWord.checkedButtonId){
            binding.buttonToggleTypeofWord1.id -> VocabularyWord.TYPE_TRANSLATION
            binding.buttonToggleTypeofWord2.id -> VocabularyWord.TYPE_SYNONYM
            binding.buttonToggleTypeofWord3.id -> VocabularyWord.TYPE_ANTONYM
            else -> VocabularyWord.TYPE_UNKNOWN
        }

        if(typeOfWord == VocabularyWord.TYPE_UNKNOWN)
            isCorrect = false

        val word = WordTranslation(firstWord, vocabularyGroup.firstLanguage, secondWord, vocabularyGroup.secondLanguage, binding.switchVocabularyWordIgnoreCaseManage.isChecked, typeOfWord = typeOfWord)

        val tempVocGroup = arrayListOf<VocabularyWord>()
        tempVocGroup.addAll(vocabulary)

        if(tempVocGroup.apply { removeAt(pos) }.contains(word)){
            Toast.makeText(requireContext(), getString(R.string.word_already_in_vocabulary_group), Toast.LENGTH_LONG).show()
            isCorrect = false
        }

        vocabulary[pos] = word
        return isCorrect
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
                binding.textEditEditorFirstWord.text = null
                binding.textEditEditorSecondWord.text = null
                vocabulary.add(pos,WordTranslation("", firstLanguage, arrayListOf(), secondLanguage, binding.switchVocabularyWordIgnoreCaseManage.isChecked))
                binding.textEditEditorSecondWord.requestFocus()

                val imm: InputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.textEditEditorSecondWord, InputMethodManager.SHOW_IMPLICIT)
            binding.chipGroupEditorSuggestions.removeAllViews()
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
        vocabularyGroup.id.unregister(requireContext())
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
            vocabularyGroup.vocabulary = vocabulary

            if(args.keyMode == MODE_EDIT)
                vocabularyGroup.refreshNameInIndex()

            if(args.keyMode == MODE_IMPORT || args.keyMode == MODE_CREATE)
                vocabularyGroup.saveInIndex()

            vocabularyGroup.saveInFile()
            findNavController().navigate(VocabularyGroupWordEditorFragmentDirections.actionEditVocabularyGroupFragmentToLearnFragment())
        }
    }

    private fun refresh() {
        binding.textEditEditorSecondWordLayout.error = null
        binding.textEditEditorFirstWordLayout.error = null
        binding.textEditEditorFirstWord.setText(vocabulary[pos].mainWord)
        binding.textEditEditorSecondWord.setText(vocabulary[pos].getSecondWordsAsString())
        binding.switchVocabularyWordIgnoreCaseManage.isChecked = vocabulary[pos].isIgnoreCase
        binding.textViewNumberOfVocManage.text = getString(R.string.number_voc_of_rest, (pos + 1), vocabulary.size)

        /** when (pos) {
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
        } **/

        when(vocabulary[pos].typeOfWord){
            VocabularyWord.TYPE_TRANSLATION -> binding.buttonToggleTypeofWord1.isChecked = true
            VocabularyWord.TYPE_SYNONYM -> binding.buttonToggleTypeofWord2.isChecked = true
            VocabularyWord.TYPE_ANTONYM -> binding.buttonToggleTypeofWord3.isChecked = true
        }

        if(args.keyMode != MODE_CREATE){
            binding.sliderEditVocabularyGroupPosition.apply {
                value = pos+1F
                valueTo = vocabulary.size.toFloat()
            }
        }

        /** binding.buttonNextVocabularyWord.apply {
            if(isEnabled)
                setBackgroundColor(MaterialColors.harmonizeWithPrimary(requireContext(), context.getColor(R.color.Blue)))
            else
                setBackgroundColor(MaterialColors.harmonizeWithPrimary(requireContext(), context.getColor(R.color.Gray)))
        }

        binding.buttonBackVocabularyWord.apply {
            if(isEnabled)
                setBackgroundColor(MaterialColors.harmonizeWithPrimary(requireContext(), context.getColor(R.color.Blue)))
            else
                setBackgroundColor(MaterialColors.harmonizeWithPrimary(requireContext(), context.getColor(R.color.Gray)))
        } **/

        binding.buttonSaveAndGoBackManage.isEnabled = vocabulary.size > 1

        binding.buttonDeleteVocabularyWord.apply {
            isEnabled = vocabulary.size > 2
            if(isEnabled){
                setBackgroundColor(MaterialColors.harmonizeWithPrimary(requireContext(), context.getColor(R.color.Red)))
            }else{
                setBackgroundColor(MaterialColors.harmonizeWithPrimary(requireContext(), context.getColor(R.color.Gray)))
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