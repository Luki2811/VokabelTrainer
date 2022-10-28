package de.luki2811.dev.vokabeltrainer.ui.practice

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.color.MaterialColors
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.databinding.FrameCorrectionBottomSheetBinding

class CorrectionBottomSheet: BottomSheetDialogFragment() {

    private var _binding: FrameCorrectionBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FrameCorrectionBottomSheetBinding.inflate(layoutInflater, container, false)

        if(arguments?.getBoolean("isCorrect") == true) {
            binding.textViewCorrection.text = getString(R.string.correct)
            binding.layoutBottomSheet.setBackgroundColor(MaterialColors.harmonizeWithPrimary(requireContext(), requireContext().getColor(R.color.Green)))

            if (!arguments?.getString("correctWord").isNullOrEmpty()){
                val otherAlternatives = arguments?.getString("correctWord")?.split(";")?.toMutableList()

                if(null != otherAlternatives) {
                    otherAlternatives.forEach { it.trim() }
                    otherAlternatives.removeAll{ it.trim() == arguments?.getString("givenAnswer")?.trim() }

                    if(otherAlternatives.isEmpty()){
                        binding.textViewCorrectAnswer.visibility = View.GONE
                    }else {
                        val sb = StringBuilder(otherAlternatives[0].trim())
                        if(otherAlternatives.size > 1) {
                            for (i in 1 until otherAlternatives.size) {
                                sb.append("; ").append(otherAlternatives[i].trim())
                            }
                        }
                        binding.textViewCorrectAnswer.text = getString(R.string.alternatives, sb.toString())
                    }
                }
            } else
                binding.textViewCorrectAnswer.visibility = View.GONE


        }
        else {
            binding.textViewCorrection.text = getString(R.string.wrong)
            binding.layoutBottomSheet.setBackgroundColor(MaterialColors.harmonizeWithPrimary(requireContext(), requireContext().getColor(R.color.DarkRed)))

            if (!arguments?.getString("correctWord").isNullOrEmpty())
                binding.textViewCorrectAnswer.text = getString(R.string.correct_answer, arguments?.getString("correctWord"))
            else
                binding.textViewCorrectAnswer.visibility = View.GONE
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