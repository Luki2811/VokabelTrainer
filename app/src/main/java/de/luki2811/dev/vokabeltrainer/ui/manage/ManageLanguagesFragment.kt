package de.luki2811.dev.vokabeltrainer.ui.manage

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.luki2811.dev.vokabeltrainer.Language
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.databinding.FragmentManageLanguagesBinding
import java.util.*

class ManageLanguagesFragment : Fragment(), TextToSpeech.OnInitListener {

    var _binding: FragmentManageLanguagesBinding? = null
    val binding get() = _binding!!
    private lateinit var tts: TextToSpeech
    private var languages: ArrayList<Language> = arrayListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentManageLanguagesBinding.inflate(inflater, container, false)

        // val calback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
        //     findNavController().navigate(ManageLanguagesFragmentDirections.actionManageLanguagesFragmentToNavigationManage())
        // }

        for(i in 0..9){
            languages.add(Language(i, requireContext()))
        }

        binding.editTextLan0.setText(languages[0].name)
        binding.editTextLan1.setText(languages[1].name)
        binding.editTextLan2.setText(languages[2].name)
        binding.editTextLan3.setText(languages[3].name)
        binding.editTextLan4.setText(languages[4].name)
        binding.editTextLan5.setText(languages[5].name)
        binding.editTextLan6.setText(languages[6].name)
        binding.editTextLan7.setText(languages[7].name)
        binding.editTextLan8.setText(languages[8].name)
        binding.editTextLan9.setText(languages[9].name)

        tts = TextToSpeech(context,this)

        binding.buttonSaveLanguages.setOnClickListener {
            refreshNameList()
            if(isAllCorrect()){
                Toast.makeText(requireContext(), R.string.saved, Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_manageLanguagesFragment_pop)
                for(lang in languages){
                    lang.refreshInIndex()
                }
            }else{
                Toast.makeText(requireContext(), R.string.err_missing_input, Toast.LENGTH_SHORT).show()
            }

        }

        binding.buttonResetLanguages.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.reset)
                .setIcon(R.drawable.ic_baseline_refresh_24)
                .setMessage("Möchten sie wirklich alle Sprachen zurücksetzen ??")
                .setPositiveButton(R.string.reset){_,_ -> setDefault()}
                .setNegativeButton(R.string.cancel){_,_ -> }
                .show()
        }

        return binding.root
    }

    private fun isAllCorrect(): Boolean{
        for(lang in languages){
            if(lang.name.trim().isBlank())
                return false
        }
        return true
    }

    private fun setDefault(){
        binding.editTextLan0.setText(Language.DEFAULT_0)
        binding.editTextLan1.setText(Language.DEFAULT_1)
        binding.editTextLan2.setText(Language.DEFAULT_2)
        binding.editTextLan3.setText(Language.DEFAULT_3)
        binding.editTextLan4.setText(Language.DEFAULT_4)
        binding.editTextLan5.setText(Language.DEFAULT_5)
        binding.editTextLan6.setText(Language.DEFAULT_6)
        binding.editTextLan7.setText(Language.DEFAULT_7)
        binding.editTextLan8.setText(Language.DEFAULT_8)
        binding.editTextLan9.setText(Language.DEFAULT_9)
        refreshNameList()
    }

    private fun refreshNameList(){
        languages[0].name = binding.editTextLan0.text.toString()
        languages[1].name = binding.editTextLan1.text.toString()
        languages[2].name = binding.editTextLan2.text.toString()
        languages[3].name = binding.editTextLan3.text.toString()
        languages[4].name = binding.editTextLan4.text.toString()
        languages[5].name = binding.editTextLan5.text.toString()
        languages[6].name = binding.editTextLan6.text.toString()
        languages[7].name = binding.editTextLan7.text.toString()
        languages[8].name = binding.editTextLan8.text.toString()
        languages[9].name = binding.editTextLan9.text.toString()
        tts = TextToSpeech(context,this)
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS)
            for (i in languages) {
                if(i.getShortName() == null)
                    i.isSpeakable = false
                else{
                    val result =
                        tts.isLanguageAvailable(Locale(i.getShortName()!!))
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        i.isSpeakable = false
                        Log.w("TTS", "The Language specified is not supported!")
                    } else {
                        i.isSpeakable = true
                    }
                }
            }

        if(languages[0].isSpeakable)
            binding.editTextLan0Layout.setStartIconDrawable(R.drawable.ic_outline_volume_up_24)
        else
            binding.editTextLan0Layout.setStartIconDrawable(R.drawable.ic_outline_volume_off_24)

        if(languages[1].isSpeakable)
            binding.editTextLan1Layout.setStartIconDrawable(R.drawable.ic_outline_volume_up_24)
        else
            binding.editTextLan1Layout.setStartIconDrawable(R.drawable.ic_outline_volume_off_24)

        if(languages[2].isSpeakable)
            binding.editTextLan2Layout.setStartIconDrawable(R.drawable.ic_outline_volume_up_24)
        else
            binding.editTextLan2Layout.setStartIconDrawable(R.drawable.ic_outline_volume_off_24)

        if(languages[3].isSpeakable)
            binding.editTextLan3Layout.setStartIconDrawable(R.drawable.ic_outline_volume_up_24)
        else
            binding.editTextLan3Layout.setStartIconDrawable(R.drawable.ic_outline_volume_off_24)

        if(languages[4].isSpeakable)
            binding.editTextLan4Layout.setStartIconDrawable(R.drawable.ic_outline_volume_up_24)
        else
            binding.editTextLan4Layout.setStartIconDrawable(R.drawable.ic_outline_volume_off_24)

        if(languages[5].isSpeakable)
            binding.editTextLan5Layout.setStartIconDrawable(R.drawable.ic_outline_volume_up_24)
        else
            binding.editTextLan5Layout.setStartIconDrawable(R.drawable.ic_outline_volume_off_24)

        if(languages[6].isSpeakable)
            binding.editTextLan6Layout.setStartIconDrawable(R.drawable.ic_outline_volume_up_24)
        else
            binding.editTextLan6Layout.setStartIconDrawable(R.drawable.ic_outline_volume_off_24)

        if(languages[7].isSpeakable)
            binding.editTextLan7Layout.setStartIconDrawable(R.drawable.ic_outline_volume_up_24)
        else
            binding.editTextLan7Layout.setStartIconDrawable(R.drawable.ic_outline_volume_off_24)

        if(languages[8].isSpeakable)
            binding.editTextLan8Layout.setStartIconDrawable(R.drawable.ic_outline_volume_up_24)
        else
            binding.editTextLan8Layout.setStartIconDrawable(R.drawable.ic_outline_volume_off_24)

        if(languages[9].isSpeakable)
            binding.editTextLan9Layout.setStartIconDrawable(R.drawable.ic_outline_volume_up_24)
        else
            binding.editTextLan9Layout.setStartIconDrawable(R.drawable.ic_outline_volume_off_24)

    }

}