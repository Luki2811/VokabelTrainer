package de.luki2811.dev.vokabeltrainer.ui.practice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.databinding.FragmentPracticeOutOfThreeBinding
import de.luki2811.dev.vokabeltrainer.ui.practice.PracticeActivity.Companion.quitPractice
import java.util.*

class PracticeOutOfThreeFragment: Fragment() {

    private var _binding: FragmentPracticeOutOfThreeBinding? = null
    private val binding get() = _binding!!
    private val args: PracticeOutOfThreeFragmentArgs by navArgs()
    private lateinit var exercise: Exercise
    private lateinit var word: VocabularyWord
    private var isCorrect = false
    private var wordOptions = ArrayList<VocabularyWord>()
    private var wordSelected: String = ""
    private var tts: TextToSpeechUtil? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPracticeOutOfThreeBinding.inflate(inflater, container, false)

        exercise = args.exercise
        tts = TextToSpeechUtil(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            quitPractice(requireActivity(),requireContext())
        }

        word = exercise.words[0]

        if(Settings(requireContext()).readOutVocabularyGeneral)
            binding.buttonSpeakChooseThree.setOnClickListener { speakWord(binding.textViewPracticeChooseThreeBottom.text.toString()) }
        else
            binding.buttonSpeakChooseThree.setIconResource(R.drawable.ic_outline_volume_off_24)

        binding.buttonSpeakChooseThree.setOnLongClickListener {
            val settings = Settings(requireContext())

            settings.readOutVocabularyGeneral = !settings.readOutVocabularyGeneral

            if(settings.readOutVocabularyGeneral) {
                binding.buttonSpeakChooseThree.setIconResource(R.drawable.ic_outline_volume_up_24)
                binding.buttonSpeakChooseThree.setOnClickListener { speakWord(binding.textViewPracticeChooseThreeBottom.text.toString()) }
            }
            else {
                binding.buttonSpeakChooseThree.setIconResource(R.drawable.ic_outline_volume_off_24)
                binding.buttonSpeakChooseThree.setOnClickListener {  }
            }

            settings.saveSettingsInFile()

            true
        }

        // Set words
        wordOptions.addAll(exercise.words)
        wordOptions.shuffle()

        binding.chipPracticeOption1.text = if(!exercise.isOtherWordAskedAsAnswer) wordOptions[0].mainWord else wordOptions[0].getSecondWordsAsString()
        binding.chipPracticeOption2.text = if(!exercise.isOtherWordAskedAsAnswer) wordOptions[1].mainWord else wordOptions[1].getSecondWordsAsString()
        binding.chipPracticeOption3.text = if(!exercise.isOtherWordAskedAsAnswer) wordOptions[2].mainWord else wordOptions[2].getSecondWordsAsString()

        if(exercise.isOtherWordAskedAsAnswer){
            binding.textViewPracticeChooseThreeBottom.text = word.mainWord

            when(word.typeOfWord){
                VocabularyWord.TYPE_TRANSLATION -> {
                    binding.textViewPracticeChooseThreeTop.text = getString(R.string.translate_in_lang, (word as WordTranslation).otherLanguage.getDisplayLanguage(Settings(requireContext()).appLanguage))
                }
                VocabularyWord.TYPE_SYNONYM -> {
                    binding.textViewPracticeChooseThreeTop.text = getString(R.string.action_find_synonym)
                }
                VocabularyWord.TYPE_ANTONYM -> {
                    binding.textViewPracticeChooseThreeTop.text = getString(R.string.action_find_antonym)
                }
                VocabularyWord.TYPE_WORD_FAMILY -> {
                    binding.textViewPracticeChooseThreeTop.text = getString(R.string.action_find_word_family_type, when((word as WordFamily).otherWordsType){
                        WordFamily.WORD_NOUN -> getText(R.string.word_type_noun)
                        WordFamily.WORD_ADVERB -> getText(R.string.word_type_adverb)
                        WordFamily.WORD_VERB -> getText(R.string.word_type_verb)
                        WordFamily.WORD_ADJECTIVE -> getText(R.string.word_type_adjective)
                        else -> "UNKNOWN"
                    })
                }
            }

            if(exercise.readOut[0])
                speakWord(binding.textViewPracticeChooseThreeBottom.text.toString())
        }
        else {
            binding.textViewPracticeChooseThreeTop.text = getString(R.string.translate_in_lang, (word as WordTranslation).mainLanguage.getDisplayLanguage(Settings(requireContext()).appLanguage))
            binding.textViewPracticeChooseThreeBottom.text = word.getSecondWordsAsString()

            if(exercise.readOut[1]){
                speakWord(binding.textViewPracticeChooseThreeBottom.text.toString())
            }
        }

        if(word.isIgnoreCase) {
            binding.textViewPracticeInfo2.text = ""
            binding.textViewPracticeInfo2.visibility = View.GONE
        }
        else {
            binding.textViewPracticeInfo2.setText(R.string.look_for_case)
            binding.textViewPracticeInfo2.visibility = View.VISIBLE
        }

