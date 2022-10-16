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
import org.json.JSONObject
import java.time.LocalDate

class PracticeOutOfThreeFragment: Fragment() {

    private var _binding: FragmentPracticeOutOfThreeBinding? = null
    private val binding get() = _binding!!
    private val args: PracticeOutOfThreeFragmentArgs by navArgs()
    private lateinit var word: VocabularyWord
    private lateinit var exercise: Exercise
    private var isCorrect = false
    private lateinit var wordOption1: VocabularyWord
    private lateinit var wordOption2: VocabularyWord
    private lateinit var wordOption3: VocabularyWord
    private var wordSelected: String = ""



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPracticeOutOfThreeBinding.inflate(inflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            quitPractice(requireActivity(),requireContext())
        }

        exercise = Exercise(JSONObject(args.exercise))
        word = exercise.words[0]


        if(Settings(requireContext()).readOutVocabularyGeneral)
            binding.buttonSpeakChooseThree.setOnClickListener { speakWord(binding.textViewPracticeChooseThreeBottom.text.toString()) }
        else
            binding.buttonSpeakChooseThree.setIconResource(R.drawable.ic_outline_volume_off_24)

        setWords()
        binding.chipPracticeOption1.text = if(!exercise.isSecondWordAskedAsAnswer) wordOption1.firstWord else wordOption1.secondWord
        binding.chipPracticeOption2.text = if(!exercise.isSecondWordAskedAsAnswer) wordOption2.firstWord else wordOption2.secondWord
        binding.chipPracticeOption3.text = if(!exercise.isSecondWordAskedAsAnswer) wordOption3.firstWord else wordOption3.secondWord

        if(exercise.isSecondWordAskedAsAnswer){
            binding.textViewPracticeChooseThreeBottom.text = word.firstWord
            binding.textViewPracticeChooseThreeTop.text = getString(R.string.translate_in_lang, word.secondLanguage.getDisplayLanguage(Settings(requireContext()).appLanguage))
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

            if(exercise.readOut[1]){
                when(binding.chipGroupPracticeOptions.checkedChipId){
                    binding.chipPracticeOption1.id -> AppTextToSpeak(binding.chipPracticeOption1.text.toString(), if (exercise.isSecondWordAskedAsAnswer) wordOption1.secondLanguage else wordOption1.firstLanguage, requireContext() )
                    binding.chipPracticeOption2.id -> AppTextToSpeak(binding.chipPracticeOption2.text.toString(), if (exercise.isSecondWordAskedAsAnswer) wordOption2.secondLanguage else wordOption2.firstLanguage, requireContext() )
                    binding.chipPracticeOption3.id -> AppTextToSpeak(binding.chipPracticeOption3.text.toString(), if (exercise.isSecondWordAskedAsAnswer) wordOption3.secondLanguage else wordOption3.firstLanguage, requireContext() )
                }
            }else if(exercise.isSecondWordAskedAsAnswer){
                when(binding.chipGroupPracticeOptions.checkedChipId){
                    binding.chipPracticeOption1.id -> AppTextToSpeak(binding.chipPracticeOption1.text.toString(), wordOption1.secondLanguage, requireContext() )
                    binding.chipPracticeOption2.id -> AppTextToSpeak(binding.chipPracticeOption2.text.toString(), wordOption1.secondLanguage, requireContext() )
                    binding.chipPracticeOption3.id -> AppTextToSpeak(binding.chipPracticeOption3.text.toString(), wordOption1.secondLanguage, requireContext() )
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

    private fun setWords() {
        val tempWords = exercise.words

        //1
        var tempWord = tempWords.random()
        tempWords.remove(tempWord)
        wordOption1 = tempWord

        //2
        tempWord = tempWords.random()
        tempWords.remove(tempWord)
        wordOption2 = tempWord

        //3
        tempWord = tempWords.random()
        tempWords.remove(tempWord)
        wordOption3 = tempWord

    }

    private fun speakWord(text: String){
        Thread {
            Thread.sleep(200L)
            val lang = if (exercise.isSecondWordAskedAsAnswer) word.firstLanguage else word.secondLanguage
            AppTextToSpeak(text, lang, requireContext())
        }.start()
    }

    private fun startCorrection(){
        isCorrect = isInputCorrect()

        val correctionBottomSheet = CorrectionBottomSheet()

        if(exercise.isSecondWordAskedAsAnswer)
            correctionBottomSheet.arguments = bundleOf("correctWord" to word.secondWord, "isCorrect" to isCorrect, "givenAnswer" to wordSelected)
        else
            correctionBottomSheet.arguments = bundleOf("correctWord" to word.firstWord, "isCorrect" to isCorrect, "givenAnswer" to wordSelected)

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