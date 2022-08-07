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
import de.luki2811.dev.vokabeltrainer.AppTextToSpeak
import de.luki2811.dev.vokabeltrainer.VocabularyWord
import de.luki2811.dev.vokabeltrainer.databinding.FragmentPracticeMatchFiveWordsBinding
import org.json.JSONObject

class PracticeMatchFiveWordsFragment : Fragment() {

    private var _binding: FragmentPracticeMatchFiveWordsBinding? = null
    private val binding get() = _binding!!
    private val args: PracticeMatchFiveWordsFragmentArgs by navArgs()
    private var words: ArrayList<VocabularyWord> = arrayListOf()

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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            PracticeActivity.quitPractice(requireActivity(), requireContext())
        }
        for(string in args.wordAsJson){
            words.add(VocabularyWord(JSONObject(string)))
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
                requireActivity().supportFragmentManager.setFragmentResult("finished", bundleOf("wordResult" to ""))

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
                if(word.knownWord == chipKnownWordChecked.text && word.newWord == chipNewWordChecked.text){
                    chipKnownWordChecked.isEnabled = false
                    chipNewWordChecked.isEnabled = false
                    resetSelection()
                    words.remove(word)
                    if(words.isEmpty()){
                        correctionBottomSheet.arguments = bundleOf("correctWord" to "", "isCorrect" to true)
                        correctionBottomSheet.show(childFragmentManager, CorrectionBottomSheet.TAG)
                    }
                    return
                }
            }

            // If wrong ->
            // TODO: No mistakes noted down right know
            // Toast.makeText(requireContext(), getString(R.string.wrong), Toast.LENGTH_SHORT).show()
            resetSelection()
            correctionBottomSheet.arguments = bundleOf("correctWord" to "", "isCorrect" to false)
            correctionBottomSheet.show(childFragmentManager, CorrectionBottomSheet.TAG)
        }
    }

    private fun speakOutWord(chipId: Int){
        Thread{
            when(chipId) {
                binding.chip0LearnNativeLan.id -> AppTextToSpeak(binding.chip0LearnNativeLan.text.toString(), vocKnown0.languageKnown, requireContext())
                binding.chip0LearnNewLan.id -> AppTextToSpeak(binding.chip0LearnNewLan.text.toString(), vocKnown0.languageNew, requireContext())

                binding.chip1LearnNativeLan.id -> AppTextToSpeak(binding.chip1LearnNativeLan.text.toString(), vocKnown1.languageKnown, requireContext())
                binding.chip1LearnNewLan.id -> AppTextToSpeak(binding.chip1LearnNewLan.text.toString(), vocKnown1.languageNew, requireContext())

                binding.chip2LearnNativeLan.id -> AppTextToSpeak(binding.chip2LearnNativeLan.text.toString(), vocKnown2.languageKnown, requireContext())
                binding.chip2LearnNewLan.id -> AppTextToSpeak(binding.chip2LearnNewLan.text.toString(), vocKnown2.languageNew, requireContext())

                binding.chip3LearnNativeLan.id -> AppTextToSpeak(binding.chip3LearnNativeLan.text.toString(), vocKnown3.languageKnown, requireContext())
                binding.chip3LearnNewLan.id -> AppTextToSpeak(binding.chip3LearnNewLan.text.toString(), vocKnown3.languageNew, requireContext())

                binding.chip4LearnNativeLan.id -> AppTextToSpeak(binding.chip4LearnNativeLan.text.toString(), vocKnown4.languageKnown, requireContext())
                binding.chip4LearnNewLan.id -> AppTextToSpeak(binding.chip4LearnNewLan.text.toString(), vocKnown4.languageNew, requireContext())
            }
        }.start()

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
        val tempWords: ArrayList<VocabularyWord> = arrayListOf()

        for(string in args.wordAsJson){
            tempWords.add(VocabularyWord(JSONObject(string)))
        }

        vocKnown0 = tempWords.random()
        tempWords.remove(vocKnown0)
        binding.chip0LearnNativeLan.text = vocKnown0.knownWord

        vocKnown1 = tempWords.random()
        tempWords.remove(vocKnown1)
        binding.chip1LearnNativeLan.text = vocKnown1.knownWord

        vocKnown2 = tempWords.random()
        tempWords.remove(vocKnown2)
        binding.chip2LearnNativeLan.text = vocKnown2.knownWord

        vocKnown3 = tempWords.random()
        tempWords.remove(vocKnown3)
        binding.chip3LearnNativeLan.text = vocKnown3.knownWord

        vocKnown4 = tempWords.random()
        tempWords.remove(vocKnown4)
        binding.chip4LearnNativeLan.text = vocKnown4.knownWord

        // New
        val tempWords2: ArrayList<VocabularyWord> = arrayListOf()

        for(string in args.wordAsJson){
            tempWords2.add(VocabularyWord(JSONObject(string)))
        }

        vocNew0 = tempWords2.random()
        tempWords2.remove(vocNew0)
        binding.chip0LearnNewLan.text = vocNew0.newWord

        vocNew1 = tempWords2.random()
        tempWords2.remove(vocNew1)
        binding.chip1LearnNewLan.text = vocNew1.newWord

        vocNew2 = tempWords2.random()
        tempWords2.remove(vocNew2)
        binding.chip2LearnNewLan.text = vocNew2.newWord

        vocNew3 = tempWords2.random()
        tempWords2.remove(vocNew3)
        binding.chip3LearnNewLan.text = vocNew3.newWord

        vocNew4 = tempWords2.random()
        tempWords2.remove(vocNew4)
        binding.chip4LearnNewLan.text = vocNew4.newWord
    }
}