        binding.chipGroupPracticeOptions.setOnCheckedStateChangeListener{ _,_ ->
            binding.buttonCheckPractice2.isEnabled = binding.chipGroupPracticeOptions.checkedChipIds.isNotEmpty()

            when(binding.chipGroupPracticeOptions.checkedChipId){
                binding.chipPracticeOption1.id -> wordSelected = binding.chipPracticeOption1.text.toString()
                binding.chipPracticeOption2.id -> wordSelected = binding.chipPracticeOption2.text.toString()
                binding.chipPracticeOption3.id -> wordSelected = binding.chipPracticeOption3.text.toString()
            }


            val lang = when(word){
                is WordTranslation -> { if (exercise.isOtherWordAskedAsAnswer) (word as WordTranslation).otherLanguage else (word as WordTranslation).mainLanguage }
                is Synonym -> { (word as Synonym).language }
                is WordFamily -> { (word as WordFamily).language }
                else -> { Locale.ENGLISH }
            }

            when(binding.chipGroupPracticeOptions.checkedChipId){
                binding.chipPracticeOption1.id -> {
                    if((exercise.readOut[0] && !exercise.isOtherWordAskedAsAnswer) || (exercise.readOut[1] && exercise.isOtherWordAskedAsAnswer))
                        tts?.speak(binding.chipPracticeOption1.text.toString(), lang)
                }
                binding.chipPracticeOption2.id -> {
                    if((exercise.readOut[0] && !exercise.isOtherWordAskedAsAnswer) || (exercise.readOut[1] && exercise.isOtherWordAskedAsAnswer))
                        tts?.speak(binding.chipPracticeOption2.text.toString(), lang)
                }
                binding.chipPracticeOption3.id -> {
                    if((exercise.readOut[0] && !exercise.isOtherWordAskedAsAnswer) || (exercise.readOut[1] && exercise.isOtherWordAskedAsAnswer))
                        tts?.speak(binding.chipPracticeOption3.text.toString(), lang)
                }
            }
        }

        binding.buttonCheckPractice2.isEnabled = false

        binding.buttonCheckPractice2.setOnClickListener {
            startCorrection()
        }

        childFragmentManager.setFragmentResultListener("finishFragment", this){ _, _ ->
            findNavController().navigate(PracticeOutOfThreeFragmentDirections.actionPracticeOutOfThreeFragmentToPracticeStartFragment())
            requireActivity().supportFragmentManager.setFragmentResult("finished", bundleOf("result" to ExerciseResult(isCorrect, wordSelected) ))
        }

        return binding.root
    }

    private fun speakWord(text: String){
        val lang = when(word){
            is WordTranslation -> { if (exercise.isOtherWordAskedAsAnswer) (word as WordTranslation).otherLanguage else (word as WordTranslation).mainLanguage }
            is Synonym -> { (word as Synonym).language }
            is WordFamily -> { (word as WordFamily).language }
            else -> { Locale.ENGLISH }
        }
        if((exercise.readOut[0] && exercise.isOtherWordAskedAsAnswer) || (exercise.readOut[1] && !exercise.isOtherWordAskedAsAnswer))
            tts?.speak(text, lang)
    }

    private fun startCorrection(){
        isCorrect = isInputCorrect()

        val correctionBottomSheet = CorrectionBottomSheet()
        val alternativeText: String

        if(isCorrect){
            val answer: String = when(binding.chipGroupPracticeOptions.checkedChipId){
                binding.chipPracticeOption1.id -> binding.chipPracticeOption1.text.toString()
                binding.chipPracticeOption2.id -> binding.chipPracticeOption2.text.toString()
                binding.chipPracticeOption3.id -> binding.chipPracticeOption3.text.toString()
                else -> ""
            }

            val otherWords: ArrayList<String> = when(word){
                is WordTranslation -> (word as WordTranslation).otherWords
                is Synonym -> (word as Synonym).otherWords
                is WordFamily -> { (word as WordFamily).otherWords
                }
                else -> { ArrayList() }
            }

            var otherAlternatives = if(exercise.isOtherWordAskedAsAnswer) otherWords else ArrayList<String>().apply { add(word.mainWord) }

            otherAlternatives.replaceAll { it.trim() }

            val inputStrings = answer.trim().split(";").toMutableList()
            inputStrings.replaceAll { if(exercise.words[0].isIgnoreCase) it.lowercase().trim() else it.trim() }

            otherAlternatives = otherAlternatives.filter { !inputStrings.contains(it.lowercase()) } as ArrayList<String>

            alternativeText = if(otherAlternatives.isEmpty()){
                ""
            }else {
                val sb = StringBuilder(otherAlternatives[0].trim())
                if(otherAlternatives.size > 1) {
                    for (i in 1 until otherAlternatives.size) {
                        sb.append("; ").append(otherAlternatives[i].trim())
                    }
                }
                sb.toString()
            }
            correctionBottomSheet.arguments = bundleOf("alternativesText" to alternativeText, "isCorrect" to true)
        }else{
            if(exercise.isOtherWordAskedAsAnswer) {
                val otherWords: ArrayList<String> = when(word){
                    is WordTranslation -> (word as WordTranslation).otherWords
                    is Synonym -> (word as Synonym).otherWords
                    is WordFamily -> { (word as WordFamily).otherWords }
                    else -> { ArrayList() }
                }
                correctionBottomSheet.arguments = bundleOf("alternativesText" to otherWords, "isCorrect" to false)
            }
            else
                correctionBottomSheet.arguments = bundleOf("alternativesText" to word.mainWord, "isCorrect" to false)
        }

        correctionBottomSheet.show(childFragmentManager, CorrectionBottomSheet.TAG)
    }

    private fun isInputCorrect(): Boolean{

        val answer: String = when(binding.chipGroupPracticeOptions.checkedChipId){
            binding.chipPracticeOption1.id -> binding.chipPracticeOption1.text.toString()
            binding.chipPracticeOption2.id -> binding.chipPracticeOption2.text.toString()
            binding.chipPracticeOption3.id -> binding.chipPracticeOption3.text.toString()
            else -> ""
        }

        return if(exercise.isOtherWordAskedAsAnswer){
            word.getSecondWordsAsString() == answer
        }else{
            answer.equals(word.mainWord, word.isIgnoreCase)
        }
    }
}