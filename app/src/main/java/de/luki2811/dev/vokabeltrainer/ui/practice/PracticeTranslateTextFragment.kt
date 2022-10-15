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
    private lateinit var exercise: Exercise
    private var isCorrect = false
    private var mistake: Mistake? = null
    private var textToSpeak: AppTextToSpeak? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPracticeTranslateTextBinding.inflate(inflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            PracticeActivity.quitPractice(requireActivity(), requireContext())
        }

        exercise = Exercise(JSONObject(args.exercise))

        if(exercise.isSecondWordAskedAsAnswer){
            binding.textViewPracticeTranslateTextBottom.text = exercise.words[0].firstWord
            binding.textViewPracticeTranslateTextTop.text = getString(R.string.translate_in_lang, exercise.words[0].secondLanguage.getDisplayLanguage(Settings(requireContext()).appLanguage))
            speakWord()
        }
        else {
            binding.textViewPracticeTranslateTextBottom.text = exercise.words[0].secondWord
            binding.textViewPracticeTranslateTextTop.text = getString(R.string.translate_in_lang, exercise.words[0].firstLanguage.getDisplayLanguage(Settings(requireContext()).appLanguage))
            if(exercise.readOut[1]){
                speakWord()
            }
        }

        if(Settings(requireContext()).readOutVocabularyGeneral)
            binding.buttonSpeakTranslateText.setOnClickListener { speakWord() }
        else
            binding.buttonSpeakTranslateText.setIconResource(R.drawable.ic_outline_volume_off_24)

        if(exercise.words[0].isIgnoreCase) {
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
                mistake = Mistake(exercise.words[0], binding.practiceTextInput.text.toString(), Exercise.TYPE_TRANSLATE_TEXT, LocalDate.now(), askedForSecondWord = exercise.isSecondWordAskedAsAnswer )
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
            val lang = if (exercise.isSecondWordAskedAsAnswer) exercise.words[0].firstLanguage else exercise.words[0].secondLanguage
            textToSpeak = AppTextToSpeak(binding.textViewPracticeTranslateTextBottom.text.toString(),lang,requireContext())
        }.start()
    }

    private fun startCorrection(){
        isCorrect = isInputCorrect()
        val correctionBottomSheet = CorrectionBottomSheet()

        if(exercise.isSecondWordAskedAsAnswer)
            correctionBottomSheet.arguments = bundleOf("correctWord" to exercise.words[0].secondWord, "isCorrect" to isCorrect)
        else
            correctionBottomSheet.arguments = bundleOf("correctWord" to exercise.words[0].firstWord, "isCorrect" to isCorrect)

        correctionBottomSheet.show(childFragmentManager, CorrectionBottomSheet.TAG)
    }

    private fun isInputCorrect(): Boolean{
        val solution = binding.practiceTextInput.text.toString()

        if(!exercise.isSecondWordAskedAsAnswer){
            val firstWords = exercise.words[0].getFirstWordList()
            for(word in firstWords) {
                if(word.trim().equals(solution.trim(), this.exercise.words[0].isIgnoreCase)) {
                     return true
                }
            }
            return false
        }else{
            val secondWords = exercise.words[0].getSecondWordList()
            for(word in secondWords) {
                if(word.trim().equals(solution.trim(), this.exercise.words[0].isIgnoreCase)) {
                    return true
                }
            }
            return false
        }
    }
}