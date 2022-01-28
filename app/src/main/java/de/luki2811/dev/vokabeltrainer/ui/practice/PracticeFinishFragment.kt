package de.luki2811.dev.vokabeltrainer.ui.practice

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.addCallback
import androidx.navigation.fragment.navArgs
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.Streak
import de.luki2811.dev.vokabeltrainer.databinding.FragmentPracticeFinishBinding
import de.luki2811.dev.vokabeltrainer.ui.MainActivity

class PracticeFinishFragment : Fragment() {

    private var _binding: FragmentPracticeFinishBinding? = null
    private val binding get() = _binding!!

    private val args: PracticeFinishFragmentArgs by navArgs()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPracticeFinishBinding.inflate(layoutInflater, container, false)

        val calback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            requireActivity().startActivity(Intent(context, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }

        // val counterRest = intent.getIntExtra("counterRest", 1)
        // val correctInPerCent = MainActivity.round(10.toDouble() / counterRest * 100, 0).toInt()

        binding.progressBarFinishedLesson.progress = args.correctInPercent
        binding.textViewFinishPracticeReachedInPercent.text = getString(R.string.correct_in_percent, args.correctInPercent)

        binding.buttonFinishPractice.setOnClickListener { startActivity(Intent(requireContext(), MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)) }

        val streak = Streak(requireContext())
        // Basic XP (1XP for 1Word)
        // streak.addXP(10)

        // MAX 5 extra XP (decrease 1 XP for each mistake)
        //if (5 - (counterRest - 10) >= 0) {
        //    // streak.addXP(5 - (counterRest - 10))
        //    binding.textViewFinishedXP.text = getString(R.string.xp_reached, 10, 5 - (counterRest - 10))
        //}
        // else binding.textViewFinishedXP.text = getString(R.string.xp_reached, 10, 0)

        return binding.root
    }
}