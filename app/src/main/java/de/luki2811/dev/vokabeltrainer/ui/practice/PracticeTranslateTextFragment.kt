package de.luki2811.dev.vokabeltrainer.ui.practice

import android.graphics.drawable.Icon
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
    private var textToSpeak: AppTextToSpeak? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPracticeTranslateTextBinding.inflate(inflater, container, false)

        word = VocabularyWord(JSONObject(args.wordAsJson))

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            PracticeActivity.quitPractice(requireActivity(), requireContext())
        }

        if(word.isKnownWordAskedAsAnswer){
            binding.textViewPracticeTranslateTextBottom.text = word.newWord
            binding.textViewPracticeTranslateTextTop.text = getString(R.string.translate_in_lang, word.languageKnown.getDisplayLanguage(Settings(requireContext()).appLanguage))
            speakWord()
        }
        else {
            binding.textViewPracticeTranslateTextBottom.text = word.knownWord
            binding.textViewPracticeTranslateTextTop.text = getString(R.string.translate_in_lang, word.languageNew.getDisplayLanguage(Settings(requireContext()).appLanguage))
            if(args.settingsReadBoth){
                speakWord()
            }
        }

        if(Settings(requireContext()).readOutVocabularyGeneral)
            binding.buttonSpeakTranslateText.setOnClickListener { speakWord() }
        else
            binding.buttonSpeakTranslateText.setImageIcon(Icon.createWithResource(requireContext(),R.drawable.ic_outline_volume_off_24))

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
                requireActivity().supportFragmentManager.setFragmentResult("finished", bundleOf("wordMistake" to null))
            else{
                mistake = Mistake(word, binding.practiceTextInput.text.toString(), Exercise.TYPE_TRANSLATE_TEXT, LocalDate.now())
                requireActivity().supportFragmentManager.setFragmentResult("finished", bundleOf("wordMistake" to mistake!!.getAsJson().toString()))
            }
        }

        return binding.root
    }

    override fun onDestroy() {
        textToSpeak?.shutdown()
        super.onDestroy()
    }

    private fun speakWord(){
        Thread {
            Thread.sleep(200L)
            val lang = if (word.isKnownWordAskedAsAnswer) word.languageNew else word.languageKnown
            textToSpeak = AppTextToSpeak(binding.textViewPracticeTranslateTextBottom.text.toString(),lang,requireContext())
        }.start()
    }

    private fun startCorrection(){
        isCorrect = isInputCorrect()
        val correctionBottomSheet = CorrectionBottomSheet()

        if(word.isKnownWordAskedAsAnswer)
            correctionBottomSheet.arguments = bundleOf("correctWord" to word.knownWord, "isCorrect" to isCorrect)
        else
            correctionBottomSheet.arguments = bundleOf("correctWord" to word.newWord, "isCorrect" to isCorrect)

        correctionBottomSheet.show(childFragmentManager, CorrectionBottomSheet.TAG)
    }

    private fun isInputCorrect(): Boolean{
        val solution = binding.practiceTextInput.text.toString()

        return if(word.isKnownWordAskedAsAnswer){
            val knownWords = word.getKnownWordList()
            for(word in knownWords) {
                if(word.trim().equals(solution.trim(), this.word.isIgnoreCase)) {
                     return true
                }
            }
            return false
        }else{
            solution.trim().equals(word.newWord, word.isIgnoreCase)
        }
    }
}