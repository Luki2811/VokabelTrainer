package de.luki2811.dev.vokabeltrainer.ui.practice

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.databinding.FragmentPracticeTranslateTextBinding
import java.util.*


class PracticeTranslateTextFragment : Fragment(){

    private var _binding: FragmentPracticeTranslateTextBinding? = null
    private val binding get() = _binding!!
    private val args: PracticeTranslateTextFragmentArgs by navArgs()
    private var exercise = args.exercise
    private var isCorrect = false
    private var tts: TextToSpeechUtil? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPracticeTranslateTextBinding.inflate(inflater, container, false)

        tts = TextToSpeechUtil(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            PracticeActivity.quitPractice(requireActivity(), requireContext())
        }

        // Set Text
        when(exercise.typeOfWord){
            VocabularyWord.TYPE_TRANSLATION -> {
                if(exercise.isOtherWordAskedAsAnswer){
                    binding.textViewPracticeTranslateTextBottom.text = exercise.words[0].mainWord
                    binding.textViewPracticeTranslateTextTop.text = getString(R.string.translate_in_lang, (exercise.words[0] as WordTranslation).otherLanguage.getDisplayLanguage(Settings(requireContext()).appLanguage))
                    if(exercise.readOut[0]){
                        speakWord()
                    }
                }
                else {
                    binding.textViewPracticeTranslateTextBottom.text = exercise.words[0].getSecondWordsAsString()
                    binding.textViewPracticeTranslateTextTop.text = getString(R.string.translate_in_lang, (exercise.words[0] as WordTranslation).mainLanguage.getDisplayLanguage(Settings(requireContext()).appLanguage))
                    if(exercise.readOut[1]){
                        speakWord()
                    }
                }
            }
            VocabularyWord.TYPE_SYNONYM -> {
                binding.textViewPracticeTranslateTextTop.text = getString(R.string.action_write_synonyms)
                binding.textViewPracticeTranslateTextBottom.text = exercise.words[0].mainWord
                if(exercise.readOut[1]){
                    speakWord()
                }
            }
            VocabularyWord.TYPE_ANTONYM -> {
                binding.textViewPracticeTranslateTextTop.text = getString(R.string.action_write_antonyms)
                binding.textViewPracticeTranslateTextBottom.text = exercise.words[0].mainWord
                if(exercise.readOut[1]){
                    speakWord()
                }
            }
            VocabularyWord.TYPE_WORD_FAMILY -> {
                TODO()
            }
        }

        if(Settings(requireContext()).readOutVocabularyGeneral)
            binding.buttonSpeakTranslateText.setOnClickListener { speakWord() }
        else
            binding.buttonSpeakTranslateText.setIconResource(R.drawable.ic_outline_volume_off_24)

        binding.buttonSpeakTranslateText.setOnLongClickListener {
            val settings = Settings(requireContext())

            settings.readOutVocabularyGeneral = !settings.readOutVocabularyGeneral

            if(settings.readOutVocabularyGeneral) {
                binding.buttonSpeakTranslateText.setIconResource(R.drawable.ic_outline_volume_up_24)
                binding.buttonSpeakTranslateText.setOnClickListener { speakWord() }
            }
            else {
                binding.buttonSpeakTranslateText.setIconResource(R.drawable.ic_outline_volume_off_24)
                binding.buttonSpeakTranslateText.setOnClickListener {  }
            }

            settings.saveSettingsInFile()

            true
        }

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
            requireActivity().supportFragmentManager.setFragmentResult("finished", bundleOf("result" to ExerciseResult(isCorrect, binding.practiceTextInput.text.toString())))
        }

        return binding.root
    }

    private fun speakWord(){
        val lang = when(exercise.words[0]){
            is WordTranslation -> { if (exercise.isOtherWordAskedAsAnswer) (exercise.words[0] as WordTranslation).otherLanguage else (exercise.words[0] as WordTranslation).mainLanguage }
            is Synonym -> { (exercise.words[0] as Synonym).language }
            is WordFamily -> { (exercise.words[0] as WordFamily).language }
            else -> { Locale.ENGLISH }
        }
        when(tts?.speak(binding.textViewPracticeTranslateTextBottom.text.toString(), lang)){
            TextToSpeechUtil.ERROR_MISSING_LANG_DATA -> Snackbar.make(
                binding.root,
                getString(R.string.err_missing_language_data_tts),
                Snackbar.LENGTH_LONG
            ).apply {
                setAction(getString(R.string.action_install)){
                    requireActivity().startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
                }
            }.show()
        }
    }

    override fun onDestroy() {
        tts?.finish()
        super.onDestroy()
    }

    private fun startCorrection(){
        val correctionBottomSheet = CorrectionBottomSheet()

        val otherWords: ArrayList<String> = when(exercise.words[0]){
            is WordTranslation -> (exercise.words[0] as WordTranslation).otherWords
            is Synonym -> (exercise.words[0] as Synonym).otherWords
            is WordFamily -> {
                val other = ArrayList<String>()
                (exercise.words[0] as WordFamily).otherWords.forEach { if(exercise.typeOfWordInFamily == it.second) other.add(it.first) }
                other
            }
            else -> { ArrayList() }
        }

        val proofreader = if(exercise.isOtherWordAskedAsAnswer){
            Proofreader(otherWords, binding.practiceTextInput.text.toString(), exercise.askAllWords)
        }else{
            Proofreader(ArrayList<String>().apply { add(exercise.words[0].mainWord) }, binding.practiceTextInput.text.toString(), exercise.askAllWords)
        }

        if(Settings(requireContext()).allowShortFormInAnswer) {
            val lang = when(exercise.words[0]){
                is WordTranslation -> { if (exercise.isOtherWordAskedAsAnswer) (exercise.words[0] as WordTranslation).otherLanguage else (exercise.words[0] as WordTranslation).mainLanguage }
                is Synonym -> { (exercise.words[0] as Synonym).language }
                is WordFamily -> { (exercise.words[0] as WordFamily).language }
                else -> { Locale.ENGLISH }
            }

            proofreader.replaceShortForms(ShortForm.loadAllShortForms(requireContext()).filter { it.language == lang }.toMutableList() as ArrayList<ShortForm>)
        }

        isCorrect = proofreader.correct(exercise.words[0].isIgnoreCase)

        val alternativeText: String
        var wrongIndex = arrayListOf<Int>()

        if(isCorrect){
            val otherAlternatives = if(exercise.isOtherWordAskedAsAnswer) otherWords else ArrayList<String>().apply { add(exercise.words[0].mainWord) }
            otherAlternatives.replaceAll { it.trim() }

            val inputStrings = binding.practiceTextInput.text.toString().trim().split(";").toMutableList()
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
        }else{
            alternativeText = if(exercise.isOtherWordAskedAsAnswer) exercise.words[0].getSecondWordsAsString() else exercise.words[0].mainWord
            wrongIndex = proofreader.getWrongCharIndices(exercise.words[0].isIgnoreCase)
        }

        Log.w("Test", alternativeText)
        Log.w("TestWrongIndex", wrongIndex.toString())

        correctionBottomSheet.arguments = bundleOf("wrongIndex" to wrongIndex, "alternativesText" to alternativeText, "isCorrect" to isCorrect)

        correctionBottomSheet.show(childFragmentManager, CorrectionBottomSheet.TAG)
    }


}