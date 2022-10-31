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
import de.luki2811.dev.vokabeltrainer.VocabularyWord
import de.luki2811.dev.vokabeltrainer.databinding.FragmentPracticeMatchFiveWordsBinding
import org.json.JSONObject

class PracticeMatchFiveWordsFragment : Fragment() {

    private var _binding: FragmentPracticeMatchFiveWordsBinding? = null
    private val binding get() = _binding!!
    private val args: PracticeMatchFiveWordsFragmentArgs by navArgs()
    private lateinit var exercise: Exercise
    private var words: ArrayList<VocabularyWord> = arrayListOf()
    private var tts: TextToSpeechUtil? = null

    private lateinit var vocKnown0: VocabularyWord
    private lateinit var vocKnown1: VocabularyWord
    private lateinit var vocKnown2: VocabularyWord
    private lateinit var vocKnown3: VocabularyWord
    private lateinit var vocKnown4: VocabularyWord

    private lateinit var vocNew0: VocabularyWord
    private lateinit var vocNew1: VocabularyWord
    private lateinit var vocNew2: VocabularyWord
    private lateinit var vocNew3: VocabularyWord
    private lateinit var vocNew4: VocabularyWord


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPracticeMatchFiveWordsBinding.inflate(layoutInflater, container, false)

        tts = TextToSpeechUtil(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            PracticeActivity.quitPractice(requireActivity(), requireContext())
        }

        exercise = Exercise(JSONObject(args.exercise))

        words = exercise.words

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
                if(word.getFirstWordList()[0] == chipKnownWordChecked.text && word.getSecondWordList()[0] == chipNewWordChecked.text){
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
                binding.chip0LearnNativeLan.id -> if(exercise.readOut[0]) tts?.speak(binding.chip0LearnNativeLan.text.toString(), vocKnown0.firstLanguage)
                binding.chip0LearnNewLan.id -> if(exercise.readOut[1]) tts?.speak(binding.chip0LearnNewLan.text.toString(), vocKnown0.secondLanguage)

                binding.chip1LearnNativeLan.id -> if(exercise.readOut[0]) tts?.speak(binding.chip1LearnNativeLan.text.toString(), vocKnown1.firstLanguage)
                binding.chip1LearnNewLan.id -> if(exercise.readOut[1]) tts?.speak(binding.chip1LearnNewLan.text.toString(), vocKnown1.secondLanguage)

                binding.chip2LearnNativeLan.id -> if(exercise.readOut[0]) tts?.speak(binding.chip2LearnNativeLan.text.toString(), vocKnown2.firstLanguage)
                binding.chip2LearnNewLan.id -> if(exercise.readOut[1]) tts?.speak(binding.chip2LearnNewLan.text.toString(), vocKnown2.secondLanguage)

                binding.chip3LearnNativeLan.id -> if(exercise.readOut[0]) tts?.speak(binding.chip3LearnNativeLan.text.toString(), vocKnown3.firstLanguage)
                binding.chip3LearnNewLan.id -> if(exercise.readOut[1]) tts?.speak(binding.chip3LearnNewLan.text.toString(), vocKnown3.secondLanguage)

                binding.chip4LearnNativeLan.id -> if(exercise.readOut[0]) tts?.speak(binding.chip4LearnNativeLan.text.toString(), vocKnown4.firstLanguage)
                binding.chip4LearnNewLan.id -> if(exercise.readOut[1]) tts?.speak(binding.chip4LearnNewLan.text.toString(), vocKnown4.secondLanguage)
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
        val tempWords = ArrayList<VocabularyWord>()
        tempWords.addAll(exercise.words)

        vocKnown0 = tempWords.random()
        tempWords.remove(vocKnown0)
        binding.chip0LearnNativeLan.text = vocKnown0.getFirstWordList()[0]

        vocKnown1 = tempWords.random()
        tempWords.remove(vocKnown1)
        binding.chip1LearnNativeLan.text = vocKnown1.getFirstWordList()[0]

        vocKnown2 = tempWords.random()
        tempWords.remove(vocKnown2)
        binding.chip2LearnNativeLan.text = vocKnown2.getFirstWordList()[0]

        vocKnown3 = tempWords.random()
        tempWords.remove(vocKnown3)
        binding.chip3LearnNativeLan.text = vocKnown3.getFirstWordList()[0]

        vocKnown4 = tempWords.random()
        tempWords.remove(vocKnown4)
        binding.chip4LearnNativeLan.text = vocKnown4.getFirstWordList()[0]

        // New
        val tempWords2 = ArrayList<VocabularyWord>()
        tempWords2.addAll(exercise.words)

        vocNew0 = tempWords2.random()
        tempWords2.remove(vocNew0)
        binding.chip0LearnNewLan.text = vocNew0.getSecondWordList()[0]

        vocNew1 = tempWords2.random()
        tempWords2.remove(vocNew1)
        binding.chip1LearnNewLan.text = vocNew1.getSecondWordList()[0]

        vocNew2 = tempWords2.random()
        tempWords2.remove(vocNew2)
        binding.chip2LearnNewLan.text = vocNew2.getSecondWordList()[0]

        vocNew3 = tempWords2.random()
        tempWords2.remove(vocNew3)
        binding.chip3LearnNewLan.text = vocNew3.getSecondWordList()[0]

        vocNew4 = tempWords2.random()
        tempWords2.remove(vocNew4)
        binding.chip4LearnNewLan.text = vocNew4.getSecondWordList()[0]
    }
}