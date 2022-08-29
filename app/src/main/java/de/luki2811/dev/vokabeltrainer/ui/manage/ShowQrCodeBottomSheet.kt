package de.luki2811.dev.vokabeltrainer.ui.manage

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.VocabularyGroup
import de.luki2811.dev.vokabeltrainer.databinding.FrameShowQrCodeSheetBinding
import org.json.JSONObject

class ShowQrCodeBottomSheet: BottomSheetDialogFragment() {
    private var _binding: FrameShowQrCodeSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FrameShowQrCodeSheetBinding.inflate(layoutInflater, container, false)

        val content = arguments?.getString("vocabularyGroup")!!

        if(VocabularyGroup(JSONObject(content),requireContext()).vocabulary.size <= 25){
            binding.imageViewQrCode.setImageBitmap(getQrCodeBitmap(content))
            binding.textViewQrCodeFrame.text = arguments?.getString("name","")
        }else{
            binding.textViewQrCodeFrame.setText(R.string.err_too_many_vocabulary_groups_to_create_qr_code)
        }



        return binding.root
    }

    private fun getQrCodeBitmap(content: String): Bitmap {

        val size = Resources.getSystem().displayMetrics.widthPixels //pixels
        val hints = hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 } // Make the QR code buffer border narrower
        val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }

    companion object {
        const val TAG = "ShowQrCodeBottomSheet"
    }
}