package de.luki2811.dev.vokabeltrainer.ui.manage

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.color.MaterialColors
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.Settings
import de.luki2811.dev.vokabeltrainer.databinding.FrameShowQrCodeSheetBinding
import io.github.g0dkar.qrcode.QRCode
import java.io.File
import java.io.FileOutputStream

class QrCodeBottomSheet(val content: String, private val textToShow: String) : BottomSheetDialogFragment() {
    private var _binding: FrameShowQrCodeSheetBinding? = null
    private val binding get() = _binding!!

    private var isQrCodeGenerated: Boolean = false
    private var oldScreenBrightness = 0F

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FrameShowQrCodeSheetBinding.inflate(layoutInflater, container, false)

        val modalBottomSheetBehavior = (this.dialog as BottomSheetDialog).behavior
        modalBottomSheetBehavior.peekHeight = 900

        oldScreenBrightness = requireActivity().window.attributes.screenBrightness

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
                // DO NOTHING
            }
        }

        modalBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)

        binding.textViewQrCodeFrame.text = textToShow
        binding.progressBarQrCode.visibility = View.VISIBLE
        binding.buttonQrCodeShare.apply {
            setBackgroundColor(MaterialColors.harmonizeWithPrimary(requireContext(), context.getColor(R.color.Orange)))
            setOnClickListener {
                Toast.makeText(requireContext(), context.getString(R.string.wait_until_qr_code_is_generated), Toast.LENGTH_SHORT).show()
            }
        }

        Log.i("QrCodeSheet", content)

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
                    binding.progressBarQrCode.visibility = View.GONE
                }
                return@Thread
            }
            try {
                requireActivity().runOnUiThread {
                    isQrCodeGenerated = true
                    binding.progressBarQrCode.visibility = View.GONE

                    if (Settings(requireContext()).increaseScreenBrightness && modalBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
                        requireActivity().window.attributes = requireActivity().window.attributes.apply { screenBrightness = 1F }
                    }
                    binding.imageViewQrCode.setImageBitmap(BitmapFactory.decodeFile(outputFile.absolutePath))
                    binding.buttonQrCodeShare.setOnClickListener {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_STREAM,
                                FileProvider.getUriForFile(
                                    requireContext(),
                                    requireActivity().packageName + ".provider",
                                    outputFile
                                )
                            )
                            type = "image/*"
                        }
                        startActivity(Intent.createChooser(shareIntent, null))
                    }
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
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