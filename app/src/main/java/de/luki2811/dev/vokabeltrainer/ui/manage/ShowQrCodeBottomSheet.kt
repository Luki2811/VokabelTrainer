package de.luki2811.dev.vokabeltrainer.ui.manage

import android.content.ContentProvider
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.color.MaterialColors
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.Settings
import de.luki2811.dev.vokabeltrainer.VocabularyGroup
import de.luki2811.dev.vokabeltrainer.databinding.FrameShowQrCodeSheetBinding
import io.github.g0dkar.qrcode.ErrorCorrectionLevel
import io.github.g0dkar.qrcode.QRCode
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.lang.IllegalStateException

class ShowQrCodeBottomSheet: BottomSheetDialogFragment() {
    private var _binding: FrameShowQrCodeSheetBinding? = null
    private val binding get() = _binding!!

    private var oldScreenBrightness = 0F

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FrameShowQrCodeSheetBinding.inflate(layoutInflater, container, false)

        val content = arguments?.getString("vocabularyGroup")!!

        oldScreenBrightness = requireActivity().window.attributes.screenBrightness

        binding.textViewQrCodeFrame.text = arguments?.getString("name","")
        binding.progressBarQrCode.visibility = View.VISIBLE
        binding.buttonQrCodeShare.apply {
            setBackgroundColor(MaterialColors.harmonizeWithPrimary(requireContext(), context.getColor(R.color.Orange)))
            setOnClickListener { Toast.makeText(requireContext(), "Wait until QR-Code is generated", Toast.LENGTH_SHORT).show() }
        }

        Thread{
            if(VocabularyGroup(JSONObject(content),requireContext()).vocabulary.size <= 25){
                val outputFile = File(requireContext().cacheDir, "cacheQrCode.png")
                try {
                    QRCode(content, ErrorCorrectionLevel.M)
                        .render(margin = 50)
                        .writeImage(FileOutputStream(outputFile))
                }catch (e: IllegalArgumentException){
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), R.string.err_cant_create_qr_code, Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                        binding.progressBarQrCode.visibility = View.GONE
                    }
                    return@Thread
                }
                try {
                    requireActivity().runOnUiThread {
                        if(Settings(requireContext()).increaseScreenBrightness)
                            requireActivity().window.attributes = requireActivity().window.attributes.apply { screenBrightness = 1F }

                        binding.progressBarQrCode.visibility = View.GONE
                        binding.imageViewQrCode.setImageBitmap(BitmapFactory.decodeFile(outputFile.absolutePath))
                        binding.buttonQrCodeShare.setOnClickListener {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(), requireActivity().packageName + ".provider", outputFile))
                                type = "image/*"
                            }
                            startActivity(Intent.createChooser(shareIntent, null))
                        }
                    }
                }catch (e: IllegalStateException){
                    e.printStackTrace()
                }

            }else{
                requireActivity().runOnUiThread {
                    binding.buttonQrCodeShare.visibility = View.GONE
                    binding.progressBarQrCode.visibility = View.GONE
                    binding.imageViewQrCode.visibility = View.GONE
                    binding.textViewQrCodeFrame.setText(R.string.err_too_many_vocabulary_groups_to_create_qr_code)
                }
            }
        }.start()

        return binding.root
    }

    override fun onDestroy() {
        requireActivity().window.attributes = requireActivity().window.attributes.apply { screenBrightness = oldScreenBrightness }
        super.onDestroy()
    }

    companion object {
        const val TAG = "ShowQrCodeBottomSheet"
    }
}