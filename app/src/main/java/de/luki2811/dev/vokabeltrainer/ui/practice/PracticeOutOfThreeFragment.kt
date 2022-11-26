package de.luki2811.dev.vokabeltrainer.ui.practice

import android.os.Bundle
import android.util.Log
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
import org.json.JSONObject

class PracticeOutOfThreeFragment: Fragment() {

    private var _binding: FragmentPracticeOutOfThreeBinding? = null
    private val binding get() = _binding!!
    private val args: PracticeOutOfThreeFragmentArgs by navArgs()
    private lateinit var word: VocabularyWord
    private lateinit var exercise: Exercise
    private var isCorrect = false
    private var wordOptions = ArrayList<VocabularyWord>()
    private var wordSelected: String = ""
    private var tts: TextToSpeechUtil? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPracticeOutOfThreeBinding.inflate(inflater, container, false)

        tts = TextToSpeechUtil(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            quitPractice(requireActivity(),requireContext())
        }

        exercise = Exercise(JSONObject(args.exercise))
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

        binding.chipPracticeOption1.text = if(!exercise.isSecondWordAskedAsAnswer) wordOptions[0].firstWord else wordOptions[0].secondWord
        binding.chipPracticeOption2.text = if(!exercise.isSecondWordAskedAsAnswer) wordOptions[1].firstWord else wordOptions[1].secondWord
        binding.chipPracticeOption3.text = if(!exercise.isSecondWordAskedAsAnswer) wordOptions[2].firstWord else wordOptions[2].secondWord

        if(exercise.isSecondWordAskedAsAnswer){
            binding.textViewPracticeChooseThreeBottom.text = word.firstWord
            binding.textViewPracticeChooseThreeTop.text = getString(R.string.translate_in_lang, word.secondLanguage.getDisplayLanguage(Settings(requireContext()).appLanguage))
            if(exercise.readOut[0])
                speakWord(binding.textViewPracticeChooseThreeBottom.text.toString())
        }
        else {
            binding.textViewPracticeChooseThreeBottom.text = word.secondWord
            binding.textViewPracticeChooseThreeTop.text = getString(R.string.translate_in_lang, word.firstLanguage.getDisplayLanguage(Settings(requireContext()).appLanguage))
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

            val lang = if (exercise.isSecondWordAskedAsAnswer) word.secondLanguage else word.firstLanguage

            when(binding.chipGroupPracticeOptions.checkedChipId){
                binding.chipPracticeOption1.id -> {
                    if((exercise.readOut[0] && !exercise.isSecondWordAskedAsAnswer) || (exercise.readOut[1] && exercise.isSecondWordAskedAsAnswer))
                        tts?.speak(binding.chipPracticeOption1.text.toString(), lang)
                }
                binding.chipPracticeOption2.id -> {
                    if((exercise.readOut[0] && !exercise.isSecondWordAskedAsAnswer) || (exercise.readOut[1] && exercise.isSecondWordAskedAsAnswer))
                        tts?.speak(binding.chipPracticeOption2.text.toString(), lang)
                }
                binding.chipPracticeOption3.id -> {
                    if((exercise.readOut[0] && !exercise.isSecondWordAskedAsAnswer) || (exercise.readOut[1] && exercise.isSecondWordAskedAsAnswer))
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
        val lang = if (exercise.isSecondWordAskedAsAnswer) word.firstLanguage else word.secondLanguage
        if((exercise.readOut[0] && exercise.isSecondWordAskedAsAnswer) || (exercise.readOut[1] && !exercise.isSecondWordAskedAsAnswer))
            tts?.speak(text, lang)
    }

    private fun startCorrection(){
        isCorrect = isInputCorrect()

        val correctionBottomSheet = CorrectionBottomSheet()
        val alternativeText: String

        if(isCorrect){
            val solution: String = when(binding.chipGroupPracticeOptions.checkedChipId){
                binding.chipPracticeOption1.id -> binding.chipPracticeOption1.text.toString()
                binding.chipPracticeOption2.id -> binding.chipPracticeOption2.text.toString()
                binding.chipPracticeOption3.id -> binding.chipPracticeOption3.text.toString()
                else -> ""
            }

            val otherAlternatives = if(exercise.isSecondWordAskedAsAnswer) word.getSecondWordList().toMutableList() else word.getFirstWordList().toMutableList()

            otherAlternatives.replaceAll { it.trim() }

            val inputStrings = solution.trim().split(";").toMutableList()
            inputStrings.replaceAll { if(exercise.words[0].isIgnoreCase) it.lowercase().trim() else it.trim() }

            otherAlternatives.removeAll{ inputStrings.contains(it.lowercase()) }

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
            if(exercise.isSecondWordAskedAsAnswer)
                correctionBottomSheet.arguments = bundleOf("alternativesText" to word.secondWord, "isCorrect" to false)
            else
                correctionBottomSheet.arguments = bundleOf("alternativesText" to word.firstWord, "isCorrect" to false)
        }

        correctionBottomSheet.show(childFragmentManager, CorrectionBottomSheet.TAG)
    }

    private fun isInputCorrect(): Boolean{

        val solution: String = when(binding.chipGroupPracticeOptions.checkedChipId){
            binding.chipPracticeOption1.id -> binding.chipPracticeOption1.text.toString()
            binding.chipPracticeOption2.id -> binding.chipPracticeOption2.text.toString()
            binding.chipPracticeOption3.id -> binding.chipPracticeOption3.text.toString()
            else -> ""
        }

        return if(exercise.isSecondWordAskedAsAnswer){
            word.secondWord == solution
        }else{
            solution.trim().equals(word.firstWord, word.isIgnoreCase)
        }
    }
}