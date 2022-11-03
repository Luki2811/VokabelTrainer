package de.luki2811.dev.vokabeltrainer.ui.manage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.Settings
import de.luki2811.dev.vokabeltrainer.ShortForm
import de.luki2811.dev.vokabeltrainer.databinding.BottomSheetShortFormEditorBinding
import java.util.*

class ShortFormEditorBottomSheet(var shortFormToEdit: ShortForm): BottomSheetDialogFragment() {

    private var _binding: BottomSheetShortFormEditorBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetShortFormEditorBinding.inflate(inflater, container, false)

        binding.textEditShortFormListLong.apply {
            setText(shortFormToEdit.longForm)
            addTextChangedListener {
                shortFormToEdit.longForm = it.toString().trim()
            }
        }
        binding.textEditShortFormListShort.apply {
            setText(shortFormToEdit.shortForm)
            addTextChangedListener {
                shortFormToEdit.shortForm = it.toString().trim()
            }
        }
        binding.textEditShortFormListLanguage.apply {
            setText(shortFormToEdit.language.getDisplayLanguage(Settings(context).appLanguage))

            // Setup DropDownList
            val listLocales = arrayListOf<Locale>()
            Locale.getISOLanguages().filter { it != "und" && it != "mdh" && it != "mis" }.forEach { listLocales.add(
                Locale(it)
            ) }
            val listNames = arrayListOf<String>()
            listLocales.forEach { listNames.add(it.getDisplayLanguage(Settings(context).appLanguage)) }
            listNames.sortBy { it }
            setAdapter(ArrayAdapter(context, R.layout.default_list_item, listNames.toTypedArray()))

            setOnFocusChangeListener { v, hasFocus ->
                if(hasFocus){
                    binding.textEditShortFormListLanguageLayout.error = null
                }else{
                    if(listLocales.find { it.getDisplayLanguage(Settings(context).appLanguage) == binding.textEditShortFormListLanguage.text.toString().trim()} == null)
                        binding.textEditShortFormListLanguageLayout.error = context.getString(R.string.err_lang_not_available)
                    else
                        binding.textEditShortFormListLanguageLayout.error = null
                }
                shortFormToEdit.language = listLocales.find { it.getDisplayLanguage(
                    Settings(context).appLanguage) ==  binding.textEditShortFormListLanguage.text.toString().trim()}?: Locale.ROOT
            }
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        setFragmentResult("finishEditShortForm", bundleOf("resultAsJson" to shortFormToEdit.getAsJson().toString()))
    }

    companion object{
        const val TAG = "ShortFormEditorBottomSheet"
    }
}