package de.luki2811.dev.vokabeltrainer.ui.manage

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.luki2811.dev.vokabeltrainer.Exportable
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.Settings
import de.luki2811.dev.vokabeltrainer.ShortForm
import de.luki2811.dev.vokabeltrainer.databinding.BottomSheetShortFormEditorBinding
import io.github.g0dkar.qrcode.QRCode
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.*

class ShortFormEditorBottomSheet(private var shortFormToEdit: ShortForm): BottomSheetDialogFragment() {

    private var _binding: BottomSheetShortFormEditorBinding? = null
    private val binding get() = _binding!!

    private var isQrCodeGenerated: Boolean = false
    private var oldScreenBrightness = 0F


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetShortFormEditorBinding.inflate(inflater, container, false)

        val modalBottomSheetBehavior = (this.dialog as BottomSheetDialog).behavior
        modalBottomSheetBehavior.peekHeight = 900
        oldScreenBrightness = requireActivity().window.attributes.screenBrightness

        generateQrCode()

        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(newState == BottomSheetBehavior.STATE_EXPANDED){
                    if(isQrCodeGenerated){
                        if (Settings(requireContext()).increaseScreenBrightness){
                            requireActivity().window.attributes = requireActivity().window.attributes.apply { screenBrightness = 1F }
                        }

                    }
                }else if(newState == BottomSheetBehavior.STATE_COLLAPSED){
                    if(isQrCodeGenerated){
                        requireActivity().window.attributes =
                            requireActivity().window.attributes.apply { screenBrightness = oldScreenBrightness }
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Do something for slide offset.
            }
        }

        modalBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)

        binding.textEditShortFormListLong.apply {
            setText(shortFormToEdit.longForm)
            addTextChangedListener {
                shortFormToEdit.longForm = it.toString().trim()
            }
            setOnFocusChangeListener { _, hasFocus ->
                if(!hasFocus) generateQrCode()
            }
        }
        binding.textEditShortFormListShort.apply {
            setText(shortFormToEdit.shortForm)
            addTextChangedListener {
                shortFormToEdit.shortForm = it.toString().trim()
            }
            setOnFocusChangeListener { _, hasFocus ->
                if(!hasFocus) generateQrCode()
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

            setOnFocusChangeListener { _, hasFocus ->
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

                if(!hasFocus) generateQrCode()
            }
        }

        return binding.root
    }

    private fun generateQrCode(){
        val content = JSONObject()
            .put("type", Exportable.TYPE_SHORT_FORM)
            .put("items", JSONArray()
                .put(shortFormToEdit.export()))
            .toString()
        Thread {
            val outputFile = File(requireContext().cacheDir, "cacheQrCode.png")
            try {
                QRCode(content, Settings(requireContext()).correctionLevelQrCode)
                    .render(margin = 50)
                    .writeImage(FileOutputStream(outputFile))
            } catch (e: IllegalArgumentException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        R.string.err_cant_create_qr_code,
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
                return@Thread
            }
            try {
                requireActivity().runOnUiThread {
                    isQrCodeGenerated = true
                    binding.imageViewShortFormEditorQrCode.setImageBitmap(BitmapFactory.decodeFile(outputFile.absolutePath))
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().window.attributes = requireActivity().window.attributes.apply { screenBrightness = oldScreenBrightness }
        setFragmentResult("finishEditShortForm", bundleOf("resultAsJson" to shortFormToEdit.getAsJson().toString()))
    }

    companion object{
        const val TAG = "ShortFormEditorBottomSheet"
    }
}