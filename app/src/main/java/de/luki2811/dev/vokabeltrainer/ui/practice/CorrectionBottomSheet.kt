package de.luki2811.dev.vokabeltrainer.ui.practice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.databinding.FrameCorrectionBottomSheetBinding

class CorrectionBottomSheet: BottomSheetDialogFragment() {

    private var _binding: FrameCorrectionBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FrameCorrectionBottomSheetBinding.inflate(layoutInflater, container, false)

        if(arguments?.getBoolean("isCorrect") == true) {
            binding.textViewCorrection.text = getString(R.string.correct)
            binding.layoutBottomSheet.setBackgroundColor(requireActivity().getColor(R.color.Green))
            binding.textViewCorrectAnswer.visibility = View.GONE
        }
        else {
            binding.textViewCorrection.text = getString(R.string.wrong)
            if (!arguments?.getString("correctWord").isNullOrEmpty())
                binding.textViewCorrectAnswer.text = getString(R.string.correct_answer, arguments?.getString("correctWord"))
            else
                binding.textViewCorrectAnswer.visibility = View.GONE
            binding.layoutBottomSheet.setBackgroundColor(requireActivity().getColor(R.color.DarkRed))
        }

        return binding.root
    }

    override fun onDestroy() {
        setFragmentResult("finishFragment", bundleOf("isFinished" to true))
        super.onDestroy()
    }


    companion object {
        const val TAG = "CorrectionBottomSheet"
    }
}