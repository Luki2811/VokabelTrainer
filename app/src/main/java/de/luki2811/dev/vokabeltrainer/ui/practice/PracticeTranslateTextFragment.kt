package de.luki2811.dev.vokabeltrainer.ui.practice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.databinding.FragmentPracticeTranslateTextBinding
import org.json.JSONObject
import java.time.LocalDate

class PracticeTranslateTextFragment : Fragment(){

    private var _binding: FragmentPracticeTranslateTextBinding? = null
    private val binding get() = _binding!!
    private val args: PracticeTranslateTextFragmentArgs by navArgs()
    private lateinit var word: VocabularyWord
    private var isCorrect = false
    private var mistake: Mistake? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPracticeTranslateTextBinding.inflate(inflater, container, false)

        word = VocabularyWord(JSONObject(args.wordAsJson), requireContext())

        val calback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            PracticeActivity.quitPractice(requireActivity(), requireContext())
        }

        binding.buttonSpeakTranslateText.setOnClickListener { speakWord() }

        if(word.isKnownWordAskedAsAnswer){
            binding.textViewPracticeTranslateTextBottom.text = word.newWord
            binding.textViewPracticeTranslateTextTop.text = getString(R.string.translate_in_lang, word.languageKnown.name)
            speakWord()
        }
        else {
            binding.textViewPracticeTranslateTextBottom.text = word.knownWord
            binding.textViewPracticeTranslateTextTop.text = getString(R.string.translate_in_lang, word.languageNew.name)
            if(args.settingsReadBoth){
                speakWord()
            }
        }

        if(word.isIgnoreCase) {
            binding.textViewPracticeInfo.text = ""
            binding.textViewPracticeInfo.visibility = View.GONE
        }
        else {
            binding.textViewPracticeInfo.setText(R.string.look_for_case)
            binding.textViewPracticeInfo.visibility = View.VISIBLE
        }

        binding.practiceTextInput.addTextChangedListener {
            binding.buttonCheckPractice.isEnabled = binding.practiceTextInput.text.toString().isNotEmpty()
        }

        binding.buttonCheckPractice.isEnabled = false
        binding.buttonCheckPractice.setOnClickListener {
            startCorrection()
        }

        childFragmentManager.setFragmentResultListener("finishFragment", this){ _, _ ->
            findNavController().navigate(PracticeTranslateTextFragmentDirections.actionPracticeTranslateTextFragmentToPracticeStartFragment())
            if(isCorrect)
                requireActivity().supportFragmentManager.setFragmentResult("finished", bundleOf("wordResult" to word.getJson().toString()))
            else{
                mistake = Mistake(word, binding.practiceTextInput.text.toString(), Exercise.TYPE_TRANSLATE_TEXT, LocalDate.now())
                requireActivity().supportFragmentManager.setFragmentResult("finished", bundleOf("wordResult" to word.getJson().toString(), "wordMistake" to mistake!!.getAsJson().toString()))
            }
        }

        return binding.root
    }

    private fun speakWord(){
        val lang = if (word.isKnownWordAskedAsAnswer) word.languageNew else word.languageKnown

        val tts = AppTextToSpeak(binding.textViewPracticeTranslateTextBottom.text.toString(),lang,requireContext())
    }

    private fun startCorrection(){

        isCorrect = isInputCorrect()

        if(!isCorrect) {
            word.typeWrong = Exercise.TYPE_TRANSLATE_TEXT
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

        val solution = binding.practiceTextInput.text.toString()

        // TODO: Change, that it shows the mistakes

        return if(word.isKnownWordAskedAsAnswer){
            val knownWords = word.getKnownWordList()
            for(i in knownWords) {
                if(i.trim().equals(solution.trim(), word.isIgnoreCase)) {
                     return true
                }
            }
            return false
        }else{
            solution.trim().equals(word.newWord, word.isIgnoreCase)
        }


    }
}