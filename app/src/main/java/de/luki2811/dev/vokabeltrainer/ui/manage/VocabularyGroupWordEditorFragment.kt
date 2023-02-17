package de.luki2811.dev.vokabeltrainer.ui.manage

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import de.luki2811.dev.vokabeltrainer.Id
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.Settings
import de.luki2811.dev.vokabeltrainer.Synonym
import de.luki2811.dev.vokabeltrainer.VocabularyGroup
import de.luki2811.dev.vokabeltrainer.VocabularyWord
import de.luki2811.dev.vokabeltrainer.WordFamily
import de.luki2811.dev.vokabeltrainer.WordTranslation
import de.luki2811.dev.vokabeltrainer.databinding.FragmentEditVocabularyGroupBinding
import java.io.File
import kotlin.math.roundToInt

class VocabularyGroupWordEditorFragment : Fragment() {

    private var _binding: FragmentEditVocabularyGroupBinding? = null
    private val args: VocabularyGroupWordEditorFragmentArgs by navArgs()
    private val binding get() = _binding!!

    private lateinit var vocabularyGroup: VocabularyGroup
    private var pos: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditVocabularyGroupBinding.inflate(inflater, container, false)

        vocabularyGroup = args.vocabularyGroup

        when(args.keyMode){
            MODE_CREATE -> {
                vocabularyGroup.vocabulary.add(WordTranslation("", vocabularyGroup.otherLanguage, arrayListOf(), vocabularyGroup.mainLanguage, true, levelOther = 0, levelMain = 0))
            }
            MODE_IMPORT -> {
                vocabularyGroup.id = Id.generate(requireContext()).apply { register(requireContext()) }
            }
        }

