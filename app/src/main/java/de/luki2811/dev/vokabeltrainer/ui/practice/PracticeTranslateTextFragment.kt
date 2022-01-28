package de.luki2811.dev.vokabeltrainer.ui.practice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.navArgs
import de.luki2811.dev.vokabeltrainer.Language
import de.luki2811.dev.vokabeltrainer.VocabularyWord
import de.luki2811.dev.vokabeltrainer.databinding.FragmentPracticeTranslateTextBinding
import java.util.*

class PracticeTranslateTextFragment : Fragment(), TextToSpeech.OnInitListener, OnDataPass {

    private var _binding: FragmentPracticeTranslateTextBinding? = null
    val binding get() = _binding!!
    private var tts: TextToSpeech? = null
    private val args: PracticeTranslateTextFragmentArgs by navArgs()
    private lateinit var wordToAsk: String
    private var languageTypeOfWord: Int = 0
    lateinit var dataPasser: OnDataPass

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPasser = context as OnDataPass
    }

    fun passData(data: String){
        dataPasser.onDataPass(data)
    }

    override fun onDataPass(data: String) {
        TODO("Not yet implemented")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPracticeTranslateTextBinding.inflate(inflater, container, false)

        val calback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            PracticeActivity.quitPractice(requireActivity(), requireContext())
        }

        if((0..1).random() == 0){
            wordToAsk = args.wordDataNewWord
            binding.textViewPracticeTranslateTextBottom.text = wordToAsk
            languageTypeOfWord = args.languageNew
            tts = TextToSpeech(requireContext(), this)
            passData("Hellowww")
        }
        else {
            wordToAsk = args.wordDataKnownWord
            binding.textViewPracticeTranslateTextBottom.text = wordToAsk
            if(args.settingsReadBoth){
                languageTypeOfWord = args.languageKnown
                tts = TextToSpeech(requireContext(), this)
            }

        }

        passData("Hejjjjjjjj")

        val activity = requireActivity() as PracticeActivity


        return binding.root
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS && Language(languageTypeOfWord, requireContext()).getShortName() != null) {

            val result = tts!!.setLanguage(
                Locale(
                    Language(
                        languageTypeOfWord,
                        requireContext()
                    ).
                    getShortName()!!
                )
            )

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language specified is not supported!")
            }
            else {
                speakOut()
            }
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }


    private fun speakOut() {
        tts!!.speak(
            binding.textViewPracticeTranslateTextBottom.text ,
            TextToSpeech.QUEUE_FLUSH,
            null,
            ""
        )
    }

    companion object{
        fun getResult(): Boolean{
            return true
        }
    }
}