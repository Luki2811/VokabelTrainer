package de.luki2811.dev.vokabeltrainer.ui.practice

import android.graphics.drawable.Icon
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
    private var words: ArrayList<VocabularyWord> = arrayListOf()
    private var isCorrect = false
    private var mistake: Mistake? = null
    private lateinit var wordOption1: VocabularyWord
    private lateinit var wordOption2: VocabularyWord
    private lateinit var wordOption3: VocabularyWord
    private var wordSelected: String = ""



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPracticeOutOfThreeBinding.inflate(inflater, container, false)

        val calback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            quitPractice(requireActivity(),requireContext())
        }

        for(string in args.wordAsJson){
            words.add(VocabularyWord(JSONObject(string),requireContext()))
        }
        word = words[0]


        if(Settings(requireContext()).readOutVocabularyGeneral)
            binding.buttonSpeakChooseThree.setOnClickListener { speakWord(binding.textViewPracticeChooseThreeBottom.text.toString()) }
        else
            binding.buttonSpeakChooseThree.setImageIcon(Icon.createWithResource(requireContext(),R.drawable.ic_outline_volume_off_24))

        setWords()
        binding.chipPracticeOption1.text = if(wordOption1.isKnownWordAskedAsAnswer) wordOption1.knownWord else wordOption1.newWord
        binding.chipPracticeOption2.text = if(wordOption2.isKnownWordAskedAsAnswer) wordOption2.knownWord else wordOption2.newWord
        binding.chipPracticeOption3.text = if(wordOption3.isKnownWordAskedAsAnswer) wordOption3.knownWord else wordOption3.newWord

        if(word.isKnownWordAskedAsAnswer){
            binding.textViewPracticeChooseThreeBottom.text = word.newWord
            binding.textViewPracticeChooseThreeTop.text = getString(R.string.translate_in_lang, word.languageKnown.name)
            speakWord(binding.textViewPracticeChooseThreeBottom.text.toString())
        }
        else {
            binding.textViewPracticeChooseThreeBottom.text = word.knownWord
            binding.textViewPracticeChooseThreeTop.text = getString(R.string.translate_in_lang, word.languageNew.name)
            if(args.settingsReadBoth){
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

        binding.chipGroupPracticeOptions.setOnCheckedChangeListener{ _,_ ->
            binding.buttonCheckPractice2.isEnabled = binding.chipGroupPracticeOptions.checkedChipIds.isNotEmpty()

            when(binding.chipGroupPracticeOptions.checkedChipId){
                binding.chipPracticeOption1.id -> wordSelected = binding.chipPracticeOption1.text.toString()
                binding.chipPracticeOption2.id -> wordSelected = binding.chipPracticeOption2.text.toString()
                binding.chipPracticeOption3.id -> wordSelected = binding.chipPracticeOption3.text.toString()
            }

            if(args.settingsReadBoth){
                when(binding.chipGroupPracticeOptions.checkedChipId){
                    binding.chipPracticeOption1.id -> AppTextToSpeak(binding.chipPracticeOption1.text.toString(), if (wordOption1.isKnownWordAskedAsAnswer) wordOption1.languageKnown else wordOption1.languageNew, requireContext() )
                    binding.chipPracticeOption2.id -> AppTextToSpeak(binding.chipPracticeOption2.text.toString(), if (wordOption2.isKnownWordAskedAsAnswer) wordOption2.languageKnown else wordOption2.languageNew, requireContext() )
                    binding.chipPracticeOption3.id -> AppTextToSpeak(binding.chipPracticeOption3.text.toString(), if (wordOption3.isKnownWordAskedAsAnswer) wordOption3.languageKnown else wordOption3.languageNew, requireContext() )
                }
            }else if(!word.isKnownWordAskedAsAnswer){
                when(binding.chipGroupPracticeOptions.checkedChipId){
                    binding.chipPracticeOption1.id -> AppTextToSpeak(binding.chipPracticeOption1.text.toString(), wordOption1.languageNew, requireContext() )
                    binding.chipPracticeOption2.id -> AppTextToSpeak(binding.chipPracticeOption2.text.toString(), wordOption1.languageNew, requireContext() )
                    binding.chipPracticeOption3.id -> AppTextToSpeak(binding.chipPracticeOption3.text.toString(), wordOption1.languageNew, requireContext() )
                }
            }


        }

        binding.buttonCheckPractice2.isEnabled = false

        binding.buttonCheckPractice2.setOnClickListener {
            startCorrection()
        }

        childFragmentManager.setFragmentResultListener("finishFragment", this){ _, _ ->
            findNavController().navigate(PracticeOutOfThreeFragmentDirections.actionPracticeOutOfThreeFragmentToPracticeStartFragment())
            if(isCorrect)
                requireActivity().supportFragmentManager.setFragmentResult("finished", bundleOf("wordResult" to word.getJson().toString()))
            else{
                mistake = Mistake(word, wordSelected, Exercise.TYPE_CHOOSE_OF_THREE_WORDS, LocalDate.now())
                requireActivity().supportFragmentManager.setFragmentResult("finished", bundleOf("wordResult" to word.getJson().toString(), "wordMistake" to mistake!!.getAsJson().toString()))
            }

        }


        return binding.root
    }

    private fun setWords() {
        val tempWords = words

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
        val lang = if (word.isKnownWordAskedAsAnswer) word.languageNew else word.languageKnown

        val tts = AppTextToSpeak(text,lang,requireContext())
    }

    private fun startCorrection(){

        isCorrect = isInputCorrect()

        if(!isCorrect) {
            word.typeWrong = Exercise.TYPE_CHOOSE_OF_THREE_WORDS
            word.isWrong = true
        }

        val correctionBottomSheet = CorrectionBottomSheet()


        if(word.isKnownWordAskedAsAnswer)
            correctionBottomSheet.arguments = bundleOf("correctWord" to word.knownWord, "isCorrect" to isCorrect)
        else
            correctionBottomSheet.arguments = bundleOf("correctWord" to word.newWord, "isCorrect" to isCorrect)

        correctionBottomSheet.show(childFragmentManager, CorrectionBottomSheet.TAG)
    }

    private fun isInputCorrect(): Boolean{

        val solution: String = when(binding.chipGroupPracticeOptions.checkedChipId){
            binding.chipPracticeOption1.id -> binding.chipPracticeOption1.text.toString()
            binding.chipPracticeOption2.id -> binding.chipPracticeOption2.text.toString()
            binding.chipPracticeOption3.id -> binding.chipPracticeOption3.text.toString()
            else -> ""
        }

        return if(word.isKnownWordAskedAsAnswer){
            word.knownWord == solution
        }else{
            solution.trim().equals(word.newWord, word.isIgnoreCase)
        }
    }
}