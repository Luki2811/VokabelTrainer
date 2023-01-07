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
import com.google.android.material.chip.Chip
import de.luki2811.dev.vokabeltrainer.Exercise
import de.luki2811.dev.vokabeltrainer.ExerciseResult
import de.luki2811.dev.vokabeltrainer.TextToSpeechUtil
import de.luki2811.dev.vokabeltrainer.WordTranslation
import de.luki2811.dev.vokabeltrainer.databinding.FragmentPracticeMatchFiveWordsBinding

class PracticeMatchFiveWordsFragment : Fragment() {

    private var _binding: FragmentPracticeMatchFiveWordsBinding? = null
    private val binding get() = _binding!!
    private val args: PracticeMatchFiveWordsFragmentArgs by navArgs()
    private var exercise: Exercise = args.exercise
    private var words: ArrayList<WordTranslation> = arrayListOf()
    private var tts: TextToSpeechUtil? = null

    private lateinit var vocKnown0: WordTranslation
    private lateinit var vocKnown1: WordTranslation
    private lateinit var vocKnown2: WordTranslation
    private lateinit var vocKnown3: WordTranslation
    private lateinit var vocKnown4: WordTranslation

    private lateinit var vocNew0: WordTranslation
    private lateinit var vocNew1: WordTranslation
    private lateinit var vocNew2: WordTranslation
    private lateinit var vocNew3: WordTranslation
    private lateinit var vocNew4: WordTranslation


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPracticeMatchFiveWordsBinding.inflate(layoutInflater, container, false)

        tts = TextToSpeechUtil(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            PracticeActivity.quitPractice(requireActivity(), requireContext())
        }

        exercise.words.forEach {
            if(it is WordTranslation)
                words.add(it)
        }

        setWords()

        binding.chipGroupLearnNativeLan.setOnCheckedStateChangeListener{ chipGroup,_ ->
            speakOutWord(chipGroup.checkedChipId)
            correction()

        }
        binding.chipGroupLearnNewLan.setOnCheckedStateChangeListener{ chipGroup,_ ->
            speakOutWord(chipGroup.checkedChipId)
            correction()

        }