        if(Settings(requireContext()).suggestTranslation && TranslateLanguage.fromLanguageTag(vocabularyGroup.otherLanguage.language) != null && TranslateLanguage.fromLanguageTag(vocabularyGroup.mainLanguage.language) != null && vocabularyGroup.vocabulary[pos].typeOfWord == VocabularyWord.TYPE_TRANSLATION){
            val options = TranslatorOptions.Builder()
                .setTargetLanguage(TranslateLanguage.fromLanguageTag(vocabularyGroup.otherLanguage.language)!!)
                .setSourceLanguage(TranslateLanguage.fromLanguageTag(vocabularyGroup.mainLanguage.language)!!)
                .build()
            val secondToFirstTranslator = Translation.getClient(options)

            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()

            Log.i("Translator", "Start download if necessary")

            secondToFirstTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    Log.i("Translator", "Download successfully")
                    if(_binding == null)
                        return@addOnSuccessListener
                    binding.textEditEditorUpperInput.addTextChangedListener {
                        if(!it.isNullOrBlank() && vocabularyGroup.vocabulary[pos].typeOfWord == VocabularyWord.TYPE_TRANSLATION) {
                            secondToFirstTranslator.translate(it.toString())
                                .addOnSuccessListener { translatedText ->
                                    binding.chipGroupEditorSuggestionsLower.removeAllViews()
                                    if(!binding.textEditEditorLowerInput.text.toString().contains(translatedText, ignoreCase = true)) {
                                        binding.chipGroupEditorSuggestionsLower.addView(
                                            Chip(requireContext()).apply {
                                                setOnClickListener {
                                                    if(binding.textEditEditorLowerInput.text.isNullOrBlank()){
                                                        binding.textEditEditorLowerInput.setText(translatedText)
                                                        binding.chipGroupEditorSuggestionsLower.removeAllViews()
                                                    }else{
                                                        val sb = StringBuilder(binding.textEditEditorLowerInput.text!!).append("; ").append(translatedText)
                                                        binding.textEditEditorLowerInput.setText(sb.toString())
                                                        binding.chipGroupEditorSuggestionsLower.removeAllViews()
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
                                    binding.chipGroupEditorSuggestionsLower.removeAllViews()
                                    Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
                                }
                        }else{
                            binding.chipGroupEditorSuggestionsLower.removeAllViews()
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
                }

        }else
            Log.w("Translator", args.vocabularyGroup.otherLanguage.language + " or " + args.vocabularyGroup.mainLanguage.language + " are no TranslatorLanguages")

        Log.d("Editor VocabularyGroups", "Length: ${vocabularyGroup.vocabulary.size}")


        binding.textInputTypeOfWord.apply {
            setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.default_list_item,
                    arrayOf(getString(R.string.translation), getString(R.string.synonym), getString(R.string.antonym), getString(R.string.word_family))
                )
            )
            setOnItemClickListener { _, _, position, _ ->
                when(position){
                    0 -> {
                        vocabularyGroup.vocabulary[pos].typeOfWord = VocabularyWord.TYPE_TRANSLATION
                        changeLayout()
                    }
                    1 -> {
                        vocabularyGroup.vocabulary[pos].typeOfWord = VocabularyWord.TYPE_SYNONYM
                        changeLayout()
                        setChipWithMainWordFromPrevious()
                    }
                    2 -> {
                        vocabularyGroup.vocabulary[pos].typeOfWord = VocabularyWord.TYPE_ANTONYM
                        changeLayout()
                        setChipWithMainWordFromPrevious()
                    }
                    3 -> {
                        vocabularyGroup.vocabulary[pos].typeOfWord = VocabularyWord.TYPE_WORD_FAMILY
                        changeLayout()
                        setChipWithMainWordFromPrevious()
                    }
                }
            }
        }

        binding.buttonEditorChangeWords.setOnClickListener {
            val oldUpperInput = binding.textEditEditorUpperInput.text.toString()
            val oldLowerInput = binding.textEditEditorLowerInput.text.toString()
            binding.textEditEditorLowerInput.setText(oldUpperInput)
            binding.textEditEditorUpperInput.setText(oldLowerInput)
        }

        binding.buttonBackVocabularyWord.apply {
            setOnClickListener {
                if(isWordCorrect()){
                    saveWordInVocabularyGroup()
                    pos -= 1
                    if(pos == -1)
                        pos = vocabularyGroup.vocabulary.size-1
                    setWordInLayout()
                }
            }
        }

        binding.buttonNextVocabularyWord.apply {
            setOnClickListener {
                if(isWordCorrect()){
                    saveWordInVocabularyGroup()
                    pos += 1
                    if(pos == vocabularyGroup.vocabulary.size)
                        pos = 0
                    setWordInLayout()
                }
            }
        }

        binding.buttonAddNewVocabularyWordRightManage.setOnClickListener { addWord(0) }
        binding.buttonAddNewVocabularyWordLeftManage.setOnClickListener { addWord(1) }
        binding.buttonSaveAndGoBackManage.setOnClickListener { finishAndSave() }

        binding.sliderEditVocabularyGroupPosition.apply {
            if(args.keyMode != MODE_CREATE){
                valueFrom = 1F
                stepSize = 1F
                value = pos+1F
                valueTo = vocabularyGroup.vocabulary.size.toFloat()
                addOnChangeListener { slider, value, _ ->
                    if(isWordCorrect()) {
                        pos = value.roundToInt()-1
                        setWordInLayout()
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
            setOnClickListener { removeWord(pos) }
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

        binding.textEditEditorUpperInput.isFocusableInTouchMode = true
        binding.textEditEditorLowerInput.isFocusableInTouchMode = true

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

        setWordInLayout()

        return binding.root
    }

    private fun setChipWithMainWordFromPrevious() {
        if (pos != 0 && binding.textEditEditorUpperInput.text.isNullOrBlank()) {
            binding.chipGroupEditorSuggestionsUpper.addView(
                Chip(requireContext()).apply {
                    setOnClickListener {
                        binding.textEditEditorUpperInput.setText(vocabularyGroup.vocabulary[pos - 1].mainWord)
                        binding.chipGroupEditorSuggestionsUpper.removeAllViews()
                    }
                    text = vocabularyGroup.vocabulary[pos - 1].mainWord
                    chipIcon = getDrawable(requireContext(), R.drawable.ic_outline_add_24)
                    isChipIconVisible = true
                }
            )
        }
    }

    private fun setWordInLayout(){
        // Remove all from previous word
        binding.chipGroupEditorSuggestionsUpper.removeAllViews()
        binding.chipGroupEditorSuggestionsLower.removeAllViews()

        binding.textEditEditorUpperInputLayout.error = null
        binding.textEditEditorLowerInputLayout.error = null
        binding.textEditEditorWordTypeInputLayout.error = null

        // Set type of word
        binding.textInputTypeOfWord.setText(
            when(vocabularyGroup.vocabulary[pos].typeOfWord){
                VocabularyWord.TYPE_TRANSLATION -> getString(R.string.translation)
                VocabularyWord.TYPE_SYNONYM -> getString(R.string.synonym)
                VocabularyWord.TYPE_ANTONYM -> getString(R.string.antonym)
                VocabularyWord.TYPE_WORD_FAMILY -> getString(R.string.word_family)
                else -> {""} }, false
        )

        // Set words
        when(vocabularyGroup.vocabulary[pos].typeOfWord){
            VocabularyWord.TYPE_TRANSLATION, VocabularyWord.TYPE_SYNONYM, VocabularyWord.TYPE_ANTONYM -> {
                binding.textEditEditorUpperInput.setText(vocabularyGroup.vocabulary[pos].mainWord)
                binding.textEditEditorLowerInput.setText(vocabularyGroup.vocabulary[pos].getSecondWordsAsString())
            }
            VocabularyWord.TYPE_WORD_FAMILY -> {
                binding.textEditEditorWordTypeInput.setText(when((vocabularyGroup.vocabulary[pos] as WordFamily).otherWordsType){
                    WordFamily.WORD_NOUN -> getString(R.string.word_type_noun)
                    WordFamily.WORD_VERB -> getString(R.string.word_type_verb)
                    WordFamily.WORD_ADJECTIVE -> getString(R.string.word_type_adjective)
                    WordFamily.WORD_ADVERB -> getString(R.string.word_type_adverb)
                    else -> ""
                })
                binding.textEditEditorUpperInput.setText(vocabularyGroup.vocabulary[pos].mainWord)
                binding.textEditEditorLowerInput.setText((vocabularyGroup.vocabulary[pos] as WordFamily).getSecondWordsAsString())
            }
        }

        // Set ignoreCase
        binding.switchVocabularyWordIgnoreCaseManage.isChecked = vocabularyGroup.vocabulary[pos].isIgnoreCase

        // Update textView
        binding.textViewNumberOfVocManage.text = getString(R.string.number_voc_of_rest, (pos + 1), vocabularyGroup.vocabulary.size)

        // Update level
        binding.textViewLevelVocabularyWord.text = getString(R.string.level, vocabularyGroup.vocabulary[pos].levelMain.toString(), vocabularyGroup.vocabulary[pos].levelOther.toString())

        // Update slider
        if(args.keyMode != MODE_CREATE){
            binding.sliderEditVocabularyGroupPosition.apply {
                value = pos+1F
                valueTo = vocabularyGroup.vocabulary.size.toFloat()
            }
        }

        // Update Buttons
        binding.buttonSaveAndGoBackManage.isEnabled = vocabularyGroup.vocabulary.size >= 2

        binding.buttonDeleteVocabularyWord.apply {
            isEnabled = vocabularyGroup.vocabulary.size > 2
            if(isEnabled){
                setBackgroundColor(MaterialColors.harmonizeWithPrimary(requireContext(), context.getColor(R.color.Red)))
            }else{
                setBackgroundColor(MaterialColors.harmonizeWithPrimary(requireContext(), context.getColor(R.color.Gray)))
            }
        }

        changeLayout()
    }

    private fun isWordCorrect(): Boolean {
        var isCorrect = true

        // TODO: Check type

        binding.textEditEditorUpperInput.text.toString().trim().ifBlank {
            binding.textEditEditorUpperInputLayout.error = getString(R.string.err_missing_input)
            isCorrect = false
        }

        binding.textEditEditorLowerInput.text.toString().trim().ifBlank {
            binding.textEditEditorLowerInputLayout.error = getString(R.string.err_missing_input)
            isCorrect = false
        }

        if(vocabularyGroup.vocabulary[pos].typeOfWord == VocabularyWord.TYPE_WORD_FAMILY){
            if(binding.textEditEditorWordTypeInput.text.isNullOrBlank()){
                binding.textEditEditorWordTypeInputLayout.error = getString(R.string.err_missing_input)
                isCorrect = false
            }
        }

        return isCorrect
    }

    private fun saveWordInVocabularyGroup(){
        vocabularyGroup.vocabulary[pos] = when(vocabularyGroup.vocabulary[pos].typeOfWord){
            VocabularyWord.TYPE_TRANSLATION -> {
                val otherWords = binding.textEditEditorLowerInput.text.toString().split(";").toMutableList().onEach { it.trim() } as ArrayList<String>
                val mainWord = binding.textEditEditorUpperInput.text.toString().trim()
                val isIgnoreCase = binding.switchVocabularyWordIgnoreCaseManage.isChecked

                WordTranslation(mainWord, vocabularyGroup.otherLanguage, otherWords, vocabularyGroup.mainLanguage, isIgnoreCase, levelMain = vocabularyGroup.vocabulary[pos].levelMain, levelOther = vocabularyGroup.vocabulary[pos].levelOther)

            }

            VocabularyWord.TYPE_SYNONYM, VocabularyWord.TYPE_ANTONYM -> {
                val otherWords = binding.textEditEditorLowerInput.text.toString().split(";").toMutableList().onEach { it.trim() } as ArrayList<String>
                val mainWord = binding.textEditEditorUpperInput.text.toString().trim()
                val isIgnoreCase = binding.switchVocabularyWordIgnoreCaseManage.isChecked

                Synonym(mainWord, otherWords, vocabularyGroup.mainLanguage, isIgnoreCase = isIgnoreCase, typeOfWord = vocabularyGroup.vocabulary[pos].typeOfWord, levelMain = vocabularyGroup.vocabulary[pos].levelMain, levelOther = vocabularyGroup.vocabulary[pos].levelOther)
            }

            VocabularyWord.TYPE_WORD_FAMILY -> {
                val otherWords = binding.textEditEditorLowerInput.text.toString().split(";").toMutableList().onEach { it.trim() } as ArrayList<String>
                val otherWordsType: Int = when(binding.textEditEditorWordTypeInput.text.toString()){
                    getString(R.string.word_type_noun) -> WordFamily.WORD_NOUN
                    getString(R.string.word_type_verb) -> WordFamily.WORD_VERB
                    getString(R.string.word_type_adjective) -> WordFamily.WORD_ADJECTIVE
                    getString(R.string.word_type_adverb) -> WordFamily.WORD_ADVERB
                    else -> WordFamily.WORD_UNKNOWN
                }
                val mainWord = binding.textEditEditorUpperInput.text.toString().trim()
                val isIgnoreCase = binding.switchVocabularyWordIgnoreCaseManage.isChecked

                WordFamily(mainWord, otherWords, otherWordsType, vocabularyGroup.mainLanguage, isIgnoreCase, levelMain = vocabularyGroup.vocabulary[pos].levelMain, levelOther = vocabularyGroup.vocabulary[pos].levelOther)
            }

            else -> {
                WordTranslation("", vocabularyGroup.mainLanguage, arrayListOf(), vocabularyGroup.otherLanguage, true, levelMain = 0, levelOther = 0)
            }
        }

        val tempVocGroup = arrayListOf<VocabularyWord>()
        tempVocGroup.addAll(vocabularyGroup.vocabulary)

        if(tempVocGroup.apply { removeAt(pos) }.contains(vocabularyGroup.vocabulary[pos])){

            Snackbar.make(binding.root, R.string.word_already_in_vocabulary_group, Snackbar.LENGTH_LONG).apply {
                val posInt = pos
                setAction(getText(R.string.delete)){
                    removeWord(posInt)
                }
                show()
            }

        }

    }

    private fun finishAndSave(){
        if(isWordCorrect()) {
            saveWordInVocabularyGroup()

            if(args.keyMode == MODE_EDIT)
                vocabularyGroup.refreshNameInIndex(requireContext())

            if(args.keyMode == MODE_IMPORT || args.keyMode == MODE_CREATE)
                vocabularyGroup.saveInIndex(requireContext())

            vocabularyGroup.saveInFile(requireContext())
            findNavController().navigate(VocabularyGroupWordEditorFragmentDirections.actionEditVocabularyGroupFragmentToLearnFragment())
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
        vocabularyGroup.deleteFromIndex(requireContext())
        vocabularyGroup.id.unregister(requireContext())
        Log.i("Info", "Successfully deleted vocabularyGroup with id ${vocabularyGroup.id}")
        findNavController().navigate(R.id.action_editVocabularyGroupFragment_to_manageVocabularyGroupsFragment)
    }

    private fun addWord(direction: Int){
        if(isWordCorrect()){
            saveWordInVocabularyGroup()
            when(direction){
                0 -> {
                    pos += 1
                }
                1 -> {
                    // DO NOTHING
                }
            }
            vocabularyGroup.vocabulary.add(pos, WordTranslation("", vocabularyGroup.mainLanguage, arrayListOf(), vocabularyGroup.otherLanguage, binding.switchVocabularyWordIgnoreCaseManage.isChecked, levelMain = 0, levelOther = 0))
            binding.textEditEditorUpperInput.requestFocus()
            val imm: InputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.textEditEditorUpperInput, InputMethodManager.SHOW_IMPLICIT)

            setWordInLayout()

        }
    }

    private fun removeWord(pos: Int){
        vocabularyGroup.vocabulary.removeAt(pos)
        if(this.pos != 0)
            this.pos -= 1

        setWordInLayout()
    }


    private fun changeLayout() {

        when(vocabularyGroup.vocabulary[pos].typeOfWord){
            VocabularyWord.TYPE_TRANSLATION -> {
                binding.textEditEditorLowerInputLayout.helperText = getString(R.string.word_in_other_language)
                binding.textEditEditorUpperInputLayout.helperText = getString(R.string.word_in_main_language)
                binding.textEditEditorWordTypeInputLayout.visibility = View.GONE
            }
            VocabularyWord.TYPE_SYNONYM -> {
                binding.textEditEditorLowerInputLayout.helperText = getString(R.string.synonym_other)
                binding.textEditEditorUpperInputLayout.helperText = getString(R.string.synonym_main)
                binding.textEditEditorWordTypeInputLayout.visibility = View.GONE
            }
            VocabularyWord.TYPE_ANTONYM -> {
                binding.textEditEditorLowerInputLayout.helperText = getString(R.string.antonym_second)
                binding.textEditEditorUpperInputLayout.helperText = getString(R.string.antonym_main)
                binding.textEditEditorWordTypeInputLayout.visibility = View.GONE
            }
            VocabularyWord.TYPE_WORD_FAMILY -> {
                binding.textEditEditorUpperInputLayout.helperText = getString(R.string.word_family_main)
                binding.textEditEditorLowerInputLayout.helperText = getString(R.string.word_family_other)
                binding.textEditEditorWordTypeInputLayout.visibility = View.VISIBLE
                binding.textEditEditorWordTypeInput.setAdapter(ArrayAdapter(requireContext(), R.layout.default_list_item, arrayOf(getString(R.string.word_type_noun), getString(R.string.word_type_verb), getString(R.string.word_type_adjective), getString(R.string.word_type_adverb))))
            }
        }

        binding.chipGroupEditorSuggestionsUpper.removeAllViews()
        binding.chipGroupEditorSuggestionsLower.removeAllViews()
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