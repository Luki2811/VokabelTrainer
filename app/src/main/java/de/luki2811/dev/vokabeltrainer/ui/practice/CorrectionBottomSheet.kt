package de.luki2811.dev.vokabeltrainer.ui.practice

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
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

            if(!arguments?.getString("alternativesText", "").isNullOrBlank()){
                binding.textViewCorrectAnswer.text = getString(R.string.alternatives, arguments?.getString("alternativesText"))
            }else{
                binding.textViewCorrectAnswer.visibility = View.GONE
            }
        }
        else {
            binding.textViewCorrection.text = getString(R.string.wrong)
            binding.layoutBottomSheet.setBackgroundColor(MaterialColors.harmonizeWithPrimary(requireContext(), requireContext().getColor(R.color.DarkRed)))

            if (!arguments?.getString("alternativesText").isNullOrBlank()) {
                if(arguments?.getIntegerArrayList("wrongIndex") != null){
                    val wrongIndex = arguments?.getIntegerArrayList("wrongIndex")!!
                    if(wrongIndex.isNotEmpty()){
                        val content = SpannableString("${arguments?.getString("alternativesText")} ")
                        wrongIndex.forEach {
                            if(it <= arguments?.getString("alternativesText")!!.length && it >= 0){
                                content.setSpan(UnderlineSpan(), it, it + 1, 0)
                            }
                        }
                        binding.textViewCorrectAnswer.text = content
                    }
                }else{
                    binding.textViewCorrectAnswer.text = getString(R.string.correct_answer, arguments?.getString("alternativesText"))
                }
            } else
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