        childFragmentManager.setFragmentResultListener("finishFragment", this){ _, _ ->
            if(words.isEmpty()){
                findNavController().navigate(PracticeMatchFiveWordsFragmentDirections.actionPracticeMatchFiveWordsFragmentToPracticeStartFragment())
                requireActivity().supportFragmentManager.setFragmentResult("finished", bundleOf("result" to ExerciseResult(true, "")))

            }
        }
        return binding.root
    }

    private fun correction(){
        if(binding.chipGroupLearnNativeLan.checkedChipIds.isNotEmpty() && binding.chipGroupLearnNewLan.checkedChipIds.isNotEmpty()){
            val chipKnownWordChecked = getChip(binding.chipGroupLearnNativeLan.checkedChipId)
            val chipNewWordChecked = getChip(binding.chipGroupLearnNewLan.checkedChipId)
            val correctionBottomSheet = CorrectionBottomSheet()

            for(word in words){
                if(word.mainWord == chipKnownWordChecked.text && word.otherWords[0] == chipNewWordChecked.text){
                    chipKnownWordChecked.isEnabled = false
                    chipNewWordChecked.isEnabled = false
                    resetSelection()
                    words.remove(word)
                    if(words.isEmpty()){
                        correctionBottomSheet.arguments = bundleOf("alternativesText" to "", "isCorrect" to true)
                        correctionBottomSheet.show(childFragmentManager, CorrectionBottomSheet.TAG)
                    }
                    return
                }
            }
            resetSelection()
            correctionBottomSheet.arguments = bundleOf("alternativesText" to "", "isCorrect" to false)
            correctionBottomSheet.show(childFragmentManager, CorrectionBottomSheet.TAG)
        }
    }

    private fun speakOutWord(chipId: Int){

        when(chipId) {
            binding.chip0LearnNativeLan.id -> if(exercise.readOut[0]) tts?.speak(binding.chip0LearnNativeLan.text.toString(), vocKnown0.mainLanguage)
            binding.chip0LearnNewLan.id -> if(exercise.readOut[1]) tts?.speak(binding.chip0LearnNewLan.text.toString(), vocKnown0.otherLanguage)

            binding.chip1LearnNativeLan.id -> if(exercise.readOut[0]) tts?.speak(binding.chip1LearnNativeLan.text.toString(), vocKnown1.mainLanguage)
            binding.chip1LearnNewLan.id -> if(exercise.readOut[1]) tts?.speak(binding.chip1LearnNewLan.text.toString(), vocKnown1.otherLanguage)

            binding.chip2LearnNativeLan.id -> if(exercise.readOut[0]) tts?.speak(binding.chip2LearnNativeLan.text.toString(), vocKnown2.mainLanguage)
            binding.chip2LearnNewLan.id -> if(exercise.readOut[1]) tts?.speak(binding.chip2LearnNewLan.text.toString(), vocKnown2.otherLanguage)

            binding.chip3LearnNativeLan.id -> if(exercise.readOut[0]) tts?.speak(binding.chip3LearnNativeLan.text.toString(), vocKnown3.mainLanguage)
            binding.chip3LearnNewLan.id -> if(exercise.readOut[1]) tts?.speak(binding.chip3LearnNewLan.text.toString(), vocKnown3.otherLanguage)

            binding.chip4LearnNativeLan.id -> if(exercise.readOut[0]) tts?.speak(binding.chip4LearnNativeLan.text.toString(), vocKnown4.mainLanguage)
            binding.chip4LearnNewLan.id -> if(exercise.readOut[1]) tts?.speak(binding.chip4LearnNewLan.text.toString(), vocKnown4.otherLanguage)
        }

    }

    private fun getChip(chipId: Int): Chip{
        return when(chipId){
            binding.chip0LearnNativeLan.id -> binding.chip0LearnNativeLan
            binding.chip0LearnNewLan.id -> binding.chip0LearnNewLan

            binding.chip1LearnNativeLan.id -> binding.chip1LearnNativeLan
            binding.chip1LearnNewLan.id -> binding.chip1LearnNewLan

            binding.chip2LearnNativeLan.id -> binding.chip2LearnNativeLan
            binding.chip2LearnNewLan.id -> binding.chip2LearnNewLan

            binding.chip3LearnNativeLan.id -> binding.chip3LearnNativeLan
            binding.chip3LearnNewLan.id -> binding.chip3LearnNewLan

            binding.chip4LearnNativeLan.id -> binding.chip4LearnNativeLan
            binding.chip4LearnNewLan.id -> binding.chip4LearnNewLan

            else -> {Chip(requireContext())}
        }
    }

    private fun resetSelection(){
        binding.chipGroupLearnNewLan.clearCheck()
        binding.chipGroupLearnNativeLan.clearCheck()
    }



    private fun setWords(){
        val tempWords = ArrayList<WordTranslation>()
        exercise.words.forEach {
            if(it is WordTranslation)
                tempWords.add(it)
        }

        vocKnown0 = tempWords.random()
        tempWords.remove(vocKnown0)
        binding.chip0LearnNativeLan.text = vocKnown0.mainWord

        vocKnown1 = tempWords.random()
        tempWords.remove(vocKnown1)
        binding.chip1LearnNativeLan.text = vocKnown1.mainWord

        vocKnown2 = tempWords.random()
        tempWords.remove(vocKnown2)
        binding.chip2LearnNativeLan.text = vocKnown2.mainWord

        vocKnown3 = tempWords.random()
        tempWords.remove(vocKnown3)
        binding.chip3LearnNativeLan.text = vocKnown3.mainWord

        vocKnown4 = tempWords.random()
        tempWords.remove(vocKnown4)
        binding.chip4LearnNativeLan.text = vocKnown4.mainWord

        // New
        val tempWords2 = ArrayList<WordTranslation>()
        exercise.words.forEach {
            if(it is WordTranslation)
                tempWords2.add(it)
        }

        vocNew0 = tempWords2.random()
        tempWords2.remove(vocNew0)
        binding.chip0LearnNewLan.text = vocNew0.otherWords[0]

        vocNew1 = tempWords2.random()
        tempWords2.remove(vocNew1)
        binding.chip1LearnNewLan.text = vocNew1.otherWords[0]

        vocNew2 = tempWords2.random()
        tempWords2.remove(vocNew2)
        binding.chip2LearnNewLan.text = vocNew2.otherWords[0]

        vocNew3 = tempWords2.random()
        tempWords2.remove(vocNew3)
        binding.chip3LearnNewLan.text = vocNew3.otherWords[0]

        vocNew4 = tempWords2.random()
        tempWords2.remove(vocNew4)
        binding.chip4LearnNewLan.text = vocNew4.otherWords[0]
    }
}